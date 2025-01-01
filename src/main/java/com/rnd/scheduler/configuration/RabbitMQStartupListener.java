package com.rnd.scheduler.configuration;

import com.rnd.scheduler.service.RabbitMQService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
public class RabbitMQStartupListener {

    private final RabbitMQService rabbitMQListener;

    @PostConstruct
    public void init() {
        try {
            rabbitMQListener.listen();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}
