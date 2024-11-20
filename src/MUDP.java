import java.util.ArrayList;

public class MUDP implements NetProtocol{
    private final int id;
    private static int getId = 0;
    private int startTick;
    private int responseTime;
    private Node hostNode;


    public MUDP(int startTick){
        id = getId++;
        this.startTick = startTick;
        this.responseTime = 0;
    }
    public MUDP(int id, int startTick){
        this.id = id;
        this.startTick = startTick;
        this.responseTime = 0;

    }
    @Override
    public void createInitMessage(Node n, int destAddress, int dataSize) {
        n.genPacket(destAddress,STR."MUDP \{id} \{dataSize} REQUEST");
        int alt = getAltPath(n, destAddress);
        if (alt != -1){
            n.genPacket(alt,STR."MUDP \{id} \{dataSize} FORWARD_REQUEST \{destAddress}" );
        }
    }

    @Override
    public void respond(Packet p, Node n, int tick) {
        String[] data = p.message.split(" ");
        switch (data[3]){
            case "REQUEST":
                int alt = getAltPath(n, p.orig);
                for (int i = 0; i < Integer.parseInt(data[2]); i++) {
                    n.genPacket(p.orig, STR."MUDP \{id} \{i} RESPONSE");
                    if (alt != -1){
                        n.genPacket(alt,STR."MUDP \{id} \{data[2]} FORWARD_RESPONSE \{p.orig}" );
                    }
                }
                break;
            case"FOWARD_REQUEST":
                n.genPacket(Integer.parseInt(data[4]),STR."MUDP \{id} \{data[2]} REQUEST");
                break;
            case "RESPONSE":
                responseTime = tick - startTick;
                break;
            case "FOWARD_RESPONSE":
                n.genPacket(Integer.parseInt(data[4]),STR."MUDP \{id} \{data[2]} RESPONSE");
                break;
        }
    }

    @Override
    public void onTick(){}

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

    //returns the address of a node that will have a different path to the destination, if no such path exists, return -1
    public int getAltPath(Node n, int dest){
        //get destination node
        Node destNode = n.getNextInPath(dest);
        while (destNode.getAddress() != dest){
            destNode = destNode.getNextInPath(dest);
        }

        ArrayList<Node> sendPath = new ArrayList<>();
        int altNode = -1;

        for (int i = 0; i < Node.getNodeCount(); i++) {
            int numSameOnPath = 0;

            //get every node on the path from the sender
            Node next = n.getNextInPath(i);
            if (next == null){
                return altNode;
            }
            while(next.getAddress() != i){
                sendPath.add(next);
                next = next.getNextInPath(i);
            }
            //compare path from dest node
            next = destNode.getNextInPath(i);
            if (next == null){
                return altNode;
            }
            while(next.getAddress() != i){
                if(sendPath.contains(next)){
                    numSameOnPath++;
                }
                next = next.getNextInPath(i);
            }
            if (numSameOnPath == 0){
                altNode = i;
                break;
            }
        }
        return altNode;
    }
}
