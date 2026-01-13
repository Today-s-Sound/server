package com.todaysound.todaysound_server.domain.url.controller;

import com.todaysound.todaysound_server.domain.url.dto.response.UrlResponse;
import com.todaysound.todaysound_server.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@Tag(name = "Url", description = "URL 관리 API")
public interface UrlApi {

    @Operation(
            summary = "URL 목록 조회",
            description = """
                    등록된 모든 URL 목록을 조회합니다.
                    """,
            tags = {"Url"},
            operationId = "getUrls"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "URL 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            array = @ArraySchema(schema = @Schema(implementation = UrlResponse.class)),
                            examples = @ExampleObject(
                                    name = "URL 목록 조회 성공 예시",
                                    value = """
                                            {
                                                "errorCode": null,
                                                "message": "OK",
                                                "result": [
                                                    {
                                                        "id": 1,
                                                        "link": "https://example.com",
                                                        "title": "Example Site"
                                                    }
                                                ]
                                            }
                                            """
                            )
                    )
            )
    })
    List<UrlResponse> getUrls();
}

