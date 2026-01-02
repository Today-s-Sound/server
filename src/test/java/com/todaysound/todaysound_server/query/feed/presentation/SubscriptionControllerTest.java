package com.todaysound.todaysound_server.query.feed.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.todaysound.todaysound_server.domain.subscription.dto.request.SubscriptionCreateRequestDto;
import com.todaysound.todaysound_server.domain.subscription.dto.response.SubscriptionCreationResponseDto;
import com.todaysound.todaysound_server.spring.docs.RestDocsSupport;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

public class SubscriptionControllerTest extends RestDocsSupport {


    @Test
    void 신규_구독을_등록한다() throws Exception {
        // given
        SubscriptionCreateRequestDto request = new SubscriptionCreateRequestDto(
                1L,
                List.of(1L, 2L),
                "넓은마을",
                true
        );

        given(subscriptionCommandService.createSubscription(anyString(), anyString(), any(SubscriptionCreateRequestDto.class)))
                .willReturn(SubscriptionCreationResponseDto.builder()
                        .subscriptionId(1L)
                        .build()
                );

        // when then
        mockMvc.perform(
                        post("/api/subscriptions")
                                .content(objectMapper.writeValueAsString(request))
                                .header("X-User-ID", "test-user-uuid")
                                .header("X-Device-Secret", "test-device-secret")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        requestFields(
                                fieldWithPath("urlId").type(JsonFieldType.NUMBER)
                                        .description("구독할 URL의 ID"),
                                fieldWithPath("keywordIds").type(JsonFieldType.ARRAY)
                                        .optional()
                                        .description("구독에 연관된 키워드 ID 리스트"),
                                fieldWithPath("alias").type(JsonFieldType.STRING)
                                        .description("구독 별칭"),
                                fieldWithPath("isUrgent").type(JsonFieldType.BOOLEAN)
                                        .description("긴급 알림 여부")
                        ),
                        responseFields(
                                fieldWithPath("errorCode").type(JsonFieldType.NULL)
                                        .description("에러 코드, 성공 시 null"),
                                fieldWithPath("message").type(JsonFieldType.STRING)
                                        .description("응답 메시지"),
                                fieldWithPath("result").type(JsonFieldType.OBJECT)
                                        .description("응답 데이터"),
                                fieldWithPath("result.subscriptionId").type(JsonFieldType.NUMBER)
                                        .description("생성된 구독의 ID")
                        )
                ));
    }
}
