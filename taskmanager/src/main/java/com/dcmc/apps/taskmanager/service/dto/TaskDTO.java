package com.dcmc.apps.taskmanager.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A DTO for the {@link com.dcmc.apps.taskmanager.domain.Task} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TaskDTO implements Serializable {

    private Long id;

    @NotNull
    private String title;

    @NotNull
    private String description;

    @NotNull
    private Instant createTime;

    @NotNull
    private Instant updateTime;

    @NotNull
    private Boolean archived;

    private WorkGroupDTO workGroup;

    private Set<UserDTO> assignedMembers = new HashSet<>();

    private ProjectDTO project;

    private TaskPriorityDTO priority;

    private TaskStatusDTO status;

    public Boolean isArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Instant createTime) {
        this.createTime = createTime;
    }

    public Instant getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Instant updateTime) {
        this.updateTime = updateTime;
    }

    public WorkGroupDTO getWorkGroup() {
        return workGroup;
    }

    public void setWorkGroup(WorkGroupDTO workGroup) {
        this.workGroup = workGroup;
    }

    public Set<UserDTO> getAssignedMembers() {
        return assignedMembers;
    }

    public void setAssignedMembers(Set<UserDTO> assignedMembers) {
        this.assignedMembers = assignedMembers;
    }

    public ProjectDTO getProject() {
        return project;
    }

    public void setProject(ProjectDTO project) {
        this.project = project;
    }

    public TaskPriorityDTO getPriority() {
        return priority;
    }

    public void setPriority(TaskPriorityDTO priority) {
        this.priority = priority;
    }

    public TaskStatusDTO getStatus() {
        return status;
    }

    public void setStatus(TaskStatusDTO status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TaskDTO)) {
            return false;
        }

        TaskDTO taskDTO = (TaskDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, taskDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TaskDTO{" +
            "id=" + getId() +
            ", title='" + getTitle() + "'" +
            ", description='" + getDescription() + "'" +
            ", createTime='" + getCreateTime() + "'" +
            ", updateTime='" + getUpdateTime() + "'" +
            ", archived='" + isArchived() + "'" +
            ", workGroup=" + getWorkGroup() +
            ", assignedMembers=" + getAssignedMembers() +
            ", project=" + getProject() +
            ", priority=" + getPriority() +
            ", status=" + getStatus() +
            "}";
    }
}
