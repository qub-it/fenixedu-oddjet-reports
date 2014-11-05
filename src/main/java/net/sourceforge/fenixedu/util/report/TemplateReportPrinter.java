package net.sourceforge.fenixedu.util.report;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.fenixedu.oddjet.Template;
import org.fenixedu.oddjet.table.CategoricalTableData;
import org.fenixedu.oddjet.table.EntryListTableData;
import org.fenixedu.oddjet.table.PositionalTableData;
import org.fenixedu.oddjet.utils.PrintUtils;
import org.fenixedu.reports.domain.ReportTemplate;
import org.fenixedu.reports.domain.ReportTemplatesSystem;
import org.odftoolkit.simple.TextDocument;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Preconditions;

public class TemplateReportPrinter implements ReportPrinter {

    private Template defaultReport;

    public TemplateReportPrinter() {
        InputStream fileStream = ReportsUtils.class.getClassLoader().getResourceAsStream("reports/default.odt");
        if (fileStream == null) {
            throw new RuntimeException("Default odt report is missing.");
        }
        defaultReport = new Template(fileStream);
    }

    @Override
    public byte[] printReports(ReportDescription... reports) throws Exception {
        ReportTemplate report;
        Template template;
        TextDocument next, master = null;

        int n = 1;
        for (ReportDescription desc : reports) {
            report = ReportTemplatesSystem.getInstance().getReportTemplate(desc.getKey());

            if (report == null) {
                template = defaultReport;
                prepareDefaultReport(desc.getKey(), "the provided key does not match any known report", desc.getParameters());
            } else {
                template = report.getTemplate();
                prepareReport(template, desc.getParameters());
            }

            next = template.getInstance();

            if (master == null) {
                master = next;
            } else {

                //package variable names to avoid interference
                String prefix = "nr" + n;
                n++;
                packageNames(next, prefix, "text:user-field-decl");
                packageNames(next, prefix, "text:user-field-get");
                packageNames(next, prefix, "text:user-field-input");
                packageNames(next, prefix, "text:variable-decl");
                packageNames(next, prefix, "text:variable-set");
                packageNames(next, prefix, "text:variable-get");
                packageNames(next, prefix, "text:variable-input");
                packageNames(next, prefix, "text:sequence-decl");
                packageNames(next, prefix, "text:sequence");
                //styles and tables f conflicting appear to be renamed with an added random sequence so no packaging required there

                master.addPageBreak();
                master.insertContentFromDocumentAfter(next, master.getParagraphByReverseIndex(0, false), true);

                //merge all declarations into one element so libreoffice reads them all
                mergeDeclarations(master, "text:user-field-decls");
                mergeDeclarations(master, "text:sequence-decls");
                mergeDeclarations(master, "text:variable-decls");

            }
        }

        byte[] result = null;
        if (master != null) {
            result = PrintUtils.print(master, ReportTemplatesSystem.getInstance().getPrintingService());
            if (result == null) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                master.save(out);
                result = out.toByteArray();
            }
        }
        return result;
    }

    private void mergeDeclarations(TextDocument master, String name) {
        try {
            Set<String> conflictDetector = new HashSet<String>();
            NodeList containers = master.getContentRoot().getElementsByTagName(name);
            Node first = null;
            if (containers.getLength() > 0) {
                first = containers.item(0);
                Node declaration = first.getFirstChild();
                while (declaration != null) {
                    conflictDetector.add(declaration.getAttributes().getNamedItem("text:name").getNodeValue());
                    declaration = declaration.getNextSibling();
                }
                for (int i = 1; i < containers.getLength(); i++) {
                    Node current = containers.item(i);
                    declaration = current.getFirstChild();
                    while (declaration != null) {
                        Node next = declaration.getNextSibling();
                        if (conflictDetector.add(declaration.getAttributes().getNamedItem("text:name").getNodeValue())) {
                            first.appendChild(declaration);
                        };
                        declaration = next;
                    }
                    current.getParentNode().removeChild(current);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void packageNames(TextDocument next, String prefix, String type) {
        try {
            NodeList nodes = next.getContentRoot().getElementsByTagName(type);
            Node target;
            for (int i = 0; i < nodes.getLength(); i++) {
                target = nodes.item(i).getAttributes().getNamedItem("text:name");
                target.setNodeValue(prefix + target.getNodeValue());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void prepareReport(Template report, Map<String, Object> parameters) {
        Preconditions.checkNotNull(parameters);
        Preconditions.checkNotNull(report);
        clearData(report);
        //Map<String,List> objects get converted to CategoricalTableData, Iterable<Iterable> and object[][] to PositionalTableData
        //and other Iterable objects to EntryListTableData. All others are taken as simple parameters.
        for (Entry<String, Object> parameter : parameters.entrySet()) {
            if (parameter.getValue() instanceof Map) {
                try {
                    report.addTableDataSource(parameter.getKey(),
                            new CategoricalTableData((Map<String, List>) parameter.getValue()));
                } catch (ClassCastException e) {
                    report.addTableDataSource(parameter.getKey(), new EntryListTableData((Iterable) parameter.getValue()));
                }
            } else if (parameter.getValue() instanceof Iterable) {
                try {
                    report.addTableDataSource(parameter.getKey(),
                            new PositionalTableData((Iterable<Iterable>) parameter.getValue()));
                } catch (ClassCastException e) {
                    report.addTableDataSource(parameter.getKey(), new EntryListTableData((Iterable) parameter.getValue()));
                }
            } else if (parameter.getValue() instanceof Object[][]) {
                report.addTableDataSource(parameter.getKey(), new PositionalTableData((Object[][]) parameter.getValue()));
            } else {
                report.addParameter(parameter.getKey(), parameter.getValue());
            }
        }
    }

    private void prepareDefaultReport(String key, String error, Map<String, Object> parameters) {
        Preconditions.checkNotNull(parameters);

        clearData(defaultReport);

        defaultReport.addParameter("error", error);
        defaultReport.addParameter("reportKey", key);

        Map<String, List> data = new HashMap<String, List>();
        List<String> keys = new ArrayList<String>();
        List<String> types = new ArrayList<String>();
        List<String> contents = new ArrayList<String>();
        data.put("key", keys);
        data.put("type", types);
        data.put("content", contents);

        for (Entry<String, Object> parameter : parameters.entrySet()) {
            keys.add(parameter.getKey());
            types.add(parameter.getValue().getClass().getSimpleName());
            contents.add(parameter.getValue().toString());
        }

        defaultReport.addTableDataSource("reportData", new CategoricalTableData(data));

    }

    private void clearData(Template report) {
        report.clearParameters();
        report.clearTableDataSources();
    }
}
