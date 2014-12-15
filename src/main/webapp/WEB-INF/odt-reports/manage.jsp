<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<h1><spring:message code="pages.manage.title"/></h1>

<c:if test="${not empty success}">
	<div class="alert alert-info">
		<spring:message code="${success}"/>
	</div>
</c:if>

<div class="btn-group" style="margin: 10pt 0;">
	<a href="${pageContext.request.contextPath}/reports/templates/add" class="btn btn-sm btn-primary">
		<spring:message code="action.add"/>
	</a>
</div>

<c:choose>
  <c:when test="${empty reports}">
  	  <div><spring:message code="pages.manage.emptyReports"/></div>
  </c:when>

  <c:otherwise>
		<table class="table table-striped">
      		<thead>
        		<tr>
         			<th class="col-md-6"><spring:message code="pages.manage.label.name"/></th>
         			<th class="text-center"><spring:message code="pages.manage.label.key"/></th>
         			<th></th>
        		</tr>
      		</thead>
     		<tbody>
				<c:forEach var="report" items="${reports}">
					<tr>
						<td class="col-md-6"><strong>${report.name.content}</strong><br/><small>${report.description.content}</small></td>
						<td class="col-md-5 text-center"><code>${report.key}</code></td>
						<td class="col-md-1">
							<div class="btn-group">
								<a href="${pageContext.request.contextPath}/reports/templates/${report.key}/edit" class="btn btn-sm btn-default">
									<spring:message code="action.edit"/>
								</a>
            				</div>
            			</td>
					</tr>
				</c:forEach>
     		</tbody>
     	</table>
	    <c:if test="${numberOfPages > 1}">
		    <div class="row">
		        <div class="col-md-2 col-md-offset-5">
		            <ul class="pagination">
		                <li class="${currentPage <= 0 ? 'disabled' : 'active'}"><a href="${pageContext.request.contextPath}/reports/templates/${page - 1}">«</a></li>
		                <li class="disabled"><a href="#">${currentPage + 1} / ${numberOfPages}</a></li>
		                <li class="${currentPage + 1 >= numberOfPages ? 'disabled' : 'active'}"><a href="${pageContext.request.contextPath}/reports/templates/${page + 1}">»</a></li>
		            </ul>
		        </div>
		    </div>
	    </c:if>
  </c:otherwise>
</c:choose>
