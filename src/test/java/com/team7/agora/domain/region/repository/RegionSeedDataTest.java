package com.team7.agora.domain.region.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RegionSeedDataTest {

    @Autowired
    private RegionRepository regionRepository;

    @Test
    void localProfileLoadsInitialRegionMasterData() {
        assertThat(regionRepository.count()).isGreaterThanOrEqualTo(5);
        assertThat(regionRepository.existsByCode("1168010100")).isTrue();
        assertThat(regionRepository.existsByCode("1171010100")).isTrue();
        assertThat(regionRepository.existsByCode("4113510300")).isTrue();
        assertThat(regionRepository.existsByCode("4111710300")).isTrue();
        assertThat(regionRepository.existsByCode("2818510600")).isTrue();
    }
}
