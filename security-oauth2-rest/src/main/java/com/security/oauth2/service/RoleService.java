package com.security.oauth2.service;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.PagedResources.PageMetadata;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.security.oauth2.controllers.RoleController;
import com.security.oauth2.entity.Role;
import com.security.oauth2.model.AddRoleRequest;
import com.security.oauth2.model.RoleResponse;
import com.security.oauth2.repository.RoleRepository;

@Service
public class RoleService {

	@Autowired
	private RoleRepository roleRepository;

	public ResponseEntity<Object> save(AddRoleRequest addRoleRequest,
			HttpServletRequest request, HttpServletResponse response) {
		
		String uuid = getUuidByCode(addRoleRequest.getCode());
		Role roleEntity = roleRepository.findOne(uuid);
		if(null != roleEntity){
			return new ResponseEntity<Object>("角色已存在",HttpStatus.BAD_REQUEST);			
		}
		roleEntity = new Role();
		BeanUtils.copyProperties(addRoleRequest, roleEntity);
		roleEntity.setUuid(uuid);
		roleEntity = roleRepository.save(roleEntity);
		HttpHeaders headers = new HttpHeaders();

		URI selfUrl = linkTo(methodOn(RoleController.class).findOneRoleById(roleEntity.getUuid(), request, response)).toUri();
		headers.setLocation(selfUrl);
		return new ResponseEntity<Object>(headers,HttpStatus.CREATED);
	}
	
	private String getUuidByCode(String code){
		String uuid = UUID.nameUUIDFromBytes(code.getBytes()).toString();
		return uuid;
	}

	public Role findOneRoleById(String uuid) {
		// TODO Auto-generated method stub
		Role role = roleRepository.findOne(uuid);
		return role;
	}

	public ResponseEntity<Object> update(String uuid,AddRoleRequest addRoleRequest,
			HttpServletRequest request, HttpServletResponse response) {
		// TODO Auto-generated method stub
		
		Role roleEntity = roleRepository.findOne(uuid);
		if(null == roleEntity){
            return new ResponseEntity<Object>(HttpStatus.BAD_REQUEST);
		}
		BeanUtils.copyProperties(addRoleRequest, roleEntity);
		roleEntity.setUuid(uuid);
		roleEntity = roleRepository.save(roleEntity);
		
		return new ResponseEntity<Object>(HttpStatus.OK);
	}

	public ResponseEntity<Object> delete(String uuid,
			HttpServletRequest request, HttpServletResponse response) {
		// TODO Auto-generated method stub
		Role roleEntity = roleRepository.findOne(uuid);
		if(null == roleEntity){
            return new ResponseEntity<Object>(HttpStatus.BAD_REQUEST);
		}
		roleRepository.delete(roleEntity);		
		return new ResponseEntity<Object>(HttpStatus.OK);
	}

	public Page<Role> findAllRole(String keyword, Pageable pageable) {
		// TODO Auto-generated method stub
		Page<Role> rolePage = roleRepository.findByKeyword(keyword,pageable);
		return rolePage;
	}

	public ResponseEntity getResponseEntityConvertRolePage(String pathParams,
			Page<Role> rolePage, Pageable pageable, HttpServletRequest request,
			HttpServletResponse response) {
		List<Link> list = prepareLinks(pageable.getPageNumber(),
				pageable.getPageSize(), request, rolePage,pathParams);
		List<RoleResponse> content = new ArrayList<RoleResponse>();
		
		if(null != rolePage){
			for (Role role : rolePage.getContent()) {

				RoleResponse roleResponse = new RoleResponse();
				BeanUtils.copyProperties(role, roleResponse);
			    Link selfLink = linkTo(methodOn(RoleController.class).findOneRoleById(role.getUuid(), request, response)).withSelfRel();	    

			    roleResponse.add(selfLink);
				content.add(roleResponse);
			}			
		}
		
		PagedResources<RoleResponse> pagedResources = new PagedResources<RoleResponse>(
				content, new PageMetadata(rolePage.getSize(), rolePage.getNumber(),
						rolePage.getTotalElements(), rolePage.getTotalPages()),
				list);
		return new ResponseEntity(pagedResources, HttpStatus.OK); 
	}
	
	private List<Link> prepareLinks(int page, int size,
			HttpServletRequest request, Page result,String pathParams) {
		List<Link> list = new ArrayList<>();
		if (result.hasNext()) {
			list.add(new Link(getHost(request) + request.getRequestURI()
					+  "?page=" + (page + 1) + "&size=" + size+pathParams,
					Link.REL_NEXT));
		}
		if (result.hasPrevious()) {
			list.add(new Link(getHost(request) + request.getRequestURI()
					+ "?page=" + (page - 1) + "&size=" + size+pathParams,
					Link.REL_PREVIOUS));
		}
		list.add(new Link(getHost(request) + request.getRequestURI()
				+  "?page=" + page + "&size=" + size+pathParams, Link.REL_SELF));
		return list;
	}
	
	public String getHost(HttpServletRequest request) {
		int port = request.getServerPort();
		String host = request.getServerName();
		String header = request.getHeader("X-Forwarded-Host");
		if (!StringUtils.isEmpty(header)) {
			return "http://" + header;
		}
		return "http://" + host + ":" + port;
	}

}
