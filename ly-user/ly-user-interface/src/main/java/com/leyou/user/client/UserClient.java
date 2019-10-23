package com.leyou.user.client;

import com.leyou.user.DTO.AddressDTO;
import com.leyou.user.DTO.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("user-service")
public interface UserClient {

    @GetMapping("/query")
    UserDTO queryUserByPassword(@RequestParam("username") String username,
                                                       @RequestParam("password") String password);

    /**
     * 获取用户的 地址信息
     * @param userId
     * @param id
     * @return
     */
    @GetMapping("/address")
    AddressDTO findAddressByUserIdAndId(@RequestParam(name = "userId") Long userId,
                                        @RequestParam(name = "id") Long id);
}
