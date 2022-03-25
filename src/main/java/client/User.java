package client;

public class User {
    public final String username;
    public final String password;

    public User(){
        username = null;
        password = null;
    }

    public User(String username, String password){
        this.username = username;
        this.password = password;
    }

    public String getUsername(){
        return username;
    }


}
