package hello.security.listener;

import hello.model.User;
import hello.services.LoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * @author tobywang
 * @date 8/30/2019
 * 登录成功的监听器，这里的作用为清空失败计数
 */
@Component
public class AuthenticationSuccessEventListener implements ApplicationListener<AuthenticationSuccessEvent> {
    
    @Autowired
    private LoginAttemptService loginAttemptService;
    
    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent e) {
        Authentication auth = e.getAuthentication();
        if (auth.getPrincipal() instanceof UserDetails) {
            User user = (User) auth.getPrincipal();
            loginAttemptService.evictCount(user.getUsername());
        } else {
            loginAttemptService.evictCount(e.getAuthentication().getPrincipal().toString());
        }
        
    }
}
