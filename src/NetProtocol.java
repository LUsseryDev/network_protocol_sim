public interface NetProtocol {
    public void createInitMessage(Node n, int destAddress, int dataSize);

    public void respond(Packet p, Node n, int tick);

    public void onTick(int tickNum);

    public int getID();

    public int getResponseTime();

    public double getPacketLoss();
}
