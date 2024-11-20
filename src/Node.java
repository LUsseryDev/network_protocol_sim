import java.util.ArrayList;
import java.util.Random;


public class Node {
    private static final Random rand = new Random(System.currentTimeMillis()+1);
    private ArrayList<Edge> edges;
    private Queue<Packet> buffer, generatedPackets;
    private int address, bufferSize;
    private static int nodeCount = 0;
    private ArrayList<NetProtocol> protocols;
    private int[] path;
    private static int droppedPackets = 0, totalPackets = 0;


    public Node(Edge e){
        edges = new ArrayList<>();
        edges.add(e);
        buffer = new Queue<>();
        generatedPackets = new Queue<>();
        address = nodeCount++;
        bufferSize = rand.nextInt(50, 100);
        protocols = new ArrayList<>();
    }

    public Node(){
        edges = new ArrayList<>();
        buffer = new Queue<>();
        generatedPackets = new Queue<>();
        address = nodeCount++;
        bufferSize = rand.nextInt(50, 100);
        protocols = new ArrayList<>();
    }

    public void onTick() {
        //attempt to move generated packets to buffer
        while(!generatedPackets.isEmpty() && buffer.size() < bufferSize){
            buffer.add(generatedPackets.getNext());
        }

        //attempt to send packets in buffer
        for (int i = 0; i < buffer.size(); i++) {
            Packet p = buffer.getNext();
            Node next = getNextInPath(p.dest);
            Edge target;
            boolean sent = false;
            for(Edge e: edges){
                if (e.getOther(this) == next){
                    e.send(p, this);
                    sent = true;
                }
            }
            if(!sent){
                buffer.add(p);
            }
        }

        //tick all the protocols
        for(NetProtocol np : protocols){
            np.onTick();
        }
    }

    //create a packet to send to another node, returns true if successful
    public Packet genPacket(int dest, String message){
        Packet p = new Packet(message, dest, this.getAddress());
        generatedPackets.add(p);
        totalPackets++;
        return p;
    }
    public Packet genPacket(int dest, String message, int id){
        Packet p = new Packet(message, dest, this.getAddress(), id);
        generatedPackets.add(p);
        totalPackets++;
        return p;
    }
    public Packet sendPacket(Packet p){
        generatedPackets.add(p);
        totalPackets++;
        return p;
    }

    //receive packet from an edge
    public void recv(Packet p, int tick){
        //check if this node is the destination
        if(p.dest == this.address){
            //find protocol used
            String[] data = p.message.split(" ");
            switch(data[0]){
                case "UDP":
                    for(NetProtocol protocol: protocols){
                        if(protocol.getClass() == UDP.class && Integer.parseInt(data[1]) == protocol.getID()){
                            protocol.respond(p, this, tick);
                            return;
                        }
                    }
                    UDP udp = new UDP(Integer.parseInt(data[1]), tick);
                    protocols.add(udp);
                    udp.respond(p, this, tick);
                    break;

                case "MUDP":
                    for(NetProtocol protocol: protocols){
                        if(protocol.getClass() == MUDP.class && Integer.parseInt(data[1]) == protocol.getID()){
                            protocol.respond(p, this, tick);
                            return;
                        }
                    }
                    MUDP mudp = new MUDP(Integer.parseInt(data[1]), tick);
                    protocols.add(mudp);
                    mudp.respond(p, this, tick);
                    break;
                case "SUDP":
                    for(NetProtocol protocol: protocols){
                        if(protocol.getClass() == SUDP.class && Integer.parseInt(data[1]) == protocol.getID()){
                            protocol.respond(p, this, tick);
                            return;
                        }
                    }
                    SUDP sudp = new SUDP(Integer.parseInt(data[1]), tick);
                    protocols.add(sudp);
                    sudp.respond(p, this, tick);
                    break;
            }

        }

        //if buffer is full, drop the packet
        if(buffer.size() >= bufferSize){
            droppedPackets++;
            return;
        }
        buffer.add(p);
    }

    //to add a new edge to this node, returns false if the edge is not added
    public boolean addEdge(Edge e){
        //check if the edge is valid for this node
        if((e.getA() == this) == (e.getB() == this)){
            return false;
        }
        //check if the edge would be redundant
        Node o = e.getOther(this);
        for(Edge f: edges){
            if (o == f.getOther(this)){
                return false;
            }
        }
        //add edge to node
        edges.add(e);
        return true;
    }

    public void addProtocol(NetProtocol p){
        protocols.add(p);
    }

    //find paths to each other node using dijkstra's algorithm
    public int[] findPaths(int[][] adjMatrix, int startNode)
    {
        //arrays
        int[] distanceTo = new int[adjMatrix[0].length];
        boolean[] added = new boolean[adjMatrix[0].length];
        int[] paths = new int[adjMatrix[0].length];

        // Initialize arrays
        for (int i = 0; i < adjMatrix[0].length; i++)
        {
            distanceTo[i] = Integer.MAX_VALUE;
            added[i] = false;
        }
        distanceTo[startNode] = 0;
        paths[startNode] = -1;

        //loop through nodes
        for (int i = 1; i < adjMatrix[0].length; i++)
        {
            // Pick the minimum distance vertex
            // from the set of vertices not yet
            // processed. nearestVertex is
            // always equal to startNode in
            // first iteration.
            int nearestVertex = -1;
            int shortestDistance = Integer.MAX_VALUE;
            for (int j = 0; j < adjMatrix[0].length; j++)
            {
                if (!added[j] && distanceTo[j] < shortestDistance)
                {
                    nearestVertex = j;
                    shortestDistance = distanceTo[j];
                }
            }

            // Mark the picked vertex as
            // processed
            added[nearestVertex] = true;

            // Update dist value of the
            // adjacent vertices of the
            // picked vertex.
            for (int vertexIndex = 0; vertexIndex < adjMatrix[0].length; vertexIndex++)
            {
                int edgeDistance = adjMatrix[nearestVertex][vertexIndex];

                if (edgeDistance > 0 && ((shortestDistance + edgeDistance) < distanceTo[vertexIndex]))
                {
                    paths[vertexIndex] = nearestVertex;
                    distanceTo[vertexIndex] = shortestDistance + edgeDistance;
                }
            }
        }

        return paths;
    }
    public Node getNextInPath(int nextNode){
        //if next node is invalid, return null
        if(path[nextNode] == -1){
            return null;
        }
        //if next node is this one, return current node
        if(path[nextNode] == this.address){
            //find adjacent node from address
            for(Edge e: edges){
                if(e.getOther(this).getAddress() == nextNode){
                    return e.getOther(this);
                }
            }
        }
        return getNextInPath(path[nextNode]);
    }

    public int[] getPath() {
        return path;
    }

    public int getAddress() {
        return address;
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }

    public void setPath(int[] path) {
        this.path = path;
    }

    public ArrayList<NetProtocol> getProtocols() {
        return protocols;
    }

    public static int getDroppedPackets(){

        return droppedPackets;
    }
    public static int getTotalPackets(){

        return totalPackets;
    }
    public static int getNodeCount(){
        return nodeCount;
    }

    public static void reset(){
        totalPackets = 0;
        droppedPackets = 0;
        nodeCount = 0;
    }
}
