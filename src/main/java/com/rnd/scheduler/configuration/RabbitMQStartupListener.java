package com.rnd.scheduler.configuration;

import com.rnd.scheduler.service.RabbitMQService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Component
public class RabbitMQStartupListener {

    @Autowired
    private RabbitMQService rabbitMQListener;

    @PostConstruct
    public void init() {
        try {
            rabbitMQListener.listen();  // Mendengarkan pesan saat aplikasi dimulai
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}
