/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networklayer;

import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import static networklayer.NetworkLayerServer.devices;
import static networklayer.NetworkLayerServer.routers;

/**
 *
 * @author samsung
 */
public class Client {

    public static void PrintRoutingTable(ArrayList<RoutingTableEntry> table) {
        for (int i = 0; i < table.size(); i++) {
            System.out.println(table.get(i).getRouterId() + "  " + table.get(i).getDistance() + "  " + table.get(i).getGatewayRouterId());
        }
    }

    public static void main(String[] args) throws IOException {
        Socket socket;
        ObjectInputStream input = null;
        ObjectOutputStream output = null;

        try {
            socket = new Socket("localhost", 1234);
            input = new ObjectInputStream(socket.getInputStream());
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("Connected to server");
        /**
         * Tasks
         */

        //1. Receive EndDevice configuration from server
        //2. [Adjustment in NetworkLayerServer.java: Server internally
        //handles a list of active clients.] 
        try {
            //System.out.println("check");
            EndDevice end = (EndDevice) input.readObject();

            ArrayList<EndDevice> active = (ArrayList<EndDevice>) input.readObject();
            Random rand = new Random();

            //System.out.println("src :"+end.getIp()+" des :"+des.getIp());
            int hop=0;
            int drop=0;
            for (int i = 0; i < 100; i++) {
                int index;
                if (active.size() == 1) {
                    System.out.println("Clients number 1");
                    return;
                } else {
                    //System.out.println("ch2");
                    //System.out.println(active.size());
                    index = rand.nextInt(active.size());
                }

                EndDevice des = active.get(index);

                if (end.getIp() == des.getIp()) {
                    //System.out.println("Same devices selected");
                    index--;
                    if (index < 0) {
                        index = 0;
                    }
                    des = active.get(index);

                }
                //System.out.println("mess"+i);
                String message = "Real Madrid-> " + i;
                System.out.println(message);
                if (i == 20) {
                    //System.out.println("cal");
                    Packet p = new Packet(message, "SHOW_ROUTE", end.getIp(), des.getIp());
                    output.writeObject(p);
                    output.flush();
                    String routepath = (String) input.readObject();
                    System.out.println("Routing  Path:" + routepath);
                    //System.out.println("hellll");
                    hop = (int) input.readObject();
                    System.out.println("Hop Count:" + hop);
                    drop = (int) input.readObject();
                    System.out.println("Drop Count:" + drop);
//                    for (int j = 0; j < routers.size(); j++) {
//                        
//                        ArrayList<RoutingTableEntry> table = (ArrayList<RoutingTableEntry>) input.readObject();
//                        System.out.println();
//                        PrintRoutingTable(table);
//                        System.out.println();
//
//                    }
                    ArrayList<Router> table = (ArrayList<Router>) input.readObject();
                    for (int k = 0; k < table.size(); k++) {
                        System.out.println("RoutingTable of Router:"+table.get(k).getRouterId());
                        PrintRoutingTable(table.get(k).getRoutingTable());
                    }

                } else {
                    Packet p = new Packet(message, "", end.getIp(), des.getIp());
                    output.writeObject(p);
                    output.flush();
                    String routepath = (String) input.readObject();
                    System.out.println("Routing  Path:" + routepath);
                    //System.out.println("hellll");
                    hop = (int) input.readObject();
                    System.out.println("Hop Count:" + hop);
                    drop = (int) input.readObject();
                    System.out.println("Drop Count:" + drop);
                }
            }
            double avghop=(hop/100.0);
            double droprate=(drop/100.0);
            System.out.println("AvgHop="+avghop);
            System.out.println("DropRate="+droprate);
            /* 
         
        3. for(int i=0;i<100;i++)
        4. {
        5.      Generate a random message
        6.      [Adjustment in ServerThread.java] Assign a random receiver from active client list
        7.      if(i==20)
        8.      {
        9.            Send the message to server and a special request "SHOW_ROUTE"
        10.           Display routing path, hop count and routing table of each router [You need to receive 
                            all the required info from the server in response to "SHOW_ROUTE" request]
        11.     }
        12.     else
        13.     {
        14.           Simply send the message and recipient IP address to server.   
        15.     }
        16.     If server can successfully send the message, client will get an acknowledgement along with hop count
                    Otherwise, client will get a failure message [dropped packet]
        17. }
        18. Report average number of hops and drop rate
             */

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
