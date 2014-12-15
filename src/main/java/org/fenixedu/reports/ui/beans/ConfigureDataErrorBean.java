package org.fenixedu.reports.ui.beans;

import java.io.Serializable;

public class ConfigureDataErrorBean implements Serializable {

    private static final long serialVersionUID = -65980313033434823L;
    public String onHost;
    public String onPort;
    public String onOutputFormat;
    public String onConnection;

    public String getOnHost() {
        return onHost;
    }

    public String getOnPort() {
        return onPort;
    }

    public String getOnOutputFormat() {
        return onOutputFormat;
    }

    public String getOnConnection() {
        return onConnection;
    }

    public boolean isEmpty() {
        return onHost == null && onPort == null && onOutputFormat == null && onConnection == null;
    }
}
