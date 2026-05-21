package com.sep.sep_backend.Trip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class DocumentExtractController {

    private static final Logger log = LoggerFactory.getLogger(DocumentExtractController.class);

    private final DocumentExtractService extractService;

    public DocumentExtractController(DocumentExtractService extractService) {
        this.extractService = extractService;
    }

    @PostMapping("/extract")
    public ResponseEntity<?> extract(@RequestParam("file") MultipartFile file) {
        try {
            log.info("POST /api/extract aufgerufen, filename='{}', size={}",
                    file != null ? file.getOriginalFilename() : null,
                    file != null ? file.getSize() : null);

            TripCreateDto dto = extractService.extractFromFile(file);
            return ResponseEntity.ok(dto);

        } catch (RuntimeException e) {
            log.warn("Extraktion fehlgeschlagen: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Unerwarteter Fehler bei /api/extract", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Interner Fehler bei der Extraktion."));
        }
    }
}