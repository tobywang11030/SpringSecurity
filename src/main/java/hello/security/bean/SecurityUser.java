package hello.security.bean;

import hello.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;

import static hello.constant.AuthenticationConstant.ROLE_CUSTOM;

/**
 * @author tobywang
 * @date 8/29/2019
 * 实现自定义User对象的role字段和getAuthorities的转换
 * 需要实现UserDetails接口才能放到SecurityContext中
 */
public class SecurityUser extends User implements UserDetails {
    private static final long serialVersionUID = 1L;
    
    public SecurityUser(User user) {
        if (user != null) {
            setUserUuid(user.getUserUuid());
            setUsername(user.getUsername());
            setPassword(user.getPassword());
            setEmail(user.getEmail());
            setTelephone(user.getTelephone());
            setRole(user.getRole());
            setImage(user.getImage());
            setLastIp(user.getLastIp());
            setLastTime(user.getLastTime());
        }
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        String role = getRole();
        if (StringUtils.isEmpty(role)) {
            role = ROLE_CUSTOM;
        }
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
        authorities.add(authority);
        return authorities;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
}
