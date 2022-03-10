package client;


import org.junit.Before;
import org.junit.Test;
import server.Server;

import static org.junit.Assert.assertEquals;

public class ClientTest {

//    @Before
//    public void setup() {
//        Server server = new Server();
//        server.start(6666);
//    }

    @Test
    public void givenGreetingClient_whenServerRespondsWhenStarted_thenCorrect() {
        Client client = new Client();
        client.startConnection("localhost", 6666);
        String response = client.sendMessage("hello server");
        assertEquals("hello client", response);
    }
}
