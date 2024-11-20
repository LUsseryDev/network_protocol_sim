import java.util.ArrayList;

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

    public SUDP(int startTick){
        id = getId++;
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
    }

    @Override
    public void respond(Packet p, Node n, int tick) {
        hostNode = n;
        String[] data = p.message.split(" ");
        //if this packet was a response, don't respond, and find how long it took
        switch (data[3]){
            case "REQUEST":
                for (int i = 0; i < Integer.parseInt(data[2]); i++) {
                    toSend.add(new Packet(STR."SUDP \{id} \{i} RESPONSE \{p.id}", p.dest, n.getAddress()));
                }
                break;
            case "RESPONSE":
                for(PacketTimer pt: sentPackets){
                    if (Integer.parseInt(data[4]) == pt.packet.id){
                        sentPackets.remove(pt);
                        break;
                    }
                }
                n.genPacket(p.orig, STR. "SUDP \{id} \{1} ACK \{p.id}");
                responseTime = tick - startTick;
                break;
            case "ACK":
                for(PacketTimer pt: sentPackets){
                    if (Integer.parseInt(data[4]) == pt.packet.id){
                        sentPackets.remove(pt);
                        break;
                    }
                }
                break;
        }
    }

    @Override
    public void onTick(){
        boolean t = false;
        for(PacketTimer pt: sentPackets){
            pt.ticks_remaining--;
            //on packet timeout
            if (pt.ticks_remaining <= 0){
                ssthresh = cwnd/2;
                cwnd = cwndinit;
                t = true;
            }
        }
        //if no timeouts
        if (!t) {
            if (cwnd < ssthresh) {
                cwnd *= 2;
            } else {
                cwnd += 1;
            }
        }

        //send packets
        for (int i = 0; i < cwnd; i++) {
            if(toSend.isEmpty()){
                break;
            }
            hostNode.sendPacket(toSend.removeFirst());

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

    public static void reset() {
        getId = 0;
    }
    private class PacketTimer{
        public Packet packet;
        public int ticks_remaining;
        public PacketTimer(Packet p, int ticks){
            packet = p;
            ticks_remaining = ticks;
        }
    }
}
