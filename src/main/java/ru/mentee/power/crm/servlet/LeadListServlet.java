package ru.mentee.power.crm.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.output.StringOutput;
import gg.jte.resolve.DirectoryCodeResolver;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.service.LeadService;

/**
 * Сервлет для отображения списка лидов (потенциальных клиентов) в виде HTML таблицы.
 * Обрабатывает GET-запросы по адресу /leads.
 * Получает данные через LeadService.
 */
@WebServlet("/leads")
public class LeadListServlet extends HttpServlet {

    private TemplateEngine templateEngine;

    // Для тестов - package-private метод
    public void setTemplateEngine(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    public void init() {
        // Если templateEngine не установлен (не в тестах), создаем его
        if (templateEngine == null) {
            Path templatePath = Path.of("src/main/jte");
            DirectoryCodeResolver codeResolver = new DirectoryCodeResolver(templatePath);
            this.templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        LeadService leadService = (LeadService) getServletContext().getAttribute("leadService");
        List<Lead> leads = leadService.findAll();

        Map<String, Object> model = new HashMap<>();
        model.put("leads", leads);
        response.setContentType("text/html; charset=UTF-8");

        PrintWriter writer = response.getWriter();
        StringOutput output = new StringOutput();
        templateEngine.render("leads/list.jte", model, output);
        writer.write(output.toString());
    }
}