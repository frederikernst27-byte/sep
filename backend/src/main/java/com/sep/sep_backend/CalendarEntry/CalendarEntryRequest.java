package com.sep.sep_backend.CalendarEntry;

import java.time.LocalDateTime;

public class CalendarEntryRequest {
    private String name;
    private String description;
    private LocalDateTime dateTime;
    private LocalDateTime endDateTime;
    private String location;
    private String bookingType;
    private boolean autoCreated;

    private Long userId;
    private Long tripId;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public LocalDateTime getDateTime() {
        return dateTime;
    }
    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
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
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
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
