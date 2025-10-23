package com.example.shop.user.domain.repository;

import com.example.shop.user.domain.entity.QUserEntity;
import com.example.shop.user.domain.entity.UserEntity;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    private static final QUserEntity user = QUserEntity.userEntity;

    @Override
    public Page<UserEntity> searchUsers(String username, String nickname, String email, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();
        if (StringUtils.hasText(username)) {
            builder.and(user.username.containsIgnoreCase(username));
        }
        if (StringUtils.hasText(nickname)) {
            builder.and(user.nickname.containsIgnoreCase(nickname));
        }
        if (StringUtils.hasText(email)) {
            builder.and(user.email.containsIgnoreCase(email));
        }

        List<UserEntity> content = jpaQueryFactory.selectFrom(user)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(resolveOrderSpecifiers(pageable.getSort()))
                .fetch();

        Long total = jpaQueryFactory.select(user.count())
                .from(user)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }

    private OrderSpecifier<?>[] resolveOrderSpecifiers(Sort sort) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
        if (sort != null && sort.isSorted()) {
            for (Sort.Order order : sort) {
                orderSpecifiers.add(toOrderSpecifier(order));
            }
        }

        if (orderSpecifiers.isEmpty()) {
            orderSpecifiers.add(user.createdAt.desc());
        }

        return orderSpecifiers.toArray(new OrderSpecifier<?>[0]);
    }

    private OrderSpecifier<?> toOrderSpecifier(Sort.Order order) {
        return switch (order.getProperty()) {
            case "username" -> order.isAscending() ? user.username.asc() : user.username.desc();
            case "nickname" -> order.isAscending() ? user.nickname.asc() : user.nickname.desc();
            case "email" -> order.isAscending() ? user.email.asc() : user.email.desc();
            case "createdAt" -> order.isAscending() ? user.createdAt.asc() : user.createdAt.desc();
            case "updatedAt" -> order.isAscending() ? user.updatedAt.asc() : user.updatedAt.desc();
            default -> order.isAscending() ? user.createdAt.asc() : user.createdAt.desc();
        };
    }
}

