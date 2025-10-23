package com.example.shop.user.presentation.dto.response;

import com.example.shop.user.domain.entity.UserEntity;
import com.example.shop.user.domain.entity.UserRoleEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PagedModel;

import java.util.List;

@Getter
@Builder
public class ResGetUsersDtoV1 {

    private UserPage userPage;

    public static ResGetUsersDtoV1 of(Page<UserEntity> userEntityPage) {
        return ResGetUsersDtoV1.builder()
                .userPage(new UserPage(userEntityPage))
                .build();
    }

    @Getter
    @ToString
    public static class UserPage extends PagedModel<UserPage.User> {

        public UserPage(Page<UserEntity> userEntityPage) {
            super(
                    new PageImpl<>(
                            User.from(userEntityPage.getContent()),
                            userEntityPage.getPageable(),
                            userEntityPage.getTotalElements()
                    )
            );
        }

        @Getter
        @Builder
        public static class User {

            private String id;
            private String username;
            private String nickname;
            private String email;
            private List<String> roleList;

            private static List<User> from(List<UserEntity> userEntityList) {
                return userEntityList.stream()
                        .map(User::from)
                        .toList();
            }

            public static User from(UserEntity userEntity) {
                return User.builder()
                        .id(userEntity.getId().toString())
                        .username(userEntity.getUsername())
                        .nickname(userEntity.getNickname())
                        .email(userEntity.getEmail())
                        .roleList(
                                userEntity.getUserRoleList()
                                        .stream()
                                        .map(UserRoleEntity::getRole)
                                        .map(Enum::name)
                                        .toList()
                        )
                        .build();
            }
        }
    }
}
