package org.fenixedu.reports.domain;

import org.fenixedu.bennu.io.domain.GenericFile;
import org.fenixedu.bennu.io.servlets.FileDownloadServlet;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.oddjet.Template;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

public class ReportTemplate extends ReportTemplate_Base {

    private Template template = null;

    public ReportTemplate(String key, LocalizedString name, LocalizedString description, GenericFile file) {
        setReportKey(key);
        setName(name);
        setDescription(description);
        setTemplateFile(file);
        setSystem(ReportTemplatesSystem.getInstance());
    }

    public Template getTemplate() {
        return template;
    }

    public String getDownloadUrl() {
        return FileDownloadServlet.getDownloadUrl(getTemplateFile());
    }

    @Override
    public void setTemplateFile(GenericFile templateFile) {
        super.setTemplateFile(templateFile);
        if (templateFile != null) {
            template = new Template(templateFile.getContent());
        }
    }

    @Atomic(mode = TxMode.WRITE)
    public void delete() {
        GenericFile file = getTemplateFile();
        setTemplateFile(null);
        file.delete();
        setSystem(null);
        deleteDomainObject();
    };

}
