package com.example.shop.user.presentation.dto.response;

import com.example.shop.user.domain.entity.UserEntity;
import com.example.shop.user.domain.entity.UserRoleEntity;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ResGetUserDtoV1 {

    private UserDto user;

    public static ResGetUserDtoV1 of(UserEntity userEntity) {
        return ResGetUserDtoV1.builder()
                .user(UserDto.from(userEntity))
                .build();
    }

    @Getter
    @Builder
    public static class UserDto {

        private String id;
        private String username;
        private String nickname;
        private String email;
        private List<UserRoleDto> userRoleList;
//        private List<UserSocial> userSocialList;

        public static UserDto from(UserEntity userEntity) {

            return UserDto.builder()
                    .id(userEntity.getId().toString())
                    .username(userEntity.getUsername())
                    .nickname(userEntity.getNickname())
                    .email(userEntity.getEmail())
                    .userRoleList(UserRoleDto.from(userEntity.getUserRoleList()))
//                    .memberSocialList(MemberSocial.from(userEntity.getMemberSocialList()))
                    .build();
        }

        @Getter
        @Builder
        public static class UserRoleDto {

            private String id;
            private UserRoleEntity.Role role;

            public static List<UserRoleDto> from(List<UserRoleEntity> userRoleEntityList) {
                return userRoleEntityList.stream()
                        .map(UserRoleDto::from)
                        .toList();
            }

            public static UserRoleDto from(UserRoleEntity userRoleEntity) {
                return UserRoleDto.builder()
                        .id(userRoleEntity.getId().toString())
                        .role(userRoleEntity.getRole())
                        .build();
            }

        }

        @Getter
        @Builder
        public static class UserSocialDto {

            private String id;
//            private MemberSocialEntity.Provider provider;
//            private String providerId;
//            private String nickname;
//            private String email;
//
//            public static List<UserSocial> from(List<MemberSocialEntity> memberSocialEntityList) {
//                return memberSocialEntityList.stream()
//                        .map(UserSocial::from)
//                        .toList();
//            }
//
//            public static UserSocial from(MemberSocialEntity memberSocialEntity) {
//                return UserSocial.builder()
//                        .id(memberSocialEntity.getId())
//                        .provider(memberSocialEntity.getProvider())
//                        .providerId(memberSocialEntity.getProviderId())
//                        .nickname(memberSocialEntity.getNickname())
//                        .email(memberSocialEntity.getEmail())
//                        .build();
//            }

        }

    }

}
