package com.todaysound.todaysound_server.query.url.presentation;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.todaysound.todaysound_server.domain.url.dto.response.UrlResponseDto;
import com.todaysound.todaysound_server.support.DocumentationTestSupport;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

class UrlQueryControllerTest extends DocumentationTestSupport {

    @Test
    void URL목록을_조회한다() throws Exception {
        // given
        UrlResponseDto response = new UrlResponseDto(1L, "https://todaysound.com", "Today's Sound");

        List<UrlResponseDto> responseList = List.of(response);

        given(urlQueryService.getUrls()).willReturn(responseList);

        // when
        ResultActions result = mockMvc.perform(get("/api/urls"));

        // then
        result.andExpect(status().isOk()).andDo(restDocsHandler.document(
                responseFields(fieldWithPath("errorCode").description("응답 코드"),
                        fieldWithPath("message").description("응답 메시지"), fieldWithPath("result").description("URL 목록"),
                        fieldWithPath("result[].id").type(JsonFieldType.NUMBER).description("URL ID"),
                        fieldWithPath("result[].link").type(JsonFieldType.STRING).description("URL 링크"),
                        fieldWithPath("result[].title").type(JsonFieldType.STRING).description("URL 제목"))));
    }
}