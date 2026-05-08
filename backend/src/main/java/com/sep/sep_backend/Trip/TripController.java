package com.sep.sep_backend.Trip;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@CrossOrigin(origins = "*")
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

    @GetMapping
    public List<TripResponseDto> getTrips() {
        return tripService.getTrips();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTrip(@PathVariable Long id) {
        tripService.deleteTrip(id);
    }
}
