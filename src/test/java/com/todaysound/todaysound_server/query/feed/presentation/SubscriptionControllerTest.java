package com.todaysound.todaysound_server.query.feed.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.todaysound.todaysound_server.domain.subscription.dto.request.SubscriptionCreateRequest;
import com.todaysound.todaysound_server.domain.subscription.dto.request.SubscriptionUpdateRequest;
import com.todaysound.todaysound_server.domain.subscription.dto.response.KeywordListResponse;
import com.todaysound.todaysound_server.domain.subscription.dto.response.KeywordListResponse.KeywordItem;
import com.todaysound.todaysound_server.domain.subscription.dto.response.SubscriptionCreationResponse;
import com.todaysound.todaysound_server.domain.subscription.dto.response.SubscriptionResponse;
import com.todaysound.todaysound_server.domain.subscription.dto.response.SubscriptionResponse.KeywordResponse;
import com.todaysound.todaysound_server.support.DocumentationTestSupport;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

public class SubscriptionControllerTest extends DocumentationTestSupport {


    @Test
    void 신규_구독을_등록한다() throws Exception {
        // given
        SubscriptionCreateRequest request = new SubscriptionCreateRequest(1L, List.of(1L, 2L), "넓은마을", true);

        given(subscriptionService.createSubscription(anyString(), anyString(),
                any(SubscriptionCreateRequest.class))).willReturn(
                SubscriptionCreationResponse.builder().subscriptionId(1L).build());

        // when then
        ResultActions result = mockMvc.perform(
                post("/api/subscriptions").content(objectMapper.writeValueAsString(request))
                        .header("X-User-ID", "test-user-uuid").header("X-Device-Secret", "test-device-secret")
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print());

        result.andExpect(status().isOk()).andDo(restDocsHandler.document(
                requestFields(fieldWithPath("urlId").type(JsonFieldType.NUMBER).description("구독할 URL의 ID"),
                        fieldWithPath("keywordIds").type(JsonFieldType.ARRAY).optional()
                                .description("구독에 연관된 키워드 ID 리스트"),
                        fieldWithPath("alias").type(JsonFieldType.STRING).description("구독 별칭"),
                        fieldWithPath("isAlarmEnabled").type(JsonFieldType.BOOLEAN).description("알림 여부")),
                responseFields(fieldWithPath("errorCode").type(JsonFieldType.NULL).description("에러 코드, 성공 시 null"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("result").type(JsonFieldType.OBJECT).description("응답 데이터"),
                        fieldWithPath("result.subscriptionId").type(JsonFieldType.NUMBER).description("생성된 구독의 ID"))));
    }

    @Test
    void 구독_목록_조회() throws Exception {
        //given
        SubscriptionResponse response1 = new SubscriptionResponse(1L, "https://example.com/feed1", "기술 블로그", true,
                List.of(new KeywordResponse(1L, "AI"), new KeywordResponse(2L, "개발")));
        SubscriptionResponse response2 = new SubscriptionResponse(2L, "https://example.com/feed2", "뉴스 피드", false,
                List.of(new KeywordResponse(3L, "뉴스")));

        List<SubscriptionResponse> responseList = List.of(response1, response2);

        given(subscriptionQueryService.getMySubscriptions(any(), anyString(), anyString())).willReturn(responseList);

        //when then
        mockMvc.perform(get("/api/subscriptions").header("X-User-ID", "test-user-uuid")
                        .header("X-Device-Secret", "test-device-secret").param("page", "0").param("size", "10")).andDo(print())
                .andExpect(status().isOk()).andDo(restDocsHandler.document(
                        queryParameters(parameterWithName("page").description("페이지 번호").optional(),
                                parameterWithName("size").description("페이지 크기").optional()),
                        responseFields(fieldWithPath("errorCode").type(JsonFieldType.NULL).description("에러 코드, 성공 시 null"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                                fieldWithPath("result").type(JsonFieldType.ARRAY).description("구독 목록"),
                                fieldWithPath("result[].id").type(JsonFieldType.NUMBER).description("구독 ID"),
                                fieldWithPath("result[].url").type(JsonFieldType.STRING).description("구독 URL"),
                                fieldWithPath("result[].alias").type(JsonFieldType.STRING).description("구독 별칭"),
                                fieldWithPath("result[].isAlarmEnabled").type(JsonFieldType.BOOLEAN).description("알림 활성화 여부"),
                                fieldWithPath("result[].keywords").type(JsonFieldType.ARRAY).description("키워드 목록"),
                                fieldWithPath("result[].keywords[].id").type(JsonFieldType.NUMBER).description("키워드 ID"),
                                fieldWithPath("result[].keywords[].name").type(JsonFieldType.STRING).description("키워드 이름"))));
    }

    @Test
    void 구독을_삭제한다() throws Exception {
        //given
        Long subscriptionId = 1L;

        doNothing().when(subscriptionService).deleteSubscription(anyLong(), anyString(), anyString());

        //when then
        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/subscriptions/{subscriptionId}", subscriptionId)
                        .header("X-User-ID", "test-user-uuid").header("X-Device-Secret", "test-device-secret")).andDo(print())
                .andExpect(status().isOk()).andDo(restDocsHandler.document(
                        pathParameters(parameterWithName("subscriptionId").description("삭제할 구독의 ID")),
                        responseFields(fieldWithPath("errorCode").type(JsonFieldType.NULL).description("에러 코드, 성공 시 null"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                                fieldWithPath("result").type(JsonFieldType.NULL).description("응답 데이터, 삭제 시 null"))));
    }

    @Test
    void 키워드_목록을_조회한다() throws Exception {
        //given
        KeywordListResponse response = new KeywordListResponse(
                List.of(new KeywordItem(1L, "AI"), new KeywordItem(2L, "개발"), new KeywordItem(3L, "뉴스")));

        given(subscriptionQueryService.getAllKeywords()).willReturn(response);

        //when then
        mockMvc.perform(get("/api/subscriptions/keywords").header("X-User-ID", "test-user-uuid")
                        .header("X-Device-Secret", "test-device-secret")).andDo(print()).andExpect(status().isOk())
                .andDo(restDocsHandler.document(responseFields(
                        fieldWithPath("errorCode").type(JsonFieldType.NULL).description("에러 코드, 성공 시 null"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("result").type(JsonFieldType.OBJECT).description("응답 데이터"),
                        fieldWithPath("result.keywords").type(JsonFieldType.ARRAY).description("키워드 목록"),
                        fieldWithPath("result.keywords[].id").type(JsonFieldType.NUMBER).description("키워드 ID"),
                        fieldWithPath("result.keywords[].name").type(JsonFieldType.STRING).description("키워드 이름"))));
    }

    @Test
    void 구독을_수정한다() throws Exception {
        //given
        Long subscriptionId = 1L;
        SubscriptionUpdateRequest request = new SubscriptionUpdateRequest(List.of(1L, 2L), "수정된 별칭", true);

        doNothing().when(subscriptionService)
                .updateSubscription(anyLong(), anyString(), anyString(), any(SubscriptionUpdateRequest.class));

        //when then
        ResultActions result = mockMvc.perform(
                        RestDocumentationRequestBuilders.patch("/api/subscriptions/{subscriptionId}", subscriptionId)
                                .content(objectMapper.writeValueAsString(request)).header("X-User-ID", "test-user-uuid")
                                .header("X-Device-Secret", "test-device-secret").contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        result.andExpect(status().isNoContent()).andDo(restDocsHandler.document(
                pathParameters(parameterWithName("subscriptionId").description("수정할 구독의 ID")), requestFields(
                        fieldWithPath("keywordIds").type(JsonFieldType.ARRAY).optional().description("변경할 키워드 ID 리스트"),
                        fieldWithPath("alias").type(JsonFieldType.STRING).optional().description("변경할 구독 별칭"),
                        fieldWithPath("isAlarmEnabled").type(JsonFieldType.BOOLEAN).optional()
                                .description("알림 활성화 여부"))));
    }
}
