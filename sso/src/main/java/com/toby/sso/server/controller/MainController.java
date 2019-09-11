package com.toby.sso.server.controller;

import com.toby.sso.server.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author tobywang
 * @date 9/9/2019
 */
@Controller
public class MainController {
    
    @Autowired
    private HttpServletRequest request;
    
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    @RequestMapping("/index")
    public String home(Model model) {
        model.addAttribute("user", getUser());
        return "index";
    }
    
    @RequestMapping("/")
    public String defaultPage(Model model) {
        model.addAttribute("user", getUser());
        return "index";
    }
    
    private Authentication getAuthentication() {
        SecurityContext ctx = SecurityContextHolder.getContext();
        return ctx.getAuthentication();
    }
    
    public User getUser() { //为了session从获取用户信息,可以配置如下
        User user = new User();
        Authentication auth = getAuthentication();
        if (auth.getPrincipal() instanceof UserDetails) {
            user = (User) auth.getPrincipal();
        } else {
            user.setUsername(auth.getPrincipal().toString());
        }
        List<GrantedAuthority> authorities = new ArrayList<>(auth.getAuthorities());
        user.setRole(authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(",")));
        return user;
    }
    
}
