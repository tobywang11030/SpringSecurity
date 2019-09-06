package hello.security.filter;

import hello.security.event.PhoneNumberAuthenticationFailureEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

import static hello.constant.AuthenticationConstant.ROLE_PREFIX;
import static hello.constant.AuthenticationConstant.ROLE_USER;
import static hello.constant.AuthenticationConstant.SMS_CODE;

/**
 * @author tobywang
 * @date 9/4/2019
 */
public class PhoneNumberAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    
    private ApplicationEventPublisher eventPublisher;
    
    public PhoneNumberAuthenticationFilter(String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl);
        setAuthenticationManager(new NoopAuthenticationManager());
    }
    
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException,
            ServletException {
        String codeByUser = request.getParameter("verificationcode");
        String phoneNumber = request.getParameter("phonenumber");
        HttpSession session = request.getSession();
        if (session.getAttribute(SMS_CODE) != null) {
            String code = session.getAttribute(SMS_CODE).toString();
            if (codeByUser.equals(code)) {
                List<GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(ROLE_PREFIX + ROLE_USER);
                Authentication authentication = new UsernamePasswordAuthenticationToken(phoneNumber, "N/A", authorities);
                //publish(new AuthenticationSuccessEvent(authentication));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                session.removeAttribute(SMS_CODE);
                return authentication;
                
            }
        }
        
        BadCredentialsException bad = new BadCredentialsException("SMS Verification code is wrong");
        publish(new PhoneNumberAuthenticationFailureEvent(bad));
        throw bad;
    }
    
    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
        super.setApplicationEventPublisher(eventPublisher);
    }
    
    private void publish(ApplicationEvent event) {
        if (eventPublisher != null) {
            eventPublisher.publishEvent(event);
        }
    }
    
    private static class NoopAuthenticationManager implements AuthenticationManager {
        
        @Override
        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            throw new UnsupportedOperationException("No authentication should be done with this AuthenticationManager");
        }
        
    }
}
