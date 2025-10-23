package com.example.shop.user.infrastructure.config.jpa;

import com.example.shop.user.domain.entity.UserEntity;
import com.example.shop.user.domain.entity.UserRoleEntity;
import com.example.shop.user.domain.repository.UserRepository;
import com.example.shop.user.domain.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Profile("dev")
@Component
@RequiredArgsConstructor
public class UserCommandLineRunner implements CommandLineRunner {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            saveUserEntityBy(1);
            saveUserEntityBy(2);
            saveUserEntityBy(3);
        }
    }

    private void saveUserEntityBy(Integer number) {
        UserEntity userEntity = UserEntity.builder()
                .username("temp" + number)
                .password(passwordEncoder.encode("temp1234"))
                .nickname("temp" + number)
                .email("temp%d@temp.com".formatted(number))
                .build();
        userRepository.save(userEntity);
        UserRoleEntity userRoleEntity = UserRoleEntity.builder()
                .user(userEntity)
                .role(UserRoleEntity.Role.USER)
                .build();
        userRoleRepository.save(userRoleEntity);
    }
}
