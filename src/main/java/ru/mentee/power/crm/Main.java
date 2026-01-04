package ru.mentee.power.crm;

import ru.mentee.power.crm.web.HelloCrmServer;

/** Точка входа в программу */
public class Main {

    static void main() throws Exception {
        int port = 8080;
        HelloCrmServer server = new HelloCrmServer(port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Stopping server...");
            server.stop();
        }));

        server.start();

        Thread.currentThread().join();
    }
}