package com.dcmc.apps.taskmanager.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.dcmc.apps.taskmanager.domain.TaskPriority} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TaskPriorityDTO implements Serializable {

    private Long id;

    @NotNull
    private String name;

    private Integer level;

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

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Boolean getVisible() {
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
        if (!(o instanceof TaskPriorityDTO)) {
            return false;
        }

        TaskPriorityDTO taskPriorityDTO = (TaskPriorityDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, taskPriorityDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TaskPriorityDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", level=" + getLevel() +
            ", visible='" + getVisible() + "'" +
            "}";
    }
}
