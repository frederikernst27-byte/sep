package com.sep.sep_backend.CalenderEntry;

import java.time.LocalDateTime;

public class CalenderEntryResponse {
    public Long id;
    public String name;
    public String description;
    public LocalDateTime dateTime;

    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return this.description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public LocalDateTime getDateTime() {
        return dateTime;
    }
    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getId() {
        return id;
    }
}
