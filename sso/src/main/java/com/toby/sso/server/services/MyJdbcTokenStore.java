package com.toby.sso.server.services;

import com.toby.sso.server.model.OauthAccessToken;
import com.toby.sso.server.model.SecurityUser;
import com.toby.sso.server.model.User;
import com.toby.sso.server.repository.AccessTokenRepository;
import com.toby.sso.server.repository.UserRepository;
import org.apache.tomcat.util.http.parser.Authorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author tobywang
 * @date 9/11/2019
 */

public class MyJdbcTokenStore extends JdbcTokenStore {
    
    private static final Logger logger = LoggerFactory.getLogger(MyJdbcTokenStore.class);
    private AccessTokenRepository accessTokenRepository;
    private UserRepository userRepository;
    public MyJdbcTokenStore(DataSource dataSource, AccessTokenRepository accessTokenRepository, UserRepository userRepository) {
        super(dataSource);
        this.accessTokenRepository = accessTokenRepository;
        this.userRepository = userRepository;
    }
    
    @Override
    public OAuth2Authentication readAuthentication(String token) {
        OAuth2Authentication authentication = null;
        OAuth2Authentication authenticationFromToken = super.readAuthentication(token);
        if (authenticationFromToken != null) {
            OauthAccessToken oauthAccessToken = accessTokenRepository.findByTokenId(extractTokenKey(token));
            if (oauthAccessToken != null) {
                if (authenticationFromToken.getOAuth2Request().getScope().contains("user_info")){
                    User user = userRepository.findByUsername(oauthAccessToken.getUserName());
                    //user.setPassword(null);
                    Authentication userAuthentication = new UsernamePasswordAuthenticationToken(new SecurityUser(user),null,
                            AuthorityUtils.commaSeparatedStringToAuthorityList(user.getRole()));
                    authentication = new OAuth2Authentication(authenticationFromToken.getOAuth2Request(),userAuthentication);
                } else if (authenticationFromToken.getOAuth2Request().getScope().contains("base_info")){
                    Authentication userAuthentication = new UsernamePasswordAuthenticationToken(oauthAccessToken.getUserName(),null,
                            authenticationFromToken.getAuthorities());
                    authentication = new OAuth2Authentication(authenticationFromToken.getOAuth2Request(),userAuthentication);
                }
            }
        }
        
        if (authentication != null) {
            return authentication;
        }
        return authenticationFromToken;
    }
}
