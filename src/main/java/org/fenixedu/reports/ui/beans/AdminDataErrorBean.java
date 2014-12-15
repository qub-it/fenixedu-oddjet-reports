package org.fenixedu.reports.ui.beans;

import java.io.Serializable;

public class AdminDataErrorBean implements Serializable {

    private static final long serialVersionUID = -1108186100882718887L;
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