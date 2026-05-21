package com.sep.sep_backend.CalendarEntry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CalendarEntryService {
    private final CalendarEntryRepository repository;

    @Autowired
    public CalendarEntryService(CalendarEntryRepository repository) {
        this.repository = repository;
    }

    public List<CalendarEntryResponse> getEntries() {
        return repository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public List<CalendarEntryResponse> getEntriesByTripId(Long tripId) {
        return repository.findByTripId(tripId).stream()
                .map(this::toDto)
                .toList();
    }

    public CalendarEntryResponse getEntryById(Long id) {
        CalendarEntry entry = repository.findById(id).orElseThrow(()
                -> new RuntimeException("Kalendereintrag nicht gefunden"));
        return toDto(entry);
    }

    public CalendarEntryResponse patchDateTime(Long id, CalendarEntryRequest request) {
        CalendarEntry entry = repository.findById(id).orElseThrow(()
                -> new RuntimeException("Kalendereintrag nicht gefunden"));
        entry.setDateTime(request.getDateTime());
        CalendarEntry saved = repository.save(entry);
        return toDto(saved);
    }

    public CalendarEntryResponse updateEntry(Long id, CalendarEntryRequest request) {
        CalendarEntry entry = repository.findById(id).orElseThrow();

        entry.setName(request.getName());
        entry.setDescription(request.getDescription());
        entry.setDateTime(request.getDateTime());
        entry.setEndDateTime(request.getEndDateTime());
        entry.setLocation(request.getLocation());
        entry.setBookingType(request.getBookingType());
        entry.setAutoCreated(request.isAutoCreated());
        entry.setUserId(request.getUserId());
        entry.setTripId(request.getTripId());

        repository.save(entry);

        return toDto(entry);
    }

    public CalendarEntryResponse createEntry(CalendarEntryRequest request) {
        CalendarEntry entry = new CalendarEntry();

        entry.setName(request.getName());
        entry.setDescription(request.getDescription());
        entry.setDateTime(request.getDateTime());
        entry.setEndDateTime(request.getEndDateTime());
        entry.setLocation(request.getLocation());
        entry.setBookingType(request.getBookingType());
        entry.setAutoCreated(request.isAutoCreated());
        entry.setUserId(request.getUserId());
        entry.setTripId(request.getTripId());

        CalendarEntry saved = repository.save(entry);

        return toDto(saved);
    }

    public CalendarEntryResponse createFromTravelData(TravelDataRequest request) {
        CalendarEntry entry = new CalendarEntry();

        entry.setName(resolveTitle(request));
        entry.setDescription(request.getDescription());
        entry.setDateTime(request.getStartDateTime());
        entry.setEndDateTime(request.getEndDateTime());
        entry.setLocation(request.getLocation());
        entry.setBookingType(resolveBookingType(request));
        entry.setAutoCreated(true);
        entry.setUserId(request.getUserId());
        entry.setTripId(request.getTripId());

        CalendarEntry saved = repository.save(entry);

        return toDto(saved);
    }

    public List<CalendarEntryResponse> createFromTravelData(List<TravelDataRequest> requests) {
        return requests.stream()
                .map(this::createFromTravelData)
                .toList();
    }

    public void deleteEntry(Long id) {
        if(!repository.existsById(id)) {
            throw new RuntimeException("Kalendereintag nicht gefunden");
        } else {
            repository.deleteById(id);
        }
    }

    private CalendarEntryResponse toDto(CalendarEntry entry) {
        CalendarEntryResponse response = new CalendarEntryResponse();

        response.setId(entry.getId());
        response.setDescription(entry.getDescription());
        response.setName(entry.getName());
        response.setDateTime(entry.getDateTime());
        response.setEndDateTime(entry.getEndDateTime());
        response.setLocation(entry.getLocation());
        response.setBookingType(entry.getBookingType());
        response.setAutoCreated(entry.isAutoCreated());
        response.setUserId(entry.getUserId());
        response.setTripId(entry.getTripId());

        return response;
    }

    private String resolveTitle(TravelDataRequest request) {
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            return request.getTitle();
        }

        String bookingType = resolveBookingType(request);
        if (bookingType != null && request.getLocation() != null && !request.getLocation().isBlank()) {
            return bookingType + " - " + request.getLocation();
        }

        if (bookingType != null) {
            return bookingType;
        }

        return "Reisedatum";
    }

    private String resolveBookingType(TravelDataRequest request) {
        if (request.getBookingType() != null && !request.getBookingType().isBlank()) {
            return request.getBookingType();
        }

        if (request.getType() != null && !request.getType().isBlank()) {
            return request.getType();
        }

        return null;
    }
}
