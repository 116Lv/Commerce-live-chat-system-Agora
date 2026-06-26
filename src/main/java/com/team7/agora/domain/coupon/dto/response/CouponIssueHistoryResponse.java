package com.team7.agora.domain.coupon.dto.response;

import com.team7.agora.domain.coupon.entity.CouponIssue;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response payload for returning coupon issue history data.
 * @param couponId the coupon id value
 * @param totalIssued the total issued value
 * @param issues the issues value
 */
public record CouponIssueHistoryResponse(
    Long couponId,
    int totalIssued,
    List<IssueRecord> issues
) {

    /**
     * Handles of behavior.
     * @param couponId the coupon id value
     * @param couponIssues the coupon issues value
     * @return the of result
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
