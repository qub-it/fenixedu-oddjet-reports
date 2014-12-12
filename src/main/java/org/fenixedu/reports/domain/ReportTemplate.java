package org.fenixedu.reports.domain;

import java.util.Iterator;

import org.fenixedu.bennu.io.domain.GenericFile;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.oddjet.Template;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

public class ReportTemplate extends ReportTemplate_Base {

    public ReportTemplate(String key, LocalizedString name, LocalizedString description, GenericFile file) {
        setReportKey(key);
        setName(name);
        setDescription(description);
        addTemplateFile(file);
        setSystem(ReportTemplatesSystem.getInstance());
    }

    public Template getTemplate() {
        return new Template(getTemplateFile().getContent());
    }

    public GenericFile getTemplateFile() {
        return getTemplateFileSet().stream().sorted((f1, f2) -> f2.getCreationDate().compareTo(f1.getCreationDate())).findFirst()
                .get();
    }

    @Atomic(mode = TxMode.WRITE)
    public void delete() {
        Iterator<GenericFile> iterator = getTemplateFileSet().iterator();
        while (iterator.hasNext()) {
            GenericFile file = iterator.next();
            removeTemplateFile(file);
            file.delete();
        };
        setSystem(null);
        deleteDomainObject();
    };

}
