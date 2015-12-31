package ChatApp.domain;

/**
 * Created by Michael on 13/04/2015.
 */
public class RemoveSessionMessage {

    private User user;

    public RemoveSessionMessage(){}

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
