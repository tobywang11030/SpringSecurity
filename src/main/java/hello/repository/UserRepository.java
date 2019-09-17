package hello.repository;

import hello.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author tobywang
 * @date 8/29/2019
 *  使用JPA查询数据库，可不写具体sql，采用 [fandBy字段] 的方式
 */

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);
    
    User findByEmail(String email);
}
