package com.dliriotech.tms.tyreservice.repository;

import com.dliriotech.tms.tyreservice.entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthUserRepository extends JpaRepository<AuthUser, Integer> {
}