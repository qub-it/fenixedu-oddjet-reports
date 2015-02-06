package org.fenixedu.reports.ui;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.tika.Tika;
import org.fenixedu.bennu.core.groups.DynamicGroup;
import org.fenixedu.bennu.io.domain.GroupBasedFile;
import org.fenixedu.bennu.spring.portal.SpringApplication;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.reports.domain.ReportTemplate;
import org.fenixedu.reports.domain.ReportTemplatesSystem;
import org.fenixedu.reports.exceptions.ReportsDomainException;
import org.fenixedu.reports.ui.beans.AdminDataErrorBean;
import org.fenixedu.reports.ui.beans.FileBean;
import org.fenixedu.reports.ui.beans.ReportBean;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

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
        List<List<ReportBean>> pages = Lists.partition(getReportTemplateBeans(), ITEMS_PER_PAGE);
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
        AdminDataErrorBean errors = new AdminDataErrorBean();
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
            editReportTemplate(null, key, name, file.getOriginalFilename(), description, fileContent);
            model.addAttribute("success", "pages.add.success");
            return list(model);
        }
        model.addAttribute("errors", errors);
        model.addAttribute("reportDescription", description.json());
        model.addAttribute("reportName", name.json());
        model.addAttribute("reportKey", key);
        return "odt-reports/edit";
    }

    @RequestMapping(value = "/{key}/edit", method = RequestMethod.GET)
    public String edit(@PathVariable("key") String key, Model model) {
        ReportTemplate report = ReportTemplatesSystem.getInstance().getReportTemplate(key);
        if (report != null) {
            model.addAttribute("reportPreviousFiles", getPreviousFileBeans(report));
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

        AdminDataErrorBean errors = new AdminDataErrorBean();
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
            editReportTemplate(report, key, name, file.getOriginalFilename(), description, fileContent);
            model.addAttribute("success", "pages.edit.success");
            return list(model);
        }
        model.addAttribute("errors", errors);
        model.addAttribute("reportPreviousFiles", getPreviousFileBeans(report));
        model.addAttribute("reportDescription", description.json());
        model.addAttribute("reportName", name.json());
        model.addAttribute("reportKey", key);
        return "odt-reports/edit";
    }

    @RequestMapping(value = "/{key}/delete")
    public String delete(@PathVariable("key") String key, Model model) {
        ReportTemplate report;
        if (key != null && (report = ReportTemplatesSystem.getInstance().getReportTemplate(key)) != null) {
            report.delete();
            model.addAttribute("success", "pages.edit.delete.success");
            return list(model);
        } else {
            throw ReportsDomainException.keyNotFound();
        }
    }

    private List<FileBean> getPreviousFileBeans(ReportTemplate report) {
        return report.getTemplateFileSet().stream().sorted((f1, f2) -> f2.getCreationDate().compareTo(f1.getCreationDate()))
                .map(file -> new FileBean(file)).collect(Collectors.toList());
    }

    private List<ReportBean> getReportTemplateBeans() {
        return ReportTemplatesSystem.getInstance().getReportTemplatesSet().stream().map(report -> new ReportBean(report))
                .collect(Collectors.toList());
    }

    @Atomic(mode = TxMode.WRITE)
    private ReportTemplate editReportTemplate(ReportTemplate report, String key, LocalizedString name, String filename,
            LocalizedString description, byte[] fileContent) {
        if (report != null) {
            report.setReportKey(key);
            report.setName(name);
            report.setDescription(description);
            if (fileContent != null) {
                report.addTemplateFile(new GroupBasedFile(name.getContent(), filename, fileContent, DynamicGroup.get("managers")));
            }
        } else {
            report =
                    new ReportTemplate(key, name, description, new GroupBasedFile(name.getContent(), filename, fileContent,
                            DynamicGroup.get("managers")));
        }
        return report;
    }
}
