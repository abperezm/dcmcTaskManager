// src/main/java/com/dcmc/apps/taskmanager/service/dto/MemberSummaryDTO.java
package com.dcmc.apps.taskmanager.service.dto;

import com.dcmc.apps.taskmanager.domain.enumeration.WorkGroupRole;
import java.io.Serializable;

public class MemberSummaryDTO implements Serializable {

    private String userId;
    private String login;
    private WorkGroupRole role;

    public MemberSummaryDTO() {}

    public MemberSummaryDTO(String userId, String login, WorkGroupRole role) {
        this.userId = userId;
        this.login = login;
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public WorkGroupRole getRole() {
        return role;
    }

    public void setRole(WorkGroupRole role) {
        this.role = role;
    }
}
