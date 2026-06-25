package com.team7.agora.domain.coupon.dto.response;

import com.team7.agora.domain.coupon.entity.CouponIssue;
import java.time.LocalDateTime;
import java.util.List;

public record CouponIssueHistoryResponse(
    Long couponId,
    int totalIssued,
    List<IssueRecord> issues
) {

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
