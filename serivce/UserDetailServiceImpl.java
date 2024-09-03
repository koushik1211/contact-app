package com.monetize360.contact_application.serivce;

import com.monetize360.contact_application.dao.UserDao;
import com.monetize360.contact_application.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
    private UserDao userDao;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        User user= userDao.findByUsername(userName);
        if(user==null)
        {
            throw new UsernameNotFoundException("User Not Found");
        }



        return new UserDetailImpl(user);
    }
}
