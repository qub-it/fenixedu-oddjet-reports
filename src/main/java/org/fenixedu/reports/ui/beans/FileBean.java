package org.fenixedu.reports.ui.beans;

import java.io.Serializable;
import java.text.DecimalFormat;

import org.fenixedu.bennu.io.domain.GenericFile;
import org.fenixedu.bennu.io.servlets.FileDownloadServlet;

public class FileBean implements Serializable {

    private static final long serialVersionUID = -5975971883863953123L;
    public String name;
    public String date;
    public String size;
    public String link;

    public FileBean(GenericFile file) {
        name = file.getFilename();
        date = file.getCreationDate().toString("dd/MM/yyyy");
        size = readableFileSize(file.getSize());
        link = FileDownloadServlet.getDownloadUrl(file);
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getSize() {
        return size;
    }

    public String getLink() {
        return link;
    }

    //Taken and adapted from Mr Ed's solution @ http://stackoverflow.com/questions/3263892/format-file-size-as-mb-gb-etc
    private static String readableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB", "PB", "EB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1000));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1000, digitGroups)) + " " + units[digitGroups];
    }

};