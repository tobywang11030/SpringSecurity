package hello.config;

import org.springframework.context.annotation.Configuration;

/**
 * @author tobywang
 * @date 9/2/2019
 */
@Configuration
public class FilterOrderConfig {
   /* @Bean
    public FilterRegistrationBean filterRegistrationBean1() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(new OAuth2ClientAuthenticationProcessingFilter("/order/**"));
        
        filterRegistrationBean.setOrder(0);//order的数值越小 则优先级越高
        return filterRegistrationBean;
    }*/
    
}
