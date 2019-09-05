package hello.config;

import hello.security.filter.PhoneNumberAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;

import javax.servlet.Filter;

/**
 * @author tobywang
 * @date 9/3/2019
 */

@Configuration
public class SsoConfig {
    
    @Autowired
    OAuth2ClientContext oauth2ClientContext;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    
    @Bean
    @ConfigurationProperties("github.client")
    public AuthorizationCodeResourceDetails github() {
        return new AuthorizationCodeResourceDetails();
    }
    
    @Bean
    @ConfigurationProperties("github.resource")
    public ResourceServerProperties githubResource() {
        return new ResourceServerProperties();
    }
    
    @Bean
    public FilterRegistrationBean oauth2ClientFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new OAuth2ClientContextFilter());
        registration.setOrder(-100);
        return registration;
    }
    
    @Bean
    public Filter githubSsoFilter() {
        OAuth2ClientAuthenticationProcessingFilter githubFilter = new OAuth2ClientAuthenticationProcessingFilter("/login/github");
        OAuth2RestTemplate facebookTemplate = new OAuth2RestTemplate(github(), oauth2ClientContext);
        githubFilter.setRestTemplate(facebookTemplate);
        UserInfoTokenServices tokenServices = new UserInfoTokenServices(githubResource().getUserInfoUri(), github().getClientId());
        tokenServices.setRestTemplate(facebookTemplate);
        githubFilter.setTokenServices(tokenServices);
        return githubFilter;
    }
    
    @Bean
    public Filter phoneNumberAuthFilter() {
        PhoneNumberAuthenticationFilter phoneNumberilter = new PhoneNumberAuthenticationFilter("/phonelogin");
        phoneNumberilter.setApplicationEventPublisher(applicationContext);
        return phoneNumberilter;
    }
}
