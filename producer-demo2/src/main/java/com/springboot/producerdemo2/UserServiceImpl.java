package com.springboot.producerdemo2;

import com.springboot.rpccommon.annotation.RpcService;
import com.springboot.rpccommon.domain.User;
import com.springboot.rpccommon.domain.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RpcService(version = "1.0")
public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public User getUserById(Long userId) {
        log.info("getUserById");
        return new User(userId, "test", 2222); // 模拟查询
    }

    @Override
    public Boolean saveUser(User user) {
        return false; // 模拟保存
    }
}
