<%@ page trimDirectiveWhitespaces="true"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags"%>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="nav" tagdir="/WEB-INF/tags/responsive/nav"%>
<ul class="nav__links nav__links--account">
	<c:if test="${empty hideHeaderLinks}">
		<c:if test="${uiExperienceOverride}">
			<li class="backToMobileLink">
				<c:url value="/_s/ui-experience?level=" var="backToMobileStoreUrl" />
				<a href="${fn:escapeXml(backToMobileStoreUrl)}">
					<spring:theme code="text.backToMobileStore" />
				</a>
			</li>
		</c:if>

		<sec:authorize access="!hasAnyRole('ROLE_ANONYMOUS')">
			<c:set var="maxNumberChars" value="25" />
			<c:if test="${fn:length(user.firstName) gt maxNumberChars}">
				<c:set target="${user}" property="firstName"
					value="${fn:substring(user.firstName, 0, maxNumberChars)}..." />
			</c:if>

			<li class="logged_in js-logged_in">
				<ycommerce:testId code="header_LoggedUser">
					<spring:theme code="header.welcome" arguments="${user.firstName},${user.lastName}" />
				</ycommerce:testId>
			</li>
		</sec:authorize>

		<sec:authorize access="hasAnyRole('ROLE_ANONYMOUS')" >
			<li class="liOffcanvas">
				<ycommerce:testId code="header_Login_link">
					<c:url value="/login" var="loginUrl" />
					<a href="${fn:escapeXml(loginUrl)}">
						<spring:theme code="header.link.login" />
					</a>
				</ycommerce:testId>
			</li>
		</sec:authorize>

		<sec:authorize access="!hasAnyRole('ROLE_ANONYMOUS')" >
			<li class="liOffcanvas">
				<ycommerce:testId code="header_signOut">
					<c:url value="/logout" var="logoutUrl"/>
					<a href="${fn:escapeXml(logoutUrl)}">
						<spring:theme code="header.link.logout" />
					</a>
				</ycommerce:testId>
			</li>
		</sec:authorize>
	</c:if>
</ul>
 