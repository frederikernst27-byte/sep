package com.sep.sep_backend.CalenderEntry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/calendar")
public class CalenderEntryController {
    private final CalenderEntryService service;

    @Autowired
    public CalenderEntryController(CalenderEntryService service) {
        this.service = service;
    }

    @GetMapping
    public List<CalenderEntryResponse> getEntries() {
        return service.getEntries();
    }

    @GetMapping("/{id}")
    public CalenderEntryResponse getEntryById(@PathVariable Long id) {
        return service.getEntryById(id);
    }

    @GetMapping("/trip/{tripId}")
    public List<CalenderEntryResponse> getEntriesByTripId(@PathVariable Long tripId) {
        return service.getEntriesByTripId(tripId);
    }

    @PutMapping("/{id}")
    public CalenderEntryResponse updateEntry(@PathVariable Long id,
                                             @RequestBody CalenderEntryRequest request) {
        return service.updateEntry(id,request);
    }

    @PostMapping
    public CalenderEntryResponse createEntry(@RequestBody CalenderEntryRequest request) {
        return service.createEntry(request);
    }

    @PostMapping("/auto-create")
    @ResponseStatus(HttpStatus.CREATED)
    public CalenderEntryResponse autoCreateEntry(@RequestBody TravelDataRequest request) {
        return service.createFromTravelData(request);
    }

    @PostMapping("/auto-create/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public List<CalenderEntryResponse> autoCreateEntries(@RequestBody List<TravelDataRequest> requests) {
        return service.createFromTravelData(requests);
    }

    @PatchMapping("/{id}")
    public CalenderEntryResponse patchDateTime(@PathVariable Long id,
                                               @RequestBody CalenderEntryRequest request) {
        return service.patchDateTime(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteEntry(@PathVariable Long id){
        service.deleteEntry(id);
    }



}
