package server;

public class UserCredentials {
    public final String username;
    public final String password;

    public UserCredentials(){
        username = null;
        password = null;
    }

    public UserCredentials(String username, String password){
        this.username = username;
        this.password = password;
    }

    public String getUsername(){
        return username;
    }


}
