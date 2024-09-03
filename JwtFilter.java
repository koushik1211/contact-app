package com.monetize360.contact_application.util;

import com.monetize360.contact_application.serivce.JWTService;
import com.monetize360.contact_application.serivce.UserDetailServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Component

public class JwtFilter extends OncePerRequestFilter {
    @Autowired
    JWTService jwtService;

    @Autowired
    ApplicationContext context;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //from client we get the token as follow , in the httpservletrequest header
        //Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyNiIsImlhdCI6MTcyNTI2MjQzNSwiZXhwIjoxNzI1MjY4OTE1fQ.cKPQkh8Pw23tdkTJ23IC2ghPiLP4uK5i_k3ogTe5jNA
        String authHeader=request.getHeader("Authorization");
        String token=null;
        String username=null;
        if(authHeader!=null && authHeader.startsWith("Bearer "))
        {
            token=authHeader.substring(7);
            username=jwtService.extractUserName(token);

        }
        if(username!=null && SecurityContextHolder.getContext().getAuthentication()==null)
        {
            UserDetails userDetails=context.getBean(UserDetailServiceImpl.class).loadUserByUsername(username);
           if(jwtService.validateToken(token,userDetails))
           {
               UsernamePasswordAuthenticationToken authToken=
                       new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
               authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
               SecurityContextHolder.getContext().setAuthentication(authToken);


           }
        }
        filterChain.doFilter(request,response);

    }
}
