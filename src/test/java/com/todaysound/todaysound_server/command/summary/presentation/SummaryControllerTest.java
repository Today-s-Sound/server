package com.todaysound.todaysound_server.command.summary.presentation;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.todaysound.todaysound_server.support.DocumentationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;

class SummaryControllerTest extends DocumentationTestSupport {

    @Test
    void 요약을_삭제한다() throws Exception {
        // given
        Long summaryId = 1L;

        doNothing().when(summaryCommandService).deleteSummary(anyString(), anyString(), anyLong());

        // when then
        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/summaries/{summaryId}", summaryId)
                        .header("X-User-ID", "test-user-uuid")
                        .header("X-Device-Secret", "test-device-secret"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        pathParameters(parameterWithName("summaryId").description("삭제할 요약의 ID")),
                        responseFields(
                                fieldWithPath("errorCode").type(JsonFieldType.NULL).description("에러 코드, 성공 시 null"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                                fieldWithPath("result").type(JsonFieldType.NULL).description("응답 데이터, 삭제 시 null"))));
    }
}
