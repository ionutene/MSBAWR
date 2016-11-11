function doReindex() {
    $.ajax({
        type: "GET",
        contentType: "text/plain",
        url: "/getZip",
        dataType: 'text',
        timeout: 100000,
        async: false,
        success: function (data) {
            $("#section").empty();
            console.log(data);
            $("#section").html(data);
            /*$.each(data, function (key) {
             $("#section").append(key);
             });*/
            $("#reindex").prop("disabled", false);
            console.log("And we're out of here!");
        },
        error: function (e) {
            console.log("ERROR_FILTER: ", e);
        },
        done: function (e) {
            //console.log("DONE");
        }
    });
}

function webSockety() {
    // Write your code in the same way as for native WebSocket:
    var ws = new WebSocket("ws://localhost:8080/WebSockety");
    ws.onopen = function () {
        ws.send("GET_ZIP");  // Sends a message.
    };
    ws.onmessage = function (e) {
        // Receives a message.
        console.log(e.data);
    };
    ws.onerror = function (e) {
        console.log(e);
    };
    ws.onclose = function () {
        console.log("closed");
        ws.send("CLOSE");
    };
}


/*$.ajax({
 type: "POST",
 contentType: "application/json",
 url: "/getOptionsFromCheckboxes",
 data: JSON.stringify(composite),
 dataType: 'text',
 timeout: 100000,
 success: function (data) {
 },
 error: function (e) {
 console.log("ERROR_FILTER: ", e);
 },
 done: function (e) {
 //console.log("DONE");
 }
 });*/

function stopRunningTests() {
    var options = gatherOptions();

    $.ajax({
        type: "POST",
        contentType: "application/json",
        url: "/stopTestsOnEnv",
        data: JSON.stringify(options),
        dataType: 'text',
        timeout: 100000,
        success: function (data) {
        },
        error: function (e) {
            console.log("ERROR: ", e);
        },
        done: function (e) {
            //console.log("DONE");
        }
    });
}


    $("a").click(function (e) {
 e.preventDefault();
 var addressValue = $(this).attr("href");
 $("#section").empty();
 $("#section").load(addressValue, function (response, status, xhr) {
 if (status == "error") {
 var msg = "Sorry but there was an error: ";
 console.log(msg + xhr.status + " " + xhr.statusText);
 }
 });
 });

function prepareForTests() {
    var options = gatherOptions();

    $.ajax({
        type: "POST",
        contentType: "application/json",
        url: "/prepareForTestsOnEnv",
        data: JSON.stringify(options),
        dataType: 'text',
        timeout: 100000,
        success: function (data) {
        },
        error: function (e) {
            console.log("ERROR: ", e);
        },
        done: function (e) {
            //console.log("DONE");
        }
    });
}

/*        $.ajax({
 type: "GET",
 url: addressValue,
 dataType: "html",
 success: function (data) {
 printResults(data);
 },
 error: function (e) {
 console.log("ERROR: ", e.responseText);
 },
 done: function (e) {
 //console.log("DONE");
 }
 });*/



/*    $("#testNGPage").click(function (e) {
 e.preventDefault();
 var addressValue = $(this).attr("href");
 $("#section").empty();
 console.log("Incerc sa incarc pagina de testNG!" + addressValue);
 $("#section").load(addressValue);
 });*/



/*    $("#msbarLog").click(function (e) {
 e.preventDefault();
 var addressValue = $(this).attr("href");
 $("#section").empty();
 console.log("Incerc sa incarc pagina de msbarLog!" + addressValue);
 $("#section").load(addressValue);
 });*/

/*        if (stompClient != null) {
 stompClient.disconnect();
 }*/

/*
*
* <%--<form action="">--%>
 <%--<label for="select_envs">Select Environment:</label><select id="select_envs" class="form-control"></select>--%>
 <%--<label for="select_type">Select Test Type:</label><select id="select_type" class="form-control"></select>--%>
 <%--<label for="select_filter">Select Filter:</label><select id="select_filter" class="form-control"></select>--%>
 <%--</form>--%>
 <%--<form class="form-inline">
 <div class="form-group form-group-sm">
 <label class="col-sm-2 control-label" for="select_envs">Environment:</label>
 <div class="col-sm-4">
 <select id="select_envs" class="form-control"></select>
 </div>
 </div>
 <div class="form-group form-group-sm">
 <label class="col-sm-2 control-label" for="select_type">TestType:</label>
 <div class="col-sm-4">
 <select id="select_type" class="form-control"></select>
 </div>
 </div>
 <div class="form-group form-group-sm">
 <label class="col-sm-2 control-label" for="select_filter">Filter:</label>
 <div class="col-sm-4">
 <select id="select_filter" class="form-control"></select>
 </div>
 </div>
 </form>--%>
*
* */