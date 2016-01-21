package com.security.oauth2.model;

import java.util.List;

import org.springframework.hateoas.ResourceSupport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class UserResponse extends ResourceSupport{

    private String uuid;
    
    private String name;

    private List<RoleResponse> Roles;
    
    public UserResponse() {

    }  
    
    public UserResponse(String uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	public List<RoleResponse> getRoles() {
		return Roles;
	}

	public void setRoles(List<RoleResponse> roles) {
		Roles = roles;
	}
   
}
