package com.leyou.auth.controller;

import com.leyou.auth.service.AuthService;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.user.DTO.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 登录
     * @param username
     * @param password
     * @param response
     * @return
     */
    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestParam(name = "username") String username,
                                      @RequestParam(name = "password") String password,
                                      HttpServletResponse response) {
        authService.login(username,password,response);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

    /**
     * 验证用户登录状态
     * @return
     */
    @GetMapping("/verify")
    public ResponseEntity<UserInfo> userVerify(HttpServletRequest request,HttpServletResponse response){
        return ResponseEntity.ok(authService.userVerify(request,response));
    }

    /**
     * 退出登录
     * @param request
     * @param response
     * @return
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request,HttpServletResponse response){
        authService.logout(request,response);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
