package com.security.oauth2.service;

import static com.google.common.collect.Lists.newArrayList;
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
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.security.oauth2.controllers.RoleController;
import com.security.oauth2.controllers.UserController;
import com.security.oauth2.entity.Role;
import com.security.oauth2.entity.User;
import com.security.oauth2.entity.UserRoles;
import com.security.oauth2.model.RegisterUser;
import com.security.oauth2.model.RoleResponse;
import com.security.oauth2.model.UpdateUserRoleRequest;
import com.security.oauth2.model.UserResponse;
import com.security.oauth2.repository.RoleRepository;
import com.security.oauth2.repository.UserRepository;
import com.security.oauth2.repository.UserRolesRepository;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private UserRolesRepository userRolesRepository;

	@Autowired
	private RoleRepository roleRepository;
	
	public ResponseEntity<Object> updateUserRoles(
			UpdateUserRoleRequest updateUserRoleRequest,
			HttpServletRequest request, HttpServletResponse response) {
		
    	User user = userRepository.findByLoginName(updateUserRoleRequest.getLoginName());
        if(null == user){
			return new ResponseEntity<Object>("user not exist",HttpStatus.BAD_REQUEST);			
        }
        
        List<UserRoles> userRoleList =  userRolesRepository.findByUserId(user.getUuid());
        if(null != userRoleList && userRoleList.size()>0){
        	for(UserRoles userRoles : userRoleList){
            	userRolesRepository.delete(userRoles);
        	}
        }
        
    	List<String> roleCodes = updateUserRoleRequest.getRoleCodes();
    	if(null != roleCodes && roleCodes.size()>0){
            for (String roleCode : roleCodes){
                Role role = roleRepository.findByCode(roleCode);
                if (null != role){
                    UserRoles userRoles = new UserRoles();
                    userRoles.setUuid(UUID.randomUUID().toString());
                    userRoles.setUserId(user.getUuid());
                    userRoles.setRoleId(role.getUuid());
                    userRolesRepository.save(userRoles);
                }
            }
    	}

        return new ResponseEntity<Object>(HttpStatus.OK);
	}

	public ResponseEntity<Object> save(RegisterUser registerUser,
			HttpServletRequest request, HttpServletResponse response) {
		
		    String uuid = getUuidByCode(registerUser.getLoginName());
		    User user = userRepository.findOne(uuid);
		    if(null != user){
				return new ResponseEntity<Object>("user already exist",HttpStatus.BAD_REQUEST);			

		    }
		    user = new User();
		    user.setUuid(uuid);
		    user.setLoginName(registerUser.getLoginName());
		    user.setPassword(registerUser.getPassword());
		    user.setEnabled(true);		
		    user = userRepository.save(user);
		    
	    	List<String> roleCodes = registerUser.getRoleCodes();
	    	if(null != roleCodes && roleCodes.size()>0){
	            for (String roleCode : roleCodes){
	                Role role = roleRepository.findByCode(roleCode);
	                if (null != role){
	                    UserRoles userRoles = new UserRoles();
	                    userRoles.setUuid(UUID.randomUUID().toString());
	                    userRoles.setUserId(user.getUuid());
	                    userRoles.setRoleId(role.getUuid());
	                    userRolesRepository.save(userRoles);
	                }
	            }
	    	}
			HttpHeaders headers = new HttpHeaders();

			URI selfUrl = linkTo(methodOn(UserController.class).findOneUserById(user.getUuid(), request, response)).toUri();
			headers.setLocation(selfUrl);
			return new ResponseEntity<Object>(headers,HttpStatus.CREATED);			
	}
	
	private String getUuidByCode(String code){
		String uuid = UUID.nameUUIDFromBytes(code.getBytes()).toString();
		return uuid;
	}

	public ResponseEntity<?> findOneUserById(String uuid,
			HttpServletRequest request, HttpServletResponse response) {
		
		User user = userRepository.findOne(uuid);
		if(null == user){
            return new ResponseEntity<Object>(HttpStatus.NOT_FOUND);
		}
		
		UserResponse userResponse = new UserResponse();		
		BeanUtils.copyProperties(user, userResponse);
		
		List<UserRoles> userRoles = userRolesRepository.findByUserId(user.getUuid());
		if(null != userRoles && userRoles.size() > 0){
			List<RoleResponse> roles = new ArrayList<RoleResponse>();
			for(UserRoles userRole : userRoles){
				Role role = roleRepository.findOne(userRole.getRoleId());
				RoleResponse roleResponse  = new RoleResponse();
				BeanUtils.copyProperties(role, roleResponse);
				BeanUtils.copyProperties(role, roleResponse);
			    Link selfLink = linkTo(methodOn(RoleController.class).findOneRoleById(role.getUuid(), request, response)).withSelfRel();	    
			    roleResponse.add(selfLink);
			    roles.add(roleResponse);
			}
			userResponse.setRoles(roles);
		}
	    Link selfLink = linkTo(methodOn(UserController.class).findOneUserById(user.getUuid(), request, response)).withSelfRel();	    
	    userResponse.add(selfLink);
        return new ResponseEntity<Resource>(new Resource<UserResponse>(userResponse), HttpStatus.OK);		

	}	
}
