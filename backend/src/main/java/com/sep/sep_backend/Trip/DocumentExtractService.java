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
import java.util.List;
import java.util.Map;

@Service
public class DocumentExtractService {

    private static final Logger log = LoggerFactory.getLogger(DocumentExtractService.class);

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.model:gemini-2.0-flash}")
    private String geminiModel;

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
                    return callGeminiWithText(text);
                }
            }

            if (lowerFilename.endsWith(".docx")) {
                log.info("Dateityp erkannt: DOCX");
                try (XWPFDocument doc = new XWPFDocument(file.getInputStream());
                     XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
                    String text = extractor.getText();
                    validateExtractedText(text);
                    log.info("DOCX-Text erfolgreich gelesen, length={}", text.length());
                    return callGeminiWithText(text);
                }
            }

            if (isImageFile(lowerFilename, file.getContentType())) {
                log.info("Dateityp erkannt: Bild");
                String mimeType = resolveMimeType(lowerFilename, file.getContentType());
                String base64 = Base64.getEncoder().encodeToString(file.getBytes());
                log.info("Bild vorbereitet, mimeType='{}', base64Length={}", mimeType, base64.length());
                return callGeminiWithImage(base64, mimeType);
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

    private TripCreateDto callGeminiWithText(String text) throws Exception {
        String shortenedText = text.length() > 12000 ? text.substring(0, 12000) : text;

        String prompt = """
                Extrahiere aus folgendem Dokumenttext Reisedaten als reines JSON.
                Antworte ausschließlich mit gültigem JSON, ohne Markdown, ohne Erklärung.

                Erwartetes Format:
                {
                  "name": "kurzer Reisename",
                  "destination": "Zielort",
                  "startDate": "YYYY-MM-DD",
                  "endDate": "YYYY-MM-DD"
                }

                Regeln:
                - Falls ein Wert nicht gefunden wird, setze ihn auf null.
                - Erfinde keine Daten.
                - destination ist der Hauptzielort der Reise.
                - name soll kurz und sinnvoll sein.

                Dokumenttext:
                """ + shortenedText;

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                ),
                "generationConfig", Map.of(
                        "temperature", 0,
                        "responseMimeType", "application/json"
                )
        );

        return sendGeminiRequest(body);
    }

    private TripCreateDto callGeminiWithImage(String base64, String mimeType) throws Exception {
        String prompt = """
                Extrahiere aus diesem Bild Reisedaten als reines JSON.
                Antworte ausschließlich mit gültigem JSON, ohne Markdown, ohne Erklärung.

                Erwartetes Format:
                {
                  "name": "kurzer Reisename",
                  "destination": "Zielort",
                  "startDate": "YYYY-MM-DD",
                  "endDate": "YYYY-MM-DD"
                }

                Regeln:
                - Falls ein Wert nicht erkennbar ist, setze ihn auf null.
                - Erfinde keine Daten.
                - destination ist der Hauptzielort der Reise.
                - name soll kurz und sinnvoll sein.
                """;

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt),
                                        Map.of(
                                                "inlineData", Map.of(
                                                        "mimeType", mimeType,
                                                        "data", base64
                                                )
                                        )
                                )
                        )
                ),
                "generationConfig", Map.of(
                        "temperature", 0,
                        "responseMimeType", "application/json"
                )
        );

        return sendGeminiRequest(body);
    }

    private TripCreateDto sendGeminiRequest(Map<String, Object> body) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + geminiModel
                + ":generateContent";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", geminiApiKey);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            log.info("Gemini-Request wird gesendet, model={}", geminiModel);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            String response = responseEntity.getBody();

            if (response == null || response.isBlank()) {
                throw new RuntimeException("Leere Antwort von Gemini erhalten.");
            }

            log.info("Gemini-Antwort erhalten, status={}", responseEntity.getStatusCode());

            JsonNode json = mapper.readTree(response);
            JsonNode textNode = json.at("/candidates/0/content/parts/0/text");

            if (textNode.isMissingNode() || textNode.isNull() || textNode.asText().isBlank()) {
                throw new RuntimeException("Kein Antwortinhalt von Gemini erhalten: " + response);
            }

            String content = textNode.asText().trim();
            log.info("Rohinhalt von Gemini: {}", content);

            content = sanitizeJson(content);

            TripCreateDto dto = mapper.readValue(content, TripCreateDto.class);

            if (isCompletelyEmpty(dto)) {
                throw new RuntimeException("Es konnten keine Reisedaten erkannt werden.");
            }

            log.info("Extraktion erfolgreich: name='{}', destination='{}', startDate={}, endDate={}",
                    dto.getName(), dto.getDestination(), dto.getStartDate(), dto.getEndDate());

            return dto;

        } catch (HttpStatusCodeException e) {
            String responseBody = e.getResponseBodyAsString();
            log.error("Gemini HTTP-Fehler: status={}, body={}", e.getStatusCode(), responseBody);

            int status = e.getStatusCode().value();

            if (status == 400) {
                throw new RuntimeException("Ungültige Anfrage an Gemini: " + responseBody);
            }
            if (status == 401 || status == 403) {
                throw new RuntimeException("Gemini API-Key ungültig oder nicht berechtigt.");
            }
            if (status == 429) {
                throw new RuntimeException("Die KI-Extraktion ist gerade ausgelastet. Bitte in ein paar Sekunden erneut versuchen.");
            }

            throw new RuntimeException("Gemini-Fehler: HTTP " + e.getStatusCode() + " - " + responseBody, e);
        } catch (Exception e) {
            log.error("Fehler beim Gemini-Request", e);
            throw e;
        }
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