package ChatApp.domain;

import java.util.List;

public class TypingMessage {

    private String sender;
    private List<User> targetUsers;

    public TypingMessage(){}

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public List<User> getTargetUsers() {
        return targetUsers;
    }

    public void setTargetUsers(List<User> targetUsers) {
        this.targetUsers = targetUsers;
    }
}
