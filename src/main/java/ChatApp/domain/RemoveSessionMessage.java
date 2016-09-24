package ChatApp.domain;

/**
 * Created by Michael on 13/04/2015.
 */
public class RemoveSessionMessage {

    private String userName;

    public RemoveSessionMessage(){}

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
