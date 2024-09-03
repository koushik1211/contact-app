package com.monetize360.contact_application.serivce;

import com.monetize360.contact_application.dao.UserDao;
import com.monetize360.contact_application.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

@Repository
public class UserService {
    @Autowired
    UserDao userDao;

    @Autowired
    private JWTService  jwtService;
    @Autowired
    AuthenticationManager authManager;

    private BCryptPasswordEncoder encoder=new BCryptPasswordEncoder(12);

    public User register(User user)
    {
        user.setPassword(encoder.encode((user.getPassword())));

      return   userDao.save(user);
    }


    public String verify(User user) {
        Authentication authentication=authManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(),user.getPassword()));

       if( authentication.isAuthenticated())
           return jwtService.generateToken(user.getUsername());

       return "fail";
    }
}
