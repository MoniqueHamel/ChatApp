package common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import server.Server;

import java.util.List;

public class Chatroom {

    private static final Logger log = LoggerFactory.getLogger(Server.class);
    private static final Jedis jedis = new Jedis("localhost", 6379);
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String getChatroomIdFor(String sender, String destination){
        if (sender.compareTo(destination) <= 0){
            return String.format("%s-%s", sender, destination);
        } else {
            return String.format("%s-%s", destination, sender);
        }
    }

    public static void addMessageToChatHistory(String chatroomId, Message message) {
        try {
            String messageString = mapper.writeValueAsString(message);
            jedis.rpush(chatroomId + "_chatHistory", messageString);
        } catch (JsonProcessingException e) {
            log.error("Could not save message to chat.", e);
        }
    }

    public static boolean doesChatroomExist(String chatroomId) {
        return jedis.sismember("chatroomIds", chatroomId);
    }

    public static void createChatroom(String chatroomId, List<String> memberUsernames) {
        jedis.sadd("chatroomIds", chatroomId);
        memberUsernames.forEach((username) -> {
            jedis.sadd(chatroomId + "_members", username);
            //Todo: use pipelining when adding users here.
        });
    }

    public static void addUserToChatroom(String chatroomId, String username) {
        jedis.sadd(chatroomId + "_members", username);
    }
}
