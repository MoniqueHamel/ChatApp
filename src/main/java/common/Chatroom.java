package common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Chatroom {
    private Set<String> members = new HashSet<>();
    private List<Message> chatHistory = new ArrayList<>();

    public void addMember(String username){
        members.add(username);
    }

    public void addMessage(Message msg){
        chatHistory.add(msg);
    }

    public static String getChatroomIdFor(String sender, String destination){
        if (sender.compareTo(destination) <= 0){
            return String.format("%s-%s", sender, destination);
        } else {
            return String.format("%s-%s", destination, sender);
        }
    }
}
