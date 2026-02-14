package ru.mentee.power.crm.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Простейший HTTP-сервер для отображения приветственной страницы CRM. Использует встроенный JDK
 * HttpServer и слушает указанный порт.
 */
public class HelloCrmServer {

  private final HttpServer server;
  private final int port;

  /**
   * Создаёт HTTP-сервер, привязанный к заданному порту.
   *
   * @param port порт, на котором будет запущен сервер (например, 8080)
   * @throws IOException если порт уже занят или недоступен
   */
  public HelloCrmServer(int port) throws IOException {
    this.port = port;
    this.server = HttpServer.create(new InetSocketAddress(port), 0);
  }

  /**
   * Запускает сервер и регистрирует обработчик для эндпоинта /hello. После запуска сервер
   * обрабатывает входящие HTTP-запросы.
   */
  public void start() {
    server.createContext("/hello", new HelloHandler());
    server.start();
    System.out.println("Server started on http://localhost:" + port);
  }

  /** Корректно останавливает HTTP-сервер без задержки. */
  public void stop() {
    server.stop(0);
  }

  /**
   * Обработчик HTTP-запросов к эндпоинту /hello. Возвращает HTML-страницу "Hello CRM!" только на
   * GET-запросы.
   */
  static class HelloHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {

      String method = exchange.getRequestMethod();
      String path = exchange.getRequestURI().getPath();
      System.out.println("Received " + method + " request for " + path);

      if (!method.equals("GET")) {
        exchange.sendResponseHeaders(405, -1);
        exchange.close();
        return;
      }

      String html =
          """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                        <title>Hello CRM!</title>
                    </head>
                    <body>
                        <h1>Hello CRM!</h1>
                    </body>
                    </html>
                    """;
      byte[] response = html.getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
      exchange.sendResponseHeaders(200, response.length);

      try (OutputStream os = exchange.getResponseBody()) {
        os.write(response);
      }
    }
  }
}
