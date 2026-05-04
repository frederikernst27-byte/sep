package com.sep.sep_backend.CalenderEntry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/calendar")
public class CalenderEntryController {
    private final CalenderEntryService service;

    @Autowired
    public CalenderEntryController(CalenderEntryService service) {
        this.service = service;
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

    @DeleteMapping("/{id}")
    public void deleteEntry(@PathVariable Long id){
        service.deleteEntry(id);
    }



}
