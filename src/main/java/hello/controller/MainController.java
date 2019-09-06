package hello.controller;

import hello.model.User;
import hello.repository.UserRepository;
import hello.services.LoginAttemptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.websocket.server.PathParam;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static hello.constant.AuthenticationConstant.ROLE_ADMIN;
import static hello.constant.AuthenticationConstant.ROLE_CUSTOM;
import static hello.constant.AuthenticationConstant.ROLE_PREFIX;

@RestController
public class MainController {
    
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    
    @Autowired
    UserRepository userRepository;
    
    @Autowired
    private LoginAttemptService loginAttemptService;
    
    @Autowired
    private HttpServletRequest request;
    
    @Autowired
    private HttpServletResponse response;
    
    @Autowired
    protected AuthenticationManager authenticationManager;
    
    //@Secured({ROLE_CUSTOM, ROLE_ADMIN})
    @RequestMapping("/home")
    public ModelAndView hello() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", getUser());
        modelAndView.setViewName("home");
        return modelAndView;
    }
    
    //@Secured({ROLE_CUSTOM, ROLE_ADMIN})
    @RequestMapping("/")
    public ModelAndView defaultPage() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", getUser());
        modelAndView.setViewName("home");
        return modelAndView;
    }
    
    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public ModelAndView registerPage() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("register");
        return modelAndView;
    }
    
    @RequestMapping(value = "/phone", method = RequestMethod.GET)
    public ModelAndView phone() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("phone");
        return modelAndView;
    }
    
    
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public void register(@ModelAttribute User user) throws IOException {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
        String enPassword = encoder.encode(user.getPassword());
        String originalPassword = user.getPassword();
        user.setPassword(enPassword);
        user.setRole(ROLE_PREFIX + ROLE_CUSTOM);
        userRepository.save(user);
        //进行授权登录
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user.getUsername(), originalPassword);
        try {
            token.setDetails(new WebAuthenticationDetails(request));
            Authentication authenticatedUser = authenticationManager.authenticate(token);
            SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
            request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
        } catch (AuthenticationException e) {
            logger.info("Authentication failed: " + e.getMessage());
            response.sendRedirect("/register");
        }
        response.sendRedirect("/");
    }
    
    //@Secured(ROLE_ADMIN)
    @ResponseBody
    @RequestMapping("/search")
    public User search(@PathParam("email") String email) {
        return userRepository.findByEmail(email);
    }
    
    public User getUser() { //为了session从获取用户信息,可以配置如下
        User user = new User();
        Authentication auth = getAuthentication();
        if (auth.getPrincipal() instanceof UserDetails) {
            user = (User) auth.getPrincipal();
        } else {
            user.setUsername(auth.getPrincipal().toString());
        }
        return user;
    }
    
    @RequestMapping("/login")
    public ModelAndView login() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        HttpSession session = request.getSession();
        Object uid = session.getAttribute("failureUser");
        if (uid != null) {
            int count = 3 - loginAttemptService.getUserLoginCount(uid.toString());
            request.setAttribute("failureUser", uid);
            request.setAttribute("count", count);
            if (count == 0) {
                request.setAttribute("blocked", true);
            }
        }
        return modelAndView;
    }
    
    private Authentication getAuthentication() {
        SecurityContext ctx = SecurityContextHolder.getContext();
        return ctx.getAuthentication();
    }
    
    @RequestMapping("/updateRole")
    public void updateRole() throws IOException {
        // 得到当前的认证信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        //  生成当前的所有授权
        List<GrantedAuthority> updatedAuthorities = new ArrayList<>(auth.getAuthorities());
        // 添加 ROLE_VIP 授权
        updatedAuthorities.add(new SimpleGrantedAuthority(ROLE_PREFIX+ROLE_ADMIN));
        // 生成新的认证信息
        Authentication newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(), updatedAuthorities);
        // 重置认证信息
        SecurityContextHolder.getContext().setAuthentication(newAuth);
        response.sendRedirect("/");
    }
    
    
}
