package net.warpgame.engine.net.event;

/**
 * @author Hubertus
 * Created 14.05.2018
 */
public interface InternalMessageHandler {
    void handleMessage(InternalMessageEnvelope message);
}
