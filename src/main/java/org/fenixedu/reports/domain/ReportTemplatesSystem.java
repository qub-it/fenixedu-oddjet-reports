package org.fenixedu.reports.domain;

import java.util.Iterator;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.oddjet.utils.OpenOfficePrintingService;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

public class ReportTemplatesSystem extends ReportTemplatesSystem_Base {

    private static final String PDF_FORMAT = "pdf";

    private ReportTemplatesSystem() {
        super();
        setUseService(false);
        setServiceHost("localhost");
        setServicePort(8100);
        setBennu(Bennu.getInstance());
    }

    public static ReportTemplatesSystem getInstance() {
        if (Bennu.getInstance().getReportTemplatesSystem() == null) {
            return initialize();
        }
        return Bennu.getInstance().getReportTemplatesSystem();
    }

    @Atomic(mode = TxMode.WRITE)
    private static ReportTemplatesSystem initialize() {
        if (Bennu.getInstance().getReportTemplatesSystem() == null) {
            return new ReportTemplatesSystem();
        }
        return Bennu.getInstance().getReportTemplatesSystem();
    }

    public OpenOfficePrintingService getPrintingService() {
        if (getUseService()) {
            try {
                return new OpenOfficePrintingService(getServiceHost(), getServicePort(), PDF_FORMAT);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public boolean hasValidService() {
        return OpenOfficePrintingService.isValidService(getServiceHost(), getServicePort());
    }

    public ReportTemplate getReportTemplate(String key) {
        Iterator<ReportTemplate> rtIt = getReportTemplatesSet().iterator();
        ReportTemplate rt;
        while (rtIt.hasNext()) {
            rt = rtIt.next();
            if (key.equals(rt.getReportKey())) {
                return rt;
            }
        }
        return null;
    }

}
