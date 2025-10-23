package com.example.shop.user.presentation.dto.response;

import com.example.shop.user.domain.entity.UserEntity;
import com.example.shop.user.domain.entity.UserRoleEntity;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ResGetUsersWithIdDtoV1 {

    private User user;

    public static ResGetUsersWithIdDtoV1 of(UserEntity userEntity) {
        return ResGetUsersWithIdDtoV1.builder()
                .user(User.from(userEntity))
                .build();
    }

    @Getter
    @Builder
    public static class User {

        private String id;
        private String username;
        private String nickname;
        private String email;
        private List<UserRole> userRoleList;
//        private List<UserSocial> userSocialList;

        public static User from(UserEntity userEntity) {

            return User.builder()
                    .id(userEntity.getId().toString())
                    .username(userEntity.getUsername())
                    .nickname(userEntity.getNickname())
                    .email(userEntity.getEmail())
                    .userRoleList(UserRole.from(userEntity.getUserRoleList()))
//                    .memberSocialList(MemberSocial.from(userEntity.getMemberSocialList()))
                    .build();
        }

        @Getter
        @Builder
        public static class UserRole {

            private String id;
            private UserRoleEntity.Role role;

            public static List<UserRole> from(List<UserRoleEntity> userRoleEntityList) {
                return userRoleEntityList.stream()
                        .map(UserRole::from)
                        .toList();
            }

            public static UserRole from(UserRoleEntity userRoleEntity) {
                return UserRole.builder()
                        .id(userRoleEntity.getId().toString())
                        .role(userRoleEntity.getRole())
                        .build();
            }

        }

        @Getter
        @Builder
        public static class UserSocial {

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
