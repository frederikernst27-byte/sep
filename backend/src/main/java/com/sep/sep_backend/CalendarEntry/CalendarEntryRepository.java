package com.sep.sep_backend.CalendarEntry;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalendarEntryRepository extends JpaRepository<CalendarEntry,Long> {
    List<CalendarEntry> findByUserId(Long userId);
    List<CalendarEntry> findByTripId(Long tripId);
}
