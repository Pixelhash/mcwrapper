var webSocket = null;//new WebSocket("ws://localhost:23843/chat?peter=Hallo");
var newline = String.fromCharCode(13, 10);

$(document).ready(function () {
    $('.main').hide();

    $('#login').on('click', function () {
        $(this).toggleClass('is-loading is-disabled');
        webSocket = new WebSocket("ws://" + location.hostname + ":" + location.port + "/console?password=" + $('#password').val());

        webSocket.onerror = function () {
            alert("Could not connect to server :(");
            $('#login').toggleClass('is-loading is-disabled');
            webSocket.close();
            webSocket = null;
        };

        webSocket.onopen = function () {
            webSocket.send("Ping!");

            setTimeout(function () {
                if (webSocket == null || webSocket.readyState == 3) {
                    return;
                }
                $('#auth').hide();
                $('.main').show();
                alert("Connected");
                //Establish the WebSocket connection and set up event handlers
                webSocket.onmessage = function (msg) {
                    updateChat(msg);
                };
                webSocket.onclose = function () {
                    alert("WebSocket connection closed")
                };

                //Send message if "Send" is clicked
                $('#send').on("click", function () {
                    if ($('#message').val() && /\S/.test($('#message').val())) sendMessage($("#message").val());
                });

                //Send message if enter is pressed in the input field
                $('#message').on("keypress", function (e) {
                    if (e.keyCode === 13) {
                        sendMessage(e.target.value);
                    }
                });
            }, 2500);
        };
    });

});

//Send a message if it's not empty, then clear the input field
function sendMessage(message) {
    if (message !== "") {
        webSocket.send(message);
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
    $('.console').append('<blockquote class="line">' + escapeHtml(data.userMessage) + '</blockquote>');
    $('.console').scrollTop($('.console')[0].scrollHeight);
    var count = $('.console > blockquote').length;
    if (count > 50) $('.console blockquote:first').remove();
}

function escapeHtml(unsafe) {
    return unsafe
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}
