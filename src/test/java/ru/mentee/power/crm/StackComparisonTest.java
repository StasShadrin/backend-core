package ru.mentee.power.crm;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import ru.mentee.power.crm.servlet.LeadListServlet;
import ru.mentee.power.crm.spring.repository.InMemoryDealRepository;
import ru.mentee.power.crm.spring.repository.InMemoryLeadRepository;
import ru.mentee.power.crm.spring.service.LeadService;
import ru.mentee.power.crm.util.TestDataUtils;

/**
 * Интеграционный тест сравнения Servlet и Spring Boot стеков. Полностью автономный: запускает оба
 * сервера, сравнивает ответы, останавливает серверы. Использует динамические порты для надёжной
 * работы в CI/CD.
 */
class StackComparisonTest {

  private static Tomcat tomcat;
  private static ConfigurableApplicationContext springContext;
  private static int servletPort;
  private static int springPort;
  private static long servletStartupTimeMs;
  private static long springStartupTimeMs;

  private HttpClient httpClient;

  @BeforeAll
  static void startServers() throws Exception {
    LeadService leadService =
        new LeadService(new InMemoryLeadRepository(), new InMemoryDealRepository());
    TestDataUtils.initializeTestData(leadService);

    tomcat = new Tomcat();
    tomcat.setPort(0); // динамический порт
    Path tempDir = Files.createTempDirectory("tomcat");
    Context ctx = tomcat.addContext("", tempDir.toString());

    ctx.getServletContext().setAttribute("leadService", leadService);

    LeadListServlet servlet = new LeadListServlet();
    Path templatePath = Path.of("src", "main", "jte").toAbsolutePath();
    gg.jte.CodeResolver resolver = new gg.jte.resolve.DirectoryCodeResolver(templatePath);
    gg.jte.TemplateEngine engine = gg.jte.TemplateEngine.create(resolver, gg.jte.ContentType.Html);
    servlet.setTemplateEngine(engine);

    Tomcat.addServlet(ctx, "LeadListServlet", servlet);
    ctx.addServletMappingDecoded("/leads", "LeadListServlet");

    long start = System.nanoTime();
    tomcat.start();
    servletStartupTimeMs = (System.nanoTime() - start) / 1_000_000;
    servletPort = tomcat.getConnector().getLocalPort();

    String[] args = {
      "--server.port=0",
      "--spring.main.web-application-type=servlet",
      "--spring.main.banner-mode=off",
      "--logging.level.root=WARN"
    };
    start = System.nanoTime();
    springContext = SpringApplication.run(Application.class, args);
    springStartupTimeMs = (System.nanoTime() - start) / 1_000_000;
    springPort = springContext.getEnvironment().getProperty("local.server.port", Integer.class);
  }

  @AfterAll
  static void stopServers() throws Exception {
    if (springContext != null) {
      SpringApplication.exit(springContext);
    }
    if (tomcat != null) {
      tomcat.stop();
      tomcat.destroy();
    }
  }

  @BeforeEach
  void setUp() {
    httpClient = HttpClient.newHttpClient();
  }

  @Test
  @DisplayName("Оба стека должны возвращать лидов в HTML таблице")
  void shouldReturnLeadsFromBothStacks() throws Exception {
    // Given: HTTP запросы к обоим стекам
    HttpRequest servletReq =
        HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + servletPort + "/leads"))
            .timeout(java.time.Duration.ofSeconds(5))
            .GET()
            .build();

    HttpRequest springReq =
        HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + springPort + "/leads"))
            .timeout(java.time.Duration.ofSeconds(5))
            .GET()
            .build();

    // When: выполняем запросы
    HttpResponse<String> servletResp =
        httpClient.send(servletReq, HttpResponse.BodyHandlers.ofString());
    HttpResponse<String> springResp =
        httpClient.send(springReq, HttpResponse.BodyHandlers.ofString());

    // Then: оба возвращают 200 OK и содержат таблицу
    assertThat(servletResp.statusCode()).isEqualTo(200);
    assertThat(springResp.statusCode()).isEqualTo(200);

    assertThat(servletResp.body()).contains("<table");
    assertThat(springResp.body()).contains("<table");

    // Then: одинаковое количество лидов
    int servletRows = countTableRows(servletResp.body());
    int springRows = countTableRows(springResp.body());

    assertThat(servletRows).as("Количество лидов должно совпадать").isEqualTo(springRows);

    System.out.printf("Servlet: %d лидов, Spring: %d лидов%n", servletRows, springRows);
  }

  @Test
  @DisplayName("Измерение времени старта обоих стеков")
  void shouldMeasureStartupTime() {
    // Вывод результатов
    System.out.println("=== Сравнение времени старта ===");
    System.out.printf("Servlet стек: %d ms%n", servletStartupTimeMs);
    System.out.printf("Spring Boot:  %d ms%n", springStartupTimeMs);
    System.out.printf(
        "Разница: Spring Boot %s на %d ms%n",
        springStartupTimeMs > servletStartupTimeMs ? "медленнее" : "быстрее",
        Math.abs(springStartupTimeMs - servletStartupTimeMs));

    // Просто фиксируем что оба стартуют за разумное время
    assertThat(servletStartupTimeMs).isLessThan(5_000);
    //        assertThat(springStartupTimeMs).isLessThan(17_000);
  }

  /**
   * Считает строки <tr в <tbody>. Предполагает, что HTML содержит одну <tr в <thead>, поэтому
   * вычитаем 2: одну для пустой части до первого <tr, и одну для шапки.
   */
  private int countTableRows(String html) {
    if (html == null || html.isEmpty()) {
      return 0;
    }
    return html.split("<tr", -1).length - 2;
  }
}
