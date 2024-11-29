import java.util.ArrayList;
import java.util.Collections;

public class SUDP implements NetProtocol{
    private final int id;
    private static int getId = 0;
    private int startTick;
    private int responseTime;
    private int cwnd;
    private Node hostNode;
    private int ssthresh;
    private final int cwndinit = 5;
    private final static int RTO = 30;
    private ArrayList<PacketTimer> sentPackets;
    private ArrayList<Packet> toSend;
    private int datasize;
    private ArrayList<Boolean> recived;
    private static int lastCCUpdate = 0;


    public SUDP(int startTick){
        this.id = getId++;
        this.startTick = startTick;
        this.responseTime = 0;
        this.cwnd = cwndinit;
        this.ssthresh = Integer.MAX_VALUE;
        this.toSend = new ArrayList<>();
        this.sentPackets = new ArrayList<>();
    }
    public SUDP(int id, int startTick){
        this.id = id;
        this.startTick = startTick;
        this.responseTime = 0;
        this.cwnd = cwndinit;
        this.ssthresh = Integer.MAX_VALUE;
        this.toSend = new ArrayList<>();
        this.sentPackets = new ArrayList<>();
    }
    @Override
    public void createInitMessage(Node n, int destAddress, int dataSize) {
        toSend.add(new Packet(STR."SUDP \{id} \{dataSize} REQUEST", destAddress, n.getAddress()));
        hostNode = n;
        this.datasize = dataSize;
        this.recived = new ArrayList<Boolean>(Collections.nCopies(dataSize, false));
    }

    @Override
    public void respond(Packet p, Node n, int tick) {
        hostNode = n;
        String[] data = p.message.split(" ");
        //if this packet was a response, don't respond, and find how long it took
        switch (data[3]){
            case "REQUEST":
                for (int i = 0; i < Integer.parseInt(data[2]); i++) {
                    toSend.add(new Packet(STR."SUDP \{id} \{i} RESPONSE", p.orig, n.getAddress(), p.pid));
                }
                break;
            case "RESPONSE":
                recived.set(Integer.parseInt(data[2]), true);
                for(PacketTimer pt: sentPackets){
                    if (p.pid == pt.packet.pid){
                        sentPackets.remove(pt);
                        break;
                    }
                }
                n.genPacket(p.orig, STR."SUDP \{id} \{1} ACK", p.pid);
                if (responseTime == 0){
                    responseTime = tick - startTick;
                }
                break;
            case "ACK":
                for(PacketTimer pt: sentPackets){
                    if (p.pid == pt.packet.pid){
                        sentPackets.remove(pt);
                        break;
                    }
                }
                break;
        }
    }

    @Override
    public void onTick(int tickNum){
        boolean t = false;
        for(PacketTimer pt: sentPackets){
            pt.ticks_remaining--;
            //on packet timeout
            if (pt.ticks_remaining <= 0){
                ssthresh = cwnd/2;
                cwnd = cwndinit;
                t = true;
                toSend.add(pt.packet);
                pt.ticks_remaining = RTO;
                break;
            }
        }
        //if no timeouts

        if (!t) {
            if (cwnd < ssthresh) {
                cwnd *= 1.3;
            } else {
                cwnd += 1;
            }
        }

        //send packets
        for (int i = 0; i < cwnd; i++) {
            if(toSend.isEmpty()){
                break;
            }
            Packet p = toSend.removeFirst();
            hostNode.sendPacket(p);
            sentPackets.add(new PacketTimer(p, tickNum + RTO));
        }
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public int getResponseTime() {
        return responseTime;
    }

    @Override
    public double getPacketLoss() {
        if(recived == null){
            return -1;
        }
        int sum = 0;
        for(Boolean b:recived){
            if (b){
                sum++;
            }
        }
        return (double) sum/datasize;
    }

    public static void reset() {
        getId = 0;
    }

    private static class PacketTimer{
        public Packet packet;
        public int ticks_remaining;
        public PacketTimer(Packet p, int ticks){
            packet = p;
            ticks_remaining = ticks;
        }
    }
}
