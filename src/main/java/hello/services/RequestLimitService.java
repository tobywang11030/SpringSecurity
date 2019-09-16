package hello.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * @author tobywang
 * @date 8/30/2019
 */

@Service
public class RequestLimitService {
    
    @Autowired
    CacheManager cacheManager;
    
    
    @CachePut(value = "limits", key = "#ip")
    public int putUserLoginCount(String ip, int count) {
        return count;
    }
    
    @Cacheable(value = "limits", key = "#ip")
    public int getUserLoginCount(String ip) {
        return 0;
    }
    
    @CacheEvict(value = "limits", key = "#ip")
    public void evictCount(String ip) {
    
    }
    
}
