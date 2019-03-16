package ChatApp.domain;

public class UserSettings {
    String name;
    boolean soundNotification;
    boolean browserNotification;
    boolean typingText;
    String labelColor;
    boolean inlineImages;
    int messageType = 5;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getLabelColor() {
        return labelColor;
    }

    public void setLabelColor(String labelColor) {
        this.labelColor = labelColor;
    }

    public boolean getInlineImages() {
        return inlineImages;
    }

    public void setInlineImages(boolean inlineImages) {
        this.inlineImages = inlineImages;
    }
}
