package com.springboot.consumerdemo;

import com.springboot.rpccommon.annotation.RpcReference;
import com.springboot.rpccommon.domain.User;
import com.springboot.rpccommon.domain.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
    @RpcReference // 自动注入代理
    private UserService userService;

    @GetMapping("/user/abc")
    public User getUser() {
        // 远程调用
        return userService.getUserById(2L);
    }

    @PostMapping("/user")
    public Boolean saveUser(@RequestBody User user) {
        return userService.saveUser(user); // 远程调用
    }

    @GetMapping("/test")
    public String test() {
        return "调用成功"; // 远程调用
    }
}
