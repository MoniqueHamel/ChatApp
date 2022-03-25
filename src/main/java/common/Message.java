package common;

import java.io.Serializable;

public class Message implements Serializable {
    public static final long serialVersionUID = 1L;
    public final String sender;
    public final String message;
    public final MessageType type;
    public final String destination;

    private Message(String sender, String message, MessageType type){
        this.sender = sender;
        this.message = message;
        this.type = type;
        destination = null;
    }

    private Message(String sender, String destination, String message, MessageType type){
        this.sender = sender;
        this.message = message;
        this.type = type;
        this.destination = destination;
    }

    public static Message userLeft(String user) {
        return new Message(user, "", MessageType.USER_LEFT);
    }

    public static Message userJoined(String user){
        return new Message(user, "", MessageType.USER_JOINED);
    }

    public static Message createUserMessage(String sender, String destination, String message){
        return new Message(sender, destination, message, MessageType.USER_MESSAGE);
    }

    public static Message addActiveUser(String user){
        return new Message(user, "", MessageType.ADD_ACTIVE_USER);
    }

    public static Message attemptLogin(String sender, String password){
        return new Message(sender, password, MessageType.LOGIN_MESSAGE);
    }

    public static Message loginSuccessful(String destination){
        return new Message("", destination, "", MessageType.LOGIN_SUCCESSFUL);
    }

    public static Message loginFailed(String destination, String message){
        return new Message("", destination, message, MessageType.LOGIN_FAILED);
    }

    public static Message attemptRegistration(String sender, String password){
        return new Message(sender, password, MessageType.REGISTER_MESSAGE);
    }

    public static Message registerSuccessful(String destination){
        return new Message("", destination, "", MessageType.REGISTER_SUCCESSFUL);
    }

    public static Message registerFailed(String destination){
        return new Message("", destination, "Username already in use.", MessageType.REGISTER_FAILED);
    }

    public enum MessageType{
        USER_JOINED,
        USER_LEFT,
        USER_MESSAGE,
        ADD_ACTIVE_USER,
        LOGIN_MESSAGE,
        REGISTER_MESSAGE,
        LOGIN_FAILED,
        LOGIN_SUCCESSFUL,
        REGISTER_FAILED,
        REGISTER_SUCCESSFUL
    }
}
