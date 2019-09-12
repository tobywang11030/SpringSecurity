package com.toby.sso.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationManager;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationProcessingFilter;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;

import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.sql.DataSource;

/**
 * @author tobywang
 * @date 9/9/2019
 */
@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {
    
    @Resource
    private DataSource dataSource;
    
    @Autowired
    private TokenStore jDbcTokenStore;
    
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.requestMatchers().antMatchers("/user/**")
                .and()
                .authorizeRequests()
                .anyRequest().authenticated();
    }
    
    
    @Bean
    public ResourceServerTokenServices customTokenService() {
        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        ClientDetailsService clientDetailsService = new JdbcClientDetailsService(dataSource);
        defaultTokenServices.setTokenStore(jDbcTokenStore);
        defaultTokenServices.setClientDetailsService(clientDetailsService);
        return defaultTokenServices;
    }
}
