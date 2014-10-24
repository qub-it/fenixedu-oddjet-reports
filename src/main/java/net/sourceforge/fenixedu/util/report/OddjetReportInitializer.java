package net.sourceforge.fenixedu.util.report;


import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class OddjetReportInitializer implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ReportsUtils.setPrinter(new OdtReportPrinter());
    }
 
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
} 