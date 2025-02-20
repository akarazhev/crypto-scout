package com.github.akarazhev.cryptoscout.bybit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Arrays;

@Entity
@Table(name = "bybit_event", schema = "cryptoscout")
public class BybitEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_time", nullable = false)
    private Long eventTime;

    @Column(name = "publish_time", nullable = false)
    private Long publishTime;

    @Column(name = "start_time", nullable = false)
    private Long startTime;

    @Column(name = "end_time", nullable = false)
    private Long endTime;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(columnDefinition = "TEXT[]")
    private String[] tags;

    private String url;

    @Column(name = "created_at")
    private Instant createdAt;

    // Default constructor
    public BybitEvent() {}

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEventTime() {
        return eventTime;
    }

    public void setEventTime(Long eventTime) {
        this.eventTime = eventTime;
    }

    public Long getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Long publishTime) {
        this.publishTime = publishTime;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "BybitEvent{" +
                "id=" + id +
                ", eventTime=" + eventTime +
                ", publishTime=" + publishTime +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", eventType='" + eventType + '\'' +
                ", tags=" + Arrays.toString(tags) +
                ", url='" + url + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}