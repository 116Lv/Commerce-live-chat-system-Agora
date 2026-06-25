-- 운영 환경은 spring.sql.init.mode=never 이므로 배포 전 DB에서 1회 실행합니다.
-- code 컬럼의 unique 제약을 기준으로 중복 실행해도 기존 데이터를 최신 값으로 유지합니다.
INSERT INTO regions (id, name, code, sido, sigungu, eupmyeondong)
VALUES
    (1, '서울특별시 강남구 역삼동', '1168010100', '서울특별시', '강남구', '역삼동'),
    (2, '서울특별시 송파구 잠실동', '1171010100', '서울특별시', '송파구', '잠실동'),
    (3, '경기도 성남시 분당구 정자동', '4113510300', '경기도', '성남시 분당구', '정자동'),
    (4, '경기도 수원시 영통구 광교동', '4111710300', '경기도', '수원시 영통구', '광교동'),
    (5, '인천광역시 연수구 송도동', '2818510600', '인천광역시', '연수구', '송도동')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    sido = VALUES(sido),
    sigungu = VALUES(sigungu),
    eupmyeondong = VALUES(eupmyeondong);
