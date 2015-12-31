package ChatApp.domain;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by Michael on 17/03/2015.
 */
public class IncomingChatMessage {
    private List<User> targetUsers;
    private LocalDateTime messageTime;
    private String sender;
    private String textMessage;

    public IncomingChatMessage(){}

    public String getTextMessage() {
        return textMessage;
    }

    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
    }

    public List<User> getTargetUsers() {
        return targetUsers;
    }

    public void setTargetUsers(List<User> targetUsers) {
        this.targetUsers = targetUsers;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public LocalDateTime getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(LocalDateTime messageTime) {
        this.messageTime = messageTime;
    }
}
