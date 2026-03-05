package com.dliriotech.tms.tyreservice.service;

import com.dliriotech.tms.tyreservice.dto.UserInfoResponse;

import java.util.Optional;

public interface UserService {
    Optional<UserInfoResponse> getUserInfoById(Integer userId);
}