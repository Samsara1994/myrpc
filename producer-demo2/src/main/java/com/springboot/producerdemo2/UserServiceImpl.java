package com.springboot.producerdemo2;

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
        return new User(userId, "test", 2222); // 模拟查询
    }

    @Override
    public Boolean saveUser(User user) {
        return false; // 模拟保存
    }
}
