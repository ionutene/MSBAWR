<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<c:url value="/static/img/spring.png" var="springIMG"/>
<c:url value="/static/img/reload.png" var="reloadIMG"/>
<c:url value="/static/pages/howtorun.html" var="staticHowTo"/>
<c:url value="/static/pages/results.html" var="staticResults"/>
<c:url value="/static/pages/checkboxesView.html" var="checkboxesView"/>
<c:url value="/zipper" var="zipper"/>
<c:url value="/static/css/style.css" var="staticCSS"/>
<c:url value="/static/js/jquery-3.0.0.min.js" var="staticJQuery"/>
<c:url value="/static/js/logic.js" var="staticJS"/>
<c:url value="/static/js/sockjs-1.1.1.min.js" var="staticSockJS"/>
<c:url value="/static/js/stomp.min.js" var="staticSTOMP"/>
<c:url value="/static/json/envCorrelation.json" var="staticEnvironments"/>
<c:url value="/static/json/testTypes.json" var="staticTestTypes"/>

<html>
<head>
    <title>There's no place like home!</title>
    <link rel="stylesheet" type="text/css" href="${staticCSS}">
    <script type="text/javascript" src="${staticJQuery}"></script>
    <script type="text/javascript" src="${staticJS}"></script>
    <script type="text/javascript" src="${staticSockJS}"></script>
    <script type="text/javascript" src="${staticSTOMP}"></script>
    <script type="text/javascript">appendOptionsFromJSONPath("${staticEnvironments}", "#select_envs");</script>
    <script type="text/javascript">appendOptionsFromJSONPath("${staticTestTypes}", "#select_type");</script>
</head>
<body>
<div id="header">
    <h1>This is huge!</h1>
</div>

<div id="nav">
    <p>Select Environment:
        <select id="select_envs"></select>
    </p>

    <p>Select Test Type:
        <select id="select_type"></select>
    </p>

    <p>Select Filter:
        <select id="select_filter"></select>
    </p>
    <a href="${staticHowTo}" id="aLink">How to run tests</a><br>
    <a href="${staticResults}" id="aLink2">Results</a><br>
    <a href="${checkboxesView}" id="aLink3">Checkboxes</a><br>

    <form action="">
        <input type="image" id="reindex" src="${reloadIMG}" alt="Submit"/>
    </form>
    <br>

    <div id="checkers" hidden></div>
</div>

<div id="section">
    <h1>London</h1>

    <p><img src="${springIMG}"></p>

    <p>
        Lorem ipsum dolor sit amet, consectetur adipiscing elit. In arcu libero, maximus sed imperdiet non, ullamcorper
        ut arcu. Suspendisse arcu mauris, placerat luctus odio quis, vehicula ultrices arcu. Donec eget nibh in sapien
        fringilla commodo et nec tortor. Pellentesque gravida pellentesque tincidunt. Vivamus lectus lorem, scelerisque
        viverra pharetra sit amet, hendrerit vitae tellus. Nam hendrerit, arcu id tincidunt interdum, quam nisl mollis
        erat, eu pretium orci lacus id arcu. Pellentesque habitant morbi tristique senectus et netus et malesuada fames
        ac turpis egestas. Praesent mollis consequat augue, ac imperdiet est malesuada quis.
    </p>

    <p>
        Nunc facilisis nisi eu quam rhoncus imperdiet. Nulla euismod purus in scelerisque tincidunt. Sed mollis nibh sed
        ex lobortis sodales. Ut nisl dolor, commodo ac tincidunt a, maximus et enim. Aliquam turpis eros, elementum at
        quam eu, aliquet lobortis lacus. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per
        inceptos himenaeos. Donec a gravida tortor, sit amet malesuada augue. Vestibulum ut dolor eget orci gravida
        sollicitudin sed vitae augue. Proin et imperdiet ante, sed vulputate urna. Nunc bibendum maximus ex, quis luctus
        turpis ultricies aliquet. Integer mattis neque euismod lacus pharetra pulvinar. Proin rutrum, dui at cursus
        tempus, nisl purus posuere quam, non aliquam est ligula quis nunc.
    </p>

    <p>
        Proin purus lectus, auctor quis iaculis eget, euismod et lorem. Quisque at dolor et augue consequat suscipit.
        Morbi nunc ipsum, congue ac libero id, pretium lacinia magna. Sed interdum imperdiet quam. Sed non sem vitae
        lectus tincidunt imperdiet quis sit amet nibh. Phasellus sapien lectus, varius eu magna sit amet, ultrices
        aliquam magna. Vestibulum commodo mi nec sodales interdum. Quisque blandit, felis eget condimentum dignissim,
        odio erat semper nulla, ac faucibus nibh est tincidunt arcu. In eleifend consequat dui eget vestibulum. Mauris
        magna lacus, dapibus in orci sed, consequat euismod turpis. Cras sed tincidunt massa. Sed cursus ante quis
        posuere pharetra.
    </p>

    <p>
        Ut ac quam eget ligula bibendum lobortis in et lacus. Duis vitae velit congue, accumsan quam et, venenatis
        magna. Mauris ornare turpis iaculis, luctus nibh vitae, commodo felis. Duis non augue laoreet, ornare erat ut,
        molestie lorem. Vestibulum cursus imperdiet molestie. Nullam vitae quam in eros rhoncus lobortis et laoreet
        ipsum. Donec suscipit tempor eros, vitae molestie erat pharetra id. Mauris leo magna, tincidunt vel pretium
        pharetra, vulputate non sem. Pellentesque dictum iaculis dictum.
    </p>

    <p>
        Donec ac ullamcorper odio, a rutrum eros. Nullam nunc orci, aliquet sed justo ut, gravida tristique urna. Sed
        ante augue, malesuada id mi eu, gravida aliquet felis. Aliquam nec dolor facilisis, iaculis sem eu, ullamcorper
        mauris. Sed eleifend elementum risus, non viverra justo placerat eget. Vivamus mauris eros, imperdiet nec
        gravida eget, malesuada id nunc. Interdum et malesuada fames ac ante ipsum primis in faucibus. Nulla facilisi.
        Vivamus sodales tempor rutrum. Proin fringilla eros tellus, id luctus velit aliquam sit amet. Donec tortor orci,
        lobortis id justo at, ultricies interdum nibh. Nulla tincidunt urna ac nibh maximus, eu condimentum nisl tempor.
        Mauris ornare facilisis congue. Cras placerat quam nec sapien vulputate, eget blandit erat eleifend.
    </p>

    <p>
        Nullam bibendum volutpat auctor. Pellentesque tincidunt ligula a tellus tempor, et tristique ipsum rhoncus.
        Pellentesque tempor, ante a hendrerit euismod, ex enim luctus est, nec sodales nulla turpis sed metus. Morbi
        suscipit, quam sed tempor mattis, magna eros imperdiet augue, eu porttitor velit risus non velit. Nunc iaculis
        suscipit nunc, in rutrum sapien volutpat sit amet. Etiam sagittis, leo vel fermentum congue, sem odio vestibulum
        sem, eget elementum turpis est ut magna. Pellentesque habitant morbi tristique senectus et netus et malesuada
        fames ac turpis egestas. Phasellus nunc elit, convallis ac vulputate non, condimentum et enim.
    </p>

    <p>
        Duis augue dui, convallis at rhoncus at, mattis et enim. Pellentesque posuere ligula bibendum orci ullamcorper
        ornare. Cras non sem nec enim dictum lacinia. Nulla ut elementum erat. Integer et metus vitae urna luctus
        interdum id eu dolor. Cras nec eros lacus. Phasellus vitae blandit libero, commodo tempor ligula. Suspendisse in
        ex non mi efficitur imperdiet.
    </p>

    <p>
        Fusce finibus tortor nibh, ac lobortis arcu tristique vel. Nullam purus urna, pharetra id pellentesque ac,
        feugiat id libero. Sed non tempus nunc, non sagittis turpis. Sed sollicitudin eget odio eu pharetra. Nullam
        rhoncus ante turpis, a finibus ligula aliquet sed. Aenean eget neque bibendum, sollicitudin justo sit amet,
        malesuada nisi. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas.
        Suspendisse vestibulum non purus vel elementum. Ut rutrum tempus massa sit amet mattis. Mauris mattis erat sit
        amet justo ultrices, ut pellentesque ipsum pellentesque. Vestibulum at nisi at ipsum scelerisque feugiat a
        dictum sem. Quisque sit amet nibh ipsum.
    </p>

    <p>
        In hac habitasse platea dictumst. Etiam tristique felis at mattis vestibulum. Integer egestas at nisl vel porta.
        Phasellus porttitor nulla in accumsan luctus. Duis fringilla enim metus, in pellentesque erat luctus congue.
        Mauris non rhoncus sem. Cras semper, nisl ut lobortis feugiat, sapien felis tempor lacus, placerat mattis nunc
        orci quis libero. Nulla eros nisl, tempor quis enim et, hendrerit blandit tellus. Nullam faucibus et est eget
        suscipit. Ut cursus interdum justo vel tempor. Sed nec arcu justo.
    </p>

    <p>
        Vestibulum eros lacus, sollicitudin et lorem vel, fringilla eleifend dui. Nam porta mollis elit vitae aliquam.
        Maecenas posuere lacus nec tempus ultrices. Praesent pellentesque, enim sed condimentum commodo, nulla sapien
        sodales leo, eget tincidunt metus nisl quis neque. Aenean tempor ultrices purus, a porttitor turpis sollicitudin
        vel. Nulla pretium vel ipsum et maximus. Sed nibh erat, elementum sed ante ut, vehicula tempus felis. Mauris
        finibus sed erat id congue. In a iaculis ante. Morbi sed urna id justo accumsan sodales id et elit. Quisque
        laoreet ipsum ut dolor feugiat egestas. Vivamus sit amet nunc fringilla, hendrerit dui eget, facilisis mauris.
        Etiam nec maximus est. Maecenas eros sapien, scelerisque in quam nec, dictum tempus elit.
    </p>
</div>

<div id="footer">
    Copyright © W3Schools.com
</div>
</body>
</html>
