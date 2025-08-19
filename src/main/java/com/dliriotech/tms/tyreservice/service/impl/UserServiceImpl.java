package com.dliriotech.tms.tyreservice.service.impl;

import com.dliriotech.tms.tyreservice.dto.UserInfoResponse;
import com.dliriotech.tms.tyreservice.entity.AuthUser;
import com.dliriotech.tms.tyreservice.repository.AuthUserRepository;
import com.dliriotech.tms.tyreservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final AuthUserRepository authUserRepository;

    @Override
    public Mono<UserInfoResponse> getUserInfoById(Integer userId) {
        if (userId == null || userId <= 0) {
            return Mono.empty();
        }

        return authUserRepository.findById(userId)
                .map(this::mapToUserInfoResponse)
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("No se encontró usuario con ID: {}", userId);
                    return Mono.empty();
                }))
                .subscribeOn(Schedulers.boundedElastic());
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