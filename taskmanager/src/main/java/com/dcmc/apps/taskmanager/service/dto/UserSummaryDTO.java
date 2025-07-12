package com.dcmc.apps.taskmanager.service.dto;

public class UserSummaryDTO {
    
    private String id;
    private String login;

    public UserSummaryDTO(String id, String login) {
        // Default constructor
        this.id = id;
        this.login = login;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getLogin() {
        return login;
    }
    public void setLogin(String login) {
        this.login = login;
    }
}
