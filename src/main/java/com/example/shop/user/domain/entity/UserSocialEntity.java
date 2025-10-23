package com.example.shop.user.domain.entity;

import com.example.shop.common.domain.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.UUID;

@Entity
@Table(
        name = "USER_SOCIAL",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_USER_SOCIAL_PROVIDER_ID",
                        columnNames = {"provider", "provider_id"}
                )
        }

)
@DynamicInsert
@DynamicUpdate
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id", callSuper = false)
public class UserSocialEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", updatable = false, nullable = false)
    private UserEntity user;

    @Column(name = "provider", nullable = false)
    @Enumerated(EnumType.STRING)
    private Provider provider;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "email")
    private String email;

    public enum Provider {
        KAKAO,
        GOOGLE,
        NAVER
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

}
