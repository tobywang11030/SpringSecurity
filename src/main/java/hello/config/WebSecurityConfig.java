package hello.config;

import hello.model.User;
import hello.repository.UserRepository;
import hello.security.bean.SecurityUser;
import hello.services.LoginAttemptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static hello.constant.AuthenticationConstant.ROLE_ADMIN;

@Configuration
@EnableWebSecurity
//@EnableOAuth2Sso 若启用此注解，全局所有认证都将跳转到SSO认证中心去认证，而应用本身的其他登录方式比如表单登录将失效,具体可以看这个注解的源码
//中的WebSecurityConfigurerAdapter覆盖了自己的配置
@EnableOAuth2Client//启用此注解，需自己注册oauth2的相关Filter
@EnableGlobalMethodSecurity(securedEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);
    
    @Autowired
    private LoginAttemptService loginAttemptService;
    
    @Autowired
    private Filter githubSsoFilter;
    
    @Autowired
    private Filter phoneNumberAuthFilter;
    
    @Autowired
    private Filter casSsoFilter;
    
    @Autowired
    private OAuth2ClientContextFilter oauth2ClientContextFilter;
    
    
    @Override
    protected void configure(HttpSecurity http) throws Exception { //配置策略
        http.csrf().disable();
        http.authorizeRequests().
                antMatchers("/search").hasRole(ROLE_ADMIN).
                antMatchers("/phone", "/register", "/sendSmsCode", "/indexFile/**", "/searchFile").permitAll().anyRequest().authenticated().
                //antMatchers("/home", "/").hasAnyRole(ROLE_ADMIN, ROLE_CUSTOM, ROLE_USER).
                        and().formLogin().loginPage("/login").permitAll().successHandler(loginSuccessHandler()).
                and().logout().permitAll().invalidateHttpSession(true).clearAuthentication(true).
                logoutRequestMatcher(new AntPathRequestMatcher("/logout")).
                deleteCookies("JSESSIONID").logoutSuccessHandler(logoutSuccessHandler()).and().rememberMe().key(
                "unique-and-secret").rememberMeCookieName("remember-me-cookie-name").tokenValiditySeconds(24 * 60 * 60).
                and().sessionManagement().maximumSessions(10).expiredUrl("/login");
        //http.requiresChannel().antMatchers("/**").requiresSecure();
        
        //@Bean注解可以自动注册Filter，但它不是添加到SpringSecurity的过滤器链当中，造成无法正确处理认证结果，所以需要使用
        //下面的方式注册
        
        //Oauth2的全局filter，需注册到合适位置，作用为捕获为认证异常转发到认证中心
        http.addFilterBefore(oauth2ClientContextFilter, SecurityContextPersistenceFilter.class);
        //Github 的 SSO filter
        http.addFilterBefore(githubSsoFilter, BasicAuthenticationFilter.class);
        //自定义认证中心的SSO filter
        http.addFilterBefore(casSsoFilter, BasicAuthenticationFilter.class);
        //自定义filter实现手机号+短信验证码登录
        http.addFilterBefore(phoneNumberAuthFilter, BasicAuthenticationFilter.class);
    }
    
    
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/js/**", "/css/**", "/img/**", "/webjars/**");
    }
    
    
 /*   @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder());
        auth.eraseCredentials(false);
    }*/
    
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder());
        auth.eraseCredentials(false);
    }
    
    
    //也可通过创建一个类实现UserDetailsService接口加上@service注解，这里采用的是JavaConfig 的方法返回Bean的形式
    @Override
    @Bean
    public UserDetailsService userDetailsService() {    //用户登录实现
        return new UserDetailsService() {
            
            //自己实现的用户D奥层，从数据库获得User
            @Autowired
            private UserRepository userRepository;
            
            @Override
            public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
                if (isBlocked(s)) { //登录失败次数验证
                    throw new RuntimeException("blocked");
                }
                User user = userRepository.findByUsername(s);
                if (user == null) {
                    throw new UsernameNotFoundException("Username " + s + " not found");
                }
                
                return new SecurityUser(user);
            }
        };
    }
    
    private Boolean isBlocked(String uid) {
        return loginAttemptService.getUserLoginCount(uid) > 2;
    }
    
    //存储用户密码的时候也必须使用此Encoder
    @Bean
    public BCryptPasswordEncoder passwordEncoder() { //密码加密
        return new BCryptPasswordEncoder(4);
    }
    
    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() { //登出处理
        return new LogoutSuccessHandler() {
            @Override
            public void onLogoutSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                        Authentication authentication) throws IOException, ServletException {
                try {
                    if (authentication.getPrincipal() instanceof UserDetails) {
                        SecurityUser user = (SecurityUser) authentication.getPrincipal();
                        logger.info("USER : " + user.getUsername() + " LOGOUT SUCCESS !  ");
                    } else {
                        logger.info("USER : " + authentication.getPrincipal().toString() + " LOGOUT SUCCESS !  ");
                    }
                    
                } catch (Exception e) {
                    logger.info("LOGOUT EXCEPTION , e : " + e.getMessage());
                }
                httpServletResponse.sendRedirect("/login");
            }
        };
    }
    
    @Bean
    public SavedRequestAwareAuthenticationSuccessHandler loginSuccessHandler() { //登入处理
        return new SavedRequestAwareAuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws
                    IOException, ServletException {
                if (authentication.getPrincipal() instanceof UserDetails) {
                    SecurityUser user = (SecurityUser) authentication.getPrincipal();
                    logger.info("USER : " + user.getUsername() + " LOGIN SUCCESS !  ");
                } else {
                    logger.info("USER : " + authentication.getPrincipal().toString() + " LOGIN SUCCESS !  ");
                }
                super.onAuthenticationSuccess(request, response, authentication);
            }
        };
    }
    
    
}