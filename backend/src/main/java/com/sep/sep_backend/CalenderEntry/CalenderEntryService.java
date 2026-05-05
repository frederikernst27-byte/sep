package com.sep.sep_backend.CalenderEntry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CalenderEntryService {
    private final CalenderEntryRepository repository;

    @Autowired
    public CalenderEntryService(CalenderEntryRepository repository) {
        this.repository = repository;
    }

    public CalenderEntryResponse getEntryById(Long id) {
        CalenderEntry entry = repository.findById(id).orElseThrow(()
                -> new RuntimeException("Kalendereintrag nicht gefunden"));
        return toDto(entry);
    }

    public CalenderEntryResponse patchDateTime(Long id, CalenderEntryRequest request) {
        CalenderEntry entry = repository.findById(id).orElseThrow(()
                -> new RuntimeException("Kalendereintrag nicht gefunden"));
        entry.setDateTime(request.getDateTime());
        CalenderEntry saved = repository.save(entry);
        return toDto(saved);
    }

    public CalenderEntryResponse updateEntry(Long id, CalenderEntryRequest request) {
        CalenderEntry entry = repository.findById(id).orElseThrow();

        entry.setName(request.getName());
        entry.setDescription(request.getDescription());
        entry.setDateTime(request.getDateTime());
        entry.setUserId(request.getUserId());
        entry.setTripId(request.getTripId());

        repository.save(entry);

        return toDto(entry);
    }

    public CalenderEntryResponse createEntry(CalenderEntryRequest request) {
        CalenderEntry entry = new CalenderEntry();

        entry.setName(request.getName());
        entry.setDescription(request.getDescription());
        entry.setDateTime(request.getDateTime());
        entry.setUserId(request.getUserId());
        entry.setTripId(request.getTripId());

        CalenderEntry saved = repository.save(entry);

        return toDto(saved);
    }

    public void deleteEntry(Long id) {
        if(!repository.existsById(id)) {
            throw new RuntimeException("Kalendereintag nicht gefunden");
        } else {
            repository.deleteById(id);
        }
    }

    private CalenderEntryResponse toDto(CalenderEntry entry) {
        CalenderEntryResponse response = new CalenderEntryResponse();

        response.setId(entry.getId());
        response.setDescription(entry.getDescription());
        response.setName(entry.getName());
        response.setDateTime(entry.getDateTime());
        response.setUserId(entry.getUserId());
        response.setTripId(entry.getTripId());

        return response;
    }
}
