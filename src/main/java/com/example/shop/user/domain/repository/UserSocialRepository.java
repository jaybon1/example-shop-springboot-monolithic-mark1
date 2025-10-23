package com.example.shop.user.domain.repository;

import com.example.shop.user.domain.entity.UserSocialEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserSocialRepository extends JpaRepository<UserSocialEntity, UUID> {

    Optional<UserSocialEntity> findByProviderAndProviderId(UserSocialEntity.Provider provider, String providerId);

}

