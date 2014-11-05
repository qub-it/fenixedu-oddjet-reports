<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<h1>
	<spring:message code="pages.configure.title"/>
</h1>

<c:if test="${successful}">
	<div class="alert alert-info">
		<spring:message code="pages.configure.success"/>
	</div>
</c:if>
<c:if test="${errors.onConnection != null}">
	<div class="alert alert-danger">
		<spring:message code="${errors.onConnection}"/>
	</div>
</c:if>

<form class="form-horizontal" enctype="multipart/form-data" action="" method="post" role="form">
	<div class="form-group">
    	<label for="UseService" class="col-sm-2 control-label"><spring:message code="pages.configure.label.use"/>:</label>
    	<div class="col-sm-6">
      		<input type="checkbox" name="use" id="UseService" ${use ? 'checked' : ''} value="true">
    	</div>
  	</div>

    <div class="form-group ${errors.onHost != null ? 'has-error' : ''}">
    	<label for="ServiceHost" class="col-sm-2 control-label"><spring:message code="pages.configure.label.host"/>:</label>
    	<div class="col-sm-3">
      		<input class="form-control service-dependent-ro" required type="text" name="host" id="ServiceHost" placeholder="<spring:message code="pages.configure.label.host"/>..." value="${host}">
      		<c:if test="${errors.onHost != null}"><p class="text-danger"><spring:message code="${errors.onHost}"/></p></c:if>
    	</div>
  	</div>
  	
  	<div class="form-group ${errors.onPort != null ? 'has-error' : ''}">
    	<label for="ServicePort" class="col-sm-2 control-label"><spring:message code="pages.configure.label.port"/>:</label>
    	<div class="col-sm-1">
      		<input class="form-control text-center service-dependent-ro" required type="number" min="1" max="65535" name="port" id="ServicePort" placeholder="<spring:message code="pages.configure.label.port"/>..." value="${port}">
      		<c:if test="${errors.onPort != null}"><p class="text-danger"><spring:message code="${errors.onPort}"/></p></c:if>
       	</div>
  	</div>
  	  	
  	<div class="form-group ${errors.onOutputFormat != null ? 'has-error' : ''}">
    	<label for="OutputFormat" class="col-sm-2 control-label"><spring:message code="pages.configure.label.format"/>:</label>
    	<div class="col-sm-1">
    		<select name="formatSel" id="OutputFormat" class="text-center form-control service-dependent-d" onchange="form.format.value = this.value">
				<c:forEach items="${formats}" var="formatOpt">
					<option class="text-center" value="${formatOpt}" ${format == formatOpt? 'selected' : ''} >${formatOpt}</option>
				</c:forEach>
			</select>
			<input type="hidden" name="format" value="${format}">
      		<c:if test="${errors.onOutputFormat != null}"><p class="text-danger"><spring:message code="${errors.onOutputFormat}"/></p></c:if>
    	</div>
  	</div>
  	
    <div class="form-group">
        <div class="col-sm-offset-2 col-sm-6">
            <button type="submit" class="btn btn-default btn-primary">
				<spring:message code="action.configure"/>
			</button>
        </div>
    </div>
</form>

<script>
(function(){
	var SDReadonly = $(".service-dependent-ro");
	var SDDisabled = $(".service-dependent-d");
	var useService = $("#UseService");
	useService.click(function(){
		SDReadonly.attr('readonly', !this.checked);
		SDDisabled.attr('disabled', !this.checked);
	});
	SDReadonly.attr('readonly', !useService[0].checked);
	SDDisabled.attr('disabled', !useService[0].checked);
})()
</script>

${portal.toolkit()}