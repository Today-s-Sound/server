package com.todaysound.todaysound_server.domain.subscription.dto.response;

import com.todaysound.todaysound_server.domain.subscription.entity.Keyword;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 키워드 목록 조회 응답 DTO
 */
public record KeywordListResponseDto(
        @Schema(description = "키워드 목록")
        List<KeywordItem> keywords
) {
    public static KeywordListResponseDto from(List<Keyword> keywords) {
        List<KeywordItem> items = keywords.stream()
                .map(KeywordItem::from)
                .toList();
        return new KeywordListResponseDto(items);
    }

    public record KeywordItem(
            @Schema(description = "키워드 ID", example = "1")
            Long id,
            @Schema(description = "키워드 이름", example = "AI")
            String name
    ) {
        public static KeywordItem from(Keyword keyword) {
            return new KeywordItem(keyword.getId(), keyword.getName());
        }
    }
}

