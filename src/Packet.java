public class Packet {

    int dest, orig, id;
    private static int getId = 0;
    String message;

    public Packet(String message, int dest, int orig){
        this.message = message;
        this.dest = dest;
        this.orig = orig;
        this.id = getId++;
    }
    public Packet(String message, int dest, int orig, int id){
        this.message = message;
        this.dest = dest;
        this.orig = orig;
        this.id = id;
    }
    public static void reset(){
        getId = 0;
    }
}
