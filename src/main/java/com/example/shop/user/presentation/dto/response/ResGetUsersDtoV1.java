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
    public static class UserPage extends PagedModel<UserPage.UserDto> {

        public UserPage(Page<UserEntity> userEntityPage) {
            super(
                    new PageImpl<>(
                            UserDto.from(userEntityPage.getContent()),
                            userEntityPage.getPageable(),
                            userEntityPage.getTotalElements()
                    )
            );
        }

        @Getter
        @Builder
        public static class UserDto {

            private String id;
            private String username;
            private String nickname;
            private String email;
            private List<String> roleList;

            private static List<UserDto> from(List<UserEntity> userEntityList) {
                return userEntityList.stream()
                        .map(UserDto::from)
                        .toList();
            }

            public static UserDto from(UserEntity userEntity) {
                return UserDto.builder()
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
