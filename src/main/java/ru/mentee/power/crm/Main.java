package ru.mentee.power.crm;

import java.io.File;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

import ru.mentee.power.crm.repository.InMemoryLeadRepository;
import ru.mentee.power.crm.repository.LeadRepository;
import ru.mentee.power.crm.service.LeadService;
import ru.mentee.power.crm.servlet.LeadListServlet;
import ru.mentee.power.crm.util.TestDataUtils;

/** Точка входа в программу */
public class Main {

    static void main() throws Exception {

        LeadRepository leadRepository = new InMemoryLeadRepository();
        LeadService leadService = new LeadService(leadRepository);

        TestDataUtils.initializeTestData(leadService);

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.getConnector();

        Context context = tomcat.addContext("", new File(".").getAbsolutePath());
        context.getServletContext().setAttribute("leadService", leadService);

        String servletName = "LeadListServlet";
        Tomcat.addServlet(context, servletName, new LeadListServlet());
        context.addServletMappingDecoded("/leads", servletName);

        tomcat.start();
        System.out.println("Tomcat started on port 8080");
        System.out.println("Open http://localhost:8080/leads in browser");

        tomcat.getServer().await();
    }
}