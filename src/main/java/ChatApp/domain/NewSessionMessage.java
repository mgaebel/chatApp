package ChatApp.domain;

import java.time.LocalDate;

public class NewSessionMessage {

    private String name;

    private LocalDate sessionDate;

    public NewSessionMessage(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDate sessionDate) {
        this.sessionDate = sessionDate;
    }
}
