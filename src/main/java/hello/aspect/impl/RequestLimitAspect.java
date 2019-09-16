package hello.aspect.impl;

import hello.aspect.RequestLimit;
import hello.exception.RequestLimitException;
import hello.services.RequestLimitService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

import static hello.utils.IpAdrressUtil.getIpAddr;

/**
 * @author tobywang
 * @date 9/16/2019
 */

@Aspect
@Component
public class RequestLimitAspect {
    private static final Logger logger = LoggerFactory.getLogger(RequestLimitAspect.class);
    
    @Autowired
    private RequestLimitService requestLimitService;
    
    
    @Before("within(@org.springframework.stereotype.Controller *) && @annotation(limit)")
    public void requestLimit(final JoinPoint joinPoint, RequestLimit limit) {
        Object[] args = joinPoint.getArgs();
        HttpServletRequest request = null;
        for(Object object : args) {
            if (object instanceof HttpServletRequest) {
                request = (HttpServletRequest)object;
                break;
            }
        }
        if (request == null) {
            return;
        }
        
        String ip = getIpAddr(request);
        String key = "req_limit_".concat(ip);
        int count = requestLimitService.getUserLoginCount(key) + 1;
        requestLimitService.putUserLoginCount(key,count);
        logger.info("key:" + key + " count:" + count);
        
        if (count > limit.count()) {
            logger.info("用户IP[{}]访问地址[{}]超过了限定的访问次数[{}]",ip,request.getRequestURL().toString(),limit.count());
            throw new RequestLimitException(444,"请求频率太快，请过5秒后再访问");
        }
    }
}
