package com.team7.agora.domain.review.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team7.agora.domain.product.entity.QProduct;
import com.team7.agora.domain.review.dto.request.MyReviewType;
import com.team7.agora.domain.review.dto.response.MyReviewResponse;
import com.team7.agora.domain.review.entity.QReview;
import com.team7.agora.domain.trade.entity.QTrade;
import com.team7.agora.domain.user.entity.QUser;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class ReviewQueryRepositoryImpl implements ReviewQueryRepository {

    private final JPAQueryFactory queryFactory;

    public ReviewQueryRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<MyReviewResponse> findMyReviews(Long userId, MyReviewType type, Pageable pageable) {
        QReview review = QReview.review;
        QTrade trade = QTrade.trade;
        QProduct product = QProduct.product;
        QUser reviewer = new QUser("reviewer");
        QUser targetUser = new QUser("targetUser");

        BooleanBuilder where = myReviewCondition(review, userId, type);

        List<MyReviewResponse> content = queryFactory
            .select(Projections.constructor(
                MyReviewResponse.class,
                review.id,
                trade.id,
                product.id,
                product.title,
                reviewer.nickname,
                targetUser.nickname,
                review.rating,
                review.content,
                review.createdAt
            ))
            .from(review)
            .join(review.trade, trade)
            .join(trade.product, product)
            .join(review.reviewer, reviewer)
            .join(review.targetUser, targetUser)
            .where(where)
            .orderBy(review.createdAt.desc(), review.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory
            .select(review.count())
            .from(review)
            .where(where)
            .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanBuilder myReviewCondition(QReview review, Long userId, MyReviewType type) {
        BooleanBuilder builder = new BooleanBuilder();
        if (type == MyReviewType.RECEIVED) {
            return builder.and(review.targetUser.id.eq(userId));
        }
        return builder.and(review.reviewer.id.eq(userId));
    }
}
