package com.toby.sso.server.repository;


import com.toby.sso.server.model.OauthAccessToken;
import com.toby.sso.server.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author tobywang
 * @date 8/29/2019
 */

@Repository
public interface AccessTokenRepository extends JpaRepository<OauthAccessToken, Integer> {
    OauthAccessToken findByTokenId(String tokenId);
    
}
