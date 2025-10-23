package com.example.shop.user.domain.repository;

import com.example.shop.user.domain.entity.UserEntity;
import com.example.shop.user.presentation.advice.UserError;
import com.example.shop.user.presentation.advice.UserException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID>, UserRepositoryCustom {

    default UserEntity findDefaultById(UUID userId) {
        return findById(userId).orElseThrow(() -> new UserException(UserError.USER_CAN_NOT_FOUND));
    }

    Optional<UserEntity> findByUsername(String username);

}
