package com.springboot.producerdemo;

import com.springboot.rpccommon.annotation.RpcService;
import com.springboot.rpccommon.domain.User;
import com.springboot.rpccommon.domain.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RpcService
public class UserServiceImpl2 implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl2.class);

    @Override
    public User getUserById(Long userId) {
        log.info("getUserById");
        return new User(userId, "test", 20); // 模拟查询
    }

    @Override
    public Boolean saveUser(User user) {
        return true; // 模拟保存
    }
}
