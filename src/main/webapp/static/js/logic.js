$(document).ready(function () {

//  Initialise and start WebSocket Connection
    var stompConnectCallback = function (frame) {
            console.log('Connected: ' + frame);
            stompClient.subscribe('/topic/message', function (calResult) {
                // Receives a message.
                // console.log(calResult.body);
                $("#section").append(calResult.body);
            });
        },

        stompErrorCallback = function (error) {
            console.log("ERROR_STOMP: ", error);
        };

    var webSocket,
        serviceLocation = '/ws-stomp-stockjs',
        stompClient = null;

    var stompHeader = {
        login: 'mylogin',
        passcode: 'mypasscode',
        // additional header
        'client-id': 'my-client-id'
    };

    webSocket = new SockJS(serviceLocation);
    stompClient = Stomp.over(webSocket);
    stompClient.connect({}, stompConnectCallback, stompErrorCallback);

//  Start with the last Select disabled and an action message at the bottom of the Selects
    $('#select_filter').empty();
    $("<option>").attr("value", "NONE").text("None").appendTo("#select_filter");
    $('#select_filter').prop("disabled", true);

    $("#checkers").hide();
    $("#checkers").empty();
    $("<p>").text("No tests selected, use the drop-down and select one!").appendTo("#checkers");
    $("#checkers").show();

//  PrepareTests for already selected buk30_8600 machines
/*    $("#section").empty();
    stompClient.send('/app/prepareForTests', {}, JSON.stringify(gatherOptions()));*/

//  If How To Run Tests page is clicked, load the div #container from old page and spew it into div #section
    $("#howToRunTests").click(function (e) {
        e.preventDefault();
        var addressValue = $(this).attr("href");
        $("#section").empty();
        $("#section").load(addressValue + " #container");
    });

//  If Results page is clicked, create a table and populate it from results.xml
    $("#resultsLink").click(function (e) {
        e.preventDefault();
        var addressValue = $(this).attr("href");
        $("#section").empty();
        $( "<table>" ).attr({
            border: "1",
            cellpadding: "10px",
            id: "resultsTable"
        }).appendTo("#section");
        $("#resultsTable").hide();
        $("<thead>").appendTo("#resultsTable");
        $("<tr>").appendTo("#resultsTable > thead");
        $("<th>").text("Date").appendTo("#resultsTable > thead > tr");
        $("<th>").text("TestNG Results").appendTo("#resultsTable > thead > tr");
        $("<th>").text("Log").appendTo("#resultsTable > thead > tr");
        $("<th>").text("MAS adapter version").appendTo("#resultsTable > thead > tr");
        $("<th>").text("MPOS adapter version").appendTo("#resultsTable > thead > tr");
        $("<tbody>").appendTo("#resultsTable");

        getXMLFromResources(addressValue);
    });

//  If ReIndex is clicked, get the latest version of MSBAdapterRegression
    $("#reindex").click(function (e) {
        e.preventDefault();
        $("#section").empty();
        stompClient.send('/app/reindex', {}, "reindex");
    });

//  If Start is clicked, preapare and start tests
    $("#submitTests").click(function (e) {
        e.preventDefault();
        $("#section").empty();
        // var headers = {'content-type': 'application/json'};
        stompClient.send('/app/runTests', {}, JSON.stringify(collectValuesFromCheckboxes()));
    });

//  If STOP is clicked, stop running tests
    $("#cancelTests").click(function (e) {
        e.preventDefault();
        // There's no need to erase, just append!
        // $("#section").empty();
        stompClient.send('/app/stopTests', {}, JSON.stringify(gatherOptions()));
    });

//  RESET if someone changes the Environment
    $("#select_envs").change(function () {
        $("#select_type").prop('selectedIndex', 0);
        $('#select_filter').empty();
        $("<option>").attr("value", "NONE").text("None").appendTo("#select_filter");
        $('#select_filter').prop("disabled", true);

        $("#checkers").hide();
        $("#checkers").empty();
        $("<p>").text("No tests selected, use the drop-down and select one!").appendTo("#checkers");
        $("#checkers").show();

        stompClient.send('/app/prepareForTests', {}, JSON.stringify(gatherOptions()));
    });

//  If someone changes the Type of test some action must be taken
    $("#select_type").change(function () {
        switch ($(this).val()) {
//          You want to define what happens when DefaultValue/Empty value is selected
            case "NONE":
                $('#select_filter').empty();
                $("<option>").attr("value", "NONE").text("None").appendTo("#select_filter");
                $('#select_filter').prop("disabled", true);
                $("#checkers").hide();
                $("#checkers").empty();
                $("<p>").text("No tests selected, use the drop-down and select one!").appendTo("#checkers");
                $("#checkers").show();
                break;
//          For all other cases you want to deffer the logic to the server
            default :
                $('#select_filter').empty();
                getFilterOptions();
                break;
        }
    });

//  If someone changes the Filter of test some action must be taken
    $("#select_filter").change(function () {
        switch ($(this).val()) {
//          You want to define what happens when DefaultValue/Empty value is selected
            case "NONE":
                $("#checkers").hide();
                $("#checkers").empty();
                $("<p>").text("Here is where the checkboxes will be, please select a filter!").appendTo("#checkers");
                $("#checkers").show();
                break;
//          For all other cases you want to deffer the logic to the server
            default :
                getCheckboxes();
                break;
        }
    });

//  When dealing with dynamically created elements, the normal Event Handlers don't work
//  http://api.jquery.com/on/ #Direct and delegated events
    $(document).on("change", "input[type='checkbox']", function () {
        // console.log($(this).val());
        $(this).siblings('ul')
            .find("input[type='checkbox']")
            .prop('checked', this.checked);
    });

});

//  Function to GET JSON from an URL and insert it into a DOM Element with the name idName
//  JSON must be a simple Map --> {"key": "value", "key2": "value2"}
function appendOptionsFromJSONPath(JSONPath, idName) {
    $.getJSON(JSONPath)
        .done(function (json) {
            $.each(json, function (key, value) {
                $("<option>").attr("value", value).text(key).appendTo(idName);
            })
        });
}

//  Function to append JSON data to a DOM Element with the name idName
//  JSON must be a simple Map --> {"key": "value", "key2": "value2"}
function appendOptionsFromJSON(json, idName) {
    $.each(json, function (key, value) {
        $("<option>").attr("value", value).text(key).appendTo(idName);
    });
}

function getFilterOptions() {
    var options = gatherOptions();

    $.ajax({
        type: "POST",
        contentType: "application/json",
        url: "/getOptions",
        data: JSON.stringify(options),
        dataType: 'json',
        timeout: 100000,
        success: function (data) {
            appendOptionsFromJSON(data, "#select_filter");

            if ($('#select_filter').find('option:first').html() == "Not Available!") {
                $('#select_filter').prop("disabled", true);
                $("#checkers").hide();
                $("#checkers").empty();
                $("<p>").text("No tests available, please select another Test Type or Environment!").appendTo("#checkers");
                $("#checkers").show();

            } else {
                $('#select_filter').prop("disabled", false);
                $("#checkers").hide();
                $("#checkers").empty();
                $("<p>").text("Here is where the checkboxes will be, please select a filter!").appendTo("#checkers");
                $("#checkers").show();
            }

        },
        error: function (e) {
            console.log("ERROR: ", e);
        },
        done: function (e) {
            //console.log("DONE");
        }
    });
}

function getCheckboxes() {
    var options = gatherOptions();

    $.ajax({
        type: "POST",
        contentType: "application/json",
        url: "/getCheckboxes",
        data: JSON.stringify(options),
        dataType: 'text',
        timeout: 100000,
        success: function (data) {
            $("#checkers").empty();
            $("#checkers").html(data);
            $("#submitTests").show();
            $("#cancelTests").show();
        },
        error: function (e) {
            console.log("ERROR_FILTER: ", e);
        },
        done: function (e) {
            //console.log("DONE");
        }
    });
}

function gatherOptions() {
    var options = {};
    options["env"] = $('#select_envs').find('option:selected').html();
    options["type"] = $("#select_type").val();
    options["filter"] = $("#select_filter").val();
    // console.log(JSON.stringify(options));
    return options;
}

function collectValuesFromCheckboxes() {
    var favorite = [];
    $.each($("input[type='checkbox']:checked"), function () {
        favorite.push($(this).val());
    });
    var composite = {};
    composite["environment"] = $('#select_envs').find('option:selected').html();
    composite["checkBoxes"] = favorite;

    // console.log(JSON.stringify(favorite));
    // console.log(JSON.stringify(composite));

    return composite;
}

function getXMLFromResources(link) {
    $.ajax({
        type: "GET",
        url: link,
        dataType: "xml",
        success: function (data) {
            printResults(data);
        },
        error: function (e) {
            console.log("ERROR: ", e.responseText);
        },
        done: function (e) {
            //console.log("DONE");
        }
    });
}

function printResults(xml) {
    var $xmlParsed = $(xml);
    var $result = $xmlParsed.find("Result");

    $result.each(function () {
        var date = $(this).find('Date').text(),
            name = $(this).find('Name').text(),
            log = $(this).find('Log').text(),
            mas = $(this).find('Mas').text(),
            mpos = $(this).find('Mpos').text();

        var columnDate = "<td>" + date + "</td>",
            columnTestNG = "<td><a href='/results/" + name + "/index.html' id='testNGPage'>" + name + "</a></td>",
            columnLog = "<td><a href='/results/" + name + "/" + log + "' id='msbarLog'>" + log + "</a></td>",
            columnMASVersion = "<td>" + mas + "</td>",
            columnMPOSVersion = "<td>" + mpos + "</td>";

        var tableRow = "<tr>" + columnDate + columnTestNG + columnLog + columnMASVersion + columnMPOSVersion + "</tr>";

        // $('#resultsTable tr:last').after(tableRow);
        $('#resultsTable > tbody').append(tableRow);

    });

    //  When dealing with dynamically created elements, the normal Event Handlers don't work
//  http://api.jquery.com/on/ #Direct and delegated events
    $(document).on("click", "a#testNGPage", function (event) {
        event.preventDefault();
        var addressValue = $(this).attr("href");
        $("#section").empty();
        $("<iframe>").attr("src", addressValue).appendTo("#section");
        // $("#section").load(addressValue);
    });

    //  When dealing with dynamically created elements, the normal Event Handlers don't work
//  http://api.jquery.com/on/ #Direct and delegated events
    $(document).on("click", "a#msbarLog", function (event) {
        event.preventDefault();
        var addressValue = $(this).attr("href");
        $("#section").empty();
        $("<pre>").attr("id", "formattedLog").appendTo("#section");
        $("#formattedLog").load(addressValue);
    });

    $("#resultsTable").show();
}