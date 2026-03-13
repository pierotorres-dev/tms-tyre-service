package com.dliriotech.tms.tyreservice.service.impl;

import com.dliriotech.tms.tyreservice.dto.UserInfoResponse;
import com.dliriotech.tms.tyreservice.entity.AuthUser;
import com.dliriotech.tms.tyreservice.repository.AuthUserRepository;
import com.dliriotech.tms.tyreservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final AuthUserRepository authUserRepository;

    @Override
    public Optional<UserInfoResponse> getUserInfoById(Integer userId) {
        if (userId == null || userId <= 0) {
            return Optional.empty();
        }

        return authUserRepository.findById(userId)
                .map(this::mapToUserInfoResponse);
    }

    private UserInfoResponse mapToUserInfoResponse(AuthUser user) {
        return UserInfoResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .lastName(user.getLastName())
                .fullName(user.getName() + " " + user.getLastName())
                .build();
    }
}