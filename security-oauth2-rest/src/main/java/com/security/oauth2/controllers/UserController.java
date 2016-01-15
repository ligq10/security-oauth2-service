package com.security.oauth2.controllers;




import com.security.oauth2.entity.Role;
import com.security.oauth2.entity.User;
import com.security.oauth2.model.*;
import com.security.oauth2.repository.RoleRepository;
import com.security.oauth2.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;






import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Date;
import java.util.List;





import static com.google.common.collect.Lists.newArrayList;

@Controller
public class UserController {
	//@Autowired
	//private KafkaProducerConfiguration kafkaProducerConfiguration;
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    private static final String USER_ROLE = "USER";

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public HttpEntity<?> register(@RequestBody RegisterUser registerUser, HttpServletResponse response){
        if (StringUtils.isEmpty(registerUser.getLoginName()) || StringUtils.isEmpty(registerUser.getPassword()) || !isValidCheckCode(registerUser.getCheckCode())){
            return new ResponseEntity<HttpStatus>(HttpStatus.BAD_REQUEST);
        }
        User user = new User();
        user.setLoginName(registerUser.getLoginName());
        user.setPassword(registerUser.getPassword());
        user.setEnabled(true);
        Role role = roleRepository.findByName(USER_ROLE);
        userRepository.save(newArrayList(user));
        response.setHeader("Location", "/security/users/" + user.getUuid());
        //UserEvent.UserRegisteredEvent(user, kafkaProducerConfiguration);
        return new ResponseEntity<HttpStatus>(HttpStatus.OK);
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
    public HttpEntity<?> updateUserRoles(@RequestBody UpdateUserRoleRequest updateUserRoleRequest){
        User user = userRepository.findByLoginName(updateUserRoleRequest.getLoginName());
        List<String> roleNames = updateUserRoleRequest.getRoleNames();
        //user.getRoles().clear();
        for (String roleName : roleNames){
            Role role = roleRepository.findByName(roleName);
            if (role != null){
                //user.getRoles().add(role);
            }
        }
        userRepository.save(user);
        return new ResponseEntity<HttpStatus>(HttpStatus.OK);
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


}
