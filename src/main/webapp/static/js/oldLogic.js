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