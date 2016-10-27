$(document).ready(function () {

    var stompConnectCallback = function (frame) {
            console.log('Connected: ' + frame);
            stompClient.subscribe('/topic/message', function (calResult) {
                // Receives a message.
                console.log(calResult.body);
                $("#section").append(calResult.body);
            });
        },

        stompErrorCallback = function (error) {
            console.log("ERROR_STOMP: ", error.headers.message);
        };

//  Start with the last Select disabled and an action message at the bottom of the Selects
    $('#select_filter').empty();
    $("<option>").attr("value", "NONE").text("None").appendTo("#select_filter");
    $('#select_filter').prop("disabled", true);

    $("#checkers").hide();
    $("#checkers").empty();
    $("<p>").text("No tests selected, use the drop-down and select one!").appendTo("#checkers");
    $("#checkers").show();

    var webSocket,
        serviceLocation = '/section',
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

    $("#reindex").click(function (e) {
        e.preventDefault();
        $("#section").empty();
        stompClient.send('/app/section', {}, "reindex");
    });
    
    $("#submitTests").click(function (e) {
        e.preventDefault();
        $("#section").empty();
        collectValuesFromCheckboxes();
    });

    $("#cancelTests").click(function (e) {
        e.preventDefault();
        // There's no need to erase, just append!
        // $("#section").empty();
        stopRunningTests();
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

        prepareForTests();
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
        console.log($(this).val());
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

    console.log(JSON.stringify(options));

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

    console.log(JSON.stringify(options));

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

function collectValuesFromCheckboxes() {
    var favorite = [];
    $.each($("input[type='checkbox']:checked"), function(){
        favorite.push($(this).val());
    });
    var composite = {};
    composite["environment"] = $('#select_envs').find('option:selected').html();
    composite["checkBoxes"] = favorite;

    console.log(JSON.stringify(favorite));
    console.log(JSON.stringify(composite));

    $.ajax({
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
    });
}

function stopRunningTests() {
    var options = gatherOptions();

    console.log(JSON.stringify(options));

    $.ajax({
        type: "POST",
        contentType: "application/json",
        url: "/stopTestsOnEnv",
        data: JSON.stringify(options),
        dataType: 'json',
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

function prepareForTests() {
    var options = gatherOptions();

    console.log(JSON.stringify(options));

    $.ajax({
        type: "POST",
        contentType: "application/json",
        url: "/prepareForTestsOnEnv",
        data: JSON.stringify(options),
        dataType: 'json',
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

function gatherOptions() {
    var options = {};
    options["env"] = $('#select_envs').find('option:selected').html();
    options["type"] = $("#select_type").val();
    options["filter"] = $("#select_filter").val();
    return options;
}