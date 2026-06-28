package com.team7.agora.domain.review.dto.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.team7.agora.global.exception.BusinessException;
import org.junit.jupiter.api.Test;

class MyReviewTypeTest {

    @Test
    void fromDefaultsNullAndBlankToWritten() {
        assertThat(MyReviewType.from(null)).isEqualTo(MyReviewType.WRITTEN);
        assertThat(MyReviewType.from(" ")).isEqualTo(MyReviewType.WRITTEN);
    }

    @Test
    void fromParsesCaseInsensitively() {
        assertThat(MyReviewType.from("written")).isEqualTo(MyReviewType.WRITTEN);
        assertThat(MyReviewType.from("ReCeIvEd")).isEqualTo(MyReviewType.RECEIVED);
    }

    @Test
    void fromRejectsInvalidType() {
        assertThatThrownBy(() -> MyReviewType.from("all"))
            .isInstanceOf(BusinessException.class);
    }
}
