package com.group04.config;

import com.group04.IGenericRepository;
import com.group04.Order;
import com.group04.realtime.RealtimeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Bean realtime: thread poll snapshot đơn và phát SSE.
 * Bean tách riêng khỏi ConfigBeans vì phụ thuộc ObjectMapper của Spring MVC.
 */
@Configuration
public class RealtimeConfig {

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public RealtimeService realtimeService(
            IGenericRepository<Order, Integer> orderRepository,
            ObjectMapper objectMapper,
            @Value("${app.realtime.poll-interval-ms:300}") long pollIntervalMs) {
        return new RealtimeService(orderRepository, objectMapper, pollIntervalMs);
    }
}
