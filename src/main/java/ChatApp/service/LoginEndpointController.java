package ChatApp.service;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Objects.equal;

@RestController
public class LoginEndpointController {

    Map<String,String> userAuthMap = new HashMap<>();
    Map<String,String> userRoleMap;

    public LoginEndpointController() {
        userAuthMap.put("michael","fileAdmin");
        userAuthMap.put("kevan","kevan");
        userAuthMap.put("kirk","kirk");
        userAuthMap.put("kyle","kyle");
        userAuthMap.put("corey","corey");

        userRoleMap = new HashMap<>();
        userRoleMap.put("michael","ADMIN");
        userRoleMap.put("kevan","General");
        userRoleMap.put("kirk","General");
        userRoleMap.put("kyle","General");
        userAuthMap.put("corey","General");
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public AuthResponse login(@RequestBody AuthRequest authRequest ){
        try {
            if( equal( userAuthMap.get( authRequest.getUsername().toLowerCase() ), authRequest.getPassword() ) ){
                AuthResponse authResponse = new AuthResponse();
                authResponse.setRole( userRoleMap.get( authRequest.getUsername().toLowerCase() ) );
                authResponse.setAuthenticated(true);
                authResponse.setUserName( authRequest.getUsername() );
                return authResponse;
            }
            throw new AuthenticationException("Invalid");
        } catch (Exception e) {
            AuthResponse authResponse = new AuthResponse();
            authResponse.setUserName(authRequest.getUsername());
            authResponse.setAuthenticated(false);
            authResponse.setRole("General");
            return authResponse;
        }
    }

    public static class AuthResponse {
        private boolean authenticated;
        private String userName;
        private String role;

        public AuthResponse(){}

        public boolean isAuthenticated() {
            return authenticated;
        }

        public void setAuthenticated(boolean authenticated) {
            this.authenticated = authenticated;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

    public static class AuthRequest {
        private String username;
        private String password;

        public AuthRequest(){}

        public AuthRequest(String username, String password){
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
