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
    });

    $("#select_type").change(function () {
        switch ($(this).val()) {
//          You want to define what happens when DefaultValue/Empty value is selected
            case "NONE":
                $('#select_filter').empty();
                $("<option>").attr("value", "").text("Not Available!").appendTo("#select_filter");
                $('#select_filter').prop("disabled", true );
                $("#checkers").hide();
                $("#checkers").empty();
                $("<p>").text("No tests selected, use the drop-down and select one!").appendTo("#checkers");
                $("#checkers").show();
                break;
//          For all other cases you want to deffer the logic to the server
            default :
                $('#select_filter').empty();
                $("<option>").attr("value", "Available").text("Available").appendTo("#select_filter");
                searchWithAjax();
                $('#select_filter').prop("disabled", false );
                break;
        }
    });

});

//  When handicapable remove this function and do it exclusively with JQuery
function appendOptionsFromJSON(JSONPath, idName) {
    $.getJSON(JSONPath)
        .done(function (json) {
            $.each(json, function (key, value) {
                $("<option>").attr("value", value).text(key).appendTo(idName);
            })
        })
        .fail(function (jqxhr, textStatus, error) {
            var err = textStatus + ", " + error;
            console.log("Request Failed: " + err);
        });
}

function searchWithAjax(){
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
        },
        error : function(e) {
            console.log("ERROR: ", e);
            appendOptionsFromJSON(e, "#select_filter");
        },
        done : function(e) {
            console.log("DONE");
        }
    });
}
