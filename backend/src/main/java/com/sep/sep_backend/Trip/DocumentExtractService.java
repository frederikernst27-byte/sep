package com.sep.sep_backend.Trip;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class DocumentExtractService {

    @Value("${openrouter.api.key}")
    private String apiKey;

    private final ObjectMapper mapper = new ObjectMapper();

    public TripCreateDto extractFromFile(MultipartFile file) {
        String filename = file.getOriginalFilename().toLowerCase();
        try {
            if (filename.endsWith(".pdf")) {
                PDDocument doc = Loader.loadPDF(file.getBytes());
                String text = new PDFTextStripper().getText(doc);
                doc.close();
                return callOpenAIText(text);
            } else if (filename.endsWith(".docx")) {
                XWPFDocument doc = new XWPFDocument(file.getInputStream());
                String text = new XWPFWordExtractor(doc).getText();
                return callOpenAIText(text);
            } else {
                String base64 = Base64.getEncoder().encodeToString(file.getBytes());
                return callOpenAIImage(base64);
            }
        } catch (Exception e) {
            throw new RuntimeException("Datei konnte nicht verarbeitet werden: " + e.getMessage());
        }
    }

    private TripCreateDto callOpenAIText(String text) throws Exception {
        String prompt = """
            Extrahiere aus folgendem Text diese Reisedaten als reines JSON (kein Markdown, nur JSON):
            {
              "name": "kurzer Reisename",
              "destination": "Zielort",
              "startDate": "YYYY-MM-DD",
              "endDate": "YYYY-MM-DD"
            }
            Falls ein Wert nicht gefunden wird, setze null.
            Text: """ + text;

        Map<String, Object> body = Map.of(
                "model", "meta-llama/llama-3.1-8b-instruct:free",
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        return sendRequest(body);
    }

    private TripCreateDto callOpenAIImage(String base64) throws Exception {
        Map<String, Object> imageContent = Map.of(
                "type", "image_url",
                "image_url", Map.of("url", "data:image/jpeg;base64," + base64)
        );
        Map<String, Object> textContent = Map.of(
                "type", "text",
                "text", """
                Extrahiere aus diesem Bild Reisedaten als reines JSON (kein Markdown):
                {
                  "name": "kurzer Reisename",
                  "destination": "Zielort",
                  "startDate": "YYYY-MM-DD",
                  "endDate": "YYYY-MM-DD"
                }
                Falls ein Wert nicht erkennbar ist, setze null.
                """
        );

        Map<String, Object> body = Map.of(
                "model", "meta-llama/llama-3.1-8b-instruct:free",
                "messages", List.of(Map.of(
                        "role", "user",
                        "content", List.of(textContent, imageContent)
                ))
        );

        return sendRequest(body);
    }

    private TripCreateDto sendRequest(Map<String, Object> body) throws Exception {
        RestTemplate rest = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String response = rest.postForObject(
                "https://openrouter.ai/api/v1/chat/completions",
                new HttpEntity<>(body, headers),
                String.class
        );

        JsonNode json = mapper.readTree(response);
        String content = json.at("/choices/0/message/content").asText();
        content = content.replace("```json", "").replace("```", "").trim();

        return mapper.readValue(content, TripCreateDto.class);
    }
}