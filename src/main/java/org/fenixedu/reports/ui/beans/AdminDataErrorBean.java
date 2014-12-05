package org.fenixedu.reports.ui.beans;

import java.io.Serializable;

public class AdminDataErrorBean implements Serializable {

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