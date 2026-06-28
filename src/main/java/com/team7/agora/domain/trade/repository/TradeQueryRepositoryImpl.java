package com.team7.agora.domain.trade.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team7.agora.domain.payment.entity.QPayment;
import com.team7.agora.domain.product.entity.QProduct;
import com.team7.agora.domain.trade.dto.request.MyTradeRole;
import com.team7.agora.domain.trade.dto.response.MyTradeResponse;
import com.team7.agora.domain.trade.entity.QTrade;
import com.team7.agora.domain.user.entity.QUser;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class TradeQueryRepositoryImpl implements TradeQueryRepository {

    private final JPAQueryFactory queryFactory;

    public TradeQueryRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<MyTradeResponse> findMyTrades(Long userId, MyTradeRole role, Pageable pageable) {
        QTrade trade = QTrade.trade;
        QProduct product = QProduct.product;
        QPayment payment = QPayment.payment;
        QUser seller = new QUser("seller");
        QUser buyer = new QUser("buyer");

        BooleanBuilder where = myTradeCondition(trade, userId, role);
        StringExpression roleExpression = new CaseBuilder()
            .when(buyer.id.eq(userId)).then("BUYER")
            .otherwise("SELLER");
        StringExpression counterpartNicknameExpression = new CaseBuilder()
            .when(buyer.id.eq(userId)).then(seller.nickname)
            .otherwise(buyer.nickname);

        List<MyTradeResponse> content = queryFactory
            .select(Projections.constructor(
                MyTradeResponse.class,
                trade.id,
                product.id,
                product.title,
                product.price,
                trade.price,
                trade.status.stringValue(),
                roleExpression,
                counterpartNicknameExpression,
                payment.status.stringValue(),
                trade.completedAt,
                trade.createdAt
            ))
            .from(trade)
            .join(trade.product, product)
            .join(trade.seller, seller)
            .join(trade.buyer, buyer)
            .leftJoin(payment).on(payment.trade.eq(trade))
            .where(where)
            .orderBy(trade.createdAt.desc(), trade.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory
            .select(trade.count())
            .from(trade)
            .join(trade.seller, seller)
            .join(trade.buyer, buyer)
            .where(where)
            .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanBuilder myTradeCondition(QTrade trade, Long userId, MyTradeRole role) {
        BooleanBuilder builder = new BooleanBuilder();
        if (role == MyTradeRole.BUYER) {
            return builder.and(trade.buyer.id.eq(userId));
        }
        if (role == MyTradeRole.SELLER) {
            return builder.and(trade.seller.id.eq(userId));
        }
        return builder.and(trade.buyer.id.eq(userId).or(trade.seller.id.eq(userId)));
    }
}
