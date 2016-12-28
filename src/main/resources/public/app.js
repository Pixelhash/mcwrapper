var webSocket = null;//new WebSocket("ws://localhost:23843/chat?peter=Hallo");
var newline = String.fromCharCode(13, 10);
var lastCmd = null;

$(document).ready(function () {
    $('.main').hide();

    $('#login').on('click', function () {
        if (webSocket != null) return;
        $(this).toggleClass('is-loading is-disabled');
        try {
            webSocket = new WebSocket("wss://" + location.hostname + ":" + location.port + "/console?password=" + $('#password').val());
        } catch (err) {
            $('#status').text(err.message);
            return;
        }

        webSocket.onerror = function () {
            $('#status').text("Could not connect to server :(");
            if ($('#login').hasClass('is-loading')) $('#login').toggleClass('is-loading is-disabled');
            webSocket.close();
            webSocket = null;
            $('.main').hide();
        };

        webSocket.onclose = function () {
            $('#status').text("Connection closed!");
            if ($('#login').hasClass('is-loading')) $('#login').toggleClass('is-loading is-disabled');
            webSocket = null;
            $('.main').hide();
        };

        webSocket.onopen = function () {
            //webSocket.send("ping");

            setTimeout(function () {
                if (webSocket == null || webSocket.readyState != 1) {
                    return;
                }
                $('.main').show();
                $('#status').text("Successfully connected!");
                //Establish the WebSocket connection and set up event handlers
                webSocket.onmessage = function (msg) {
                    updateChat(msg);
                };
                $('#auth').hide();

                //Send message if "Send" is clicked
                $('#send').on("click", function () {
                    if ($('#message').val() && /\S/.test($('#message').val())) sendMessage($("#message").val());
                });

                //Send message if enter is pressed in the input field
                $('#message').on("keypress", function (e) {
                    if (e.keyCode === 13) {
                        sendMessage(e.target.value);
                    } else if (e.keyCode == 38 && lastCmd != null) {
                        $('#message').val(lastCmd);
                    }
                });
            }, 2000);
        };
    });

});

//Send a message if it's not empty, then clear the input field
function sendMessage(message) {
    if ($('#message').val() && /\S/.test($('#message').val())) {
        webSocket.send(message);
        lastCmd = message;
        $("#message").val("");
    }
}

//Update the chat-panel, and the list of connected users
function updateChat(msg) {
    var data = JSON.parse(msg.data);
    //$("#console").val($("#console").val() + data.userMessage + newline);
    //$("#console").scrollTop = $("#console").scrollHeight;
    /*var psconsole = $('#console');
    if(psconsole.length)
        psconsole.scrollTop(psconsole[0].scrollHeight - psconsole.height());*/
    if (data.userMessage != "") $('.console').append('<blockquote class="line">' + escapeHtml(data.userMessage) + '</blockquote>');
    $('.console').scrollTop($('.console')[0].scrollHeight);
    var count = $('.console > blockquote').length;
    if (count > 50) $('.console blockquote:first').remove();

    $('#users').html("");
    data.userList.forEach(function (user) {
       $('#users').append('<li>' + user + '</li>');
    });
}

function escapeHtml(unsafe) {
    return unsafe
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}
