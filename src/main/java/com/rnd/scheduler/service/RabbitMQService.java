package com.rnd.scheduler.service;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Component
public class RabbitMQService {
    public void listen() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setVirtualHost("/");

        Connection connection = null;
        final Channel channel;
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();

            // Exchange and Queue Declaration
            Map<String, Object> args = new HashMap<>();
            args.put("x-delayed-type", "direct");
            channel.exchangeDeclare("delayed_exchange", "x-delayed-message", true, false, args);

            String queueName = "delayed_queue";
            channel.queueDeclare(queueName, true, false, false, null);
            channel.queueBind(queueName, "delayed_exchange", "");

            System.out.println("Mendengarkan pesan di queue: " + queueName);

            channel.basicConsume(queueName, false, (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println("RabbitMQ | Pada " + new Date() + " | Pesan diterima : " + message);

                // Process message and Ack after completion
                try {
                    if (channel.isOpen()) {
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                        System.out.println("RabbitMQ | Pesan berhasil di-acknowledge.");
                    } else {
                        System.out.println("RabbitMQ | Channel sudah tertutup, tidak bisa ack pesan.");
                    }
                } catch (IOException e) {
                    System.out.println("RabbitMQ | Gagal meng-ack pesan: " + e.getMessage());
                }
            }, consumerTag -> {
                System.out.println("RabbitMQ | Consumer dibatalkan: " + consumerTag);
            });
        } catch (IOException | TimeoutException e) {
            System.out.println("RabbitMQ | Gagal koneksi ke RabbitMQ: " + e.getMessage());
        }
    }


    public void sendMessage(String message, int delayInMillis) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setVirtualHost("/");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // Declare a delayed exchange if it doesn't exist
            Map<String, Object> args = new HashMap<>();
            args.put("x-delayed-type", "direct");  // exchange type
            channel.exchangeDeclare("delayed_exchange", "x-delayed-message", true, false, args);

            // Send messages with delay
            byte[] messageBodyBytes = message.getBytes("UTF-8");
            Map<String, Object> headers = new HashMap<>();
            headers.put("x-delay", delayInMillis);

            AMQP.BasicProperties.Builder props = new AMQP.BasicProperties.Builder().headers(headers);

            // Send a message to delayed_exchange
            channel.basicPublish("delayed_exchange", "", props.build(), messageBodyBytes);
            System.out.println("RabbitMQ | Pada " + new Date() + " | Pesan terkirim : " + message + " Delay " + delayInMillis + "ms");
        }
    }


/*

    public void listen() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setVirtualHost("/");

        Connection connection = null;
        Channel channel = null;

        try {
            connection = factory.newConnection();
            channel = connection.createChannel();

            // Deklarasi Exchange dan Queue
            Map<String, Object> args = new HashMap<>();
            args.put("x-delayed-type", "direct");
            channel.exchangeDeclare("delayed_exchange", "x-delayed-message", true, false, args);

            String queueName = "delayed_queue";
            channel.queueDeclare(queueName, true, false, false, null);
            channel.queueBind(queueName, "delayed_exchange", "");

            // Menampilkan nama queue untuk debugging
            System.out.println("Mendengarkan pesan di queue: " + queueName);

            // Konsumsi pesan dari queue dengan lebih banyak logging
            Channel finalChannel = channel;
            channel.basicConsume(queueName, false, (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println("Pesan diterima: " + message);

                // Ack pesan setelah diproses
                try {
                    if (finalChannel.isOpen()) {
                        finalChannel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                        System.out.println("Pesan diacknowledge.");
                    } else {
                        System.out.println("Channel sudah tertutup, tidak bisa ack pesan.");
                    }
                } catch (IOException e) {
                    System.out.println("Gagal meng-ack pesan: " + e.getMessage());
                }
            }, consumerTag -> {});

        } catch (IOException | TimeoutException e) {
            System.out.println("Gagal koneksi ke RabbitMQ: " + e.getMessage());
        } finally {
            if (channel != null && channel.isOpen()) {
                try {
                    channel.close(); // Pastikan channel ditutup dengan benar jika masih terbuka
                } catch (IOException | TimeoutException e) {
                    System.out.println("Error saat menutup channel: " + e.getMessage());
                }
            }
            if (connection != null && connection.isOpen()) {
                try {
                    connection.close(); // Pastikan koneksi ditutup dengan benar
                } catch (IOException e) {
                    System.out.println("Error saat menutup koneksi: " + e.getMessage());
                }
            }
        }
    }




    public static void sendMessage(String message, int delayInMillis) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setVirtualHost("/");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // Deklarasikan delayed exchange jika belum ada
            Map<String, Object> args = new HashMap<>();
            args.put("x-delayed-type", "direct");  // Tentukan jenis exchange yang digunakan
            channel.exchangeDeclare("delayed_exchange", "x-delayed-message", true, false, args);

            // Kirim pesan dengan delay
            byte[] messageBodyBytes = message.getBytes("UTF-8");
            Map<String, Object> headers = new HashMap<>();
            headers.put("x-delay", delayInMillis);  // Tentukan waktu delay pesan

            AMQP.BasicProperties.Builder props = new AMQP.BasicProperties.Builder().headers(headers);

            // Kirim pesan ke delayed_exchange
            channel.basicPublish("delayed_exchange", "", props.build(), messageBodyBytes);
            System.out.println("Pesan terkirim: " + message + " dengan delay: " + delayInMillis + "ms");
        }
    }
*/

    /*

    public void listen() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setVirtualHost("/");

        Connection connection = null;
        final Channel finalChannel;  // Deklarasi sebagai final

        try {
            connection = factory.newConnection();
            finalChannel = connection.createChannel();  // Menginisialisasi channel

            // Deklarasi Exchange dan Queue
            Map<String, Object> args = new HashMap<>();
            args.put("x-delayed-type", "direct");
            finalChannel.exchangeDeclare("delayed_exchange", "x-delayed-message", true, false, args);

            String queueName = "delayed_queue";
            finalChannel.queueDeclare(queueName, true, false, false, null);
            finalChannel.queueBind(queueName, "delayed_exchange", "");

            // Menampilkan nama queue untuk debugging
            System.out.println("Mendengarkan pesan di queue: " + queueName);

            // Konsumsi pesan dari queue secara terus-menerus
            finalChannel.basicConsume(queueName, false, (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println("Pesan diterima: " + message);

                // Proses pesan dan Ack setelah selesai
                try {
                    if (finalChannel.isOpen()) {
                        // Proses pesan (contoh: print pesan atau proses lainnya)
                        System.out.println("Memproses pesan: " + message);

                        // Ack pesan setelah diproses
                        finalChannel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                        System.out.println("Pesan berhasil di-acknowledge.");
                    } else {
                        System.out.println("Channel sudah tertutup, tidak bisa ack pesan.");
                    }
                } catch (IOException e) {
                    System.out.println("Gagal meng-ack pesan: " + e.getMessage());
                }
            }, consumerTag -> {
                // Menangani kasus jika consumer dibatalkan
                System.out.println("Consumer dibatalkan: " + consumerTag);
            });

            // Tidak menutup channel atau connection di sini, biarkan listener terus berjalan
        } catch (IOException | TimeoutException e) {
            System.out.println("Gagal koneksi ke RabbitMQ: " + e.getMessage());
        }
    }
    */

    /*
    public void listen() throws IOException, TimeoutException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setVirtualHost("/");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.basicConsume(QUEUE_NAME, true, (consumerTag, delivery) -> {
                String message = new String(delivery.getBody());
                System.out.println("Pesan diterima: " + message);
            }, consumerTag -> {});
        }catch (IOException | TimeoutException e) {
            System.out.println("Gagal koneksi ke RabbitMQ: " + e.getMessage());
        }
    }

//    @RabbitListener(queues = QUEUE_NAME)
//    public void receiveMessage(String message) {
//        System.out.println("Pesan diterima: " + message);
//    }
    public static void sendMessage(String message, int delayInMillis) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setVirtualHost("/");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // Kirim pesan dengan delay
            Map<String, Object> headers = new HashMap<>();
            headers.put("x-delay", delayInMillis);

            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                    .headers(headers)
                    .build();

            channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, props, message.getBytes());
            System.out.println("Pesan terkirim: " + message);
        }
    }
*/
}