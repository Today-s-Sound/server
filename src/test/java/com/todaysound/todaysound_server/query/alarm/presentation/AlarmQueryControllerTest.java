package com.todaysound.todaysound_server.query.alarm.presentation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.todaysound.todaysound_server.domain.alarm.dto.response.RecentAlarmResponse;
import com.todaysound.todaysound_server.support.DocumentationTestSupport;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

class AlarmQueryControllerTest extends DocumentationTestSupport {

    @Test
    void 알림_목록을_조회합니다() throws Exception {
        // given
        PageRequest pageRequest = PageRequest.of(0, 10);

        RecentAlarmResponse response1 = new RecentAlarmResponse(1L, 2L, "제목1", "내용1", "http://naver.com", "1시간 전",
                true);
        RecentAlarmResponse response2 = new RecentAlarmResponse(2L, 3L, "제목2", "내용2", "http://google.com", "2시간 전",
                false);
        RecentAlarmResponse response3 = new RecentAlarmResponse(3L, 4L, "제목3", "내용3", "http://daum.net", "3시간 전", true);

        List<RecentAlarmResponse> responseList = List.of(response1, response2, response3);

        given(alarmQueryService.getRecentAlarms(any(), anyString(), anyString())).willReturn(responseList);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/alarms").param("page", String.valueOf(pageRequest.getPageNumber()))
                        .param("size", String.valueOf(pageRequest.getPageSize())).header("X-User-ID", "test-user-uuid")
                        .header("X-Device-Secret", "test-device-secret"));
        // then
        result.andExpect(status().isOk()).andDo(restDocsHandler.document(
                queryParameters(parameterWithName("page").description("페이지 번호 (0부터 시작)"),
                        parameterWithName("size").description("페이지 크기")),
                responseFields(fieldWithPath("errorCode").description("응답 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("result").type(JsonFieldType.ARRAY).description("피드 목록"),
                        fieldWithPath("result[].subscriptionId").type(JsonFieldType.NUMBER).description("구독 ID"),
                        fieldWithPath("result[].summaryId").type(JsonFieldType.NUMBER).description("요약 ID"),
                        fieldWithPath("result[].alias").type(JsonFieldType.STRING).description("구독 별칭"),
                        fieldWithPath("result[].summaryContent").type(JsonFieldType.STRING).description("요약 내용"),
                        fieldWithPath("result[].postUrl").type(JsonFieldType.STRING).description("연결 url"),
                        fieldWithPath("result[].timeAgo").type(JsonFieldType.STRING).description("피드 작성 후 경과 시간"),
                        fieldWithPath("result[].isKeywordMatched").type(JsonFieldType.BOOLEAN)
                                .description("키워드 매칭 여부"))));

    }
}