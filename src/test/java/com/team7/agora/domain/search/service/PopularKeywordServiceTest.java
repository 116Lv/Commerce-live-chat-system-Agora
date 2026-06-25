package com.team7.agora.domain.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.search.dto.PopularKeywordResponse;
import com.team7.agora.domain.search.repository.PopularKeywordRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PopularKeywordServiceTest {

    @Mock
    private PopularKeywordRepository popularKeywordRepository;

    @Test
    void recordSearchKeyword_normalizesKeywordBeforeIncrement() {
        PopularKeywordService service = new PopularKeywordService(popularKeywordRepository);

        service.recordSearchKeyword("  자전거  ");

        verify(popularKeywordRepository).increment("자전거");
        verify(popularKeywordRepository).incrementDaily(eq("자전거"), any());
        verify(popularKeywordRepository).incrementWeekly(eq("자전거"), any());
    }

    @Test
    void getTopKeywords_returnsRepositoryRanking() {
        PopularKeywordService service = new PopularKeywordService(popularKeywordRepository);
        when(popularKeywordRepository.getTopKeywords(10)).thenReturn(List.of(
            new PopularKeywordResponse("자전거", 12),
            new PopularKeywordResponse("아이폰", 8)
        ));

        List<PopularKeywordResponse> responses = service.getTopKeywords(10);

        assertThat(responses).extracting(PopularKeywordResponse::keyword)
            .containsExactly("자전거", "아이폰");
    }

    @Test
    void recordSearchKeyword_ignoresRepositoryFailure() {
        PopularKeywordService service = new PopularKeywordService(popularKeywordRepository);
        doThrow(new RuntimeException("redis down")).when(popularKeywordRepository).increment("자전거");

        assertThatCode(() -> service.recordSearchKeyword("자전거"))
            .doesNotThrowAnyException();
    }

    @Test
    void getTopKeywords_returnsEmptyListWhenRepositoryFails() {
        PopularKeywordService service = new PopularKeywordService(popularKeywordRepository);
        when(popularKeywordRepository.getTopKeywords(10)).thenThrow(new RuntimeException("redis down"));

        List<PopularKeywordResponse> responses = service.getTopKeywords(10);

        assertThat(responses).isEmpty();
    }

    @Test
    void getTopDailyKeywords_returnsRepositoryRanking() {
        PopularKeywordService service = new PopularKeywordService(popularKeywordRepository);
        when(popularKeywordRepository.getTopDailyKeywords(any(), eq(10))).thenReturn(List.of(
            new PopularKeywordResponse("자전거", 5)
        ));

        List<PopularKeywordResponse> responses = service.getTopDailyKeywords(10);

        assertThat(responses).extracting(PopularKeywordResponse::keyword).containsExactly("자전거");
    }

    @Test
    void getTopDailyKeywords_returnsEmptyListWhenRepositoryFails() {
        PopularKeywordService service = new PopularKeywordService(popularKeywordRepository);
        when(popularKeywordRepository.getTopDailyKeywords(any(), eq(10))).thenThrow(new RuntimeException("redis down"));

        List<PopularKeywordResponse> responses = service.getTopDailyKeywords(10);

        assertThat(responses).isEmpty();
    }

    @Test
    void getTopWeeklyKeywords_returnsRepositoryRanking() {
        PopularKeywordService service = new PopularKeywordService(popularKeywordRepository);
        when(popularKeywordRepository.getTopWeeklyKeywords(any(), eq(10))).thenReturn(List.of(
            new PopularKeywordResponse("아이폰", 7)
        ));

        List<PopularKeywordResponse> responses = service.getTopWeeklyKeywords(10);

        assertThat(responses).extracting(PopularKeywordResponse::keyword).containsExactly("아이폰");
    }

    @Test
    void getTopWeeklyKeywords_returnsEmptyListWhenRepositoryFails() {
        PopularKeywordService service = new PopularKeywordService(popularKeywordRepository);
        when(popularKeywordRepository.getTopWeeklyKeywords(any(), eq(10))).thenThrow(new RuntimeException("redis down"));

        List<PopularKeywordResponse> responses = service.getTopWeeklyKeywords(10);

        assertThat(responses).isEmpty();
    }
}
