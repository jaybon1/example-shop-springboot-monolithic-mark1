package com.example.shop.user.domain.repository;

import com.example.shop.user.domain.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryCustom {

    Page<UserEntity> searchUsers(String username, String nickname, String email, Pageable pageable);
}

