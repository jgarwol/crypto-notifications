var stompClient = null;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#notificationsBody").show();
    }
    else {
        $("#notificationsBody").hide();
    }
    $("#notificationsBody").html("");
}

function connect() {
    var socket = new SockJS('/alerts');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/alerts', function (alert) {
            showAlertNotifications(alert.body);
        });
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function sendName() {
    $.ajax({
        url: '/api/alert',
        type: 'PUT',
        data: {
                "pair": $( "#pair" ).val(),
                "limit": $( "#limit" ).val(),
        },
        success: function(result) {
            refreshAlerts()
        },
        error: function(result){
            alert("Adding alert failed with message: "+result.responseText)
        }
    });

}

function refreshAlerts() {
    $.ajax({
        url: '/api/alert',
        type: 'GET',
        success: function(result) {
            refreshAlertsDefinitions(result)
        }
    });

}

function refreshAlertsDefinitions(alerts) {

    $("#alertBody").empty();
    if(alerts.length == 0){
        $("#alertBody").append("<tr><td colspan=\"3\">There are no alerts defined</td></tr>");
    } else {
        for(var i=0; i<alerts.length; i++){
            var pair = alerts[i].pair;
            var limit = alerts[i].limit;
            var newRow = $("<tr></tr>");
            newRow.append("<td>" + pair + "</td>");
            newRow.append("<td>"+ limit +"</td>");
            newRow.append($("<td></td>").append(deleteButton(pair, limit)));
            $("#alertBody").append(newRow);
        }
    }
}
function deleteButton(pair, limit){
    return $("<button>Remove</button>")
        .addClass("btn btn-default")
        .click(function(){deleteAlert(pair, limit)})
}

function deleteAlert(pair, limit){
        $.ajax({
            url: '/api/alert',
            type: 'DELETE',
            data: {
                    "pair": pair,
                    "limit": limit,
            },
            success: function(result) {
                refreshAlerts()
            }
        });
}
function showAlertNotifications(stringMessage) {
    var message = JSON.parse(stringMessage)  ;
    var newRow = $("<tr></tr>");
    newRow.append("<td>" + message.pair + "</td>");
    newRow.append("<td>"+ message.limit +"</td>");
    newRow.append("<td>"+ message.currentRate +"</td>");
    newRow.append("<td>"+ message.timestamp+"</td>");
    $("#notificationsBody").prepend(newRow);

}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#send" ).click(function() { sendName(); });
    $( "#refreshAlerts" ).click(function() { refreshAlerts(); });
});

