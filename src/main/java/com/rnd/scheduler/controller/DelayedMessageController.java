package com.rnd.scheduler.controller;

import com.rnd.scheduler.configuration.NsqMessageConsumer;
import com.rnd.scheduler.service.RabbitMQService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class DelayedMessageController {
    private final RabbitMQService rabbitMQService;
    private final NsqMessageConsumer nsqMessageConsumer;

    @PostMapping("/send/rabbitmq")
    public ResponseEntity<String> sendDelayedMessage(@RequestParam String message,
                                                     @RequestParam Integer delay) {
        try {
            rabbitMQService.sendMessage(message, delay);
            return ResponseEntity.ok("RabbitMQ | Pesan dengan delay " + delay + "ms dikirim. ");
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Gagal mengirim pesan.");
        }
    }


    @PostMapping("/send/nsq")
    public String publishMessage(
            @RequestParam String message,
            @RequestParam(required = false) Integer delay) throws Exception {
        nsqMessageConsumer.publishMessage(message, delay);
        return "NSQ | Pesan dengan delay " + delay + "ms dikirim. ";
    }
}
