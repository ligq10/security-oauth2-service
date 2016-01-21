package com.security.oauth2.controllers;




import com.security.oauth2.entity.User;
import com.security.oauth2.model.*;
import com.security.oauth2.repository.RoleRepository;
import com.security.oauth2.repository.UserRepository;
import com.security.oauth2.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;





import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.google.common.collect.Lists.newArrayList;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Controller
public class UserController {
	private final static Logger logger = LoggerFactory.getLogger(UserController.class); 

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserService userService;

    private static final String USER_ROLE = "USER";

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public HttpEntity<?> register(
    		@RequestBody RegisterUser registerUser,
    		HttpServletRequest request,
    		HttpServletResponse response){
        if (StringUtils.isEmpty(registerUser.getLoginName()) || StringUtils.isEmpty(registerUser.getPassword()) || !isValidCheckCode(registerUser.getCheckCode())){
            return new ResponseEntity<HttpStatus>(HttpStatus.BAD_REQUEST);
        }
        
		ResponseEntity<Object> responseEntity =  null;		
		try {	        
	        responseEntity=userService.save(registerUser,request,response);			
		} catch (Exception e) {			
			logger.error(e.getMessage(),e);
			responseEntity=new ResponseEntity<Object>(HttpStatus.BAD_REQUEST);			
		}			
		return responseEntity;
    }

	@RequestMapping(value = "/users/password.update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public HttpEntity<?> updateUserPassword(@RequestBody UpdatePasswordRequest updatePasswordRequest, HttpServletRequest request){
        if (!updatePasswordRequest.getLoginName().equals( request.getUserPrincipal().getName())){
            return new ResponseEntity<>("Access denied.", HttpStatus.FORBIDDEN);
        }
        User user = userRepository.findByLoginName(updatePasswordRequest.getLoginName());
        if (!updatePasswordRequest.getOldPassword().equals(user.getPassword())){
            return new ResponseEntity<>("{\"code\":\"password.incorrect\", \"message\": \"Old password is incorrect\"}", HttpStatus.BAD_REQUEST);
        }
        if (StringUtils.isEmpty(updatePasswordRequest.getNewPassword())){
            return new ResponseEntity<>("{\"code\":\"password.empty\", \"message\": \"Password can not be empty\"}", HttpStatus.BAD_REQUEST);
        }
        if (user.getPassword().equals(updatePasswordRequest.getNewPassword())){
            return new ResponseEntity<>("{\"code\":\"password.same\", \"message\": \"New password can not be same to the old password\"}", HttpStatus.BAD_REQUEST);
        }
        user.setPassword(updatePasswordRequest.getNewPassword());
        userRepository.save(newArrayList(user));
        return new ResponseEntity<HttpStatus>(HttpStatus.OK);
    }

    @RequestMapping(value = "/users/password.reset", method = RequestMethod.POST)
    public HttpEntity<?> resetUserPassword(@RequestBody ResetPasswordRequest resetPasswordRequest){
        User user = userRepository.findByLoginName(resetPasswordRequest.getLoginName());
        if (StringUtils.isEmpty(resetPasswordRequest.getPassword())){
            return new ResponseEntity<>("{\"code\":\"password.empty\", \"message\": \"Password can not be empty\"}", HttpStatus.BAD_REQUEST);
        }
        if (user.getPassword().equals(resetPasswordRequest.getPassword())){
            return new ResponseEntity<>("{\"code\":\"password.same\", \"message\": \"New password can not be same to the old password\"}", HttpStatus.BAD_REQUEST);
        }
        user.setPassword(resetPasswordRequest.getPassword());
        userRepository.save(newArrayList(user));
        return new ResponseEntity<HttpStatus>(HttpStatus.OK);
    }

    @RequestMapping(value = "/users/forbidden", method = RequestMethod.POST)
    public HttpEntity<?> forbiddenUser(@RequestBody ForbiddenUser forbiddenUser){
        User user = userRepository.findByLoginName(forbiddenUser.getLoginName());
        if(null == user){
            return new ResponseEntity<>("{\"code\":\"user.empty\", \"message\": \"user not exist\"}", HttpStatus.BAD_REQUEST);
        }
        if (StringUtils.isEmpty(forbiddenUser.getPassword())){
            return new ResponseEntity<>("{\"code\":\"password.empty\", \"message\": \"Password can not be empty\"}", HttpStatus.BAD_REQUEST);
        }
        if (user.getPassword().equals(forbiddenUser.getPassword())){
            return new ResponseEntity<>("{\"code\":\"password.same\", \"message\": \"password is invalid\"}", HttpStatus.BAD_REQUEST);
        }
        user.setEnabled(false);;
        userRepository.save(newArrayList(user));
        return new ResponseEntity<HttpStatus>(HttpStatus.OK);
    }
    
    @RequestMapping(value = "/users/roles.update", method = RequestMethod.POST)
    public HttpEntity<?> updateUserRoles(
    		@RequestBody UpdateUserRoleRequest updateUserRoleRequest,
    		HttpServletRequest request,
    		HttpServletResponse response){

    	
		ResponseEntity<Object> responseEntity =  null;		
		try {	        
	        responseEntity=userService.updateUserRoles(updateUserRoleRequest,request,response);			
		} catch (Exception e) {			
			logger.error(e.getMessage(),e);
			responseEntity=new ResponseEntity<Object>(HttpStatus.BAD_REQUEST);			
		}	
		
		return responseEntity;
    }

    @RequestMapping(value = "/users/checkloginname", method = RequestMethod.GET)
    public HttpEntity<?> checkLoginName(
            @RequestParam(value = "loginname") String loginName) {

        User user = userRepository.findByLoginName(loginName);
        if (user == null) {
            return new ResponseEntity<HttpStatus>( HttpStatus.OK);
        } else {
            return new ResponseEntity<HttpStatus>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/users/myInfo", method = RequestMethod.GET)
    public HttpEntity<?> getCurrentUserInfo(HttpServletRequest request) {
        User user = userRepository.findByLoginName(request.getUserPrincipal().getName());
        UserResponse userResponse = new UserResponse(user.getUuid(), user.getLoginName());
        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/users/userInfo", method = RequestMethod.GET)
    public HttpEntity<?> getuserInfo(String username,HttpServletRequest request) {
        User user = userRepository.findByLoginName(username);
        UserResponse userResponse = new UserResponse(user.getUuid(), user.getLoginName());
        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    private boolean isValidCheckCode(String checkCode) {
        return true;
    }

	@RequestMapping(value="/users/{uuid}",method = RequestMethod.GET, produces = "application/hal+json;charset=utf-8")
	@Transactional
	public HttpEntity<?> findOneUserById(
			 @PathVariable String uuid,
			 HttpServletRequest request,
			 HttpServletResponse response){
		
		ResponseEntity<?> responseEntity =  null;		
		try {	        
	        responseEntity=userService.findOneUserById(uuid,request,response);			
		} catch (Exception e) {			
			logger.error(e.getMessage(),e);
			responseEntity=new ResponseEntity<Object>(HttpStatus.BAD_REQUEST);			
		}	
		
		return responseEntity;
		
	}
}
