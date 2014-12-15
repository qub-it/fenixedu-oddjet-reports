package org.fenixedu.reports.ui;

import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.oddjet.utils.OpenOfficePrintingService;
import org.fenixedu.reports.domain.ReportTemplatesSystem;
import org.fenixedu.reports.ui.beans.ConfigureDataErrorBean;
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
        model.addAttribute("status", system.getUseService() ? system.hasValidService() ? "success" : "warning" : "default");
        model.addAttribute("host", system.getServiceHost());
        model.addAttribute("port", system.getServicePort());
        return "odt-reports/configure";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String configure(Model model, @RequestParam(required = false, defaultValue = "false") Boolean use,
            @RequestParam String host, @RequestParam Integer port) {

        ConfigureDataErrorBean errors = new ConfigureDataErrorBean();

        if (host == null || host.isEmpty()) {
            errors.onHost = "pages.configure.error.host.empty";
        }
        if (port < 1 || port > 65535) {
            errors.onPort = "pages.configure.error.port.invalid";
        }
        if (use && !OpenOfficePrintingService.isValidService(host, port)) {
            errors.onConnection = "pages.configure.error.connection";
        }

        if (errors.isEmpty()) {
            configure(use, host, port);
            model.addAttribute("successful", true);
        } else {
            model.addAttribute("errors", errors);
        }
        model.addAttribute("use", use);
        model.addAttribute("status",
                use ? ReportTemplatesSystem.getInstance().hasValidService() ? "success" : "warning" : "default");
        model.addAttribute("host", host);
        model.addAttribute("port", port);
        return "odt-reports/configure";
    }

    @Atomic(mode = TxMode.WRITE)
    public void configure(Boolean use, String host, int port) {
        ReportTemplatesSystem system = ReportTemplatesSystem.getInstance();
        system.setUseService(use);
        system.setServiceHost(host);
        system.setServicePort(port);
    }

}
