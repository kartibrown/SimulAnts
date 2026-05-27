package com.kartibrown.simulants.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class SimulationWebSocketHandler extends TextWebSocketHandler {
    private final SimulationManager simulationManager;
    private final ObjectMapper objectMapper;

    public SimulationWebSocketHandler(final SimulationManager simulationManager, final ObjectMapper objectMapper) {
        this.simulationManager = simulationManager;
        this.objectMapper = objectMapper;
    }

    @Override
    public final void afterConnectionEstablished(final WebSocketSession webSocketSession) {
        System.out.println("WebSocket connected: " + webSocketSession.getId());

        final SimulationSession session = simulationManager.createSession(webSocketSession);
        webSocketSession.getAttributes().put("sessionId", session.getId());
    }

    @Override
    protected final void handleTextMessage(final WebSocketSession webSocketSession, final TextMessage message) {
        final String sessionId = webSocketSession.getAttributes().get("sessionId").toString();
        final SimulationSession session = simulationManager.getSession(sessionId);

        if (session == null) {
            return;
        }

        final String payload = message.getPayload();

        session.markActive();

        if (payload.contains("PAUSE")) {
            session.setPaused(true);
        }

        if (payload.contains("RESUME")) {
            session.setPaused(false);
        }

        if (payload.contains("STOP")) {
            simulationManager.removeSession(sessionId);
        }
    }

    @Override
    public final void afterConnectionClosed(final WebSocketSession webSocketSession, final CloseStatus status) {
        System.out.println("WebSocket closed: " + status);

        final String sessionId = (String) webSocketSession.getAttributes().get("sessionId");

        if (sessionId != null) {
            simulationManager.removeSession(sessionId);
        }
    }

    // Clean up any expired sessions after every 10 sec if there's any
    @Scheduled(fixedRate = 10_000)
    public final void cleanupTimedOutSessions()
    {
        simulationManager.cleanupTimedOutSessions();
    }

    @Scheduled(fixedRate = 50)
    public final void sendWorldStates()
    {
        for (final SimulationSession session : simulationManager.getSessions()) {
            final WebSocketSession webSocketSession = session.getWebSocketSession();

            if (!webSocketSession.isOpen()) {
                simulationManager.removeSession(session.getId());
                continue;
            }

            try {
                final String payload = objectMapper.writeValueAsString(session.getWorld().toState());
                webSocketSession.sendMessage(new TextMessage(payload));
                session.markActive();
            } catch (final JsonProcessingException e) {
                System.out.println("Couldn't serialize world state for session " + session.getId());
            } catch (final Exception e) {
                simulationManager.removeSession(session.getId());
            }
        }
    }
}
