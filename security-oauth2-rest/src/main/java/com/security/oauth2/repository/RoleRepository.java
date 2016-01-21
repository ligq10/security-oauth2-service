package com.security.oauth2.repository;

import com.security.oauth2.entity.Role;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

@RepositoryRestResource(path = "roles", collectionResourceRel = "roles")
public interface RoleRepository extends PagingAndSortingRepository<Role, String>{

    @RestResource(exported = false)
    public Role findByCode(String code);
    
	@RestResource(exported = false)
	@Query(value = "select t from Role t where  t.code like %:keyword% or t.name like %:keyword%")
	public Page<Role> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
