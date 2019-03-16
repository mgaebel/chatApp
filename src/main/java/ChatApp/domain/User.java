package ChatApp.domain;

import com.google.gson.GsonBuilder;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name="name")
    private String name;
    @Column(name="status")
    @Enumerated(EnumType.STRING)
    private StatusEnum status;
    @Column( name="date_time")
    private LocalDateTime localDateTime;
    @Column( name="sound_notification" )
    private Boolean soundNotification;
    @Column( name="browser_notification" )
    private Boolean browserNotification;
    @Column( name="typing_text" )
    private Boolean typingText;
    @Column( name="label_color" )
    private String labelColor;
    @Column(name="inline_images")
    private Boolean inlineImages;

    public User(){
        soundNotification = true;
        browserNotification = false;
        typingText = false;
        labelColor = "#00bcc7";
        inlineImages = true;
        status = StatusEnum.ONLINE;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public StatusEnum getStatus() {
        return status;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public Boolean getSoundNotification() {
        return soundNotification;
    }

    public void setSoundNotification(Boolean soundNotification) {
        this.soundNotification = soundNotification;
    }

    public Boolean getBrowserNotification() {
        return browserNotification;
    }

    public void setBrowserNotification(Boolean browserNotification) {
        this.browserNotification = browserNotification;
    }

    public Boolean getTypingText() {
        return typingText;
    }

    public void setTypingText(Boolean typingText) {
        this.typingText = typingText;
    }

    public String getLabelColor() {
        return labelColor;
    }

    public void setLabelColor(String labelColor) {
        this.labelColor = labelColor;
    }

    public Boolean getInlineImages() {
        return inlineImages;
    }

    public void setInlineImages(Boolean inlineImages) {
        this.inlineImages = inlineImages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (!id.equals(user.id)) return false;
        if (!name.equals(user.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserSettings getUserSettings() {
        UserSettings settings = new UserSettings();
        settings.setName(name);
        settings.setLabelColor(labelColor);
        settings.setBrowserNotification(browserNotification);
        settings.setInlineImages(inlineImages);
        settings.setSoundNotification(soundNotification);
        settings.setTypingText(typingText);
        return settings;
    }

    public void setUserSettings(UserSettings userSettings) {
        name = userSettings.getName();
        labelColor = userSettings.getLabelColor();
        browserNotification = userSettings.isBrowserNotification();
        inlineImages = userSettings.getInlineImages();
        soundNotification = userSettings.isSoundNotification();
        typingText = userSettings.isTypingText();
    }
}
