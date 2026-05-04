package com.sep.sep_backend.Trip;

import org.springframework.stereotype.Service;

@Service
public class TripService {

    private final TripRepository tripRepository;

    public TripService(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    public TripResponseDto createTrip(TripCreateDto dto) {
        Trip trip = new Trip(
                dto.getName(),
                dto.getDestination(),
                dto.getStartDate(),
                dto.getEndDate()
        );

        Trip savedTrip = tripRepository.save(trip);

        return new TripResponseDto(
                savedTrip.getId(),
                savedTrip.getName(),
                savedTrip.getDestination(),
                savedTrip.getStartDate(),
                savedTrip.getEndDate()
        );
    }
}
