package com.questbuddy.messages.guard;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingGuardsConfig {
    @Bean
    @ConditionalOnMissingBean(TripMembershipGate.class)
    public TripMembershipGate allowAllTripMembershipGate() {
        return (tripId, userId) -> tripId != null && userId != null;
    }
}
