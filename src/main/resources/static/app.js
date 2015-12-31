var chatApp = angular.module("chatApp", ['ui.bootstrap',
  "chatApp.controllers", "ngSanitize"
]);

angular.module("chatApp.controllers", []);

angular.module("chatApp.controllers").controller("ChatCtrl", function($scope,$rootScope,$modal) {
  $scope.messages = [];
  $scope.message = "";
  $scope.name = "";
  $scope.targetUsers = [];
  $scope.privateMessageTargetUser = "";
  $scope.users = [];
  $scope.settings = [];
  $scope.typing = [];
  $scope.timeout;
  $scope.browserNotifications = [];
  $scope.connected = false;
  $scope.controlsCollapsed = false;
  $scope.cooldown = false;
  $scope.settings = {
    "browserNotify":false,
    "notificationSound":true,
    "typingText":false,
    "inlineImages":true
  }
  var socketClient;

  $scope.getLocalDateTime = function(){
      var d = new Date();
      timeOffsetInHours = (d.getTimezoneOffset()/60) * (-1);
      d.setHours(d.getHours() + timeOffsetInHours);
      return d;
  }

  $scope.getUsers = function(){
    //TODO get websocket sessions
  };

  $scope.sendMessage = function(text,targetUsers) {
      socketClient.send("2|"+JSON.stringify({
        "textMessage" : text,
        "targetUsers" : targetUsers,
        "sender" : $scope.name
      }));
  };

  var processMessage = function(data) {
    var type = data.messageType;
    switch(type) {
        case 1 : {
            checkLeft(data.users);
            $scope.users = data.users;
            if( data.newSessionAdded.length ){
                var date = $scope.getLocalDateTime();
                var dateString = date.toISOString();
                $scope.messages.push({"textMessage":"-- " + data.newSessionAdded + " Joined! --","sender":"","messageDateTime":dateString});
            }
            $scope.$apply();
            setTimeout(function(){
                $scope.$apply();
            },1)
            console.log($scope.users);
            break;
        }
        case 2 : {
            $scope.messages.push(data);
            $scope.typing = [];
            $("button.userButton").removeClass("btn-success");
            $("button.userButton").removeClass("active");
            notify(data);
            $scope.$apply();
            $scope.parseLinkedImg(data.messageDateTime);
            $("#messagePanel").scrollTop($("#messagePanel")[0].scrollHeight);
            //navigator.vibrate([1000]);
            break;
        }
        case 4 : {
            var targetIndexToRemove = $scope.typing.indexOf(data.sender);
            if( targetIndexToRemove > -1 ){
                clearTimeout( $scope.timeout );
            } else {
                $scope.typing.push(data.sender);
                targetIndexToRemove = $scope.typing.length -1;
                $("#"+data.sender).addClass("btn-success");
                $("#"+data.sender).addClass("active");
                $scope.$apply();
            }
            $scope.timeout = setTimeout( function(){
                $("#"+data.sender).removeClass("btn-success");
                $("#"+data.sender).removeClass("active");
                $scope.typing.splice( targetIndexToRemove, 1 );
                $scope.$apply();
            }, 1500);
            break;
        }
        case 5 : {
            $scope.settings.browserNotify = data.browserNotification;
            $scope.settings.notificationSound = data.soundNotification;
            $scope.settings.typingText = data.typingText;
            $scope.$apply();
            break;
        }
        default : {
            console.log( "default");
            console.log( data );
        }
    }
  };

  var checkLeft = function( newUserList ){
    $scope.users.forEach( function( user ){
        var found = false;
        newUserList.forEach(function( newUser ){
            if( user.name === newUser.name ){
                found = true;
            }
        });
        if( !found ){
            var date = $scope.getLocalDateTime();
            var dateString = date.toISOString();
            $scope.messages.push({"textMessage":"-- " + user.name + " Left! --","sender":"","messageDateTime":dateString});
        }
    });
  }

var windowActive = true;
$(window).focus(function() {
    windowActive = true;
});

$(window).blur(function() {
    windowActive = false;
});

  var notify = function( data ){
    if( $scope.settings.browserNotify && !windowActive ) {
        var options = {
            body: data.textMessage,
            dir : "ltr"
        };
        var notification = null;
        if(Notification.permission === "granted") {
            notification = new Notification(data.sender,options);
        } else {
            if (Notification.permission !== 'denied') {
                Notification.requestPermission(function (permission) {
                    if (!('permission' in Notification)) {
                        Notification.permission = permission;
                    }
                });
            }
            if (permission === "granted") {
                notification = new Notification(data.sender,options);
            }
        }
        if( notification != null ){

            $scope.browserNotifications.push(notification);

            notification.onclick = function()
            {
                window.focus();
                notification.close();
                $("#messageText").focus();
                //$scope.messageFocus();
            };
        }
    }
    if( $scope.settings.notificationSound ) {
        console.log("in sound notify");
        document.getElementById('audioNotification').play();
    }
  }

  var newSession = function( name ){
      socketClient.send( "0|" + JSON.stringify({"name":name}) );
  }

  var getSettings = function( name ){
    /*
      socketClient.send( "5|"+JSON.stringify({"name":name}));
      */
  }

  $scope.saveSettings = function(){
  /*
      socketClient.send( "6|"+JSON.stringify({
        "userName":$scope.userName,
        "messageType":6,
        "soundNotification":$scope.notificationSound,
        "browserNotification":$scope.browserNotify,
        "typingText":$scope.typingText,
        "colorHex":""
      }));
      */
  }

  var setConnected = function(connected) {
      $scope.connected = connected;
      $scope.$apply();
  }

  var disconnect = function() {
      $("#connect").attr("disabled",false);
      socketClient.send( "1|" + JSON.stringify( $scope.getUser( $scope.name ) ) );
      setConnected(false);
      $scope.targetUsers = [];
      console.log("Disconnected");
  }

  var initialize = function() {
        $("#connect").attr("disabled",true);
        var socket = new SockJS('/websocket');
        var missed_heartbeats = 0;
        socket.onopen = function() {
         newSession( $scope.name );
         setConnected(true);
         $("#messageText").focus();

         var heartbeat = setInterval( function(){
            try {
                missed_heartbeats++;
                if (missed_heartbeats >= 3){
                    throw new Error("Too many missed heartbeats.");
                }
                socket.send("3|--Client Heartbeat--");
            } catch(e) {
                clearInterval(heartbeat);
                heartbeat = null;
                console.warn("Closing connection. Reason: " + e.message);
                setConnected(false);
                var date = $scope.getLocalDateTime();
                var dateString = date.toISOString();
                $scope.messages.push({"textMessage":"-- Connection to server lost --","sender":"","messageDateTime":dateString});
                socket.close();
            }
        }, 5000);
        //getSettings($scope.name);
        };


        socket.onmessage = function(e) {
         if (e.data === "--Server Heartbeat--") {
             // reset the counter for missed heartbeats
             missed_heartbeats = 0;
             return;
         }

         processMessage( JSON.parse( e.data ) );
        };

        socket.onerror = function(ev){
            console.log( ev.data );
        };

        socket.onclose = function(e) {
         disconnect();
         console.log('close');
         console.log(e);
         $scope.initUser();
        };

        socketClient = socket;
  };

  $scope.addMessage = function() {
      $scope.sendMessage($scope.message, $scope.targetUsers);
      var date = $scope.getLocalDateTime();
      var dateString = date.toISOString();
      $scope.messages.push({"textMessage":$scope.message,"sender":$scope.name,"messageDateTime" : dateString, "sentTo" : "Everyone"});
      $scope.message = "";
      $scope.$$postDigest(function(){
       $("#messagePanel").scrollTop($("#messagePanel")[0].scrollHeight);
       $scope.parseLinkedImg(dateString);
      },0,false);

  };

  $scope.clearMessages = function() {
    $scope.messages = [];
    $scope.$apply();
  };

  $scope.isAction = function( textString ) {
    return textString.indexOf( "/me" ) === 0;
  }

  $scope.actionMessage = function( textString, sender ) {
    return sender + " " + textString.substring( 4 );
  }

  $scope.parseLinkedImg = function(dateString) {
        console.log("1");
        console.log(dateString);
        if( $scope.settings.inlineImages ){
        console.log("2");
            var link = $(".messageText").last().find("a").attr("href");
            if( link ){
            console.log("3");
                if(link.indexOf(".gif") > -1 || link.indexOf(".jpeg") > -1 || link.indexOf(".jpg") > -1 || link.indexOf(".png") > -1 ){
                console.log("4");
                    $scope.messages.push({"textMessage":"","sender":"","messageDateTime" : dateString, "sentTo" : "", "hasImage":true,"imageUrl":link});
                    console.log("5");
                    $scope.$apply();
                    $scope.$$postDigest(function(){
                           $("#messagePanel").scrollTop($("#messagePanel")[0].scrollHeight);
                           console.log("6");
                    },0,false);
                }
            }
        }
  };

  $scope.sendPrivateMessage = function( userName ){
    if( userName.indexOf(" @") > -1 ){
        userName = userName.substring( 0,userName.indexOf(" @") );
    }
    $scope.privateMessageTargetUser = userName;
    var modalInstance = $modal.open({
      templateUrl: 'privateModal.html',
      controller: 'PrivateMessageCtrl',
      scope: $scope
    });
  }

  $scope.getUser = function( userName ){
    var selectedUser = null;
    $scope.users.forEach( function( user ){
        if( user.name === userName ){
            selectedUser = user;
        }
    });
    return selectedUser;
  }

  $scope.initUser = function(){
    //var duplicate = false;
    //console.log("init");
    //$($scope.users).each(function(index,user){
    //console.log(user.name);
    //console.log($scope.name);
    //  if(user.name === $scope.name){
    //      duplicate = true;
    //  }
    //});
    //if( duplicate ){
    //    window.alert($scope.name+" is already in there! Go away Kyle!");
    //} else {
        initialize();
    //}
  };

  $scope.disconnect = function(){
    console.log("disconnect");
    disconnect();
  }

  $scope.messageKeypress = function( $event ){
    if ($event.keyCode == 13){
        if( !$event.shiftKey ){
            $scope.addMessage();
        }
        else {
            $scope.message +="\n";
        }
    }
    if( !$scope.cooldown ){
        $scope.cooldown = true;
        socketClient.send("4|"+JSON.stringify({
            "sender":$scope.name,
            "targetUsers":$scope.targetUsers
        }))
        setTimeout( function(){
            $scope.cooldown = false;
        }, 1500 );
    }
    $scope.messageFocus();
  }

  $scope.messageFocus = function(){
    $scope.browserNotifications.forEach( function( notify ){
        notify.close();
    } );
    $scope.browserNotifications = [];
  }

  $scope.nameKeypress = function( $event ){
    if ($event.keyCode == 13){
        $scope.initUser();
    }
  }

  $scope.settingsModal = function () {
        var modalInstance = $modal.open({
          templateUrl: 'settingsModal.html',
          controller: 'SettingsCtrl',
          scope: $scope
        });
    };

  window.onbeforeunload = function(e){
          disconnect();
      };

});

chatApp.controller('PrivateMessageCtrl', ['$scope', function ($scope){
    var user = [];
    user.push( $scope.getUser($scope.privateMessageTargetUser) );

    $scope.privateMessageKeypress = function( $event ){
        if ($event.keyCode == 13){
                if( !$event.shiftKey ){
                    $scope.addPrivateMessage();
                }
                else {
                    $scope.message +="\n";
                }
            }
      }

    $scope.addPrivateMessage = function(){
        $scope.sendMessage($scope.privateMessage, user);
        var date = $scope.getLocalDateTime();
        var dateString = date.toISOString();
        $scope.messages.push({"textMessage":$scope.privateMessage,"sender":$scope.name+" @"+$scope.privateMessageTargetUser,"messageDateTime" : dateString, "sentTo" : $scope.privateMessageTargetUser});
        $scope.privateMessage = "";
        $scope.$$postDigest(function(){
         $("#messagePanel").scrollTop($("#messagePanel")[0].scrollHeight);
        },0,false);
    }
}]);


chatApp.controller('SettingsCtrl', ['$scope', function ($scope){

    $scope.toggleBrowserNotify = function(){

      $scope.saveSettings();
      console.log("browser notify "+ $scope.settings.browserNotify);
    }

    $scope.toggleNotificationSound = function(){

      $scope.saveSettings();
      console.log("sound notify "+ $scope.settings.notificationSound);
    }

    $scope.toggleTypingText = function(){

      $scope.saveSettings();
      console.log("Typing Text "+ $scope.settings.typingText);
    }

    $scope.toggleInlineImages = function(){

      $scope.saveSettings();
      console.log("Inline images "+ $scope.settings.inlineImages);
    }
}]);