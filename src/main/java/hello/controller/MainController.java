package hello.controller;

import hello.aspect.RequestLimit;
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
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
import java.util.Map;
import java.util.stream.Collectors;

import static hello.constant.AuthenticationConstant.PRINCIPAL;
import static hello.constant.AuthenticationConstant.ROLE_ADMIN;
import static hello.constant.AuthenticationConstant.ROLE_CUSTOM;
import static hello.constant.AuthenticationConstant.ROLE_PREFIX;
import static hello.utils.BeanUtils.toBean;

@Controller
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
    public String home(Model model) {
        model.addAttribute("user", getUser());
        return "home";
    }
    
    //@Secured({ROLE_CUSTOM, ROLE_ADMIN})
    @RequestMapping("/")
    public String defaultPage(Model model) {
        model.addAttribute("user", getUser());
        return "home";
    }
    
    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String registerPage() {

        return "register";
    }
    
    @RequestMapping(value = "/phone", method = RequestMethod.GET)
    public String phone() {
        return "phone";
    }
    
    
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public void register(@ModelAttribute User user) throws IOException {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
        String enPassword = encoder.encode(user.getPassword());
        String originalPassword = user.getPassword();
        user.setPassword(enPassword);
        user.setRole(ROLE_PREFIX + ROLE_CUSTOM);
        userRepository.save(user);
        //注册成功后，自动登录，封装auth对象添加到SecurityContextHolder.getContext()
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
    
    
    public User getUser() { //为了session从获取用户信息,可以配置如下
       
        User user;
        Authentication auth = getAuthentication();
        //由于从SSO拿到的auth对象的principal不是UserDetail实例，所以需要自己封装
        if (auth instanceof OAuth2Authentication) {
            Map<String,Object> detailMap = (Map<String, Object>) ((OAuth2Authentication) auth).getUserAuthentication().getDetails();
            if (detailMap.get(PRINCIPAL) instanceof Map) {
                Map<String,Object> principal = (Map<String, Object>) detailMap.get(PRINCIPAL);
                user = toBean(principal,User.class);
            } else {
                user = new User();
                if (detailMap.containsKey(PRINCIPAL)) {
                    user.setUsername(detailMap.get(PRINCIPAL).toString());
                }else if (detailMap.containsKey("login")) {
                    user.setUsername(detailMap.get("login").toString());
                }
                
            }
            
        } else {
            user = new User();
            if (auth.getPrincipal() instanceof UserDetails) {
                user = (User) auth.getPrincipal();
            } else {
                //目前phoneNumber登录的获得的User不是User对象
                user.setUsername(auth.getPrincipal().toString());
            }
        }
        
        List<GrantedAuthority> authorities = new ArrayList<>(auth.getAuthorities());
        user.setRole(authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(",")));
        return user;
    }
    
    @RequestLimit(count = 2)
    @RequestMapping("/login")
    public String login(HttpServletRequest request) {
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
        return "login";
    }
    
    private Authentication getAuthentication() {
        SecurityContext ctx = SecurityContextHolder.getContext();
        return ctx.getAuthentication();
    }
    
    //动态修改当前用户Role，但不能持久化
    @RequestMapping("/updateRole")
    public String updateRole() throws IOException {
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
        return "redirect:/";
    }
    
    
}
