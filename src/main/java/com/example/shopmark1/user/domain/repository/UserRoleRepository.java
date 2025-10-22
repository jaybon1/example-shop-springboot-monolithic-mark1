package com.example.shopmark1.user.domain.repository;

import com.example.shopmark1.user.domain.entity.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRoleEntity, UUID> {

}
