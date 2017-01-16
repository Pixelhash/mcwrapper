var webSocket = null;
var maxRows = 50;
var maxCommands = 5;
var lastCommands = [];
var lastCommand = -1;

var colorPattern = [
    '\\[30;22m',
    '\\[34;22m',
    '\\[32;22m',
    '\\[36;22m',
    '\\[31;22m',
    '\\[35;22m',
    '\\[33;22m',
    '\\[37;22m',
    '\\[30;1m',
    '\\[34;1m',
    '\\[32;1m',
    '\\[36;1m',
    '\\[31;1m',
    '\\[35;1m',
    '\\[33;1m',
    '\\[37;1m'
];

var colorPattern2 = [
    '\\[0;30;22m',
    '\\[0;34;22m',
    '\\[0;32;22m',
    '\\[0;36;22m',
    '\\[0;31;22m',
    '\\[0;35;22m',
    '\\[0;33;22m',
    '\\[0;37;22m',
    '\\[0;30;1m',
    '\\[0;34;1m',
    '\\[0;32;1m',
    '\\[0;36;1m',
    '\\[0;31;1m',
    '\\[0;35;1m',
    '\\[0;33;1m',
    '\\[0;37;1m'
];

var formatPattern = [
    '\\[5m',
    '\\[21m',
    '\\[9m',
    '\\[4m',
    '\\[3m',
    '\\[0;39m',
    '\\[0m',
    '\\[m'
];

var colorReplace = [
    '<span style="color: #000000;">',
    '<span style="color: #0000AA;">',
    '<span style="color: #00AA00;">',
    '<span style="color: #00AAAA;">',
    '<span style="color: #AA0000;">',
    '<span style="color: #AA00AA;">',
    '<span style="color: #FFAA00;">',
    '<span style="color: #AAAAAA;">',
    '<span style="color: #555555;">',
    '<span style="color: #5555FF;">',
    '<span style="color: #55FF55;">',
    '<span style="color: #55FFFF;">',
    '<span style="color: #FF5555;">',
    '<span style="color: #FF55FF;">',
    '<span style="color: #FFFF55;">',
    '<span style="color: #FFFFFF;">',
];

var formatReplace = [
    '',
    '<b>',
    '<s>',
    '<u>',
    '<i>',
    '</b></s></u></i></span>',
    '</b></s></u></i></span>',
    '</b></s></u></i></span>'
];

function replaceBulk( str, findArray, replaceArray ){
    var i, regex = [], map = {};
    for( i=0; i<findArray.length; i++ ){
        regex.push( findArray[i]);//.replace('[-[\]{}()*+?.\\^$|#,]','\\$0') );
        map[findArray[i]] = replaceArray[i];
    }
    regex = regex.join('|');
    str = str.replace( new RegExp( regex, 'g' ), function(matched){
        return map['\\' + matched];
    });
    return str;
}

$(document).ready(function () {

    $.fn.isFadeOut = function(){
        return this.css('display') == 'none';
    };

    $('.main').hide();

    // Handle login with 'ENTER' key.
    $('#password').on("keydown", function (e) {
        if (e.keyCode === 13) {
            $('#login').trigger('click');
        }
    });

    $('#height').on('input', function () {
        var val = $('#height').val();
        if (val >= 20 && val <= 800) $('.console').height(val);
    });

    $('#rows').on('input', function () {
        var val = $(this).val();
        if (val >= 1 && val <= 200) maxRows = val;
    });

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
            if ($('#status').isFadeOut()) $('#status').fadeToggle();
            $('#status').text("Could not connect to server :(");
            if ($('#login').hasClass('is-loading')) $('#login').toggleClass('is-loading is-disabled');
            webSocket.close();
            webSocket = null;
            $('.main').hide();
        };

        webSocket.onclose = function () {
            if ($('#status').isFadeOut()) $('#status').fadeToggle();
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
                setTimeout(function () {
                    $('#status').fadeToggle();
                }, 3000);
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
                $('#message').on("keydown", function (e) {
                    if (e.keyCode === 13) {
                        sendMessage(e.target.value);
                    } else if (e.keyCode == 38 && lastCommands.length != null) {
                        console.log("UP: " + lastCommand);
                        // Arrow UP:
                        if (lastCommand - 1 < 0) return;
                        lastCommand--;
                        $('#message').val(lastCommands[lastCommand]);


                    } else if (e.keyCode == 40 && lastCommands.length != null) {
                        console.log("DOWN: " + lastCommand);
                        // Arrow DOWN:
                        e.preventDefault();
                        if (lastCommand + 1 >= lastCommands.length) {
                            $('#message').val('');
                            lastCommand = lastCommands.length;
                            return;
                        }
                        lastCommand++;
                        $('#message').val(lastCommands[lastCommand]);

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
        addCommand(message);
        lastCommand = lastCommands.length;
        $("#message").val("");
        updateCommandHistory();
    }
}

function updateCommandHistory() {
    $('#commands').html('');
    lastCommands.forEach(function (entry) {
        $('#commands').append('<li><a href="#">' + entry + '</a></li>');
    })
}

function addCommand(command) {
    if (lastCommands[lastCommands.length - 1] == command) return;
    if (lastCommands.push(command) > maxCommands) {
        lastCommands.shift();
    }
}

//Update the chat-panel, and the list of connected users
function updateChat(msg) {
    var data = JSON.parse(msg.data);
    if (data.userMessage != "") {
        var line = escapeHtml(data.userMessage);
        line = replaceBulk(line, colorPattern, colorReplace);
        line = replaceBulk(line, colorPattern2, colorReplace);
        line = replaceBulk(line, formatPattern, formatReplace);
        $('.console').append('<blockquote class="line">' + line + '</blockquote>');
    }
    $('.console').scrollTop($('.console')[0].scrollHeight);
    var count = $('.console > blockquote').length;
    if (count > maxRows) {
        for (i = count; i > maxRows; i--) {
            $('.console blockquote:first').remove();
        }
    }

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
        .replace(/'/g, "&#039;")
        .replace(/\u001B/g, "");
}
