package org.fenixedu.reports.ui;

import java.io.Serializable;

import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.oddjet.utils.OpenOfficePrintingService;
import org.fenixedu.reports.domain.ReportTemplatesSystem;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

@SpringFunctionality(app = AdminReportTemplates.class, title = "application.configure.title", accessGroup = "#managers")
@RequestMapping("/reports/configure")
public class ConfigureReportTemplatesSystem {

    @RequestMapping(method = RequestMethod.GET)
    public String configure(Model model) {
        ReportTemplatesSystem system = ReportTemplatesSystem.getInstance();
        model.addAttribute("use", system.getUseService());
        model.addAttribute("host", system.getServiceHost());
        model.addAttribute("port", system.getServicePort());
        model.addAttribute("format", system.getOutputFormat());
        model.addAttribute("formats", ReportTemplatesSystem.formats);
        return "odt-reports/configure";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String configure(Model model, @RequestParam(required = false, defaultValue = "false") Boolean use,
            @RequestParam String host, @RequestParam Integer port, @RequestParam String format) {

        DataErrorBean errors = new DataErrorBean();

        if (host == null || host.isEmpty()) {
            errors.onHost = "pages.configure.error.host.empty";
        }
        if (port < 1 || port > 65535) {
            errors.onPort = "pages.configure.error.port.invalid";
        }
        if (format == null || format.isEmpty()) {
            errors.onOutputFormat = "pages.configure.error.format.empty";
        } else if (!ReportTemplatesSystem.IsValidFormat(format)) {
            errors.onOutputFormat = "pages.configure.error.format.unknown";
        }
        if (use && !OpenOfficePrintingService.isValidService(host, port)) {
            errors.onConnection = "pages.configure.error.connection";
        }

        if (errors.isEmpty()) {
            configure(use, host, port, format);
            model.addAttribute("successful", true);
        } else {
            model.addAttribute("errors", errors);
        }
        model.addAttribute("use", use);
        model.addAttribute("host", host);
        model.addAttribute("port", port);
        model.addAttribute("format", format);
        model.addAttribute("formats", ReportTemplatesSystem.formats);
        return "odt-reports/configure";
    }

    @Atomic(mode = TxMode.WRITE)
    public void configure(Boolean use, String host, int port, String format) {
        ReportTemplatesSystem system = ReportTemplatesSystem.getInstance();
        system.setUseService(use);
        system.setServiceHost(host);
        system.setServicePort(port);
        system.setOutputFormat(format);
    }

    public class DataErrorBean implements Serializable {

        //TODO compute the serial UID again...
        private static final long serialVersionUID = 1798742222223374397L;
        public String onHost;
        public String onPort;
        public String onOutputFormat;
        public String onConnection;

        public String getOnHost() {
            return onHost;
        }

        public String getOnPort() {
            return onPort;
        }

        public String getOnOutputFormat() {
            return onOutputFormat;
        }

        public String getOnConnection() {
            return onConnection;
        }

        public boolean isEmpty() {
            return onHost == null && onPort == null && onOutputFormat == null && onConnection == null;
        }
    };
}
