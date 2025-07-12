package com.dcmc.apps.taskmanager.service.dto;

import com.dcmc.apps.taskmanager.domain.enumeration.WorkGroupRole;
import java.io.Serializable;

/**
 * DTO con la info de grupo y el rol del usuario en él.
 */
public class UserWorkGroupDTO implements Serializable {

    private Long id;
    private String name;
    private String description;
    private WorkGroupRole role;

    public UserWorkGroupDTO() {}

    public UserWorkGroupDTO(Long id, String name, String description, WorkGroupRole role) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.role = role;
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

    public WorkGroupRole getRole() {
        return role;
    }

    public void setRole(WorkGroupRole role) {
        this.role = role;
    }
}

