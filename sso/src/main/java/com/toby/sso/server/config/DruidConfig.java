package com.toby.sso.server.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import com.alibaba.druid.support.spring.stat.BeanTypeAutoProxyCreator;
import com.alibaba.druid.support.spring.stat.DruidStatInterceptor;
import com.toby.sso.server.model.User;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.SQLException;


/**
 * @author tobywang
 * @date 8/29/2019
 */
@Configuration
public class DruidConfig {
    
    @Bean
    public ServletRegistrationBean staViewServlet() {
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(new StatViewServlet(), "/druid/*");
        //白名单为空允许任何ip访问
        servletRegistrationBean.addInitParameter("allow", "127.0.0.1");
        //ip黑名单(存在共同时，deny优先于allow)：如果满足deny的即提示：Sorry you are not permitted...
        servletRegistrationBean.addInitParameter("deny", "127.0.0.2");
        //登录查看信息的账号密码
        servletRegistrationBean.addInitParameter("loginUsername", "admin");
        servletRegistrationBean.addInitParameter("loginPassword", "admin");
        //是否能够重置数据
        servletRegistrationBean.addInitParameter("resetEnable", "true");
        return servletRegistrationBean;
    }
    
    @Bean
    public FilterRegistrationBean statFilter() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new WebStatFilter());
        //添加过滤规则
        filterRegistrationBean.addUrlPatterns("/*");
        //添加不需要忽略的格式信息
        filterRegistrationBean.addInitParameter("exclusions", "*.js,*.gif,*.jpn,*.png,*.css,*.ico,/druid/*");
        return filterRegistrationBean;
    }
    
    //配置数据库的基本连接信息
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")//在application.properties中读取配置信息注入到DruidDataSource里
    public DataSource dataSource() {
        DruidDataSource druidDataSource = (DruidDataSource) DataSourceBuilder.create().type(DruidDataSource.class).build();
        druidDataSource.setInitialSize(3);//初始化物理连接的数量
        try {
            druidDataSource.addFilters("stat,wall");//stat是sql监控，wall是防火墙(如果不添加则监控无效)，不能添加log4j不然会出错
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return druidDataSource;
    }
    
    @Bean
    public DruidStatInterceptor druidStatInterceptor() {
        return new DruidStatInterceptor();
    }
    
    @Bean
    public BeanTypeAutoProxyCreator beanTypeAutoProxyCreator() {
        BeanTypeAutoProxyCreator beanTypeAutoProxyCreator = new BeanTypeAutoProxyCreator();
        beanTypeAutoProxyCreator.setTargetBeanType(User.class);
        beanTypeAutoProxyCreator.setInterceptorNames("druidStatInterceptor");
        return beanTypeAutoProxyCreator;
    }
}
