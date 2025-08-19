package com.dliriotech.tms.tyreservice.service;

import com.dliriotech.tms.tyreservice.dto.UserInfoResponse;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<UserInfoResponse> getUserInfoById(Integer userId);
}