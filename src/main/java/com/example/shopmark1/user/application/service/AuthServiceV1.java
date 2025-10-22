package com.example.shopmark1.user.application.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.shopmark1.global.infrastructure.constants.Constants;
import com.example.shopmark1.global.util.UtilFunction;
import com.example.shopmark1.user.domain.entity.UserEntity;
import com.example.shopmark1.user.domain.entity.UserRoleEntity;
import com.example.shopmark1.user.domain.repository.UserRepository;
import com.example.shopmark1.user.presentation.advice.AuthError;
import com.example.shopmark1.user.presentation.advice.AuthException;
import com.example.shopmark1.user.presentation.dto.request.ReqAuthPostRefreshDtoV1;
import com.example.shopmark1.user.presentation.dto.request.ReqPostAuthLoginDtoV1;
import com.example.shopmark1.user.presentation.dto.request.ReqPostAuthRegisterDtoV1;
import com.example.shopmark1.user.presentation.dto.response.ResPostAuthLoginDtoV1;
import com.example.shopmark1.user.presentation.dto.response.ResPostAuthRefreshDtoV1;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceV1 {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
//    private final KakaoApiRepository kakaoApiRepository;

    @Transactional
    public void postAuthRegister(ReqPostAuthRegisterDtoV1 reqDto) {

        ReqPostAuthRegisterDtoV1.User reqUser = reqDto.getUser();

        userRepository.findByUsername(reqUser.getUsername())
                .ifPresent(memberEntity -> {
                    throw new AuthException(AuthError.AUTH_USER_ALREADY_EXIST);
                });
        UserEntity userEntity = UserEntity.builder()
                .username(reqUser.getUsername())
                .password(passwordEncoder.encode(reqUser.getPassword()))
                .nickname(reqUser.getNickname())
                .email(reqUser.getEmail())
                .build();
        List<UserRoleEntity> userRoleEntityList = new ArrayList<>();
        userRoleEntityList.add(
                UserRoleEntity.builder()
                        .role(UserRoleEntity.Role.USER)
                        .build()
        );
        userRoleEntityList.forEach(userEntity::add);
        userRepository.save(userEntity);

    }

    @Transactional
    public ResPostAuthLoginDtoV1 postAuthLogin(ReqPostAuthLoginDtoV1 reqDto) {

        ReqPostAuthLoginDtoV1.User reqUser = reqDto.getUser();

        UserEntity userEntity = userRepository.findByUsername(reqUser.getUsername())
                .orElseThrow(() -> new AuthException(AuthError.AUTH_USERNAME_NOT_EXIST));
        if (!passwordEncoder.matches(reqUser.getPassword(), userEntity.getPassword())) {
            throw new AuthException(AuthError.AUTH_PASSWORD_NOT_MATCHED);
        }
        String accessJwt = UtilFunction.generateAccessJwtBy(userEntity);
        String refreshJwt = UtilFunction.generateRefreshJwtBy(userEntity);
        return ResPostAuthLoginDtoV1.of(accessJwt, refreshJwt);

    }

//    @Transactional
//    public ResPostAuthSocialDtoApiV1 postAuthSocial(ReqPostAuthSocialDtoV1 reqDto) {
//
//        UserEntity userEntity;
//        String accessJwt;
//        String refreshJwt;
//        if (UserSocialEntity.Provider.KAKAO.equals(reqDto.getUserSocial().getProvider())) {
//            ResGetKakaoUserMeDtoV2 resGetKakaoUserMeDtoV2 = kakaoApiRepository.getKakaoUserMeV2(reqDto.getUserSocial().getAccessToken());
//            String providerWithId = UserSocialEntity.Provider.KAKAO + "_" + resGetKakaoUserMeDtoV2.getId();
//            String nickname = resGetKakaoUserMeDtoV2
//                    .getKakao_account()
//                    .getProfile()
//                    .getNickname() != null
//                    ? resGetKakaoUserMeDtoV2
//                    .getKakao_account()
//                    .getProfile()
//                    .getNickname()
//                    : providerWithId;
//            userEntity = UserEntity.builder()
//                    .username(providerWithId)
//                    .password(passwordEncoder.encode(providerWithId + "youdidgoodjob"))
//                    .nickname(nickname)
//                    .email(resGetKakaoUserMeDtoV2.getKakao_account().getEmail())
//                    .build();
//            UserRoleEntity memberRoleEntity = UserRoleEntity.builder()
//                    .role(UserRoleEntity.Role.USER)
//                    .build();
//            UserSocialEntity memberSocialEntity = UserSocialEntity.builder()
//                    .provider(UserSocialEntity.Provider.KAKAO)
//                    .providerId(String.valueOf(resGetKakaoUserMeDtoV2.getId()))
//                    .nickname(nickname)
//                    .email(resGetKakaoUserMeDtoV2.getKakao_account().getEmail())
//                    .build();
//            userEntity.add(memberRoleEntity);
//            userEntity.add(memberSocialEntity);
//            userRepository.save(userEntity);
//        } else {
//            throw new IllegalArgumentException("지원하지 않는 OAuth2 제공자입니다. 현재 지원되는 제공자는 카카오입니다.");
//        }
//        accessJwt = UtilFunction.generateAccessJwtBy(userEntity);
//        refreshJwt = UtilFunction.generateRefreshJwtBy(userEntity);
//        return ResPostAuthSocialDtoApiV1.of(accessJwt, refreshJwt);
//
//    }

    @Transactional
    public ResPostAuthRefreshDtoV1 postAuthRefresh(ReqAuthPostRefreshDtoV1 reqDto) {

        DecodedJWT decodedRefreshJWT;
        try {
            decodedRefreshJWT = JWT.require(Algorithm.HMAC512(Constants.Jwt.SECRET))
                    .build()
                    .verify(reqDto.getRefreshJwt());
        } catch (JWTVerificationException e) {
            throw new AuthException(AuthError.AUTH_REFRESH_TOKEN_INVALID);
        }
        if (!Constants.Jwt.REFRESH.equals(decodedRefreshJWT.getSubject())) {
            throw new AuthException(AuthError.AUTH_REFRESH_TOKEN_INVALID);
        }
        UserEntity userEntity = userRepository.findById(UUID.fromString(decodedRefreshJWT.getClaim("id").asString()))
                .orElseThrow(() -> new AuthException(AuthError.AUTH_USER_CAN_NOT_FOUND));
        if (userEntity.getJwtValidator() > decodedRefreshJWT.getIssuedAtAsInstant().toEpochMilli()) {
            throw new AuthException(AuthError.AUTH_REFRESH_TOKEN_INVALID);
        }
        String accessJwt = UtilFunction.generateAccessJwtBy(userEntity);
        String refreshJwt = UtilFunction.generateRefreshJwtBy(userEntity);
        return ResPostAuthRefreshDtoV1.of(accessJwt, refreshJwt);

    }

}
