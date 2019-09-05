package hello.security.event;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.core.AuthenticationException;

/**
 * @author tobywang
 * @date 9/5/2019
 */
public class PhoneNumberAuthenticationFailureEvent extends AbstractAuthenticationFailureEvent {
    public PhoneNumberAuthenticationFailureEvent(AuthenticationException exception) {
        super(new FailedOAuthClientAuthentication(), exception);
    }
}

class FailedOAuthClientAuthentication extends AbstractAuthenticationToken {
    
    public FailedOAuthClientAuthentication() {
        super(null);
    }
    
    @Override
    public Object getCredentials() {
        return "";
    }
    
    @Override
    public Object getPrincipal() {
        return "UNKNOWN";
    }
    
}
