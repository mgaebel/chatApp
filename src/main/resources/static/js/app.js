var chatApp = angular.module("chatApp", ['ui.bootstrap',
  "chatApp.controllers", "ngSanitize", "colorpicker.module", "ngRoute", "chart.js"
]);

function AuthorizationError(description) {
    this.message = "Forbidden";
    this.description = description || "User authentication required.";
}

AuthorizationError.prototype = Object.create(Error.prototype);
AuthorizationError.prototype.constructor = AuthorizationError;

chatApp.config(function($routeProvider) {
    $routeProvider

        // route for the messenger
        .when('/chat', {
            templateUrl : 'messenger.html',
            controller  : 'ChatCtrl'
        })

        // route for the fileServer
        .when('/fileServer', {
            templateUrl : 'fileServer.html',
            controller  : 'FileServerController',
            authorize : true
        })

        .when('/metrics', {
            templateUrl : 'metrics.html',
            controller  : 'MetricsController',
            controllerAs: 'metricCtrl',
            authorize : true
        })

        .otherwise({
            redirectTo: "/chat"
        })
}).run(["$rootScope", "$location", function($rootScope, $location) {
    $rootScope.$on("$routeChangeStart", function(evt, to, from) {
        // requires authorization?
        if (to.authorize === true && !$rootScope.loggedInUser.authenticated) {
            evt.preventDefault();
            $location.path("/chat");
            throw new AuthorizationError();
        }
    });
    $rootScope.$on("$routeChangeError", function(evt, to, from, error) {
        if (error instanceof AuthorizationError){
            $location.path("/chat");
        }
    });
}]).service;

angular.module("chatApp.controllers", []);

angular.module("chatApp.controllers").controller("ChatCtrl", function($scope,$rootScope,$modal,$location,$routeParams) {
  $rootScope.loggedInUser = {
    authenticated: false
  };
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
    "inlineImages":true,
    "labelColor":"#5cb85c"
  }
  var socketClient;

  $scope.getLocalDateTime = function(){
      var d = new Date();
      timeOffsetInHours = (d.getTimezoneOffset()/60) * (-1);
      d.setHours(d.getHours() + timeOffsetInHours);
      return d;
  }

  $scope.sendMessage = function(text,targetUsers) {
      socketClient.send("2|"+JSON.stringify({
        "textMessage" : text,
        "targetUsers" : targetUsers,
        "sender" : $scope.name,
        "labelColor" : $scope.settings.labelColor
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
            },1);
            console.log($scope.users);
            break;
        }
        case 2 : {
            data.textMessage = $scope.styleMessageText(data.textMessage);
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
        case 6 : {
            if( typeof data.browserNotification != 'undefined' ){
                $scope.settings.browserNotify = data.browserNotification;
            }
            if( typeof data.soundNotification != 'undefined' ){
                $scope.settings.notificationSound = data.soundNotification;
            }
            if( typeof data.typingText != 'undefined' ){
                $scope.settings.typingText = data.typingText;
            }
            if( typeof data.inlineImages != 'undefined' ){
                $scope.settings.inlineImages = data.inlineImages;
            }
            if( typeof data.labelColor != 'undefined' ){
                $scope.settings.labelColor = data.labelColor;
            }
            setTimeout( function(){
                $scope.$apply();
            },100);
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
    if( ($scope.settings.browserNotify || data.sender.indexOf(" @") > -1) && !windowActive ) {
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
            if (Notification.permission === "granted") {
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

  $scope.saveSettings = function(){
      socketClient.send( "6|"+JSON.stringify({
        "name":$scope.name,
        "messageType":6,
        "soundNotification":$scope.settings.notificationSound,
        "browserNotification":$scope.settings.browserNotify,
        "typingText":$scope.settings.typingText,
        "inlineImages":$scope.settings.inlineImages,
        "labelColor":$scope.settings.labelColor
      }));
  }

  var setConnected = function(connected) {
      $scope.connected = connected;
      $scope.$apply();
  }

  var disconnect = function() {
      $("#connect").attr("disabled",false);
      socketClient.send( "1|{'userName':'" + $scope.name +"'}");
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
                disconnect();
                socket.close();

                setTimeout(function(){
                    $scope.messages.push({"textMessage":"-- Reconnecting... --","sender":"","messageDateTime":dateString});
                    $scope.initUser();
                },500);
            }
        }, 5000);
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
         var dateString = date.toISOString();
         console.log('closed!!');
         console.log(e);
         setTimeout( function(){
            $scope.messages.push({"textMessage":"-- Reconnecting... --","sender":"","messageDateTime":dateString});
            $scope.initUser();
         }, 500);
        };

        socketClient = socket;
  };

  $scope.addMessage = function() {
      $scope.sendMessage($scope.message, $scope.targetUsers);
      var date = $scope.getLocalDateTime();
      var dateString = date.toISOString();
      var messageText = $scope.styleMessageText( $scope.message );
      console.log(messageText);
      $scope.messages.push({"textMessage":messageText,"sender":$scope.name,"messageDateTime" : dateString, "sentTo" : "Everyone", "labelColor" : $scope.settings.labelColor});
      console.log($scope.messages);
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

  $scope.styleMessageText = function( messageText ) {
    console.log( messageText );
    messageText = messageText.replace(/(^|\s)_([^_]+)_(\s|$)/g,"$1<i>$2</i>$3");
    messageText = messageText.replace(/(^|\s)\*([^\*]+)\*(\s|$)/g,"$1<b>$2</b>$3");
    return messageText;
  };

  $scope.parseLinkedImg = function(dateString) {
        if( $scope.settings.inlineImages ){
            var link = $(".messageText").last().find("a").attr("href");
            if( link ){
                if(link.indexOf(".gif") > -1 || link.indexOf(".jpeg") > -1 || link.indexOf(".jpg") > -1 || link.indexOf(".png") > -1 ){
                    $(".messageText").last().find("a").detach();
                    $scope.messages.push({"textMessage":"","sender":"","messageDateTime" : dateString, "sentTo" : "", "hasImage":true,"imageUrl":link});
                    $scope.$apply();
                    $scope.$$postDigest(function(){
                           $("#messagePanel").scrollTop($("#messagePanel")[0].scrollHeight);
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
        if( user.name == userName ){
            selectedUser = user;
        }
    });
    return selectedUser;
  }

  $scope.initUser = function(){
    initialize();
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

  $scope.openMetrics = function() {
    var username = $scope.name;
    if($rootScope.loggedInUser.authenticated){
        $location.path("/metrics").search({userName : username});
    } else {
        var loginModalInstance = $modal.open({
            templateUrl: 'loginModal.html',
            controller: 'LoginCtrl',
            controllerAs: 'loginCtrl',
            scope: $scope,
            resolve: {
                path : function() {
                    return "/metrics";
                }
            }
        });
    }
  }

  $scope.openFileServer = function() {
    var username = $scope.name;
    if($rootScope.loggedInUser.authenticated){
        $location.path("/fileServer").search({userName : username});
    } else {
        var loginModalInstance = $modal.open({
            templateUrl: 'loginModal.html',
            controller: 'LoginCtrl',
            controllerAs: 'loginCtrl',
            scope: $scope,
            resolve: {
                path : function() {
                    return "/fileServer";
                }
            }

        });
    }
  };

  window.onbeforeunload = function(e){
    console.log("disconnecting...");
    disconnect();
  };

  if( $routeParams.userName ){
    console.log("in user thingy");
    $scope.name = $routeParams.userName;
    $scope.initUser();
  }

});

chatApp.factory('MetricService', function($http,$rootScope) {
    return {
        list : function() {
            return $http.get("/metrics").then( function successCallback(response){
                console.log(response);
                return response.data;
            });
        },
        get : function(metricId) {
            return $http.get("/metric/"+metricId).then( function successCallback(response){
                console.log(response);
                return response.data;
            });
        }
    };
});

chatApp.factory('LoginService', function($http,$rootScope) {
    return {
        login : function(username, password) {
            return $http.post("/login",{"username": username, "password": password}).then( function successCallback(response){
                console.log( response );
                $rootScope.loggedInUser = response.data;
            });
        },
    };
});

chatApp.directive('parseUrl', function () {
     var urlPattern = /(http|ftp|https):\/\/[\w-]+(\.[\w-]+)+([\w.,@?^=%&amp;:\/~+#-]*[\w@?^=%&amp;\/~+#-])?/gi;
     return {
         restrict: 'A',
         require: 'ngModel',
         replace: true,
         scope: {
             props: '=parseUrl',
             ngModel: '=ngModel'
         },
         link: function compile(scope, element, attrs, controller) {
             scope.$watch('ngModel', function (value) {
                 var html = value.replace(urlPattern, '<a target="' + scope.props.target + '" href="$&">$&</a>') + " | " + scope.props.otherProp;
                 element.html(html);
             });
         }
     };
 });

chatApp.filter('parseUrlFilter', function () {
    var urlPattern = /(http|ftp|https):\/\/[\w-]+(\.[\w-]+)+([\w.,@?^=%&amp;:\/~+#-]*[\w@?^=%&amp;\/~+#-])?/gi;
    return function (text, target ) {
        return text.replace(urlPattern, '<a target="' + target + '" href="$&">$&</a>');
    };
});

chatApp.directive('ngBindHtmlUnsafe', ['$sce', function($sce){
    return {
        scope: {
            ngBindHtmlUnsafe: '=',
        },
        template: "<span ng-bind-html='trustedHtml'></span>",
        link: function($scope, iElm, iAttrs, controller) {
            $scope.updateView = function() {
                $scope.trustedHtml = $sce.trustAsHtml($scope.ngBindHtmlUnsafe);
            }

            $scope.$watch('ngBindHtmlUnsafe', function(newVal, oldVal) {
                $scope.updateView(newVal);
            });
        }
    };
}]);

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
        $scope.messages.push({"textMessage":$scope.privateMessage,"sender":$scope.name+" @"+$scope.privateMessageTargetUser,"messageDateTime" : dateString, "sentTo" : $scope.privateMessageTargetUser, "labelColor" : $scope.settings.labelColor});
        $scope.privateMessage = "";
        $scope.$$postDigest(function(){
         $("#messagePanel").scrollTop($("#messagePanel")[0].scrollHeight);
        },0,false);
    }
}]);

chatApp.controller('LoginCtrl', function ($scope, LoginService, $location, $rootScope, $modalInstance, path){
    var loginCtrl = this;
    loginCtrl.userName = $scope.name;
    loginCtrl.password = "";
    loginCtrl.invalid = false;
    console.log(path);
    loginCtrl.path = path;

    loginCtrl.keyPress = function($event){
        if ($event.keyCode == 13){
            loginCtrl.login();
        }
    };

    loginCtrl.login = function(){
        LoginService.login(loginCtrl.userName, loginCtrl.password).then( function( data ){
                if( $rootScope.loggedInUser.authenticated ){
                    loginCtrl.invalid = false;
                    console.log("attempting to go to fileServer "+ loginCtrl.path);
                    $location.path(loginCtrl.path).search({userName: loginCtrl.userName});
                    $modalInstance.close();
                } else {
                    loginCtrl.invalid = true;
                }
            }
        );
    };
});

chatApp.controller('MetricsController', function ($scope,$location,$http,$routeParams,MetricService){
    $scope.userName = $routeParams.userName;
    $scope.metricList = [];
    $scope.init = function(){
        $scope.metricList = MetricService.list($scope.userName);
    };

    $scope.messenger = function(){
        $location.path("/chat").search({userName : $routeParams.userName});
    };

    $scope.addMeasurement = function(metricId){
        var measurementModal = $modal.open({
          templateUrl: 'measurement.html',
          controller: 'MeasurementController',
          scope: $scope,
          resolve:{
            metric: function(){
                return MetricService.get(metricId);
            }
          }
        });
    };

});

chatApp.controller('MeasurementController', function ($scope,$location,$http,metric){

});

chatApp.controller('FileServerController', function ($scope,$location,$http,$routeParams){
    $scope.fileTree = [];

    $scope.messenger = function(){
        $location.path("/chat").search({userName : $routeParams.userName});
    };

    $scope.init = function(){
        $scope.populateFileTree();
    };

    $scope.download = function( pathRoot ){
        var url = "/download?path="+pathRoot+"&zip=false";
        window.open(encodeURI(url),"_blank");
    };

    $scope.downloadDirectory = function( node ){
        node.compressing = true;
        $http.post( "/zipDir?path="+node.path ).then( function successCallback(response){
            node.compressing = false;
            console.log("zipResp: "+response.data.zipPath);
            var url = "/download?path="+response.data.zipPath+"&zip=true";
            window.open(encodeURI(url), "_blank");
        });
    };

    $scope.googleItem = function( itemName, type ){
        var url;
        if( type == "Anime" ){
            url = "https://anidb.net/perl-bin/animedb.pl?show=animelist&do.search=Search&adb.search="+itemName;
        } else {
            url = "http://www.imdb.com/find?ref_=nv_sr_fn&q="+itemName+"&s=all";
        }
        window.open(encodeURI(url),"_blank")
    };

    $scope.populateFileTree = function( node ){
        var path;
        if( node ){
            if( node.open ){
                node.open = false;
                node.children = [];
                return;
            }
            node.loading = true;
            path = node.path;
            console.log("pathroot "+node.path);
        }
        $http.post("/fileTree?pathRoot="+path).then(function successCallback(response){
            console.log(response);
            if( !path ){
                $scope.fileTree = response.data;
            } else {
                populateChildren( path, $scope.fileTree, response.data );
            }
        });
    }

    function populateChildren( target, searchList, newList ){
        if( searchList.length > 0 ){
            searchList.forEach( function( node ){
                if( target == node.path ){
                    node.open = true;
                    node.children = newList;
                    node.loading = false;
                    return;
                } else {
                    if( target.indexOf(node.path) > -1 ){
                        populateChildren( target, node.children, newList );
                    }
                }
            });
        }
    }
});


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

    $scope.setLabelColor = function(){

        $scope.saveSettings();
        console.log("Label color "+ $scope.settings.labelColor);
    }
}]);