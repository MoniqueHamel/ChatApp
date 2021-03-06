package common;

import java.io.Serializable;

public class Message implements Serializable {
    public static final long serialVersionUID = 1L;
    public String sender;
    public String message;
    public MessageType type;
    public String destination;
    public static final String GLOBAL = "#Global";

    private Message(){
        sender = null;
        message = null;
        type = null;
        destination = null;
    }

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

    public static Message registeredUsersList(String destination, String list){
        return new Message("", destination, list, MessageType.REGISTERED_USERS_LIST);
    }

    public static Message chatroomCreated(String sender, String destination, String chatroomName){
        return new Message(sender, destination, chatroomName, MessageType.CHATROOM_CREATED);
    }

    public static Message userChatroomList(String destination, String list){
        return new Message("", destination, list, MessageType.USER_CHATROOM_LIST);
    }

    public static Message inviteUser(String sender, String destination, String chatroomId){
        return new Message(sender, destination, chatroomId, MessageType.INVITE_USER);
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
        REGISTER_SUCCESSFUL,
        REGISTERED_USERS_LIST,
        CHATROOM_CREATED,
        USER_CHATROOM_LIST,
        INVITE_USER
    }
}
