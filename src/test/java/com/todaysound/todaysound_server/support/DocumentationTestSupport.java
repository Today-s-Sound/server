package com.todaysound.todaysound_server.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todaysound.todaysound_server.domain.alarm.controller.AlarmQueryController;
import com.todaysound.todaysound_server.domain.alarm.service.AlarmQueryService;
import com.todaysound.todaysound_server.domain.feed.controller.FeedController;
import com.todaysound.todaysound_server.domain.feed.service.FeedQueryService;
import com.todaysound.todaysound_server.domain.subscription.controller.SubscriptionController;
import com.todaysound.todaysound_server.domain.subscription.service.SubscriptionService;
import com.todaysound.todaysound_server.domain.subscription.service.SubscriptionQueryService;
import com.todaysound.todaysound_server.domain.summary.controller.SummaryController;
import com.todaysound.todaysound_server.domain.summary.service.SummaryCommandService;
import com.todaysound.todaysound_server.domain.url.controller.UrlController;
import com.todaysound.todaysound_server.domain.url.service.UrlQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {FeedController.class, AlarmQueryController.class, SubscriptionController.class,
        UrlController.class, SummaryController.class})
@Import({RestDocsConfig.class,})
@AutoConfigureRestDocs
@AutoConfigureMockMvc(addFilters = false)
public class DocumentationTestSupport {

    @Autowired
    protected RestDocumentationResultHandler restDocsHandler;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected FeedQueryService feedQueryService;

    @MockitoBean
    protected AlarmQueryService alarmQueryService;

    @MockitoBean
    protected SubscriptionQueryService subscriptionQueryService;

    @MockitoBean
    protected SubscriptionService subscriptionCommandService;

    @MockitoBean
    protected UrlQueryService urlQueryService;

    @MockitoBean
    protected SummaryCommandService summaryCommandService;

}
