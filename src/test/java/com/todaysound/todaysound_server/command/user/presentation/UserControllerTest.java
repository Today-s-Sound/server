package com.todaysound.todaysound_server.command.user.presentation;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.todaysound.todaysound_server.domain.user.dto.request.UserSecretRequest;
import com.todaysound.todaysound_server.domain.user.dto.response.UserIdResponse;
import com.todaysound.todaysound_server.support.DocumentationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

class UserControllerTest extends DocumentationTestSupport {


    @Test
    void 익명_사용자를_등록한다() throws Exception {
        // given
        UserIdResponse response = new UserIdResponse("test-user-uuid-1234");

        UserSecretRequest request = new UserSecretRequest("device-secret-5678", "Pixel 5", "fcm-token-91011");

        given(userService.anonymous(request)).willReturn(response);

        // when then
        ResultActions result = mockMvc.perform(
                post("/api/users/anonymous").content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk()).andDo(restDocsHandler.document(
                requestFields(fieldWithPath("deviceSecret").type(JsonFieldType.STRING).description("디바이스 시크릿"),
                        fieldWithPath("model").type(JsonFieldType.STRING).description("디바이스 모델명"),
                        fieldWithPath("fcmToken").type(JsonFieldType.STRING).description("FCM 토큰")),
                responseFields(fieldWithPath("errorCode").type(JsonFieldType.NULL).description("응답 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("result").type(JsonFieldType.OBJECT).description("피드 목록"),
                        fieldWithPath("result.userId").type(JsonFieldType.STRING).description("등록된 사용자 UUID"))));
    }

    @Test
    void 회원을_탈퇴한다() throws Exception {
        // given
        willDoNothing().given(userService).withdraw(anyString(), anyString());

        // when then
        ResultActions result = mockMvc.perform(
                RestDocumentationRequestBuilders.delete("/api/users/withdraw")
                        .header("X-User-ID", "test-user-uuid")
                        .header("X-Device-Secret", "test-device-secret"));

        result.andDo(print()).andExpect(status().isOk()).andDo(restDocsHandler.document(
                requestHeaders(
                        headerWithName("X-User-ID").description("사용자 UUID"),
                        headerWithName("X-Device-Secret").description("디바이스 시크릿"))));
    }
}