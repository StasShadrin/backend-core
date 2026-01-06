package ru.mentee.power.crm.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import gg.jte.TemplateEngine;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.model.LeadStatus;
import ru.mentee.power.crm.service.LeadService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeadListServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ServletContext servletContext;

    @Mock
    private ServletConfig servletConfig;

    @Mock
    private LeadService leadService;

    @Mock
    private TemplateEngine templateEngine;

    private LeadListServlet servlet;

    @BeforeEach
    void setUp() throws ServletException, NoSuchFieldException, IllegalAccessException {
        servlet = new LeadListServlet();

        when(servletConfig.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute("leadService")).thenReturn(leadService);

        java.lang.reflect.Field field = LeadListServlet.class.getDeclaredField("templateEngine");
        field.setAccessible(true);
        field.set(servlet, templateEngine);

        servlet.init(servletConfig);
    }

    @Test
    void shouldReturnHtmlTable_whenDoGetCalled() throws IOException {
        List<Lead> mockLeads = Arrays.asList(
                new Lead(UUID.randomUUID(),
                        "test1@example.com",
                        "+123",
                        "Company A",
                        LeadStatus.NEW),
                new Lead(UUID.randomUUID(),
                        "test2@example.com",
                        "+456",
                        "Company B",
                        LeadStatus.CONTACTED)
        );

        when(leadService.findAll()).thenReturn(mockLeads);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        doAnswer(invocation -> {
            gg.jte.TemplateOutput output = invocation.getArgument(2);
            output.writeContent("<!DOCTYPE html>");
            output.writeContent("<html><body>");
            output.writeContent("<table>");
            output.writeContent("<th>Email</th>");
            output.writeContent("<th>Phone</th>");
            output.writeContent("<th>Company</th>");
            output.writeContent("<th>Status</th>");
            for (Lead lead : mockLeads) {
                output.writeContent("<tr>");
                output.writeContent("<td>" + lead.email() + "</td>");
                output.writeContent("<td>" + lead.phone() + "</td>");
                output.writeContent("<td>" + lead.company() + "</td>");
                output.writeContent("<td>" + lead.status() + "</td>");
                output.writeContent("</tr>");
            }
            output.writeContent("</table>");
            output.writeContent("</body></html>");
            return null;
        }).when(templateEngine).render(
                eq("leads/list.jte"),
                any(),
                any()
        );


        servlet.doGet(request, response);


        verify(leadService, times(1)).findAll();
        verify(templateEngine, times(1)).render(eq("leads/list.jte"), any(), any());

        String htmlOutput = stringWriter.toString();
        printWriter.flush();

        for (String expected : Arrays.asList(
                "<!DOCTYPE html>",
                "<table>",
                "<th>Email</th>",
                "<th>Phone</th>",
                "<th>Company</th>",
                "<th>Status</th>",
                "test1@example.com",
                "test2@example.com",
                "Company A",
                "Company B")) {
            assertThat(htmlOutput).contains(expected);
        }

        verify(response, atLeastOnce()).getWriter();
        verify(response, times(1)).setContentType("text/html; charset=UTF-8");
    }

    @Test
    void shouldSetContentTypeToHtml_whenDoGetCalled() throws IOException {
        List<Lead> mockLeads = List.of(
                new Lead(UUID.randomUUID(), "test@example.com", "+123", "Company", LeadStatus.NEW)
        );

        when(leadService.findAll()).thenReturn(mockLeads);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        doAnswer(invocation -> {
            gg.jte.TemplateOutput output = invocation.getArgument(2);
            output.writeContent("<html><body>Test</body></html>");
            return null;
        }).when(templateEngine).render(eq("leads/list.jte"), any(), any());

        servlet.doGet(request, response);

        verify(response, times(1)).setContentType("text/html; charset=UTF-8");
    }
}