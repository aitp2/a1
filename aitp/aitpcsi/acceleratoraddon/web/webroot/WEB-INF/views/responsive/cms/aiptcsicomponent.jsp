<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<spring:url value="${queryURLs}" var="geturl"/>

<script>
//$(document).ready(
  var xmlHttp; 
  function getXmlHttpObject(){
    if (window.XMLHttpRequest){
      // code for IE7+, Firefox, Chrome, Opera, Safari 
      xmlhttp=new XMLHttpRequest();
    }else{// code for IE6, IE5 
      xmlhttp=new ActiveXObject("Microsoft.XMLHTTP"); 
    }
    return xmlhttp;
  }
  xmlHttp = getXmlHttpObject();

  var obj = document.querySelector("${jqueryKeywords}");
  obj.innerHTML = "";
  
  function getOkGet() {
    if (xmlHttp.readyState==4 && xmlHttp.status==200){
    	obj.innerHTML = xmlHttp.responseText;
    	//alert("updated");
    }
  }
  
  var geturl = "${geturl}";

  xmlHttp.open("GET",geturl,true); 
  xmlHttp.onreadystatechange=getOkGet; 
  xmlHttp.send();
  //alert("clean ok");
  
//}
</script>
		