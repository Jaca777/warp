package net.warpgame.engine.server;

import io.netty.buffer.ByteBuf;
import net.warpgame.engine.core.context.service.Service;
import net.warpgame.engine.net.ClockSynchronizer;
import net.warpgame.engine.net.ConnectionState;
import net.warpgame.engine.net.PacketType;
import net.warpgame.engine.net.event.StateChangeRequestMessage;
import net.warpgame.engine.net.event.sender.RemoteEventQueue;
import net.warpgame.engine.server.envelope.ServerInternalMessageEnvelope;

import static net.warpgame.engine.net.PacketType.*;

/**
 * @author Hubertus
 * Created 13.05.2018
 */
@Service
public class IncomingPacketProcessor {

    private ClientRegistry clientRegistry;
    private ConnectionUtil connectionUtil;
    private RemoteEventQueue eventQueue;

    public IncomingPacketProcessor(ClientRegistry clientRegistry,
                                   ConnectionUtil connectionUtil,
                                   RemoteEventQueue eventQueue) {
        this.clientRegistry = clientRegistry;
        this.connectionUtil = connectionUtil;
        this.eventQueue = eventQueue;
    }

    void processPacket(int packetType, long timestamp, ByteBuf packet) {
        int clientId = packet.readInt();
        switch (packetType) {
            case PACKET_KEEP_ALIVE:
                processKeepAlivePacket(timestamp, clientId, packet);
                break;
            case PACKET_MESSAGE:
                processEventPacket(timestamp, clientId, packet);
                break;
            case PACKET_INTERNAL_MESSAGE:
                processInternalMessagePacket(timestamp, clientId, packet);
                break;
            case PACKET_MESSAGE_CONFIRMATION:
                processEventConfirmationPacket(timestamp, clientId, packet);
                break;
            case PACKET_CLOCK_SYNCHRONIZATION_REQUEST:
                processClockSynchronizationRequestPacket(timestamp, clientId, packet);
                break;
            case PACKET_CLOCK_SYNCHRONIZATION_RESPONSE:
                processClockSynchronizationResponsePacket(timestamp, clientId, packet);
                break;
        }
    }

    private void processKeepAlivePacket(long timestamp, int clientId, ByteBuf packetData) {
        clientRegistry.updateKeepAlive(clientId);
    }

    private void processEventPacket(long timestamp, int clientId, ByteBuf packetData) {
        Client client = clientRegistry.getClient(clientId);
        if (client != null) {
            int eventType = packetData.readInt();
            int dependencyId = packetData.readInt();
            int targetComponentId = packetData.readInt();
            client.getMessageReceiver().addEvent(packetData, targetComponentId, eventType, dependencyId, timestamp);
            connectionUtil.confirmEvent(dependencyId, client);
        }
    }

    private void processInternalMessagePacket(long timestamp, int clientId, ByteBuf packetData) {
        Client client = clientRegistry.getClient(clientId);

        if (client != null) {
            int dependencyId = packetData.readInt();
            client.getMessageReceiver().addInternalMessage(packetData, dependencyId, timestamp);
            connectionUtil.confirmEvent(dependencyId, client);
        }
    }

    private void processEventConfirmationPacket(long timestamp, int clientId, ByteBuf packetData) {
        int eventDependencyId = packetData.readInt();
        Client c = clientRegistry.getClient(clientId);
        c.confirmMessage(eventDependencyId);
    }

    private void processClockSynchronizationRequestPacket(long timestamp, int clientId, ByteBuf packetData) {
        int requestId = packetData.readInt();
        Client client = clientRegistry.getClient(clientId);
        if (client != null) {
            client.getClockSynchronizer().startRequest(requestId, System.currentTimeMillis());

            ByteBuf packet =
                    connectionUtil.getHeader(PacketType.PACKET_CLOCK_SYNCHRONIZATION_RESPONSE, 4);
            packet.writeInt(requestId);
            connectionUtil.sendPacket(packet, client);
        }
    }

    private void processClockSynchronizationResponsePacket(long timestamp, int clientId, ByteBuf packetData) {
        int requestId = packetData.readInt();
        Client client = clientRegistry.getClient(clientId);
        if (client != null) {
            ClockSynchronizer synchronizer = client.getClockSynchronizer();
            synchronizer.synchronize(timestamp, requestId);
            if (synchronizer.getFinishedSynchronizations() >= 3) {
                eventQueue.pushEvent(
                        new ServerInternalMessageEnvelope(new StateChangeRequestMessage(ConnectionState.LIVE), client));
                client.getConnectionStateHolder().setRequestedConnectionState(ConnectionState.LIVE);
            }
        }
    }
}
