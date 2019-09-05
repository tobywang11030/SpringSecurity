package hello.security.listener;

import hello.services.LoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author tobywang
 * @date 8/30/2019
 */
@Component
public class AuthenticationFailureListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {
    
    @Autowired
    private LoginAttemptService loginAttemptService;
    
    @Autowired
    private HttpServletRequest request;
    
    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent e) {
        String uid = e.getAuthentication().getPrincipal().toString();
        int count = loginAttemptService.getUserLoginCount(uid) + 1;
        loginAttemptService.putUserLoginCount(uid, count);
        HttpSession session = request.getSession();
        session.setAttribute("failureUser", uid);
    }
}
