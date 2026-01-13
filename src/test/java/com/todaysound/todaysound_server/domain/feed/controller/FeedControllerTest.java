package com.todaysound.todaysound_server.domain.feed.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.todaysound.todaysound_server.domain.feed.dto.response.FeedResponseDTO;
import com.todaysound.todaysound_server.support.DocumentationTestSupport;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.ResultActions;

class FeedControllerTest extends DocumentationTestSupport {


    @Test
    void 피드_목록을_조회합니다() throws Exception {
        // given
        PageRequest request = PageRequest.of(0, 10);

        FeedResponseDTO response1 = new FeedResponseDTO(1L, "피드 내용", "작성자 이름", "2024-06-01T12:00:00", "fe",
                "1시간 전");
        FeedResponseDTO response2 = new FeedResponseDTO(2L, "또 다른 피드 내용", "다른 작성자", "2024-06-01T11:30:00", "fe2",
                "2시간 전");
        FeedResponseDTO response3 = new FeedResponseDTO(3L, "세 번째 피드 내용", "세 번째 작성자", "2024-06-01T10:00:00",
                "fe3", "3시간 전");

        List<FeedResponseDTO> responseList = List.of(response1, response2, response3);

        given(feedQueryService.findFeeds(anyString(), anyString(), any())).willReturn(responseList);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/feeds")
                        .header("X-User-Id", "test-user-id")
                        .header("X-Device-Secret", "test-device-secret")
                        .param("page", String.valueOf(request.getPageNumber()))
                        .param("size", String.valueOf(request.getPageSize()))
        );
        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        queryParameters(
                                parameterWithName("page").description("페이지 번호 (0부터 시작)"),
                                parameterWithName("size").description("페이지 크기")
                        ),
                        responseFields(
                                fieldWithPath("errorCode").description("응답 코드"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("result").description("피드 목록"),
                                fieldWithPath("result[].subscriptionId").description("구독 ID"),
                                fieldWithPath("result[].alias").description("구독 별칭"),
                                fieldWithPath("result[].summaryTitle").description("요약 제목"),
                                fieldWithPath("result[].summaryContent").description("요약 내용"),
                                fieldWithPath("result[].postUrl").description("연결 url"),
                                fieldWithPath("result[].timeAgo").description("피드 작성 후 경과 시간")
                )));
    }

}