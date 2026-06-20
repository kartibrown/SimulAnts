package com.kartibrown.simulants.server;

import com.kartibrown.simulants.world.World;
import org.springframework.web.socket.WebSocketSession;

import java.util.UUID;

public final class SimulationSession {
    private final World world;
    private final WebSocketSession webSocketSession;

    private final String id;
    private long lastSeenActive;

    private volatile boolean paused;

    public SimulationSession(final World world, final WebSocketSession webSocketSession) {
        this.world = world;
        this.webSocketSession = webSocketSession;
        this.id = UUID.randomUUID().toString();

        this.lastSeenActive = System.currentTimeMillis();
        this.paused = false;
    }

    public void markActive() {
        this.lastSeenActive = System.currentTimeMillis();
    }

    public boolean hasTimedOut()
    {
        return System.currentTimeMillis() - this.lastSeenActive > 60_000;
    }

    /*
     * GETTERS & SETTERS
     */

    public String getId(){
        return this.id;
    }

    public World getWorld(){
        return this.world;
    }

    public WebSocketSession getWebSocketSession(){
        return this.webSocketSession;
    }

    public void setPaused(final boolean b) {
        this.paused = b;
    }

    public boolean isPaused() {
        return paused;
    }
}
