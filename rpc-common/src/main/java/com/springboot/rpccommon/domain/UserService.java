package com.springboot.rpccommon.domain;

public interface UserService {
    // 泛型返回值：查询用户
    User getUserById(Long userId);
    // 泛型参数：保存用户
    Boolean saveUser(User user);
}
