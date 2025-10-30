package com.springboot.producerdemo;

import com.springboot.rpccommon.annotation.RpcService;
import com.springboot.rpccommon.domain.User;
import com.springboot.rpccommon.domain.UserService;
import org.springframework.stereotype.Component;

@Component
@RpcService
public class UserServiceImpl implements UserService {
    @Override
    public User getUserById(Long userId) {
        System.out.println("getUserById");
        return new User(userId, "test", 20); // 模拟查询
    }

    @Override
    public Boolean saveUser(User user) {
        return true; // 模拟保存
    }
}
