package com.toby.sso.server.config;

import com.toby.sso.server.model.SecurityUser;
import com.toby.sso.server.services.MyJdbcTokenStore;
import com.toby.sso.server.services.MyUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationManager;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationProcessingFilter;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;

/**
 * @author tobywang
 * @date 9/9/2019
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    
    @Autowired
    private MyUserDetailsService myUserDetailsService;
    
    @Autowired
    private ResourceServerTokenServices customTokenService;
    
    @Autowired
    private AuthenticationManager authenticationManager;

    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.authorizeRequests()
                .antMatchers("/oauth/**").permitAll()
                .anyRequest().authenticated()   // 其他地址的访问均需验证权限
                .and()
                .formLogin()
                .loginPage("/login")
                .permitAll()
                .successHandler(loginSuccessHandler())
                .and()
                .logout().permitAll()
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .deleteCookies("JSESSIONID")
                .logoutSuccessHandler(logoutSuccessHandler());
        http.addFilterBefore(resourceFilter(), AbstractPreAuthenticatedProcessingFilter.class);
    }

    
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/assets/**");
    }
    
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(myUserDetailsService).passwordEncoder(passwordEncoder());
        auth.eraseCredentials(false);
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
    
    
    public Filter resourceFilter(){
        OAuth2AuthenticationProcessingFilter resourceFilter = new OAuth2AuthenticationProcessingFilter();
        OAuth2AuthenticationManager oauthAuthenticationManager = new OAuth2AuthenticationManager();
        if (authenticationManager != null) {
            if (authenticationManager instanceof OAuth2AuthenticationManager) {
                oauthAuthenticationManager = (OAuth2AuthenticationManager) authenticationManager;
            }
        }
        oauthAuthenticationManager.setTokenServices(customTokenService);
        resourceFilter.setAuthenticationManager(oauthAuthenticationManager);
        resourceFilter.setStateless(false);
        return resourceFilter;
    }
    
    
}
