package common;

import java.io.Serializable;

public class Message implements Serializable {
    public static final long serialVersionUID = 1L;
    public final String sender;
    public final String message;
    public final MessageType type;

    private Message(String sender, String message, MessageType type){
        this.sender = sender;
        this.message = message;
        this.type = type;
    }

    public static Message userLeft(String user) {
        return new Message(user, "", MessageType.USER_LEFT);
    }

    public static Message userJoined(String user){
        return new Message(user, "", MessageType.USER_JOINED);
    }

    public static Message createUserMessage(String user, String message){
        return new Message(user, message, MessageType.USER_MESSAGE);
    }

    public static Message addActiveUser(String user){
        return new Message(user, "", MessageType.ADD_ACTIVE_USER);
    }

    public enum MessageType{
        USER_JOINED,
        USER_LEFT,
        USER_MESSAGE,
        ADD_ACTIVE_USER
    }
}
