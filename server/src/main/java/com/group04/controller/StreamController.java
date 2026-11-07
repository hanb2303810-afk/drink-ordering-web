package com.group04.controller;

import com.group04.realtime.RealtimeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Public SSE stream. Khách, phục vụ, barista, admin đều thấy chung bảng đơn,
 * vì đề tài yêu cầu khách theo dõi đơn pha chế theo thời gian thực.
 */
@RestController
@RequestMapping("/api/stream")
public class StreamController {

    private final RealtimeService realtimeService;

    public StreamController(RealtimeService realtimeService) {
        this.realtimeService = realtimeService;
    }

    @GetMapping("/orders")
    public SseEmitter orders() {
        return realtimeService.subscribe();
    }
}
