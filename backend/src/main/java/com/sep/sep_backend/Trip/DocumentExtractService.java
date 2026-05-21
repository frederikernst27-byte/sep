package com.sep.sep_backend.Trip;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DocumentExtractService {

    private static final Logger log = LoggerFactory.getLogger(DocumentExtractService.class);

    @Value("${openrouter.api.key:}")
    private String openRouterApiKey;

    @Value("${openrouter.models:google/gemma-4-31b-it:free,nvidia/nemotron-3-nano-omni-30b-a3b-reasoning:free,openrouter/free}")
    private String openRouterModels;

    private final ObjectMapper mapper;
    private final RestTemplate restTemplate;

    public DocumentExtractService(ObjectMapper mapper) {
        this.mapper = mapper;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(60_000);
        this.restTemplate = new RestTemplate(factory);
    }

    public TripCreateDto extractFromFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Keine Datei hochgeladen.");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new RuntimeException("Dateiname fehlt.");
        }

        String lowerFilename = filename.toLowerCase();

        log.info("Extraktion gestartet: name='{}', size={} bytes, contentType='{}'",
                filename, file.getSize(), file.getContentType());

        try {
            if (lowerFilename.endsWith(".pdf")) {
                log.info("Dateityp erkannt: PDF");
                try (PDDocument doc = Loader.loadPDF(file.getBytes())) {
                    String text = new PDFTextStripper().getText(doc);
                    validateExtractedText(text);
                    log.info("PDF-Text erfolgreich gelesen, length={}", text.length());
                    return callOpenRouterWithText(text);
                }
            }

            if (lowerFilename.endsWith(".docx")) {
                log.info("Dateityp erkannt: DOCX");
                try (XWPFDocument doc = new XWPFDocument(file.getInputStream());
                     XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
                    String text = extractor.getText();
                    validateExtractedText(text);
                    log.info("DOCX-Text erfolgreich gelesen, length={}", text.length());
                    return callOpenRouterWithText(text);
                }
            }

            if (isImageFile(lowerFilename, file.getContentType())) {
                log.info("Dateityp erkannt: Bild");
                String mimeType = resolveMimeType(lowerFilename, file.getContentType());
                String base64 = Base64.getEncoder().encodeToString(file.getBytes());
                log.info("Bild vorbereitet, mimeType='{}', base64Length={}", mimeType, base64.length());
                return callOpenRouterWithImage(base64, mimeType);
            }

            throw new RuntimeException("Dateityp nicht unterstützt. Bitte PDF, DOCX oder Bilddatei hochladen.");

        } catch (RuntimeException e) {
            log.warn("Extraktion abgebrochen: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Datei konnte nicht verarbeitet werden", e);
            throw new RuntimeException("Datei konnte nicht verarbeitet werden: " + e.getMessage(), e);
        }
    }

    private TripCreateDto callOpenRouterWithText(String text) throws Exception {
        String shortenedText = text.length() > 12000 ? text.substring(0, 12000) : text;

        List<Map<String, Object>> messages = List.of(
                Map.of(
                        "role", "system",
                        "content", """
                                Extrahiere aus Dokumenten Reisedaten als reines JSON.
                                Antworte ausschließlich mit gültigem JSON, ohne Markdown, ohne Erklärung.

                                Regeln:
                                - Falls ein Wert nicht gefunden wird, setze ihn auf null.
                                - Erfinde keine Daten.
                                - destination ist der Hauptzielort der Reise.
                                - name soll kurz und sinnvoll sein.
                                """
                ),
                Map.of(
                        "role", "user",
                        "content", """
                                Extrahiere aus folgendem Dokumenttext Reisedaten.

                                Dokumenttext:
                                """ + shortenedText
                )
        );

        return sendOpenRouterRequest(messages);
    }

    private TripCreateDto callOpenRouterWithImage(String base64, String mimeType) throws Exception {
        List<Map<String, Object>> messages = List.of(
                Map.of(
                        "role", "system",
                        "content", """
                                Extrahiere aus Bildern Reisedaten als reines JSON.
                                Antworte ausschließlich mit gültigem JSON, ohne Markdown, ohne Erklärung.

                                Regeln:
                                - Falls ein Wert nicht erkennbar ist, setze ihn auf null.
                                - Erfinde keine Daten.
                                - destination ist der Hauptzielort der Reise.
                                - name soll kurz und sinnvoll sein.
                                """
                ),
                Map.of(
                        "role", "user",
                        "content", List.of(
                                Map.of("type", "text", "text", "Extrahiere aus diesem Bild Reisedaten."),
                                Map.of(
                                        "type", "image_url",
                                        "image_url", Map.of(
                                                "url", "data:" + mimeType + ";base64," + base64
                                        )
                                )
                        )
                )
        );

        return sendOpenRouterRequest(messages);
    }

    private Map<String, Object> buildOpenRouterBody(String model, List<Map<String, Object>> messages) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("temperature", 0);
        body.put("max_tokens", 1024);
        body.put("response_format", Map.of(
                "type", "json_schema",
                "json_schema", Map.of(
                        "name", "trip_extract",
                        "strict", true,
                        "schema", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "name", Map.of("type", List.of("string", "null")),
                                        "destination", Map.of("type", List.of("string", "null")),
                                        "startDate", Map.of("type", List.of("string", "null")),
                                        "endDate", Map.of("type", List.of("string", "null"))
                                ),
                                "required", List.of("name", "destination", "startDate", "endDate"),
                                "additionalProperties", false
                        )
                )
        ));
        body.put("plugins", List.of(Map.of("id", "response-healing")));
        return body;
    }

    private TripCreateDto sendOpenRouterRequest(List<Map<String, Object>> messages) throws Exception {
        if (openRouterApiKey == null || openRouterApiKey.isBlank()) {
            throw new RuntimeException("OpenRouter API-Key fehlt.");
        }

        String url = "https://openrouter.ai/api/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openRouterApiKey);
        headers.set("HTTP-Referer", "http://localhost");
        headers.set("X-Title", "sep-ba");

        try {
            List<String> models = parseOpenRouterModels();
            log.info("OpenRouter-Request wird gesendet, models={}", models);

            RuntimeException lastFailure = null;
            for (String model : models) {
                try {
                    ResponseEntity<String> responseEntity = restTemplate.exchange(
                            url,
                            HttpMethod.POST,
                            new HttpEntity<>(buildOpenRouterBody(model, messages), headers),
                            String.class
                    );

                    String response = responseEntity.getBody();
                    if (response == null || response.isBlank()) {
                        throw new RuntimeException("Leere Antwort von OpenRouter erhalten.");
                    }

                    log.info("OpenRouter-Antwort erhalten, model={}, status={}", model, responseEntity.getStatusCode());

                    JsonNode json = mapper.readTree(response);
                    JsonNode textNode = json.at("/choices/0/message/content");

                    if (textNode.isMissingNode() || textNode.isNull() || textNode.asText().isBlank()) {
                        throw new RuntimeException("Kein Antwortinhalt von OpenRouter erhalten: " + response);
                    }

                    String content = textNode.asText().trim();
                    log.info("Rohinhalt von OpenRouter (model={}): {}", model, content);

                    content = sanitizeJson(content);

                    TripCreateDto dto = mapper.readValue(content, TripCreateDto.class);

                    if (isCompletelyEmpty(dto)) {
                        throw new RuntimeException("Es konnten keine Reisedaten erkannt werden.");
                    }

                    log.info("Extraktion erfolgreich mit model={}: name='{}', destination='{}', startDate={}, endDate={}",
                            model, dto.getName(), dto.getDestination(), dto.getStartDate(), dto.getEndDate());

                    return dto;
                } catch (HttpStatusCodeException e) {
                    String responseBody = e.getResponseBodyAsString();
                    int status = e.getStatusCode().value();

                    log.error("OpenRouter HTTP-Fehler mit model={}: status={}, body={}", model, e.getStatusCode(), responseBody);

                    if (status == 429 || status == 408 || status == 503 || status == 502) {
                        lastFailure = new RuntimeException("OpenRouter-Model " + model + " temporär nicht verfügbar: " + responseBody, e);
                        continue;
                    }
                    if (status == 400) {
                        throw new RuntimeException("Ungültige Anfrage an OpenRouter: " + responseBody);
                    }
                    if (status == 401 || status == 403) {
                        throw new RuntimeException("OpenRouter API-Key ungültig oder nicht berechtigt.");
                    }

                    throw new RuntimeException("OpenRouter-Fehler: HTTP " + e.getStatusCode() + " - " + responseBody, e);
                } catch (Exception e) {
                    lastFailure = new RuntimeException("Fehler bei OpenRouter-Modell " + model + ": " + e.getMessage(), e);
                    log.warn("Modell {} fehlgeschlagen, nächstes Modell wird versucht: {}", model, e.getMessage());
                }
            }

            if (lastFailure != null) {
                throw lastFailure;
            }
            throw new RuntimeException("Kein OpenRouter-Modell konnte die Extraktion durchführen.");
        } catch (Exception e) {
            log.error("Fehler beim OpenRouter-Request", e);
            throw e;
        }
    }

    private List<String> parseOpenRouterModels() {
        List<String> models = new ArrayList<>();
        for (String rawModel : openRouterModels.split(",")) {
            String model = rawModel.trim();
            if (!model.isBlank()) {
                models.add(model);
            }
        }
        if (models.isEmpty()) {
            models.add("google/gemma-4-31b-it:free");
        }
        return models;
    }

    private void validateExtractedText(String text) {
        if (text == null || text.isBlank()) {
            throw new RuntimeException("Aus dem Dokument konnte kein Text gelesen werden.");
        }
    }

    private boolean isImageFile(String lowerFilename, String contentType) {
        return lowerFilename.endsWith(".png")
                || lowerFilename.endsWith(".jpg")
                || lowerFilename.endsWith(".jpeg")
                || lowerFilename.endsWith(".webp")
                || (contentType != null && contentType.startsWith("image/"));
    }

    private String resolveMimeType(String lowerFilename, String contentType) {
        if (contentType != null && contentType.startsWith("image/")) {
            return contentType;
        }
        if (lowerFilename.endsWith(".png")) return "image/png";
        if (lowerFilename.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }

    private String sanitizeJson(String content) {
        return content
                .replace("```json", "")
                .replace("```", "")
                .trim();
    }

    private boolean isCompletelyEmpty(TripCreateDto dto) {
        return isBlank(dto.getName())
                && isBlank(dto.getDestination())
                && dto.getStartDate() == null
                && dto.getEndDate() == null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
