package ChatApp.domain;

/**
 * Created by Michael on 11/25/2015.
 */
public class UserSettings {
    String userName;
    boolean soundNotification;
    boolean browserNotification;
    boolean typingText;
    String colorHex;
    int messageType = 5;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public boolean isSoundNotification() {
        return soundNotification;
    }

    public void setSoundNotification(boolean soundNotification) {
        this.soundNotification = soundNotification;
    }

    public boolean isBrowserNotification() {
        return browserNotification;
    }

    public void setBrowserNotification(boolean browserNotification) {
        this.browserNotification = browserNotification;
    }

    public boolean isTypingText() {
        return typingText;
    }

    public void setTypingText(boolean typingText) {
        this.typingText = typingText;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }
}
