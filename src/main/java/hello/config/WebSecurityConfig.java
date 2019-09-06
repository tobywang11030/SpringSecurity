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
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static hello.constant.AuthenticationConstant.ROLE_ADMIN;
import static hello.constant.AuthenticationConstant.ROLE_CUSTOM;
import static hello.constant.AuthenticationConstant.ROLE_USER;

@Configuration
@EnableWebSecurity
//@EnableOAuth2Sso
@EnableOAuth2Client
@EnableGlobalMethodSecurity(securedEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);
    
    @Autowired
    private LoginAttemptService loginAttemptService;
    
    @Autowired
    private Filter githubSsoFilter;
    
    @Autowired
    private Filter phoneNumberAuthFilter;
    
    
    @Override
    protected void configure(HttpSecurity http) throws Exception { //配置策略
        http.csrf().disable();
        http.authorizeRequests().
                antMatchers("/search").hasRole(ROLE_ADMIN).
                antMatchers("/phone","/register","/sendSmsCode").permitAll().anyRequest().authenticated().
                //antMatchers("/home", "/").hasAnyRole(ROLE_ADMIN, ROLE_CUSTOM, ROLE_USER).
                and().formLogin().loginPage("/login").permitAll().successHandler(loginSuccessHandler()).
                and().logout().permitAll().invalidateHttpSession(true).clearAuthentication(true).
                logoutRequestMatcher(new AntPathRequestMatcher("/logout")).
                deleteCookies("JSESSIONID").logoutSuccessHandler(logoutSuccessHandler()).and().rememberMe().key(
                "unique-and-secret").rememberMeCookieName("remember-me-cookie-name").tokenValiditySeconds(24 * 60 * 60).
                and().sessionManagement().maximumSessions(10).expiredUrl("/login");
        http.requiresChannel().antMatchers("/**").requiresSecure();
        
        http.addFilterBefore(githubSsoFilter, BasicAuthenticationFilter.class);
        http.addFilterBefore(phoneNumberAuthFilter, BasicAuthenticationFilter.class);
    }
    
    
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/js/**", "/css/**", "/img/**", "/webjars/**");
    }
    
    
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder());
        auth.eraseCredentials(false);
    }
    
    @Override
    @Bean
    public UserDetailsService userDetailsService() {    //用户登录实现
        return new UserDetailsService() {
            @Autowired
            private UserRepository userRepository;
            
            @Override
            public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
                if (isBlocked(s)) {
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