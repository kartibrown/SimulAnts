package com.kartibrown.simulants.server;

import com.kartibrown.simulants.world.World;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SimulationManager {
    private final Map<String, SimulationSession> sessions;

    public SimulationManager() {
        this.sessions = new ConcurrentHashMap<>();
    }

    public final SimulationSession createSession(final WebSocketSession webSocketSession){
        SimulationSession session = new SimulationSession(new World(), webSocketSession);
        this.sessions.put(session.getId(), session);

        return session;
    }

    // cleanup if some sessions are for some reason not closed
    public void cleanupTimedOutSessions()
    {
        for (final String sessionId : this.sessions.keySet())
        {
            final SimulationSession session = this.sessions.get(sessionId);

            if (session != null && session.hasTimedOut())
                removeSession(sessionId);
        }
    }

    protected void removeSession(final String sessionId)
    {
        this.sessions.remove(sessionId);

    }

    SimulationSession getSession(final String sessionID){
        return this.sessions.get(sessionID);
    }

    Collection<SimulationSession> getSessions(){
        return new ArrayList<>(this.sessions.values());
    }
}
