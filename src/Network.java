import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

public class Network {
    private static final Random rand = new Random(System.currentTimeMillis());

    ArrayList<Node> nodes;
    ArrayList<Edge> edges;
    int[][] adjMatrix;
    int numNewMessages;
    int udpNum, mudpNum, sudpNum;


    public Network(int numNodes, int numNewMessages, String ratios){
        this.numNewMessages = numNewMessages;
        //find number of edges
        int numEdges = rand.nextInt(numNodes-1, (numNodes*(numNodes-1))/2);

        //generate nodes in a spanning tree
        nodes = new ArrayList<>(numNodes);
        edges = new ArrayList<>(numEdges);
        nodes.add(new Node());
        for (int i = 0; i < numNodes-1; i++) {
            Node n = new Node();
            Edge e = new Edge(n, nodes.get(rand.nextInt(nodes.size())));
            n.addEdge(e);
            e.getOther(n).addEdge(e);
            nodes.add(n);
            edges.add(e);
        }

        //generate remaining edges
        Edge e;
        for (int i = 0; i < numEdges-numNodes; i++) {
            do{
                e = new Edge(nodes.get(rand.nextInt(nodes.size())), nodes.get(rand.nextInt(nodes.size())));
                e.getA().addEdge(e);
                e.getB().addEdge(e);
                edges.add(e);
            }while (e.getA().addEdge(e) && e.getB().addEdge(e));
        }

        //generate initial 2d array of graph
        adjMatrix = to2dArray();

        //generate paths in nodes, this might move
        for(Node n: nodes){
            n.setPath(n.findPaths(adjMatrix, n.getAddress()));
        }

        //set ratios for each protocol
        String[] ratioData = ratios.split(" ");
        udpNum = Integer.parseInt(ratioData[0]);
        mudpNum = Integer.parseInt(ratioData[1]);
        sudpNum = Integer.parseInt(ratioData[2]);
    }
    public int[][]to2dArray(){
        //if the graph isn't done, return null
        if(nodes.isEmpty() || edges.isEmpty()){
            return null;
        }
        int[][]adjMat = new int[nodes.size()][nodes.size()];

        //create an adjacency matrix from the node network
        for (int i = 0; i < nodes.size(); i++) {
            Node n = nodes.get(i);
            for(Edge e: n.getEdges()){
                adjMat[n.getAddress()][e.getOther(n).getAddress()] = e.getWeight();
            }
        }
        return adjMat;
    }

    //function to process one tick
    public void onTick(int tickNum){
        //create messages
        for (int i = 0; i < numNewMessages; i++) {
            Node randSender = nodes.get(rand.nextInt(nodes.size()));
            Node randReceiver = nodes.get(rand.nextInt(nodes.size()));
            while (randReceiver.equals(randSender)){
                randReceiver = nodes.get(rand.nextInt(nodes.size()));
            }
            NetProtocol protocol;
            int temp = rand.nextInt(1, 101);
            if (temp <= udpNum){
                protocol = new UDP(tickNum);
            }
            else if (temp <= udpNum+mudpNum) {
                protocol = new MUDP(tickNum);
            }
            else if (temp <= 100) {
                protocol = new SUDP(tickNum);
            }
            else{
                System.out.println("something went wrong");
                return;
            }
            randSender.addProtocol(protocol);
            protocol.createInitMessage(randSender, randReceiver.getAddress(), rand.nextInt(1, 20));
        }


        //process all nodes
        for(Node n: nodes){
            n.onTick(tickNum);
        }
        //process all edges
        for(Edge e: edges){
            e.onTick(tickNum);
        }
    }
    public int getTotalPackets(){
        return Node.getTotalPackets();
    }
    public int getPacketsDropped(){
        return Node.getDroppedPackets();
    }
    public int getUDPAvrResponseTime(){
        ArrayList<NetProtocol> protocols = new ArrayList<>();
        for(Node n: nodes){
            protocols.addAll(n.getProtocols());
        }
        int sum = 0, numResponse = 0;
        for(NetProtocol np: protocols){
            if (np.getClass() == UDP.class) {
                if (np.getResponseTime() != 0) {
                    sum += np.getResponseTime();
                    numResponse++;
                }
            }
        }
        if(numResponse == 0){
            return 0;
        }
        return sum/numResponse;
    }
    public int getMUDPAvrResponseTime(){
        ArrayList<NetProtocol> protocols = new ArrayList<>();
        for(Node n: nodes){
            protocols.addAll(n.getProtocols());
        }
        int sum = 0, numResponse = 0;
        for(NetProtocol np: protocols){
            if (np.getClass() == MUDP.class) {
                if (np.getResponseTime() != 0) {
                    sum += np.getResponseTime();
                    numResponse++;
                }
            }
        }
        if(numResponse == 0){
            return 0;
        }
        return sum/numResponse;
    }
    public int getSUDPAvrResponseTime(){
        ArrayList<NetProtocol> protocols = new ArrayList<>();
        for(Node n: nodes){
            protocols.addAll(n.getProtocols());
        }
        int sum = 0, numResponse = 0;
        for(NetProtocol np: protocols){
            if (np.getClass() == SUDP.class) {
                if (np.getResponseTime() != 0) {
                    sum += np.getResponseTime();
                    numResponse++;
                }
            }
        }
        if(numResponse == 0){
            return 0;
        }
        return sum/numResponse;
    }

    public double getUDPAvrLoss(){
        ArrayList<NetProtocol> protocols = new ArrayList<>();
        for(Node n: nodes){
            protocols.addAll(n.getProtocols());
        }
        double sum = 0;
        int numResponse = 0;
        for(NetProtocol np: protocols){
            if (np.getClass() == UDP.class) {
                if (np.getPacketLoss() != -1) {
                    sum += np.getPacketLoss();
                    numResponse++;
                }
            }
        }
        if(numResponse == 0){
            return 0;
        }
        return sum/numResponse;
    }
    public double getMUDPAvrLoss(){
        ArrayList<NetProtocol> protocols = new ArrayList<>();
        for(Node n: nodes){
            protocols.addAll(n.getProtocols());
        }
        double sum = 0;
        int numResponse = 0;
        for(NetProtocol np: protocols){
            if (np.getClass() == MUDP.class) {
                if (np.getPacketLoss() != -1) {
                    sum += np.getPacketLoss();
                    numResponse++;
                }
            }
        }
        if(numResponse == 0){
            return 0;
        }
        return sum/numResponse;
    }
    public double getSUDPAvrLoss(){
        ArrayList<NetProtocol> protocols = new ArrayList<>();
        for(Node n: nodes){
            protocols.addAll(n.getProtocols());
        }
        double sum = 0;
        int numResponse = 0;
        for(NetProtocol np: protocols){
            if (np.getClass() == SUDP.class) {
                if (np.getPacketLoss() != -1) {
                    sum += np.getPacketLoss();
                    numResponse++;
                }
            }
        }
        if(numResponse == 0){
            return 0;
        }
        return sum/numResponse;
    }

    public static void reset(){
        Node.reset();
        UDP.reset();
        SUDP.reset();
        MUDP.reset();
        Packet.reset();
    }


}
