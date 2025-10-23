package com.example.shop.user.application.service;

import com.example.shop.user.domain.entity.UserEntity;
import com.example.shop.user.domain.entity.UserRoleEntity;
import com.example.shop.user.domain.repository.UserRepository;
import com.example.shop.user.presentation.advice.UserError;
import com.example.shop.user.presentation.advice.UserException;
import com.example.shop.user.presentation.dto.response.ResGetUsersDtoV1;
import com.example.shop.user.presentation.dto.response.ResGetUsersWithIdDtoV1;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceV1 {

    private final UserRepository userRepository;

    public ResGetUsersDtoV1 getUsers(
            UUID authUserId,
            List<String> authUserRoleList,
            Pageable pageable,
            String username,
            String nickname,
            String email
    ) {
        String normalizedUsername = normalize(username);
        String normalizedNickname = normalize(nickname);
        String normalizedEmail = normalize(email);

        Page<UserEntity> userEntityPage;
        if (isAdmin(authUserRoleList) || isManager(authUserRoleList)) {
            userEntityPage = userRepository.searchUsers(normalizedUsername, normalizedNickname, normalizedEmail, pageable);
        } else {
            if (authUserId == null) {
                throw new UserException(UserError.USER_BAD_REQUEST);
            }
            UserEntity userEntity = userRepository.findDefaultById(authUserId);
            if (!matchesFilter(userEntity, normalizedUsername, normalizedNickname, normalizedEmail)) {
                userEntityPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            } else if (pageable.getPageNumber() > 0) {
                userEntityPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            } else {
                userEntityPage = new PageImpl<>(List.of(userEntity), pageable, 1);
            }
        }
        return ResGetUsersDtoV1.of(userEntityPage);
    }

    public ResGetUsersWithIdDtoV1 getUsersWithId(UUID authUserId, List<String> authUserRoleList, UUID userId) {
        UserEntity userEntity = userRepository.findDefaultById(userId);
        validateBy(authUserId, authUserRoleList, userEntity);
        return ResGetUsersWithIdDtoV1.of(userEntity);
    }

    public void deleteUsersById(UUID authUserId, List<String> authUserRoleList, UUID userId) {
        UserEntity userEntity = userRepository.findDefaultById(userId);
        validateBy(authUserId, authUserRoleList, userEntity);
        if (userEntity.getUserRoleList().stream().map(UserRoleEntity::getRole).anyMatch(role -> role.equals(UserRoleEntity.Role.ADMIN))) {
            throw new UserException(UserError.USER_BAD_REQUEST);
        }
        userEntity.markDeleted(Instant.now(), authUserId);
    }

    private void validateBy(UUID authUserId, List<String> authUserRoleList, UserEntity userEntity) {
        if (
                userEntity.getUserRoleList().stream().map(UserRoleEntity::getRole).anyMatch(role -> role.equals(UserRoleEntity.Role.ADMIN))
                        && !isAdmin(authUserRoleList)
        ) {
            throw new UserException(UserError.USER_BAD_REQUEST);
        }
        if (
                (authUserId != null && authUserId.equals(userEntity.getId()))
                        || isAdmin(authUserRoleList)
                        || isManager(authUserRoleList)
        ) {
            return;
        }
        throw new UserException(UserError.USER_BAD_REQUEST);
    }

    private boolean isAdmin(List<String> authUserRoleList) {
        return authUserRoleList != null && authUserRoleList.contains(UserRoleEntity.Role.ADMIN.toString());
    }

    private boolean isManager(List<String> authUserRoleList) {
        return authUserRoleList != null && authUserRoleList.contains(UserRoleEntity.Role.MANAGER.toString());
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean matchesFilter(UserEntity userEntity, String username, String nickname, String email) {
        return matchesLike(userEntity.getUsername(), username)
                && matchesLike(userEntity.getNickname(), nickname)
                && matchesLike(userEntity.getEmail(), email);
    }

    private boolean matchesLike(String target, String filter) {
        if (filter == null) {
            return true;
        }
        if (target == null) {
            return false;
        }
        return target.toLowerCase().contains(filter.toLowerCase());
    }
}
