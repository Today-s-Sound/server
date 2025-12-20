package com.todaysound.todaysound_server.domain.user.controller;

import com.todaysound.todaysound_server.domain.user.dto.request.UserSecretRequestDto;
import com.todaysound.todaysound_server.domain.user.dto.response.UserIdResponseDto;
import com.todaysound.todaysound_server.global.exception.CustomErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "User", description = "사용자 관리 API")
public interface UserApi {

        @Operation(summary = "익명 사용자 생성", description = """
                        IOS가 생성한 랜덤 시크릿을 받아서 새 user_id를
                        생성하고 시크릿을 해시화 하고 DB에 저장한 이후에 응답을 반환합니다.
                        """, tags = {"User"}, operationId = "generateAnonymousUser")
        @ApiResponses({@ApiResponse(responseCode = "200", description = "익명 사용자 생성 성공",
                        content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = ApiResponse.class),
                                        examples = @ExampleObject(
                                                        name = "GlobalResponseAdvice로 감싸진 생성 성공 응답",
                                                        value = """
                                                                        {
                                                                            "errorCode": null,
                                                                            "message": "OK",
                                                                            "result": {
                                                                                "user_id": "user-uuid-1234"
                                                                            }
                                                                        }
                                                                        """

                                        ))),
                        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터",
                                        content = @Content(mediaType = "application/json",
                                                        schema = @Schema(
                                                                        implementation = CustomErrorResponse.class),
                                                        examples = @ExampleObject(name = "잘못된 요청",
                                                                        value = """
                                                                                        {
                                                                                            "status": 400,
                                                                                            "code": "INVALID_REQUEST",
                                                                                            "message": "deviceSecret이 비어있습니다"
                                                                                        }
                                                                                        """))),
                        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                                        content = @Content(mediaType = "application/json",
                                                        schema = @Schema(
                                                                        implementation = CustomErrorResponse.class),
                                                        examples = @ExampleObject(name = "인증 실패",
                                                                        value = """
                                                                                        {
                                                                                            "status": 401,
                                                                                            "code": "UNAUTHORIZED",
                                                                                            "message": "인증이 필요합니다"
                                                                                        }
                                                                                        """)))})
        UserIdResponseDto anonymous(@Valid @RequestBody UserSecretRequestDto userSecretRequestDto);

        @Operation(summary = "사용자 탈퇴", description = """
                        X-User-ID, X-Device-Secret 헤더를 이용해 사용자를 탈퇴 처리합니다.
                        """, tags = {"User"}, operationId = "withdrawUser")
        @ApiResponses({@ApiResponse(responseCode = "200", description = "사용자 탈퇴 성공"), @ApiResponse(
                        responseCode = "401", description = "인증되지 않은 사용자",
                        content = @Content(mediaType = "application/json", schema = @Schema(
                                        implementation = CustomErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                                        content = @Content(mediaType = "application/json",
                                                        schema = @Schema(
                                                                        implementation = CustomErrorResponse.class)))})
        void withdraw(@RequestHeader("X-User-ID") String userUuid,
                        @RequestHeader("X-Device-Secret") String deviceSecret);
}
