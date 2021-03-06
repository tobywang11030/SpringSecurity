package hello.config;

import hello.security.filter.PhoneNumberAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    
    //自定义filter可用此对象发布认证成功事件
    @Autowired
    private ApplicationContext applicationContext;
    
    
    //从配置文件读取并自动填充到Bean
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
    @ConfigurationProperties("cas.client")
    public AuthorizationCodeResourceDetails cas() {
        return new AuthorizationCodeResourceDetails();
    }
    
    @Bean
    @ConfigurationProperties("cas.resource")
    public ResourceServerProperties casResource() {
        return new ResourceServerProperties();
    }
    
    
    @Bean
    public Filter githubSsoFilter() {
        OAuth2ClientAuthenticationProcessingFilter githubFilter = new OAuth2ClientAuthenticationProcessingFilter("/login/github");
        OAuth2RestTemplate githubTemplate = new OAuth2RestTemplate(github(), oauth2ClientContext);
        githubFilter.setRestTemplate(githubTemplate);
        UserInfoTokenServices tokenServices = new UserInfoTokenServices(githubResource().getUserInfoUri(), github().getClientId());
        tokenServices.setRestTemplate(githubTemplate);
        githubFilter.setTokenServices(tokenServices);
        return githubFilter;
    }
    
    @Bean
    public Filter casSsoFilter() {
        OAuth2ClientAuthenticationProcessingFilter casFilter = new OAuth2ClientAuthenticationProcessingFilter("/login/cas");
        OAuth2RestTemplate casTemplate = new OAuth2RestTemplate(cas(), oauth2ClientContext);
        casFilter.setRestTemplate(casTemplate);
        UserInfoTokenServices tokenServices = new UserInfoTokenServices(casResource().getUserInfoUri(), cas().getClientId());
        tokenServices.setRestTemplate(casTemplate);
        casFilter.setTokenServices(tokenServices);
        return casFilter;
    }
    
    @Bean
    public Filter phoneNumberAuthFilter() {
        PhoneNumberAuthenticationFilter phoneNumberilter = new PhoneNumberAuthenticationFilter("/phonelogin");
        phoneNumberilter.setApplicationEventPublisher(applicationContext);
        return phoneNumberilter;
    }
    
    //在springboot中，@Bean注解会自动注册Filter，Servlet，listener，
    // 下面的三个filter是需要有spring security添加到springsecuritychain中的，不需要系统注册为全局filter，所以关闭全局注册
    
    @Bean
    public FilterRegistrationBean registration(@Qualifier("githubSsoFilter") Filter filter) {
        FilterRegistrationBean registration = new FilterRegistrationBean(filter);
        registration.setEnabled(false);
        return registration;
    }
    
    @Bean
    public FilterRegistrationBean registrationCasSsoFilter(@Qualifier("casSsoFilter") Filter filter) {
        FilterRegistrationBean registration = new FilterRegistrationBean(filter);
        registration.setEnabled(false);
        return registration;
    }
    
    @Bean
    public FilterRegistrationBean registrationPhoneNumberAuthFilter(@Qualifier("phoneNumberAuthFilter") Filter filter) {
        FilterRegistrationBean registration = new FilterRegistrationBean(filter);
        registration.setEnabled(false);
        return registration;
    }
}
