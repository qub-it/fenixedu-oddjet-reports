package org.fenixedu.reports.ui;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.tika.Tika;
import org.fenixedu.bennu.core.groups.DynamicGroup;
import org.fenixedu.bennu.io.domain.GenericFile;
import org.fenixedu.bennu.io.domain.GroupBasedFile;
import org.fenixedu.bennu.spring.portal.SpringApplication;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.reports.domain.ReportTemplate;
import org.fenixedu.reports.domain.ReportTemplatesSystem;
import org.fenixedu.reports.exceptions.ReportsDomainException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

import com.google.common.collect.Lists;

@SpringApplication(group = "logged", path = "reports", title = "application.title")
@SpringFunctionality(app = AdminReportTemplates.class, title = "application.manage.title", accessGroup = "#managers")
@RequestMapping("/reports/templates")
public class AdminReportTemplates {

    private static final int ITEMS_PER_PAGE = 30;

    @RequestMapping
    public String list(Model model) {
        return list(0, model);
    }

    @RequestMapping(value = "/{page}", method = RequestMethod.GET)
    public String list(@PathVariable("page") Integer page, Model model) {
        List<List<ReportTemplate>> pages = Lists.partition(getReportTemplates(), ITEMS_PER_PAGE);
        if (!pages.isEmpty()) {
            int currentPage = Optional.of(page).orElse(0);
            model.addAttribute("numberOfPages", pages.size());
            model.addAttribute("currentPage", currentPage);
            model.addAttribute("reports", pages.get(currentPage));
        }
        return "odt-reports/manage";
    }

    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String add(Model model) {
        return "odt-reports/edit";
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String add(Model model, @RequestParam String key, @RequestParam LocalizedString name,
            @RequestParam LocalizedString description, @RequestParam MultipartFile file) {

        byte[] fileContent = null;
        DataErrorBean errors = new DataErrorBean();
        if (key == null || key.isEmpty()) {
            errors.onKey = "pages.edit.error.key.empty";
        } else if (ReportTemplatesSystem.getInstance().getReportTemplate(key) != null) {
            errors.onKey = "pages.edit.error.key.duplicate";
        }
        if (name == null || name.getContent().isEmpty()) {
            errors.onName = "pages.edit.error.name.empty";
        }
        if (description == null || description.getContent().isEmpty()) {
            errors.onDescription = "pages.edit.error.description.empty";
        }
        if (file == null || file.isEmpty()) {
            errors.onFile = "pages.edit.error.file.undefined";
        } else {
            try {
                fileContent = file.getBytes();
                String filetype = new Tika().detect(fileContent, file.getName());
                if (!filetype.equals("application/vnd.oasis.opendocument.text")) {
                    errors.onFile = "pages.edit.error.file.type";
                }
            } catch (IOException e) {
                errors.onFile = "pages.edit.error.file.read";
            }
        }

        if (errors.isEmpty()) {
            editReportTemplate(null, key, name, description, fileContent);
            model.addAttribute("successful", true);
        } else {
            model.addAttribute("errors", errors);
        }
        model.addAttribute("reportDescription", description.json());
        model.addAttribute("reportName", name.json());
        model.addAttribute("reportKey", key);
        return "odt-reports/edit";
    }

    @RequestMapping(value = "/{key}/edit", method = RequestMethod.GET)
    public String edit(@PathVariable("key") String key, Model model) {
        ReportTemplate report = ReportTemplatesSystem.getInstance().getReportTemplate(key);
        if (report != null) {
            model.addAttribute("reportDescription", report.getDescription().json());
            model.addAttribute("reportName", report.getName().json());
            model.addAttribute("reportKey", key);
            return "odt-reports/edit";
        } else {
            throw ReportsDomainException.keyNotFound();
        }
    }

    @RequestMapping(value = "/{key}/edit", method = RequestMethod.POST)
    public String edit(@PathVariable("key") String oldKey, Model model, @RequestParam String key,
            @RequestParam LocalizedString name, @RequestParam LocalizedString description, @RequestParam MultipartFile file) {
        ReportTemplate report;
        if (oldKey == null || oldKey.isEmpty()
                || (report = ReportTemplatesSystem.getInstance().getReportTemplate(oldKey)) == null) {
            throw ReportsDomainException.keyNotFound();
        }

        DataErrorBean errors = new DataErrorBean();
        if (key == null || key.isEmpty()) {
            errors.onKey = "pages.edit.error.key.empty";
        }
        if (name == null || name.getContent().isEmpty()) {
            errors.onName = "pages.edit.error.name.empty";
        }
        if (description == null || description.getContent().isEmpty()) {
            errors.onDescription = "pages.edit.error.description.empty";
        }

        byte[] fileContent = null;
        if (file != null && !file.isEmpty()) {
            try {
                fileContent = file.getBytes();
                String filetype = new Tika().detect(fileContent, file.getName());
                if (!filetype.equals("application/vnd.oasis.opendocument.text")) {
                    errors.onFile = "pages.edit.error.file.type";
                }
            } catch (IOException e) {
                errors.onFile = "pages.edit.error.file.read";
            }
        }

        if (errors.isEmpty()) {
            editReportTemplate(report, key, name, description, fileContent);
            model.addAttribute("successful", true);
        } else {
            model.addAttribute("errors", errors);
        }
        model.addAttribute("reportDescription", description.json());
        model.addAttribute("reportName", name.json());
        model.addAttribute("reportKey", key);
        return "odt-reports/edit";
    }

    @RequestMapping(value = "/{key}/delete")
    public RedirectView delete(@PathVariable("key") String key, Model model) {
        ReportTemplate report;
        if (key != null && (report = ReportTemplatesSystem.getInstance().getReportTemplate(key)) != null) {
            report.delete();
        } else {
            throw ReportsDomainException.keyNotFound();
        }
        return new RedirectView("/reports/templates", true);
    }

    public class DataErrorBean implements Serializable {

        //TODO compute the serial UID again...
        private static final long serialVersionUID = 1798743927098274397L;
        public String onKey;
        public String onName;
        public String onDescription;
        public String onFile;

        public String getOnKey() {
            return onKey;
        }

        public String getOnName() {
            return onName;
        }

        public String getOnDescription() {
            return onDescription;
        }

        public String getOnFile() {
            return onFile;
        }

        public boolean isEmpty() {
            return onKey == null && onName == null && onDescription == null && onFile == null;
        }
    };

    private List<ReportTemplate> getReportTemplates() {
        return ReportTemplatesSystem.getInstance().getReportTemplatesSet().stream().collect(Collectors.toList());
    }

    @Atomic(mode = TxMode.WRITE)
    private void editReportTemplate(ReportTemplate report, String key, LocalizedString name, LocalizedString description,
            byte[] fileContent) {
        if (report != null) {
            report.setReportKey(key);
            report.setName(name);
            report.setDescription(description);
            if (fileContent != null) {
                GenericFile cur = report.getTemplateFile();
                report.setTemplateFile(new GroupBasedFile(name.getContent(), key, fileContent, DynamicGroup.get("managers")));
                cur.delete();
            }
        } else {
            new ReportTemplate(key, name, description, new GroupBasedFile(name.getContent(), key, fileContent,
                    DynamicGroup.get("managers")));
        }
    }
}
