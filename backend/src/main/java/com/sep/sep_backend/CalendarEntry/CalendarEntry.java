package com.sep.sep_backend.CalendarEntry;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class CalendarEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String description;
    private LocalDateTime dateTime;
    private LocalDateTime endDateTime;
    private String location;
    private String bookingType;
    private boolean autoCreated;

    private Long userId;
    private Long tripId;

    public CalendarEntry(){}

    public CalendarEntry(String name, String description, LocalDateTime dateTime, Long userId, Long tripId) {
        this.name = name;
        this.description = description;
        this.dateTime = dateTime;
        this.userId = userId;
        this.tripId = tripId;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public void setId(long id) {
        this.id = id;
    }
    public long getId() {
        return id;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }
    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
    public LocalDateTime getDateTime() {
        return dateTime;
    }
    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }
    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public String getBookingType() {
        return bookingType;
    }
    public void setBookingType(String bookingType) {
        this.bookingType = bookingType;
    }
    public boolean isAutoCreated() {
        return autoCreated;
    }
    public void setAutoCreated(boolean autoCreated) {
        this.autoCreated = autoCreated;
    }
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public Long getTripId() {
        return tripId;
    }
    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }
}
