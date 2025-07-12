// src/main/java/com/dcmc/apps/taskmanager/service/dto/WorkGroupDetailDTO.java
package com.dcmc.apps.taskmanager.service.dto;

import java.io.Serializable;
import java.util.List;

public class WorkGroupDetailDTO implements Serializable {

    private Long id;
    private String name;
    private String description;
    private List<ProjectSummaryDTO> projects;
    private List<MemberSummaryDTO> members;

    public WorkGroupDetailDTO() {}

    public WorkGroupDetailDTO(
        Long id,
        String name,
        String description,
        List<ProjectSummaryDTO> projects,
        List<MemberSummaryDTO> members
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.projects = projects;
        this.members = members;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ProjectSummaryDTO> getProjects() {
        return projects;
    }

    public void setProjects(List<ProjectSummaryDTO> projects) {
        this.projects = projects;
    }

    public List<MemberSummaryDTO> getMembers() {
        return members;
    }

    public void setMembers(List<MemberSummaryDTO> members) {
        this.members = members;
    }
}
