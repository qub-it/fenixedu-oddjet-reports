package org.fenixedu.reports.ui.beans;

import java.io.Serializable;

import org.fenixedu.bennu.io.servlets.FileDownloadServlet;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.reports.domain.ReportTemplate;

public class ReportBean implements Serializable {

    //TODO compute the serial UID again...
    private static final long serialVersionUID = 1984238939829213255L;
    public String key;
    public LocalizedString name;
    public LocalizedString description;
    public String link;

    public ReportBean(ReportTemplate report) {
        key = report.getReportKey();
        name = report.getName();
        description = report.getDescription();
        link = FileDownloadServlet.getDownloadUrl(report.getTemplateFile());
    }

    public String getKey() {
        return key;
    }

    public LocalizedString getName() {
        return name;
    }

    public LocalizedString getDescription() {
        return description;
    }

    public String getLink() {
        return link;
    }

}
