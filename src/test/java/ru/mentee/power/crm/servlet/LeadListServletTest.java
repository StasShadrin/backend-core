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

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.model.LeadStatus;
import ru.mentee.power.crm.service.LeadService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
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

    private LeadListServlet servlet;

    @BeforeEach
    void setUp() throws ServletException {
        servlet = new LeadListServlet();

        when(servletConfig.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute("leadService")).thenReturn(leadService);

        servlet.init(servletConfig);
    }

    @Test
    void shouldReturnHtmlTable_whenDoGetCalled() throws ServletException, IOException {
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

        servlet.doGet(request, response);

        verify(leadService, times(1)).findAll();

        String htmlOutput = stringWriter.toString();
        printWriter.flush();

        for (String s : Arrays.asList(
                "<!DOCTYPE html>",
                "<table",
                "<th>Email</th>",
                "<th>Phone</th>",
                "<th>Company</th>",
                "<th>Status</th>",
                "test1@example.com",
                "test2@example.com",
                "Company A",
                "Company B")) {
            assertThat(htmlOutput).contains(s);
        }

        verify(response, atLeastOnce()).getWriter();
    }

    @Test
    void shouldSetContentTypeToHtml_whenDoGetCalled() throws ServletException, IOException {
        List<Lead> mockLeads = List.of(
                new Lead(UUID.randomUUID(),
                        "test@example.com",
                        "+123",
                        "Company",
                        LeadStatus.NEW)
        );

        when(leadService.findAll()).thenReturn(mockLeads);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        servlet.doGet(request, response);

        verify(response, times(1)).setContentType("text/html; charset=UTF-8");
    }
}
