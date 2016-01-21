package com.security.oauth2.controllers;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.security.oauth2.entity.Role;
import com.security.oauth2.model.AddRoleRequest;
import com.security.oauth2.model.RoleResponse;
import com.security.oauth2.service.RoleService;
import com.security.oauth2.validator.AddRoleValidator;

@Controller
public class RoleController {

	private final static Logger logger = LoggerFactory.getLogger(RoleController.class); 

	
	@Autowired
	private AddRoleValidator addRoleValidator;
	
	@Autowired
	private RoleService roleService;
	
    @RequestMapping(value = "/roles", method = RequestMethod.POST)
    public HttpEntity<?> register(
    		@RequestBody AddRoleRequest addRoleRequest,
    		HttpServletRequest request,
    		HttpServletResponse response,
    		BindingResult result){
    	
    	addRoleValidator.validate(addRoleRequest, result);
		if(result.hasErrors()){
			logger.error("add role validation failed:"+result);
			return new ResponseEntity<Object>("必填项验证失败",HttpStatus.BAD_REQUEST);			
		}

		ResponseEntity<Object> responseEntity =  null;		
		try {	        
	        responseEntity=roleService.save(addRoleRequest,request,response);			
		} catch (Exception e) {			
			logger.error(e.getMessage(),e);
			responseEntity=new ResponseEntity<Object>(HttpStatus.BAD_REQUEST);			
		}	
		
		return responseEntity;
    }
    
	@RequestMapping(value="/roles/{uuid}",method = RequestMethod.GET, produces = "application/hal+json;charset=utf-8")
	@Transactional
	public HttpEntity<?> findOneRoleById(
			 @PathVariable String uuid,
			 HttpServletRequest request,
			 HttpServletResponse response){
		Role role = roleService.findOneRoleById(uuid);
		if(null == role){
            return new ResponseEntity<HttpStatus>(HttpStatus.NOT_FOUND);
		}
		
		RoleResponse roleResponse = new RoleResponse();
		BeanUtils.copyProperties(role, roleResponse);
	    Link selfLink = linkTo(methodOn(this.getClass()).findOneRoleById(role.getUuid(), request, response)).withSelfRel();	    
	    roleResponse.add(selfLink);
        return new ResponseEntity<Resource>(new Resource<RoleResponse>(roleResponse), HttpStatus.OK);		
	}
	
	@RequestMapping(value="/roles/{uuid}",method = RequestMethod.PATCH, produces = "application/hal+json;charset=utf-8")
	@Transactional
	public HttpEntity<?> updateRoleById(
			 @PathVariable String uuid,
			@RequestBody AddRoleRequest addRoleRequest,
			 HttpServletRequest request,
			 HttpServletResponse response){

		
		ResponseEntity<Object> responseEntity =  null;		
		try {	        
	        responseEntity=roleService.update(uuid,addRoleRequest,request,response);			
		} catch (Exception e) {			
			logger.error(e.getMessage(),e);
			responseEntity=new ResponseEntity<Object>(HttpStatus.BAD_REQUEST);			
		}	
		
		return responseEntity;
	}
	
	@RequestMapping(value="/roles/{uuid}",method = RequestMethod.DELETE)
	@Transactional
	public HttpEntity<?> deleteRoleById(
			 @PathVariable String uuid,
			 HttpServletRequest request,
			 HttpServletResponse response){

		
		ResponseEntity<Object> responseEntity =  null;		
		try {	        
	        responseEntity=roleService.delete(uuid,request,response);			
		} catch (Exception e) {			
			logger.error(e.getMessage(),e);
			responseEntity=new ResponseEntity<Object>(HttpStatus.BAD_REQUEST);			
		}	
		
		return responseEntity;
	}
	
	@RequestMapping(value="/roles",method = RequestMethod.GET)
	@Transactional
	public HttpEntity<?> deleteRoleById(
			@RequestParam(value = "keyword", required = true, defaultValue = "") String keyword,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "size", required = false, defaultValue = "20") int size,
			HttpServletRequest request,
			HttpServletResponse response){

		Page<Role> rolePage = null;
		Pageable pageable = new PageRequest(page, size);
		
		rolePage = roleService.findAllRole(keyword,pageable);
		
		ResponseEntity responseEntity = null;
		try {
			responseEntity = roleService.getResponseEntityConvertRolePage(keyword,rolePage, pageable, request, response);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("User Locations Not Found:",e);
            return new ResponseEntity<HttpStatus>(HttpStatus.NOT_FOUND);			
		}
		return  responseEntity;	
	}
}
