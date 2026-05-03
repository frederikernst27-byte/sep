package com.sep.sep_backend.controller;

import com.sep.sep_backend.dto.TripCreateDto;
import com.sep.sep_backend.dto.TripResponseDto;
import com.sep.sep_backend.service.TripService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TripResponseDto createTrip(@RequestBody TripCreateDto dto) {
        return tripService.createTrip(dto);
    }
}
