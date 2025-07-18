package com.dcmc.apps.taskmanager.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.dcmc.apps.taskmanager.domain.TaskStatus} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TaskStatusDTO implements Serializable {

    public TaskStatusDTO() {
        // Default constructor
    }

    public TaskStatusDTO(Long id, String name, Boolean visible) {
        this.id = id;
        this.name = name;
        this.visible = visible;
    }

    private Long id;

    @NotNull
    private String name;

    @NotNull
    private Boolean visible;

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

    public Boolean isVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TaskStatusDTO)) {
            return false;
        }

        TaskStatusDTO taskStatusDTO = (TaskStatusDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, taskStatusDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TaskStatusDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", visible='" + isVisible() + "'" +
            "}";
    }
}
