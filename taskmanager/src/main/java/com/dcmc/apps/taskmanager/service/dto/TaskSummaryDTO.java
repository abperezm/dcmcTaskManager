package com.dcmc.apps.taskmanager.service.dto;

public class TaskSummaryDTO {

    private Long id;
    private String title;
    private String priority;
    private String status;
    private Boolean archived;

    public TaskSummaryDTO() {
        // constructor vacío para Jackson, JPA, etc.
    }

    public TaskSummaryDTO(Long id, String title, String priority, String status, Boolean archived) {
        this.id = id;
        this.title = title;
        this.priority = priority;
        this.status = status;
        this.archived = archived;
    }

    public Long getId() {
        return id;
    }

    public TaskSummaryDTO setId(Long id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public TaskSummaryDTO setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getPriority() {
        return priority;
    }

    public TaskSummaryDTO setPriority(String priority) {
        this.priority = priority;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public TaskSummaryDTO setStatus(String status) {
        this.status = status;
        return this;
    }

    public Boolean getArchived() {
        return archived;
    }

    public TaskSummaryDTO setArchived(Boolean archived) {
        this.archived = archived;
        return this;
    }
}
