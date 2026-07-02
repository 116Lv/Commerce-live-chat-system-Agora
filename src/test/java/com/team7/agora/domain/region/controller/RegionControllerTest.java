package com.team7.agora.domain.region.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team7.agora.domain.region.dto.response.RegionResponse;
import com.team7.agora.domain.region.service.RegionService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class RegionControllerTest {

    @Mock
    private RegionService regionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new RegionController(regionService))
                .build();
    }

    @Test
    void findRegions_returnsSuccessEnvelope() throws Exception {
        when(regionService.findRegions("역삼")).thenReturn(List.of(
                new RegionResponse(1L, "서울 강남구 역삼동", "1168010100", "서울", "강남구", "역삼동")
        ));

        mockMvc.perform(get("/api/regions").param("keyword", "역삼"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].regionId").value(1L))
                .andExpect(jsonPath("$.data[0].name").value("서울 강남구 역삼동"));
    }

    @Test
    void findSidoList_returnsSuccessEnvelope() throws Exception {
        when(regionService.findSidoList()).thenReturn(List.of("경기도", "서울특별시"));

        mockMvc.perform(get("/api/regions/sido"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0]").value("경기도"))
                .andExpect(jsonPath("$.data[1]").value("서울특별시"));
    }

    @Test
    void findSigunguList_returnsSuccessEnvelope() throws Exception {
        when(regionService.findSigunguList("서울특별시")).thenReturn(List.of("강남구", "송파구"));

        mockMvc.perform(get("/api/regions/sigungu").param("sido", "서울특별시"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0]").value("강남구"))
                .andExpect(jsonPath("$.data[1]").value("송파구"));
    }

    @Test
    void findDongList_returnsSuccessEnvelope() throws Exception {
        when(regionService.findDongList("서울특별시", "강남구")).thenReturn(List.of(
                new RegionResponse(1L, "서울 강남구 역삼동", "1168010100", "서울특별시", "강남구", "역삼동")
        ));

        mockMvc.perform(get("/api/regions/dong").param("sido", "서울특별시").param("sigungu", "강남구"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].regionId").value(1L))
                .andExpect(jsonPath("$.data[0].eupmyeondong").value("역삼동"));
    }
}
