package com.example.shop.common.infrastructure.config.security.auth;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.shop.user.domain.entity.UserEntity;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomUserDetails implements UserDetails, OAuth2User {

    private User user;
    private Map<String, Object> attributes;

    //    public static CustomUserDetails of(MemberEntity memberEntity, Map<String, Object> attributes) {
//        return CustomUserDetails.builder()
//                .member(Member.from(memberEntity))
//                .attributes(attributes)
//                .build();
//    }
//
    public static CustomUserDetails of(UserEntity userEntity) {
        return CustomUserDetails.builder()
                .user(User.from(userEntity))
                .attributes(Map.of())
                .build();
    }

    public static CustomUserDetails of(DecodedJWT decodedAccessJwt) {
        return CustomUserDetails.builder()
                .user(User.from(decodedAccessJwt))
                .attributes(Map.of())
                .build();
    }

    @Getter
    @Builder
    public static class User {
        private UUID id;
        private String username;
        private String password;
        private String nickname;
        private String email;
        private List<String> roleList;

        public static User from(DecodedJWT decodedAccessJwt) {
            return User.builder()
                    .id(UUID.fromString(decodedAccessJwt.getClaim("id").asString()))
                    .username(decodedAccessJwt.getClaim("username").asString())
                    .password(null)
                    .nickname(String.valueOf(decodedAccessJwt.getClaim("nickname")))
                    .email(String.valueOf(decodedAccessJwt.getClaim("email")))
                    .roleList(decodedAccessJwt.getClaim("roleList").asList(String.class))
                    .build();
        }

        public static User from(UserEntity userEntity) {
            return User.builder()
                    .id(userEntity.getId())
                    .username(userEntity.getUsername())
                    .password(userEntity.getPassword())
                    .nickname(userEntity.getNickname())
                    .email(userEntity.getEmail())
                    .roleList(
                            userEntity.getUserRoleList()
                                    .stream()
                                    .map(memberRoleEntity -> memberRoleEntity.getRole().toString())
                                    .toList()
                    )
                    .build();
        }
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return user.getUsername();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoleList()
                .stream()
                .map(role -> (GrantedAuthority) () -> "ROLE_" + role)
                .toList();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

