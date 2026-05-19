package com.sep.sep_backend.Trip;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class DocumentExtractController {

    private final DocumentExtractService extractService;

    public DocumentExtractController(DocumentExtractService extractService) {
        this.extractService = extractService;
    }

    @PostMapping("/extract")
    public ResponseEntity<TripCreateDto> extract(@RequestParam("file") MultipartFile file) {
        TripCreateDto dto = extractService.extractFromFile(file);
        return ResponseEntity.ok(dto);
    }
}