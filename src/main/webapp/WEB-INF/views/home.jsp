<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<c:url value="/static/img/spring.png" var="springIMG"/>
<c:url value="/static/img/reload.png" var="reloadIMG"/>
<c:url value="/static/pages/howtorun.html" var="staticHowTo"/>
<c:url value="/results/results.xml" var="staticResults"/>
<c:url value="/static/pages/checkboxesView.html" var="checkboxesView"/>
<c:url value="/static/css/style.css" var="staticCSS"/>
<c:url value="/static/js/logic.js" var="staticJS"/>
<c:url value="/static/jsLibs/jquery-3.0.0.min.js" var="staticJQuery"/>
<c:url value="/static/jsLibs/sockjs-1.1.1.min.js" var="staticSockJS"/>
<c:url value="/static/jsLibs/stomp.min.js" var="staticSTOMP"/>
<c:url value="/static/bootstrap/js/bootstrap.min.js" var="staticBootstrapJS"/>
<c:url value="/static/bootstrap/css/bootstrap.min.css" var="staticBootstrapCSS"/>
<c:url value="/static/json/envCorrelation.json" var="staticEnvironments"/>
<c:url value="/static/json/testTypes.json" var="staticTestTypes"/>

<%--<!DOCTYPE html>--%>
<%-- HTML5 no caching code manifest="/manifest.applicationCache"--%>
<html>
<head>
    <title>There's no place like home!</title>
    <link rel="stylesheet" type="text/css" href="${staticCSS}">
    <link rel="stylesheet" type="text/css" href="${staticBootstrapCSS}">
    <script type="text/javascript" src="${staticJQuery}"></script>
    <script type="text/javascript" src="${staticBootstrapJS}"></script>
    <script type="text/javascript" src="${staticJS}"></script>
    <script type="text/javascript" src="${staticSockJS}"></script>
    <script type="text/javascript" src="${staticSTOMP}"></script>
    <script type="text/javascript">appendOptionsFromJSONPath("${staticEnvironments}", "#select_envs");</script>
    <script type="text/javascript">appendOptionsFromJSONPath("${staticTestTypes}", "#select_type");</script>
    <%-- HTML4 no caching code--%>
<%--    <meta http-equiv="cache-control" content="max-age=0" />
    <meta http-equiv="cache-control" content="no-cache" />
    <meta http-equiv="expires" content="0" />
    <meta http-equiv="expires" content="Tue, 01 Jan 1980 1:00:00 GMT" />
    <meta http-equiv="pragma" content="no-cache" />--%>
</head>
<body>
<div class="container-fluid" id="site">
    <div class="row" id="header">
        <div class="panel panel-default">
            <div class="panel-body">
                <div class="col-lg-8">
                    <form class="form-inline">
                        <div class="form-group">
                            <label class="control-label" for="select_envs">Environment:</label>
                            <div class="">
                                <select id="select_envs" class="form-control"></select>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label" for="select_type">TestType:</label>
                            <div class="">
                                <select id="select_type" class="form-control"></select>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label" for="select_filter">Filter:</label>
                            <div class="">
                                <select id="select_filter" class="form-control"></select>
                            </div>
                        </div>
                    </form>
                    <form class="form-inline">
                        <div class="btn-group" role="group" aria-label="...">
                            <a href="${staticHowTo}" id="howToRunTests" class="btn btn-info" role="button">HowTo</a>
                            <a href="${staticResults}" id="resultsLink" class="btn btn-info" role="button">Results</a>
                        </div>

                        <div class="btn-group" role="group" aria-label="...">
                            <button class="btn btn-primary" type="button" id="reindex" value="ReIndex">ReIndex</button>
                            <button class="btn btn-success" type="button" id="submitTests" value="Run" disabled>Run
                            </button>
                            <button class="btn btn-danger" type="button" id="cancelTests" value="Stop" disabled>Stop
                            </button>
                        </div>
                    </form>
                </div>
                <div class="col-lg-4">
                    <h1>MSBAdapterWeb Regression</h1>
                </div>
            </div>
        </div>
    </div>
    <div class="row" id="middle">
        <div class="col-lg-4 panel panel-default">
            <div id="nav" class="panel-body">
                <%--Alert section when something happuns--%>
                <%-- http://getbootstrap.com/components/#alerts-examples --%>
                <%--<div class="alert alert-success" role="alert">...</div>
                <div class="alert alert-info" role="alert">...</div>
                <div class="alert alert-warning" role="alert">...</div>
                <div class="alert alert-danger" role="alert">...</div>--%>
                <div id="checkers" class="alert alert-info" role="alert" hidden></div>
                <%-- http://getbootstrap.com/components/#list-group-badges --%>
            </div>
        </div>
        <div class="col-lg-8 panel panel-default">
            <div id="section" class="panel-body">
                Basic panel example
            </div>
        </div>
    </div>
</div>
</body>
</html>
