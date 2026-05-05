package com.sep.sep_backend.CalenderEntry;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalenderEntryRepository extends JpaRepository<CalenderEntry,Long> {
    List<CalenderEntry> findByUserId(Long userId);
    List<CalenderEntry> findByTripId(Long tripId);
}
