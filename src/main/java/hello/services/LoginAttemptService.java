package hello.services;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * @author tobywang
 * @date 8/30/2019
 */

@Service
public class LoginAttemptService {
    
    
    @CachePut(value = "users", key = "#uid")
    public int putUserLoginCount(String uid, int count) {
        return count;
    }
    
    @Cacheable(value = "users", key = "#uid")
    public int getUserLoginCount(String uid) {
        return 0;
    }
    
    @CacheEvict(value = "users", key = "#uid")
    public void evictCount(String uid) {
    
    }
    
}
