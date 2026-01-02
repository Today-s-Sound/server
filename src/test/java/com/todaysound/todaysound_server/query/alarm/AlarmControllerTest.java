//package com.todaysound.todaysound_server.query.alarm;
//
//import com.todaysound.todaysound_server.domain.alarm.controller.AlarmController;
//import java.time.LocalDate;
//import java.util.Date;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import static org.mockito.BDDMockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//
//import com.todaysound.todaysound_server.domain.alarm.dto.response.RecentAlarmResponse;
//import com.todaysound.todaysound_server.domain.alarm.service.AlarmQueryService;
//import com.todaysound.todaysound_server.domain.summary.service.SummaryCommandService;
//import com.todaysound.todaysound_server.global.dto.PageRequestDTO;
//
//import java.util.List;
//
//@WebMvcTest(controllers = AlarmController.class)
//@AutoConfigureMockMvc(addFilters = false) // 보안 필터 끄기
//public class AlarmControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc; // 가짜로 HTTP 요청을 보내줄 친구
//
//    @MockitoBean
//    private AlarmQueryService alarmQueryService; // 가짜 서비스 객체 1
//
//    // (CommandService는 이 테스트 메소드에선 안 쓰이지만, 컨트롤러가 의존하고 있어서 Mock으로 채워줘야 에러가 안 남)
//    @MockitoBean
//    private SummaryCommandService summaryCommandService;
//
//
//    // BDD 패턴 적용: Given - When - Then
//    @Test
//    public void getRecentAlarms_success() throws Exception {
//        // given
//        String userUuid = "test-user-uuid";
//        String deviceSecret = "test-device-secret";
//
//        RecentAlarmResponse response = new RecentAlarmResponse(
//                1L,
//                1L,
//                "동국대 SW 융합교육원",
//                "요약된 알림 내용...",
//                "https://example.com/post/1",
//                "5분 전",
//                true,
//                false
//        );
//
//        List<RecentAlarmResponse> responses = List.of(response);
//
//        // PageRequestDTO(page, size) 에 맞춰서 mock 설정
//        given(alarmQueryService.getRecentAlarms(
//                any(PageRequestDTO.class),
//                eq(userUuid),
//                eq(deviceSecret)
//        )).willReturn(responses);
//
//        // when
//        // then
//        mockMvc.perform(get("/api/alarms")
//                        .param("page", "0")
//                        .param("size", "10")
//                        .header("X-User-ID", userUuid)
//                        .header("X-Device-Secret", deviceSecret))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.result[0].subscriptionId").value(1L))
//                .andExpect(jsonPath("$.result[0].alias").value("동국대 SW 융합교육원"))
//                .andExpect(jsonPath("$.result[0].isUrgent").value(true));
//
//        // 서비스가 정확한 값으로 한 번 호출됐는지도 검증 (선택)
//        then(alarmQueryService).should(times(1))
//                .getRecentAlarms(any(PageRequestDTO.class), eq(userUuid), eq(deviceSecret));
//    }
//
//
//    @Test
//    // @DisplayName("헤더 누락 시 400 에러가 터지는지 확인") // Junit5에서는 이렇게 이름을 붙여줌
//    public void getRecentAlarms_fail_missing_header() throws Exception {
//        // given
//        // 이번엔 준비물이 필요 없어.
//        // 왜냐? 컨트롤러 입구컷 당해서 서비스까지 가지도 못할 거니까!
//
//        // when
//        mockMvc.perform(get("/api/alarms")
//                        .param("page", "0")
//                        .param("size", "10")
//                        // .header("X-User-ID", "...") <--- 이걸 일부러 뺌
//                        .header("X-Device-Secret", "test-device-secret"))
//
//        // then
//                .andExpect(status().isBadRequest()) // 1. 상태 코드가 400인지 확인
//                .andDo(print()); // 2. 로그에 요청/응답 내용을 자세히 찍음 (디버깅용)
//
//        // 진짜로 서비스가 호출 안 됐는지 확인!
//        then(alarmQueryService).shouldHaveNoInteractions();
//    }
//}
