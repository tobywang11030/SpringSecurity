package hello.security.provider;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * @author tobywang
 * @date 9/5/2019
 */
public class PhoneNumberAuthentication extends AbstractAuthenticationToken {
    private static final long serialVersionUID = -4809832298438307309L;
    
    private final Authentication userAuthentication;
    
    public PhoneNumberAuthentication(Collection<? extends GrantedAuthority> authorities, Authentication userAuthentication) {
        super(authorities);
        this.userAuthentication = userAuthentication;
    }
    
    @Override
    public Object getCredentials() {
        return "";
    }
    
    @Override
    public Object getPrincipal() {
        return userAuthentication.getPrincipal();
    }
    
    /**
     * Convenience method to check if there is a user associated with this token, or just a client application.
     *
     * @return true if this token represents a client app not acting on behalf of a user
     */
    public boolean isClientOnly() {
        return userAuthentication == null;
    }
    
    @Override
    public boolean isAuthenticated() {
        return userAuthentication == null || userAuthentication.isAuthenticated();
    }
    
    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        if (userAuthentication != null && CredentialsContainer.class.isAssignableFrom(userAuthentication.getClass())) {
            CredentialsContainer.class.cast(userAuthentication).eraseCredentials();
        }
    }
}
