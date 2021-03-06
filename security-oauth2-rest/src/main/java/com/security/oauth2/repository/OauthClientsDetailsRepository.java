package com.security.oauth2.repository;

import com.security.oauth2.entity.OauthClientDetails;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PreAuthorize;

@RepositoryRestResource(path = "apps", collectionResourceRel = "apps")
public interface OauthClientsDetailsRepository extends CrudRepository<OauthClientDetails, String>{

    @SuppressWarnings("unchecked")
	@Override
    OauthClientDetails save(OauthClientDetails entity);

    @Override
    OauthClientDetails findOne(String s);

    @RestResource(exported = false)
    OauthClientDetails findByClientId(String clientId);

    @Override
    Iterable<OauthClientDetails> findAll();

    @Override
    @PreAuthorize("hasAuthority('USER_MANAGER')")
    void delete(String s);

    @Override
    @PreAuthorize("hasAuthority('USER_MANAGER')")
    void delete(OauthClientDetails entity);
}
