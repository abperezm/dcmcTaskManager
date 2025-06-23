package com.dcmc.apps.taskmanager.service.dto;

import java.util.Set;

public class UserSyncDTO {

    private String login;
    private String email;
    private String firstName;
    private String lastName;
    private Set<String> roles;
    private boolean activated;
    private String langKey;

    
    public String getLogin() {
        return login;
    }
    public void setLogin(String login) {
        this.login = login;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public Set<String> getRoles() {
        return roles;
    }
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
    public boolean isActivated() {
        return activated;
    }
    public void setActivated(boolean activated) {
        this.activated = activated;
    }
    public String getLangKey() {
        return langKey;
    }
    public void setLangKey(String langKey) {
        this.langKey = langKey;
    }
}
