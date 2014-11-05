package org.fenixedu.reports.domain;

import java.util.Arrays;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.oddjet.utils.OpenOfficePrintingService;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

public class ReportTemplatesSystem extends ReportTemplatesSystem_Base {

    public static String[] formats = {
            //XXX could not extract formats programmatically from JODConverter
            "PDF", "RTF", "TXT", "WIKI", "HTML", "ODT", "SXW", "DOC" };

    private ReportTemplatesSystem() {
        super();
        setUseService(false);
        setServiceHost("localhost");
        setServicePort(8100);
        setOutputFormat("pdf");
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
        try {
            return new OpenOfficePrintingService(getServiceHost(), getServicePort(), getOutputFormat());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean getUseService() {
        return super.getUseService() && IsValidFormat(getOutputFormat())
                && OpenOfficePrintingService.isValidService(getServiceHost(), getServicePort());
    }

    public static boolean IsValidFormat(String format) {
        return Arrays.stream(ReportTemplatesSystem.formats).anyMatch(f -> f.equals(format));
    }

    public ReportTemplate getReportTemplate(String key) {
        return getReportTemplatesSet().stream().filter(r -> key.equals(r.getReportKey())).findAny().orElse(null);
    }

}
