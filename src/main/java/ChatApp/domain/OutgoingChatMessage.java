package ChatApp.domain;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by Michael on 17/03/2015.
 */
public class OutgoingChatMessage {
    private LocalDateTime messageTime;
    private String sender;
    private String textMessage;
    private String sentTo;
    private int messageType;

    public OutgoingChatMessage(){}

    public String getTextMessage() {
        return textMessage;
    }

    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
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

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public String getSentTo() {
        return sentTo;
    }

    public void setSentTo(String sentTo) {
        this.sentTo = sentTo;
    }
}
