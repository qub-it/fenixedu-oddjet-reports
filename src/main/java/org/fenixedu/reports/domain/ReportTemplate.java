package org.fenixedu.reports.domain;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.fenixedu.bennu.io.domain.GenericFile;
import org.fenixedu.bennu.io.servlets.FileDownloadServlet;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.oddjet.Template;
import org.joda.time.DateTime;

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
        return new Template(getLatestTemplateFile().getContent());
    }

    public String getDownloadUrl() {
        return FileDownloadServlet.getDownloadUrl(getLatestTemplateFile());
    }

    public List<Entry<DateTime, String>> getDatedDownloadUrls() {
        TreeMap<DateTime, String> urls = new TreeMap<DateTime, String>();
        for (GenericFile file : getTemplateFileSet()) {
            urls.put(file.getCreationDate(), FileDownloadServlet.getDownloadUrl(file));
        }
        return urls.entrySet().stream().collect(Collectors.toList());
    }

    public GenericFile getLatestTemplateFile() {
        return getTemplateFileSet().stream().sorted((f1, f2) -> f1.getCreationDate().compareTo(f2.getCreationDate())).findFirst()
                .get();
    }

    @Atomic(mode = TxMode.WRITE)
    public void delete() {
        Iterator<GenericFile> iterator = getTemplateFileSet().iterator();
        GenericFile firstFile = getTemplateFileSet().iterator().next();
        while (iterator.hasNext()) {
            GenericFile file = iterator.next();
            removeTemplateFile(file);
            file.delete();
        }
        addTemplateFile(null);
        removeTemplateFile(firstFile);
        setSystem(null);
        deleteDomainObject();
    };

}
