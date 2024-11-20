import java.util.ArrayList;
import java.util.Random;



public class Edge {
    private static final int MAX_BANDWIDTH = 100, MAX_DISTANCE = 100;
    private static final Random rand = new Random(System.currentTimeMillis());
    private Node a, b;
    private final int bandwidth, distance;
    private ArrayList<PacketContainer> packetsToA, packetsToB;

    public Edge(Node a, Node b){
        this.a = a;
        this.b = b;
        bandwidth = rand.nextInt(1, 100);
        distance = rand.nextInt(1, 10);
        packetsToA = new ArrayList<>();
        packetsToB = new ArrayList<>();
    }

    public void onTick(int tick){
        //process each packet on the edge
        for(PacketContainer pc: packetsToA){
            pc.ticks_remaining -= 1;
            if (pc.ticks_remaining >= 0){
                a.recv(pc.packet, tick);
            }
        }
        for(PacketContainer pc: packetsToB){
            pc.ticks_remaining -= 1;
            if (pc.ticks_remaining >= 0){
                b.recv(pc.packet, tick);
            }
        }
    }

    //receive a packet from a node, returns false if the packet is not taken.
    public boolean send(Packet p, Node sender){
        if(packetsToB.size()+packetsToA.size() >=bandwidth){
            return false;
        }
        PacketContainer pc = new PacketContainer(p, distance);
        if(sender == a){
            packetsToB.add(pc);
            return true;
        } else if (sender == b) {
            packetsToA.add(pc);
            return true;
        }
        return false;
    }

    public Node getA() {
        return a;
    }
    public Node getB() {
        return b;
    }
    public Node getOther(Node n){
        if (n == a){return b;}
        else if (n == b){return a;}
        else {return null;}
    }
    //used for finding paths between nodes
    public int getWeight(){
        return (MAX_BANDWIDTH-bandwidth)*distance;
    }
    private class PacketContainer{
        public Packet packet;
        public int ticks_remaining;
        public PacketContainer(Packet p, int ticks){
            packet = p;
            ticks_remaining = ticks;
        }
    }
}
