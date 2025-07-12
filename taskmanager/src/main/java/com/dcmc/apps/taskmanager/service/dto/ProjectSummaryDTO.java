// src/main/java/com/dcmc/apps/taskmanager/service/dto/ProjectSummaryDTO.java
package com.dcmc.apps.taskmanager.service.dto;

import java.io.Serializable;

public class ProjectSummaryDTO implements Serializable {

    private Long id;
    private String title;

    public ProjectSummaryDTO() {}

    public ProjectSummaryDTO(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

