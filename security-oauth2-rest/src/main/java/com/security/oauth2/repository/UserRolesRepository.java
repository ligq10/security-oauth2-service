package com.security.oauth2.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.security.oauth2.entity.UserRoles;

public interface UserRolesRepository extends PagingAndSortingRepository<UserRoles, String>{

	public List<UserRoles> findByUserId(String userId);
}
