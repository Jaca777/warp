package net.warpgame.engine.net;

/**
 * @author Hubertus
 * Created 10.12.2017
 */
public class PacketType {
    public static final int PACKET_CONNECT = 1;
    public static final int PACKET_CONNECTED = 2;
    public static final int PACKET_CONNECTION_REFUSED = 3;
    public static final int PACKET_KEEP_ALIVE = 4;
    public static final int PACKET_SCENE_STATE = 5;
    public static final int PACKET_EVENT = 6;
    public static final int PACKET_EVENT_CONFIRMATION = 7;
    public static final int PACKET_CLOCK_SYNCHRONIZATION_REQUEST = 8;
    public static final int PACKET_CLOCK_SYNCHRONIZATION_RESPONSE = 9;
}
