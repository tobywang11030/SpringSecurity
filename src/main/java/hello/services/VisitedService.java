package hello.services;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * @author tobywang
 * @date 8/30/2019
 */

@Service
public class VisitedService {
    
    //直接使用ehcache注解，可不用cacheManager
    private static final Map<String, Integer> cache = new HashMap<>();
    
    public int putVisitedCount(String path, int count) {
        return cache.put(path, count);
    }
    
    public int getVisitedCount(String path) {
        if (cache.containsKey(path)) {
            return cache.get(path);
        }
        return 0;
    }
    
    public String getMaxKey() {
        List<String> sorted = cache.entrySet().stream().sorted(Comparator.comparing(k -> k.getValue(), Comparator.reverseOrder())).map(
                b -> b.getKey()).collect(toList());
        return sorted.get(0);
    }
    
    
}
