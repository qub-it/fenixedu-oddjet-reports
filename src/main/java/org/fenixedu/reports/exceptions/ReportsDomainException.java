package org.fenixedu.reports.exceptions;

import javax.ws.rs.core.Response;

import org.fenixedu.bennu.core.domain.exceptions.DomainException;

public class ReportsDomainException extends DomainException {

    protected static final String BUNDLE = "OdtReportsResources";

    protected ReportsDomainException(Response.Status status, String bundle, String key, String... args) {
        super(status, bundle, key, args);
    }

    public static ReportsDomainException keyNotFound() {
        return new ReportsDomainException(Response.Status.NOT_FOUND, BUNDLE, "exception.key.not.found");
    }

    public static ReportsDomainException printMultipleOdts() {
        return new ReportsDomainException(Response.Status.BAD_REQUEST, BUNDLE, "exception.print.multiple.odts");
    }
}
