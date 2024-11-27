import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        //get data
        Scanner sc = new Scanner(System.in);
        System.out.println("enter the number of times to run the simulation");
        int numRuns;
        while (true){
            try{
                numRuns = Integer.parseInt(sc.nextLine());
                break;
            }catch (Exception e){
                System.out.println("Invalid input, enter an integer for number of times to run the simulation");
            }
        }
        System.out.println("enter the number of nodes for the simulation");
        int numNodes;
        while (true){
            try{
                numNodes = Integer.parseInt(sc.nextLine());
                break;
            }catch (Exception e){
                System.out.println("Invalid input, enter an integer for number of nodes");
            }
        }
        System.out.println("enter the number of ticks for the simulation");
        int numTicks;
        while (true){
            try{
                numTicks = Integer.parseInt(sc.nextLine());
                break;
            }catch (Exception e){
                System.out.println("Invalid input, enter an integer for number of ticks");
            }
        }
        System.out.println("enter the number of new messages per tick for the simulation");
        int numNewMessages;
        while (true){
            try{
                numNewMessages = Integer.parseInt(sc.nextLine());
                break;
            }catch (Exception e){
                System.out.println("Invalid input, enter an integer for new messages per tick");
            }
        }

        //run the sim
        int averagePacketsSent = 0, averagePacketsDropped = 0, averageResponseTime = 0;
        double averagePacketLoss = 0;
        for (int i = 0; i < numRuns; i++) {
            System.out.println("Starting simulation "+i);
            Network network = new Network(numNodes, numNewMessages);

            for (int j = 0; j < numTicks; j++) {
                network.onTick(j);
            }
            averageResponseTime += network.getAvrResponseTime();
            averagePacketsDropped += network.getPacketsDropped();
            averagePacketsSent += network.getTotalPackets();
            averagePacketLoss += network.getAvrLoss();
            Network.reset();
        }
        averageResponseTime = averageResponseTime/numRuns;
        averagePacketsDropped = averagePacketsDropped/numRuns;
        averagePacketsSent = averagePacketsSent/numRuns;
        averagePacketLoss = averagePacketLoss/numRuns;

        System.out.println("simulation complete");
        System.out.println("average response times = "+averageResponseTime);
        System.out.println("average packet loss is "+averagePacketLoss+"%");
        System.out.println("average number of packets dropped = "+averagePacketsDropped);
        System.out.println("average number of packets sent = "+averagePacketsSent);



    }
}