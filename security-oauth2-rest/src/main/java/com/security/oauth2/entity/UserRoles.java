package com.security.oauth2.entity;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;



@Entity
@Table(name = "user_roles")
public class UserRoles  implements Serializable{
	
	
	private static final long serialVersionUID = -7380169486695895666L;

	@Id
	@Column(name="uuid")
	private String uuid;

	@Column(name = "user_id")
	private String userId;
	
	@Column(name = "role_id")
	private String roleId;
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

   
}
