import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class UDP implements NetProtocol{
    private final int id;
    private static int getId = 0;
    private int startTick;
    private int responseTime;
    private int datasize;
    private ArrayList<Boolean> recived;

    public UDP(int startTick){
        id = getId++;
        this.startTick = startTick;
        this.responseTime = 0;
    }
    public UDP(int id, int startTick){
        this.id = id;
        this.startTick = startTick;
        this.responseTime = 0;
    }
    @Override
    public void createInitMessage(Node n, int destAddress, int dataSize) {
        n.genPacket(destAddress,STR."UDP \{id} \{dataSize} REQUEST");
        this.datasize = dataSize;
        this.recived = new ArrayList<Boolean>(Collections.nCopies(dataSize, false));
    }

    @Override
    public void respond(Packet p, Node n, int tick) {
        String[] data = p.message.split(" ");
        //if this packet was a response, don't respond, and find how long it took
        switch (data[3]){
            case "REQUEST":
                for (int i = 0; i < Integer.parseInt(data[2]); i++) {
                    n.genPacket(p.orig, STR."UDP \{id} \{i} RESPONSE");
                }
                break;
            case "RESPONSE":
                //record the packet being received
                recived.set(Integer.parseInt(data[2]), true);
                if (responseTime == 0){
                    responseTime = tick - startTick;
                }
                break;

        }
    }

    @Override
    public void onTick(int tickNum){}

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
}
