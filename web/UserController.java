package com.monetize360.contact_application.web;

import com.monetize360.contact_application.domain.User;
import com.monetize360.contact_application.serivce.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/register")
    public User register(@RequestBody User user)
    {
       return userService.register(user);
    }

    @PostMapping("/login")
    public String login(@RequestBody User user)
    {
       // return "success";
        return userService.verify(user);
    }
}
