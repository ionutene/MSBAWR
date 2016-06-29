$(document).ready(function () {

//  Start with the last Select disabled
    $('#select_filter').prop("disabled", true );

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

//  RESET if someone changes the Environment
    $("#select_envs").change(function () {
        $("#select_type").prop('selectedIndex',0);
        $('#select_filter').empty();
        $("<option>").attr("value", "Not Available!").text("Not Available!").appendTo("#select_filter");
        $('#select_filter').prop("disabled", true );
        $("#checkers").hide();
        $("#checkers").empty();
        $("<p>").text("No tests selected, use the drop-down and select one!").appendTo("#checkers");
        $("#checkers").show();
    });

    $("#select_type").change(function () {
        switch ($(this).val()) {
//          You want to define what happens when DefaultValue/Empty value is selected
            case "NONE":
                $('#select_filter').empty();
                $("<option>").attr("value", "Not Available!").text("Not Available!").appendTo("#select_filter");
                $('#select_filter').prop("disabled", true );
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
                $("#checkers").hide();
                $("#checkers").empty();
                $("<p>").text("So many shiny checkboxes!").appendTo("#checkers");
                $("#checkers").show();
                break;
        }
    });

});

//  When handicapable remove this function and do it exclusively with JQuery
function appendOptionsFromJSONPath(JSONPath, idName) {
    $.getJSON(JSONPath)
        .done(function (json) {
            console.log("Dumping in: " + idName + " the following JSON: " + JSON.stringify(json));
            $.each(json, function (key, value) {
                $("<option>").attr("value", value).text(key).appendTo(idName);
            })
        });
/*        .fail(function (jqxhr, textStatus, error) {
            var err = textStatus + ", " + error;
            console.log("Request Failed: " + err);
        });*/
}

function appendOptionsFromJSON(json, idName) {
    console.log("Dumping in: " + idName + " the following JSON: " + JSON.stringify(json));
    $.each(json, function (key, value) {
        $("<option>").attr("value", value).text(key).appendTo(idName);
    })
//    $(idName).prop('selectedIndex',0);
}

function getFilterOptions(){
    var options = {};
    options["env"] = $('#select_envs').find('option:selected').html();
    options["type"] = $("#select_type").val();
    console.log(options);

    $.ajax({
        type : "POST",
        contentType : "application/json",
        url : "/getOptions",
        data : JSON.stringify(options),
        dataType : 'json',
        timeout : 100000,
        success : function(data) {
            console.log("SUCCESS: ", data);
            appendOptionsFromJSON(data, "#select_filter");

            if($('#select_filter').find('option:first').html() == "Not Available!") {
                $('#select_filter').prop("disabled", true );
            } else {
                $('#select_filter').prop("disabled", false );
            }
            $("#checkers").hide();
            $("#checkers").empty();
            $("<p>").text("Here is where the checkboxes will be, please select a filter!").appendTo("#checkers");
            $("#checkers").show();
        },
        error : function(e) {
            console.log("ERROR: ", e);
        },
        done : function(e) {
            console.log("DONE");
        }
    });
}
