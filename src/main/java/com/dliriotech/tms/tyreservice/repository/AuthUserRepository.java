package com.dliriotech.tms.tyreservice.repository;

import com.dliriotech.tms.tyreservice.entity.AuthUser;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface AuthUserRepository extends ReactiveCrudRepository<AuthUser, Integer> {
}