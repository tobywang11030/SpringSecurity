package hello.exception.handler;

import hello.exception.RequestLimitException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author tobywang
 * @date 9/16/2019
 */

@ControllerAdvice
public class ExcetionViewResolver implements HandlerExceptionResolver {
    
    @Override
    public ModelAndView resolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) {
        ModelAndView modelAndView = new ModelAndView();
        if (e instanceof RequestLimitException) {
            modelAndView.setViewName("error/requestlimit");
        }
        
        return modelAndView;
    }
}
