public class Packet {

    int dest, orig, pid;
    private static int getId = 0;
    String message;

    public Packet(String message, int dest, int orig){
        this.message = message;
        this.dest = dest;
        this.orig = orig;
        this.pid = getId++;
    }
    public Packet(String message, int dest, int orig, int pid){
        this.message = message;
        this.dest = dest;
        this.orig = orig;
        this.pid = pid;
    }
    public static void reset(){
        getId = 0;
    }
}
