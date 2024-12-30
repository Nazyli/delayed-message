package com.rnd.scheduler.controller;

import com.rnd.scheduler.configuration.NsqMessageConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/nsq")
public class NsqController {

    private final NsqMessageConsumer nsqMessageConsumer;

    @PostMapping("/publish")
    public String publishMessage(
            @RequestParam String message,
            @RequestParam(required = false) Integer delay) throws Exception {
        nsqMessageConsumer.publishMessage(message, delay);
        return "Pesan berhasil dikirim dengan delay: " + delay + "ms";
    }

}
