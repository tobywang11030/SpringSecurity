package hello.config;

import org.springframework.context.annotation.Configuration;

/**
 * @author tobywang
 * @date 9/2/2019
 */
@Configuration
public class FilterOrderConfig {
    //Oauth2的全局filter，需注册到合适位置，作用为捕获为认证异常转发到认证中心
    /*@Bean
    public FilterRegistrationBean oauth2ClientFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new OAuth2ClientContextFilter());
        registration.addUrlPatterns("/");
        //配置filter的order
        registration.setOrder(-100);
        return registration;
    }*/
    
    //注册Filter的三种方式
    //1. 返回值为Filter的方法加上@Bean注解
    //2. 使用FilterRegistrationBean手动注册
    //3. 继承WebSecurityConfigurerAdapter，在configure(HttpSecurity http) 方法中用http.addFilterBefore(casSsoFilter, BasicAuthenticationFilter.class);
    //    这种其实不算注册，因为它是将这个filter添加到SpringSecurityFilterChain当中
    
}
