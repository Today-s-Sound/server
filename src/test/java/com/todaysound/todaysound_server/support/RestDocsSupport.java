package com.todaysound.todaysound_server.support;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.todaysound.todaysound_server.domain.subscription.controller.SubscriptionController;
import com.todaysound.todaysound_server.domain.subscription.service.SubscriptionCommandService;
import com.todaysound.todaysound_server.domain.subscription.service.SubscriptionQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


@WebMvcTest(
        controllers = {
                SubscriptionController.class
        }
)
@Import({
        RestDocsConfig.class,
})
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
public abstract class RestDocsSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected RestDocumentationResultHandler restDocsHandler;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected SubscriptionCommandService subscriptionCommandService;

    @MockitoBean
    protected SubscriptionQueryService subscriptionQueryService;

}
