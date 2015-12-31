package ChatApp.domain;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.google.common.io.Files.readLines;
import static com.google.common.io.Files.toString;

/**
 * Created by Michael on 14/04/2015.
 */
public class SessionMessageHandler {


    Map<User, WebSocketSession> activeSessions = new HashMap<User,WebSocketSession>();
    GsonBuilder gsonBuilder;
    Gson gson;


    public SessionMessageHandler(){
        gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter( UserListMessage.class, new UserListMessageSerializer() );
        gson = gsonBuilder.create();
    }

    public void handleNewSessionMessage( NewSessionMessage newSessionMessage, WebSocketSession session ){
        boolean alreadyActive = false;
        User user = new User();
        for( User activeUser : activeSessions.keySet() ){
            if( activeUser.getName().equalsIgnoreCase(newSessionMessage.getName())){
                activeUser.setLocalDateTime(LocalDateTime.now());
                alreadyActive = true;
                user = activeUser;
                break;
            }
        }
        if(!alreadyActive) {
            user = new User();
            user.setName(newSessionMessage.getName());
            user.setLocalDateTime(LocalDateTime.now());
            user.setStatus(StatusEnum.ONLINE);
            user.setId(UUID.randomUUID().toString());
            activeSessions.put(user, session);
        }
        try {
            updateUserList(session, false, user.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleRemoveSessionMessage( RemoveSessionMessage removeSessionMessage, WebSocketSession session ){

        User user = getActiveUser( removeSessionMessage.getUser() );

        getActiveSessions().remove(user);
        try {
            updateUserList(session, true, "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleSettingsRequest( SettingsRequestMessage settingsRequestMessage, WebSocketSession session) throws IOException{
        String jsonString = com.google.common.io.Files.toString(new File("C:/Users/Michael/App/" + settingsRequestMessage.getName() + ".json"), Charsets.UTF_8);
        //UserSettings userSettings = gson.fromJson( jsonString, UserSettings.class );
        session.sendMessage(new TextMessage(jsonString));
    }

    public void handleSettingsSave( UserSettings settingsSaveMessage, WebSocketSession session) throws IOException {
        String userSettings = gson.toJson(settingsSaveMessage, UserSettings.class);
        try {
            FileWriter file = new FileWriter("C:/Users/Michael/App/"+settingsSaveMessage.getUserName()+".json");
            file.write( userSettings );
        } catch ( Exception e ){
            throw new IOException( e );
        }
    }

    private User getActiveUser(User user) {
        for(User activeUser : getActiveSessions().keySet()){
            if( user.equals( activeUser ) ){
                return user;
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
