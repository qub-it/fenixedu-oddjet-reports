<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="taskType" scope="session" value="${f:endsWith(requestScope['javax.servlet.forward.request_uri'], '/edit') ? 'edit' : 'add'}"/>

<h1>
	<spring:message code="pages.${taskType}.title"/>
</h1>

<c:if test="${successful}">
	<div class="alert alert-info">
		<spring:message code="pages.${taskType}.success"/>
	</div>
</c:if>

<form class="form-horizontal" enctype="multipart/form-data" action="" method="post" role="form">
    <div class="form-group ${errors.onKey != null ? 'has-error' : ''}">
    	<label for="ReportKey" class="col-sm-2 control-label"><spring:message code="pages.edit.label.key"/>:</label>
    	<div class="col-sm-10">
      		<input required type="text" name="key" id="ReportKey" placeholder="<spring:message code="pages.edit.label.key"/>..." value="${reportKey}">
      		<c:if test="${errors.onKey != null}"><p class="text-danger"><spring:message code="${errors.onKey}"/></p></c:if>
    	</div>
  	</div>
  	
  	<div class="form-group ${errors.onName != null ? 'has-error' : ''}">
    	<label for="ReportName" class="col-sm-2 control-label"><spring:message code="pages.edit.label.name"/>:</label>
    	<div class="col-sm-10">
      		<input bennu-localized-string required-any name="name" id="ReportName" placeholder="<spring:message code="pages.edit.label.name"/>..." value='${reportName}' />
      		<c:if test="${errors.onName != null}"><p class="text-danger"><spring:message code="${errors.onName}"/></p></c:if>
    	</div>
  	</div>

    <div class="form-group ${errors.onDescription != null ? 'has-error' : ''}">
    	<label for="ReportDescription" class="col-sm-2 control-label"><spring:message code="pages.edit.label.description"/>:</label>
    	<div class="col-sm-10">
      		<textarea bennu-localized-string required-any name="description" id="ReportDescription" placeholder="<spring:message code="pages.edit.label.description"/>...">${reportDescription}</textarea>
      		<c:if test="${errors.onDescription != null}"><p class="text-danger"><spring:message code="${errors.onDescription}"/></p></c:if>
    	</div>
  	</div>
  	
    <div class="form-group ${errors.onFile != null ? 'has-error' : ''}">
        <label for="ReportTemplateFile" class="col-sm-2 control-label"><spring:message code="pages.edit.label.file"/>:</label>
        <div class="col-sm-10">
            <input type="file" name="file" class="form-control" id="ReportTemplateFile" placeholder="<spring:message code="pages.edit.label.file"/>...">
            <c:if test="${errors.onFile != null}"><p class="text-danger"><spring:message code="${errors.onFile}"/></p></c:if>
        	<c:if test="${templateUrl != null}"><a href="${templateUrl}" class="btn btn-sm btn-default"><spring:message code="action.download.current"/></a></c:if>
        </div>
    </div>

    <div class="form-group">
        <div class="col-sm-offset-2 col-sm-10">
            <button type="submit" class="btn btn-default btn-primary">
				<spring:message code="action.${taskType}"/>
			</button>
        </div>
    </div>
</form>

${portal.toolkit()}