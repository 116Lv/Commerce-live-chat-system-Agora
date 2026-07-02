-- 시/도 -> 시/군/구 -> 읍/면/동 계단식 지역 조회 성능을 위한 인덱스 추가.
CREATE INDEX idx_regions_sido ON regions (sido);
CREATE INDEX idx_regions_sido_sigungu ON regions (sido, sigungu);
