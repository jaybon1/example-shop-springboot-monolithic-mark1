package com.example.shopmark1.global.infrastructure.config.oauth2.provider;


public interface OAuth2UserInfo {

    String getProviderId();

    String getEmail();

    String getNickname();
}
