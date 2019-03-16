package ChatApp.domain;

import java.util.List;
import java.util.Set;

public class UserListMessage {

    private int messageType = 1;
    private boolean disconnecting = false;
    private Set<User> users;
    private String newSessionAdded = "";

    public UserListMessage(){}

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public boolean isDisconnecting() {
        return disconnecting;
    }

    public void setDisconnecting(boolean disconnecting) {
        this.disconnecting = disconnecting;
    }

    public String getNewSessionAdded() {
        return newSessionAdded;
    }

    public void setNewSessionAdded(String newSessionAdded) {
        this.newSessionAdded = newSessionAdded;
    }
}
