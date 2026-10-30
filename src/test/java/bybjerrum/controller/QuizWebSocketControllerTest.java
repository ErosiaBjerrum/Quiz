package bybjerrum.controller;

import bybjerrum.config.ApplicationConfig;
import bybjerrum.config.HibernateConfig;
import bybjerrum.provider.QuestionProvider;
import bybjerrum.security.TokenService;
import io.javalin.Javalin;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuizWebSocketControllerTest {

    private Javalin app;

    @BeforeEach
    void setUp() {
        EntityManagerFactory emf =
                HibernateConfig.getEntityManagerFactoryForTest();

        QuestionProvider testProvider = List::of;

        app = new ApplicationConfig()
                .createApp(emf, testProvider, new TokenService("test-secret"))
                .start(0);
    }

    @AfterEach
    void tearDown() {
        try {
            if (app != null) {
                app.stop();
            }
        } finally {
            HibernateConfig.shutdownTestEmf();
        }
    }

    @Test
    void testWebSocketConnection() throws Exception {

        CompletableFuture<String> receivedMessage = new CompletableFuture<>();

        try (HttpClient client = HttpClient.newHttpClient()) {

            WebSocket socket = client.newWebSocketBuilder()
                    .buildAsync(
                            URI.create("ws://localhost:" + app.port() + "/ws/quiz/1"),
                            new WebSocket.Listener() {
                                @Override
                                public java.util.concurrent.CompletionStage<?> onText(
                                        WebSocket webSocket,
                                        CharSequence data,
                                        boolean last) {

                                    receivedMessage.complete(data.toString());
                                    webSocket.request(1);
                                    return null;
                                }
                            }
                    ).get(5, TimeUnit.SECONDS);

            assertEquals("Connected to quiz 1",
                    receivedMessage.get(5, TimeUnit.SECONDS));

            socket.sendClose(WebSocket.NORMAL_CLOSURE, "Done")
                    .get(5, TimeUnit.SECONDS);
        }
    }

    private WebSocket connectClient(
            HttpClient client,
            CompletableFuture<String> message) throws Exception {

        return client.newWebSocketBuilder()
                .buildAsync(
                        URI.create("ws://localhost:" + app.port() + "/ws/quiz/1"),
                        new WebSocket.Listener() {
                            @Override
                            public java.util.concurrent.CompletionStage<?> onText(
                                    WebSocket socket,
                                    CharSequence data,
                                    boolean last) {

                                message.complete(data.toString());
                                socket.request(1);
                                return null;
                            }
                        }
                ).get(5, TimeUnit.SECONDS);
    }

    @Test
    void testTwoWebSocketConnections() throws Exception {

        CompletableFuture<String> message1 = new CompletableFuture<>();
        CompletableFuture<String> message2 = new CompletableFuture<>();

        try (HttpClient client = HttpClient.newHttpClient()) {

            WebSocket socket1 = connectClient(client, message1);
            WebSocket socket2 = connectClient(client, message2);

            assertEquals("Connected to quiz 1",
                    message1.get(5, TimeUnit.SECONDS));

            assertEquals("Connected to quiz 1",
                    message2.get(5, TimeUnit.SECONDS));

            socket1.sendClose(WebSocket.NORMAL_CLOSURE, "Done")
                    .get(5, TimeUnit.SECONDS);

            socket2.sendClose(WebSocket.NORMAL_CLOSURE, "Done")
                    .get(5, TimeUnit.SECONDS);
        }
    }


}