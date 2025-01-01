package com.rnd.scheduler.configuration;


import com.github.brainlag.nsq.NSQConsumer;
import com.github.brainlag.nsq.NSQProducer;
import com.github.brainlag.nsq.lookup.DefaultNSQLookup;
import com.github.brainlag.nsq.lookup.NSQLookup;
import jakarta.annotation.PostConstruct;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.logging.Logger;

@Component
public class NsqMessageConsumer {
    private static final String TOPIC_NAME = "test_topic";
    private static final String CHANNEL_NAME = "test_channel";

    private static final Logger logger = Logger.getLogger(NsqMessageConsumer.class.getName());

    @PostConstruct
    public void startConsumer() {
        NSQLookup lookup = new DefaultNSQLookup();
//        lookup.addLookupAddress("host.docker.internal", 4161);
        lookup.addLookupAddress("localhost", 4161); // nsqlookupd
        try {
            sendMessage("dummy message to create topic and channel", 500);
            logger.info("Topik dan saluran telah dibuat atau sudah ada: " + TOPIC_NAME + " - " + CHANNEL_NAME);
        } catch (Exception e) {
            logger.severe("Gagal membuat atau memverifikasi topik dan saluran: " + e.getMessage());
        }

        NSQConsumer consumer = new NSQConsumer(lookup, TOPIC_NAME, CHANNEL_NAME, message -> {
            String messageContent = new String(message.getMessage(), StandardCharsets.UTF_8);
            System.out.println("NSQ | Pada " + new Date() + " | Pesan diterima : " + messageContent);
            message.finished();
        });
        consumer.start();

        System.out.println("NSQ Consumer berjalan, mendengarkan topik: " + TOPIC_NAME);
    }

    public void sendMessage(String message, int delay) throws Exception {
        // Publish dummy messages to topics and channels
        NSQProducer producer = new NSQProducer();
        producer.addAddress("localhost", 4150); // Alamat nsqd
        producer.start();

        producer.produce(TOPIC_NAME, message.getBytes(StandardCharsets.UTF_8));
        Thread.sleep(delay);
        System.out.println("NSQ | Pada " + new Date() + " | Pesan terkirim : " + message + " | Delay " + delay + "ms");

    }


    public String publishMessage(String message, Integer delay) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String publishUrl = "http://127.0.0.1:4151/pub?topic=" + TOPIC_NAME;
            if (delay != null) {
                publishUrl += "&defer=" + delay;
            }

            HttpPost postRequest = new HttpPost(publishUrl);
            postRequest.setEntity(new StringEntity(message));
            postRequest.setHeader("Content-Type", "text/plain");

            httpClient.execute(postRequest);
            System.out.println("NSQ | Pada " + new Date() + " | Pesan terkirim : " + message + " | Delay " + delay + "ms");
            return "Pesan berhasil dikirim dengan delay: " + (delay != null ? delay + "ms" : "tanpa delay");
        } catch (IOException e) {
            e.printStackTrace();
            return "Gagal mengirim pesan: " + e.getMessage();
        }
    }
}
