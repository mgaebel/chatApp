package ChatApp.domain;

import ChatApp.repository.UserRepository;
import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.simp.user.UserSessionRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.io.Files.readLines;
import static com.google.common.io.Files.toString;
import static org.springframework.util.StringUtils.hasText;

@Component
public class SessionMessageHandler {

    Map<User, WebSocketSession> activeSessions = new HashMap<>();
    GsonBuilder gsonBuilder;
    Gson gson;
    @Autowired
    private UserRepository userRepository;

    public SessionMessageHandler(){
        gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter( UserListMessage.class, new UserListMessageSerializer() );
        gson = gsonBuilder.create();
    }

    public void handleNewSessionMessage( NewSessionMessage newSessionMessage, WebSocketSession session ){
        boolean alreadyActive = false;
        User user = new User();
        if( hasText( newSessionMessage.getName() ) ) {
            for (User activeUser : activeSessions.keySet()) {
                if (activeUser.getName().equalsIgnoreCase(newSessionMessage.getName())) {
                    activeUser.setLocalDateTime(LocalDateTime.now());
                    alreadyActive = true;
                    user = activeUser;
                    user.setStatus(StatusEnum.ONLINE);
                    //override old session.
                    activeSessions.put(user, session);
                    break;
                }
            }
            if (!alreadyActive) {
                List<User> dbUsers = userRepository.findByName(newSessionMessage.getName());
                if (!dbUsers.isEmpty()) {
                    user = dbUsers.get(0);
                    user.setStatus(StatusEnum.ONLINE);
                    userRepository.save(user);
                    activeSessions.put(user, session);
                } else {
                    user = new User();
                    user.setName(newSessionMessage.getName());
                    user.setLocalDateTime(LocalDateTime.now());
                    user.setStatus(StatusEnum.ONLINE);
                    user = userRepository.save(user);
                    activeSessions.put(user, session);
                }
            }
            try {
                applySavedUserSettings(user, session);
                updateUserList(session, false, user.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleRemoveSessionMessage( RemoveSessionMessage removeSessionMessage, WebSocketSession session ){

        if( hasText( removeSessionMessage.getUserName() ) ) {
            User user = getActiveUserByName(removeSessionMessage.getUserName());

            getActiveSessions().remove(user);
            try {
                updateUserList(session, true, "");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void applySavedUserSettings( User user, WebSocketSession session) throws IOException{
        UserSettings settings = user.getUserSettings();
        settings.setMessageType(6);
        String jsonString = gson.toJson(settings);
        session.sendMessage(new TextMessage(jsonString));
        /*
        File file = new File("C:\\Users\\mgaeb\\App\\" + user.getName() + ".json");
        if( file.exists() ) {
            String jsonString = com.google.common.io.Files.toString(file, Charsets.UTF_8);
            session.sendMessage(new TextMessage(jsonString));
        }
        */
    }

    public void handleSettingsSave( UserSettings settingsSaveMessage, WebSocketSession session) throws IOException {
        User user = userRepository.findByName( settingsSaveMessage.getName() ).get(0);
        user.setUserSettings( settingsSaveMessage );
        userRepository.save(user);

        /*
        String userSettings = gson.toJson(settingsSaveMessage, UserSettings.class);
        FileWriter file = new FileWriter(new File("C:\\Users\\mgaeb\\App\\"+settingsSaveMessage.getName()+".json"));
        try{
            file.write( userSettings );
        } catch ( Exception e ){
            throw new IOException( e );
        } finally {
            file.close();
        }
        */
    }

    private User getActiveUser(User user) {
        for(User activeUser : getActiveSessions().keySet()){
            if( user.equals( activeUser ) ){
                return user;
            }
        }
        return null;
    }

    private User getActiveUserByName( String name ){
        for(User activeUser : getActiveSessions().keySet()){
            if( name.equals( activeUser.getName() ) ){
                return activeUser;
            }
        }
        return null;
    }


    public void updateUserList( WebSocketSession session, boolean disconnecting, String newSessionAdded ) throws IOException {

        UserListMessage userListMessage = new UserListMessage();
        userListMessage.setUsers( getActiveSessions().keySet() );
        userListMessage.setMessageType( 2 );
        userListMessage.setDisconnecting( disconnecting );
        userListMessage.setNewSessionAdded( newSessionAdded );

        String message = gson.toJson( userListMessage );
        session.sendMessage(new TextMessage( message ));

        userListMessage.setDisconnecting( false );
        message = gson.toJson( userListMessage );
        for( WebSocketSession otherSession : getActiveSessions().values() ){
            if( otherSession != session ) {
                otherSession.sendMessage(new TextMessage(message));
            }
        }
    }


    public Map<User, WebSocketSession> getActiveSessions(){
        return activeSessions;
    }

    public void handleHearteatMessage(WebSocketSession session) throws IOException {
        session.sendMessage( new TextMessage("--Server Heartbeat--") );
    }
}
