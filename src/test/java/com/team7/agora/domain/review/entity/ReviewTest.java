package com.team7.agora.domain.review.entity;

import static com.team7.agora.support.TestEntityIds.assignId;
import static org.assertj.core.api.Assertions.assertThat;

import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.region.entity.Region;
import com.team7.agora.domain.trade.entity.Trade;
import com.team7.agora.domain.user.entity.User;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ReviewTest {

    @Test
    void createDoesNotChangeTargetSmileScore() {
        User seller = User.signup("seller@test.com", "password", "seller", "01011112222");
        assignId(seller, 1L);
        User buyer = User.signup("buyer@test.com", "password", "buyer", "01033334444");
        assignId(buyer, 2L);
        Region region = Region.create("서울 강남구 역삼동", "1168010100", "서울", "강남구", "역삼동");
        Product product = Product.create(seller, region, "자전거", "상태 좋아요", BigDecimal.valueOf(50000), "SPORTS");
        Trade trade = Trade.start(product, seller, buyer, BigDecimal.valueOf(50000));

        Review.create(trade, buyer, seller, 5, "친절해요");

        assertThat(seller.getSmileScore()).isEqualTo(60);
    }
}
