$(document).ready(function () {

    $("a").click(function (e) {
        e.preventDefault();
        var addressValue = $(this).attr("href");
//        alert(addressValue + " #container");
        $("#section").empty();
        $("#section").load(addressValue, function (response, status, xhr) {
            if (status == "error") {
                var msg = "Sorry but there was an error: ";
//                alert(msg + xhr.status + " " + xhr.statusText);
            }
        });
    });

    $("#select_type").change(function() {
        if ($(this).val() == "NO_TESTS") {
            $("#checkers").hide();
            $("#checkers").empty();
            $("<p>").text("No tests selected, use the drop-down and select one!").appendTo("#checkers");
            $("#checkers").show();
        } else {
            $("#checkers").hide();
            $("#checkers").empty();
            $("<p>").text("You have chosen: " + $(this).val() + " tests!").appendTo("#checkers");
            $("#checkers").show();
        }
    });

});

//  When handicapable remove this function and do it exclusively with JQuery
function getEnv(envJSON) {
    $.getJSON(envJSON)
        .done(function (json) {
            $.each(json.environments, function (key, value) {
                $("<option>").attr("value", value).text(value).appendTo("#select_envs");
            })
        })
        .fail(function (jqxhr, textStatus, error) {
            var err = textStatus + ", " + error;
            alert("Request Failed: " + err);
        });
}
