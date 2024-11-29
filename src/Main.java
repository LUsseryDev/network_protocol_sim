import java.util.Objects;
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

        String ratios;
        while (true){
            int udp = 0, mudp = 0, sudp = 0;
            System.out.println("enter the ratio of UDP connections in the simulation");
            while (true){
                try{
                    udp = Integer.parseInt(sc.nextLine());
                    break;
                }catch (Exception e){
                    System.out.println("Invalid input, enter an integer for the ratio of UDP connections in the simulation");
                }
            }
            if(udp+mudp+sudp == 100){
                ratios = udp+" "+mudp+" "+sudp;
                break;
            }
            System.out.println("enter the ratio of MUDP connections in the simulation");
            while (true){
                try{
                    mudp = Integer.parseInt(sc.nextLine());
                    break;
                }catch (Exception e){
                    System.out.println("Invalid input, enter an integer for the ratio of MUDP connections in the simulation");
                }
            }
            if(udp+mudp+sudp == 100){
                ratios = udp+" "+mudp+" "+sudp;
                break;
            }
            System.out.println("enter the ratio of SUDP connections in the simulation");
            while (true){
                try{
                    sudp = Integer.parseInt(sc.nextLine());
                    break;
                }catch (Exception e){
                    System.out.println("Invalid input, enter an integer for the ratio of SUDP connections in the simulation");
                }
            }
            if(udp+mudp+sudp == 100){
                ratios = udp+" "+mudp+" "+sudp;
                break;
            }
            System.out.println("invalid ratios, the ratios of each UDP, MUDP, and SUDP must sum to 100");
        }




        //run the sim
        int averagePacketsSent = 0, averagePacketsDropped = 0, averageUDPResponseTime = 0, averageMUDPResponseTime = 0, averageSUDPResponseTime = 0;
        double averageUDPLoss = 0, averageMUDPLoss = 0, averageSUDPLoss = 0;
        for (int i = 0; i < numRuns; i++) {
            System.out.println("Starting simulation "+i);
            Network network = new Network(numNodes, numNewMessages, ratios);

            for (int j = 0; j < numTicks; j++) {
                network.onTick(j);
            }
            averageUDPResponseTime += network.getUDPAvrResponseTime();
            averageMUDPResponseTime += network.getMUDPAvrResponseTime();
            averageSUDPResponseTime += network.getSUDPAvrResponseTime();
            averagePacketsDropped += network.getPacketsDropped();
            averagePacketsSent += network.getTotalPackets();
            averageUDPLoss += network.getUDPAvrLoss();
            averageMUDPLoss += network.getMUDPAvrLoss();
            averageSUDPLoss += network.getSUDPAvrLoss();
            Network.reset();
        }
        averageUDPResponseTime = averageUDPResponseTime/numRuns;
        averageMUDPResponseTime = averageMUDPResponseTime/numRuns;
        averageSUDPResponseTime = averageSUDPResponseTime/numRuns;
        averagePacketsDropped = averagePacketsDropped/numRuns;
        averagePacketsSent = averagePacketsSent/numRuns;
        averageUDPLoss = averageUDPLoss/numRuns;
        averageMUDPLoss = averageMUDPLoss/numRuns;
        averageSUDPLoss = averageSUDPLoss/numRuns;

        String[] ratioData = ratios.split(" ");
        System.out.println("simulation complete");

        if(!Objects.equals(ratioData[0], "0")) {
            System.out.println("average UDP response time = " + averageUDPResponseTime);
        }
        if(!Objects.equals(ratioData[1], "0")) {
            System.out.println("average MUDP response time = " + averageMUDPResponseTime);
        }
        if(!Objects.equals(ratioData[2], "0")) {
            System.out.println("average SUDP response time = " + averageSUDPResponseTime);
        }


        if(!Objects.equals(ratioData[0], "0")) {
            System.out.println("average UDP packet loss is " + (100 - (100 * averageUDPLoss)) + "%");
        }
        if(!Objects.equals(ratioData[1], "0")) {
            System.out.println("average MUDP packet loss is " + (100 - (100 * averageMUDPLoss)) + "%");
        }
        if(!Objects.equals(ratioData[2], "0")) {
            System.out.println("average SUDP packet loss is " + (100 - (100 * averageSUDPLoss)) + "%");
        }
        System.out.println("average number of packets dropped = "+averagePacketsDropped);
        System.out.println("average number of packets sent = "+averagePacketsSent);
        System.out.println("average packet drop ratio = "+ (double) averagePacketsDropped/averagePacketsSent);
    }
}