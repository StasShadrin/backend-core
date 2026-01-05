package ru.mentee.power.crm;

import java.io.File;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

import ru.mentee.power.crm.model.LeadStatus;
import ru.mentee.power.crm.repository.InMemoryLeadRepository;
import ru.mentee.power.crm.repository.LeadRepository;
import ru.mentee.power.crm.service.LeadService;
import ru.mentee.power.crm.servlet.LeadListServlet;

/** Точка входа в программу */
public class Main {

    static void main() throws Exception {

        LeadRepository leadRepository = new InMemoryLeadRepository();
        LeadService leadService = new LeadService(leadRepository);

        leadService.addLead("bob1@example.com", "+123", "Google", LeadStatus.NEW);
        leadService.addLead("bob2@example.com", "+456", "Meta", LeadStatus.CONTACTED);
        leadService.addLead("alice@example.com", "+789", "Apple", LeadStatus.QUALIFIED);
        leadService.addLead("john@example.com", "+111", "Microsoft", LeadStatus.NEW);
        leadService.addLead("sara@example.com", "+222", "Amazon", LeadStatus.CONTACTED);

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