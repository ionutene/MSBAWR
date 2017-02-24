<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  Created by IntelliJ IDEA.
  User: george.postelnicu
  Date: 2/24/2017
  Time: 3:37 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<c:url value="/static/bootstrap/js/bootstrap.min.js" var="staticBootstrapJS"/>
<c:url value="/static/bootstrap/css/bootstrap.min.css" var="staticBootstrapCSS"/>
<c:url value="/static/jsLibs/jquery-3.0.0.min.js" var="staticJQuery"/>
<html>
<head>
    <title>CONFIGURATION_ERROR</title>

    <link rel="stylesheet" type="text/css" href="${staticBootstrapCSS}">
    <script type="text/javascript" src="${staticJQuery}"></script>
    <script type="text/javascript" src="${staticBootstrapJS}"></script>
</head>
<body>
<h1>
    <div id="checkers" class="alert alert-danger" role="alert">server.properties configurations don't match OS</div>
</h1>
</body>
</html>
