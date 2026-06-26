package com.team7.agora.domain.coupon.dto.response;

import com.team7.agora.domain.coupon.entity.CouponIssue;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Coupon Issue History 응답 본문을 표현하는 DTO이다.
 * @param couponId 쿠폰 ID
 * @param totalIssued 전체 발급 건수
 * @param issues 쿠폰 발급 이력 목록
 */
public record CouponIssueHistoryResponse(
    Long couponId,
    int totalIssued,
    List<IssueRecord> issues
) {

    /**
     * 도메인 객체를 클라이언트 응답 DTO로 변환한다.
     * @param couponId 쿠폰 ID
     * @param couponIssues 응답으로 변환할 쿠폰 발급 엔티티 목록
     * @return 클라이언트에 반환할 API 응답
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
