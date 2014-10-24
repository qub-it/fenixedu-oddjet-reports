package net.sourceforge.fenixedu.util.report;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.fenixedu.oddjet.PrintUtils;
import org.fenixedu.oddjet.Template;
import org.fenixedu.oddjet.table.CategoricalTableData;
import org.fenixedu.oddjet.table.EntryListTableData;
import org.fenixedu.oddjet.table.PositionalTableData;
import org.odftoolkit.simple.TextDocument;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Preconditions;

public class OdtReportPrinter implements ReportPrinter {

    private static final String DEFAULT_KEY = "default";
    private final Map<String, Template> reportsMap = new ConcurrentHashMap<String, Template>();
    private final Properties properties = new Properties();
    private final Template defaultReport;

    public OdtReportPrinter() {
        try {
            loadReportsProperties(properties);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load properties files.", e);
        }
        defaultReport =
                new Template(ReportsUtils.class.getClassLoader().getResourceAsStream(properties.getProperty(DEFAULT_KEY)));
    }

    public static void loadReportsProperties(final Properties properties) throws IOException {
        final Enumeration<URL> resources = ReportsUtils.class.getClassLoader().getResources("reports.properties");
        while (resources.hasMoreElements()) {
            URL reportsURL = resources.nextElement();
            final InputStream inputStream = reportsURL.openStream();
            if (inputStream != null) {
                properties.load(inputStream);
            }
        }
    }

    @Override
    public byte[] printReports(ReportDescription... reports) throws Exception {
        String reportKey, reportPath;
        InputStream reportFile;
        Template report;
        TextDocument next, master = null;

        int n = 1;
        for (ReportDescription desc : reports) {
            reportKey = desc.getKey();
            report = reportsMap.get(reportKey);

            if (report == null) {
                if ((reportPath = properties.getProperty(reportKey)) != null
                        && (reportFile = ReportsUtils.class.getClassLoader().getResourceAsStream(reportPath)) != null) {
                    report = new Template(reportFile);
                    reportsMap.put(reportKey, report);
                    fillData(report, desc.getParameters());
                } else {
                    report = defaultReport;
                    clearData(defaultReport);
                    if (reportPath == null) {
                        fillDefaultData(reportKey, "", "the provided key does not match any known report", desc.getParameters());
                    } else {
                        fillDefaultData(reportKey, reportPath, "the report file could not be found", desc.getParameters());
                    }
                }
            } else {
                clearData(report);
                fillData(report, desc.getParameters());
            }

            next = report.getInstance();

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
            result = PrintUtils.getPDFByteArray(master);
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

    private void fillData(Template report, Map<String, Object> parameters) {
        Preconditions.checkNotNull(parameters);
        Preconditions.checkNotNull(report);
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

    private void fillDefaultData(String key, String path, String error, Map<String, Object> parameters) {
        Preconditions.checkNotNull(parameters);

        defaultReport.addParameter("error", error);
        defaultReport.addParameter("reportKey", key);
        defaultReport.addParameter("reportPath", path);

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
