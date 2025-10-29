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

    private UUID id;
    private String username;
    private String password;
    private String nickname;
    private String email;
    private List<String> roleList;
    private Map<String, Object> attributes;

    public static CustomUserDetails of(UserEntity userEntity) {
        return CustomUserDetails.builder()
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
                .attributes(Map.of())
                .build();
    }

    public static CustomUserDetails of(DecodedJWT decodedAccessJwt) {
        return CustomUserDetails.builder()
                .id(UUID.fromString(decodedAccessJwt.getClaim("id").asString()))
                .username(decodedAccessJwt.getClaim("username").asString())
                .password(null)
                .nickname(String.valueOf(decodedAccessJwt.getClaim("nickname")))
                .email(String.valueOf(decodedAccessJwt.getClaim("email")))
                .roleList(decodedAccessJwt.getClaim("roleList").asList(String.class))
                .attributes(Map.of())
                .build();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roleList
                .stream()
                .map(role -> (GrantedAuthority) () -> "ROLE_" + role)
                .toList();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
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

