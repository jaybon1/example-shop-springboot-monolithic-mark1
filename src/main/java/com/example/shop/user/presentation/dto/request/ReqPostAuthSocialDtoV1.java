package com.example.shop.user.presentation.dto.request;

import com.example.shop.user.domain.entity.UserSocialEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReqPostAuthSocialDtoV1 {

    @NotNull
    @Valid
    private UserSocial userSocial;

    @Getter
    @Builder
    public static class UserSocial {

        private UserSocialEntity.Provider provider;

        @NotBlank
        private String accessToken;

    }

}
