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
});