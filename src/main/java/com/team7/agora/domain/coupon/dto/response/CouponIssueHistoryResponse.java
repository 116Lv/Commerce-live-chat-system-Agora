package com.team7.agora.domain.coupon.dto.response;

import com.team7.agora.domain.coupon.entity.CouponIssue;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 응답 본문을 전달하는 DTO이다.
 * @param couponId 입력 값
 * @param totalIssued 입력 값
 * @param issues 입력 값
 */
public record CouponIssueHistoryResponse(
    Long couponId,
    int totalIssued,
    List<IssueRecord> issues
) {

    /**
     * 요청한 동작을 처리한다.
     * @param couponId 입력 값
     * @param couponIssues 입력 값
     * @return 처리 결과
     */
    public static CouponIssueHistoryResponse of(Long couponId, List<CouponIssue> couponIssues) {
        List<IssueRecord> records = couponIssues.stream()
            .map(IssueRecord::from)
            .toList();
        return new CouponIssueHistoryResponse(couponId, records.size(), records);
    }

    public record IssueRecord(
        Long issueId,
        Long userId,
        String userNickname,
        String status,
        LocalDateTime issuedAt
    ) {

        static IssueRecord from(CouponIssue couponIssue) {
            return new IssueRecord(
                couponIssue.getId(),
                couponIssue.getUser().getId(),
                couponIssue.getUser().getNickname(),
                couponIssue.getStatus().name(),
                couponIssue.getIssuedAt()
            );
        }
    }
}
