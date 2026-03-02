package com.todaysound.todaysound_server.support.isolation;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
class TableNameExtractorImpl implements TableNameExtractor {

    @Override
    public List<String> getNames() {
        return List.of(
                "keywords",
                "urls",
                "users",
                "fcm_tokens",
                "subscriptions",
                "subscriptions_keywords",
                "summaries",
                "notification_outbox"
        );
    }
}