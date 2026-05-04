package com.sep.sep_backend.repository;

import com.sep.sep_backend.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripRepository extends JpaRepository<Trip, Long> {
}
