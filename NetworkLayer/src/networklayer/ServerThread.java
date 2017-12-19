/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networklayer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import static networklayer.NetworkLayerServer.DVR;
import static networklayer.NetworkLayerServer.devices;
import static networklayer.NetworkLayerServer.routers;
import static networklayer.NetworkLayerServer.stateChanger;

/**
 *
 * @author samsung
 */
public class ServerThread implements Runnable {

    private Thread t;
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    EndDevice end;
    private String routepath;
    private int DropCount;
    private int Hopcount;

    public ServerThread(Socket socket, EndDevice end) {

        this.socket = socket;
        this.end = end;
        DropCount = 0;
        Hopcount = 0;
        routepath = "";
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());

        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Server Ready for client " + NetworkLayerServer.clientCount);
        NetworkLayerServer.clientCount++;

        t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {
        /**
         * Synchronize actions with client.
         */
        /*
        Tasks:
        1. Upon receiving a packet server will assign a recipient.
        [Also modify packet to add destination]
        2. call deliverPacket(packet)
        3. If the packet contains "SHOW_ROUTE" request, then fetch the required information
                and send back to client
        4. Either send acknowledgement with number of hops or send failure message back to client
         */

        try {
            //System.out.println("chh");
            output.writeObject(end);
            output.flush();
            output.writeObject(devices);
            output.flush();
            //System.out.println("chh2");
            if (devices.size() != 1) {
                for (int j = 0; j < 100; j++) {
                    try {
                        //System.out.println("chh3");
                        Packet p = (Packet) input.readObject();
                        if (p.getSpecialMessage().equals("SHOW_ROUTE")) {
                            routepath = "";
                            Hopcount=0;
                            boolean b = deliverPacket(p);
                            //System.out.println("aft del");
                            if (!b) {
                                DropCount++;
                            }
                            output.writeObject(routepath);
                            output.flush();
                            output.writeObject(Hopcount);
                            output.flush();
                            output.writeObject(DropCount);
                            output.flush();

//                            for (int i = 0; i < routers.size(); i++) {
//                                ArrayList<RoutingTableEntry> table;
//                                table = routers.get(i).getRoutingTable();
//                                for (int k = 0; k < table.size(); k++) {
//                                    System.out.println(table.get(k).getRouterId() + "  " + table.get(k).getDistance() + "  " + table.get(k).getGatewayRouterId());
//                                    
//                                }
//                                System.out.println();
//                            }
//                            for (int i = 0; i < routers.size(); i++) {
//                                output.writeObject(routers.get(i).getRoutingTable());
//                                output.flush();
//                            }
                         output.writeObject(routers);
                         output.flush();;
                        } else {
                            routepath = "";
                             Hopcount=0;
                            boolean b = deliverPacket(p);
                            //System.out.println("aft del sim");
                            if (!b) {
                                DropCount++;
                            }
                            output.writeObject(routepath);
                            output.flush();
                            output.writeObject(Hopcount);
                            output.flush();
                            output.writeObject(DropCount);
                            output.flush();
                        }

                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } else {
                System.out.println("Clients number 1");
            }
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);

        }

    }

    /**
     * Returns true if successfully delivered Returns false if packet is dropped
     *
     * @param p
     * @return
     * @throws java.lang.InterruptedException
     */
    public Boolean deliverPacket(Packet p) throws InterruptedException {

        //1. Find the router s which has an interface
        //such that the interface and source end device have same network address.
        //System.out.println("del1");
        Router s = null;
        IPAddress src = p.getSourceIP();

        //IPAddress srcnetwork = new IPAddress(src.getBytes()[0] + "." + src.getBytes()[1] + "." + src.getBytes()[2]);
        for (int i = 0; i < routers.size(); i++) {
            ArrayList<IPAddress> interfaces = routers.get(i).getInterfaceAddrs();
            IPAddress srcrouter = interfaces.get(0);
//            System.out.println(srcrouter.getString());
//            System.out.println(src.getString());
//            System.out.println();
//            System.out.println(src.getBytes()[0]);
//            System.out.println(srcrouter.getBytes()[0]);
//            System.out.println();System.out.println();
//            System.out.println(src.getBytes()[1]);
//            System.out.println(srcrouter.getBytes()[1]);
//            System.out.println();System.out.println();
//            System.out.println(src.getBytes()[2]);
//            System.out.println(srcrouter.getBytes()[2]);
//            System.out.println();System.out.println();
            //IPAddress routernetwork = new IPAddress(srcrouter.getBytes()[0] + "." + srcrouter.getBytes()[1] + "." + srcrouter.getBytes()[2]);
            if ((Objects.equals(src.getBytes()[0], srcrouter.getBytes()[0])) && (Objects.equals(src.getBytes()[1], srcrouter.getBytes()[1])) && (Objects.equals(src.getBytes()[2], srcrouter.getBytes()[2]))) {
                s = routers.get(i);
                break;
            }
        }
        //System.out.println("del2");

        //2. Find the router d which has an interface
        //such that the interface and destination end device have same network address.
        Router d = null;
        IPAddress des = p.getDestinationIP();
        //IPAddress desnetwork = new IPAddress(des.getBytes()[0] + "." + des.getBytes()[1] + "." + des.getBytes()[2]);
        for (int i = 0; i < routers.size(); i++) {
            ArrayList<IPAddress> interfaces = routers.get(i).getInterfaceAddrs();
            IPAddress desrouter = interfaces.get(0);
            //IPAddress routernetwork = new IPAddress(desrouter.getBytes()[0] + "." + desrouter.getBytes()[1] + "." + desrouter.getBytes()[2]);
            if ((Objects.equals(des.getBytes()[0], desrouter.getBytes()[0])) && (Objects.equals(des.getBytes()[1], desrouter.getBytes()[1])) && (Objects.equals(des.getBytes()[2], desrouter.getBytes()[2]))) {
                d = routers.get(i);
                break;
            }
        }

        /*                 
        3. Implement forwarding, i.e., s forwards to its gateway router x considering d as the destination.
                similarly, x forwards to the next gateway router y considering d as the destination, 
                and eventually the packet reaches to destination router d.
                
            3(a) If, while forwarding, any gateway x, found from routingTable of router r is in down state[x.state==FALSE]
                    (i) Drop packet
                    (ii) Update the entry with distance Constants.INFTY
                    (iii) Block NetworkLayerServer.stateChanger.t
                    (iv) Apply DVR starting from router r.
                    (v) Resume NetworkLayerServer.stateChanger.t
                            
            3(b) If, while forwarding, a router x receives the packet from router y, 
                    but routingTableEntry shows Constants.INFTY distance from x to y,
                    (i) Update the entry with distance 1
                    (ii) Block NetworkLayerServer.stateChanger.t
                    (iii) Apply DVR starting from router x.
                    (iv) Resume NetworkLayerServer.stateChanger.t
                            
        4. If 3(a) occurs at any stage, packet will be dropped, 
            otherwise successfully sent to the destination router
         */
        Router r = null;
        r = s;

        Router x = null;
        // System.out.println("del3");
        //System.out.println("sourcce "+s.getRouterId());
        //System.out.println("des "+d.getRouterId());
        routepath = routepath + "" + s.getRouterId();

        while (true) {
            //System.out.println("del");
            ArrayList<RoutingTableEntry> rr = r.getRoutingTable();
            int gate = rr.get(d.getRouterId() - 1).getGatewayRouterId();
            //System.out.println("gate "+ gate);
            if (r.getRouterId() == gate) {
                //routepath = routepath + gate;
                return true;
            }
            if (gate == -1) {
                return false;
            } else {
                x = routers.get(gate - 1);
                if (x.getState() == false) {
                    rr.get(d.getRouterId() - 1).setDistance(Constants.INFTY);
                    rr.get(d.getRouterId() - 1).setGatewayRouterId(-1);
                    try {
                    stateChanger.wait();
                    } catch(Exception e) {
                        System.out.println(e);
                    }
                    DVR(r.getRouterId());
                     try {
                    stateChanger.notify();
                    } catch(Exception e) {
                        System.out.println(e);
                    }
                    return false;
                } else if (x.getRoutingTable().get(r.getRouterId() - 1).getDistance() == Constants.INFTY) {
                    x.getRoutingTable().get(r.getRouterId() - 1).setDistance(1);
                     x.getRoutingTable().get(r.getRouterId() - 1).setGatewayRouterId(r.getRouterId());
                    try {
                    stateChanger.wait();
                    } catch(Exception e) {
                        System.out.println(e);
                    }
                    DVR(x.getRouterId());
                     try {
                    stateChanger.notify();
                    } catch(Exception e) {
                        System.out.println(e);
                    }

                }
                r = routers.get(gate - 1);
                routepath = routepath + "-->"+r.getRouterId() ;
                Hopcount++;
            }
            
        }

    }

//    public synchronized Boolean deliverPacket(Packet p) throws InterruptedException {
//        int senderRouterId = 0;
//        int receiverRouterId = 0;
//        int copy = 0;
//        int numOfRouters = routers.size();
//        for (int s = 0; s < numOfRouters; s++) {
//            System.out.println("router " + (s + 1) + " inteface " + routers.get(s).getInterfaceAddrs().get(0));
//        }
//
//        System.out.println("senderRouterIP " + p.getSourceIP().getString());
//
//        for (int i = 0; i < numOfRouters; i++) {
//            if (Objects.equals(routers.get(i).getInterfaceAddrs().get(0).getBytes()[0], p.getSourceIP().getBytes()[0])
//                    && Objects.equals(routers.get(i).getInterfaceAddrs().get(0).getBytes()[1], p.getSourceIP().getBytes()[1])
//                    && Objects.equals(routers.get(i).getInterfaceAddrs().get(0).getBytes()[2], p.getSourceIP().getBytes()[2])) {
//                senderRouterId = i;
//                copy = senderRouterId;
//                break;
//            }
//        }
//
//        System.out.println("senderRouterId " + (senderRouterId + 1));
//
//        System.out.println("receiverRouterIP " + p.getDestinationIP().getString());
//        for (int i = 0; i < numOfRouters; i++) {
//            if ((Objects.equals(routers.get(i).getInterfaceAddrs().get(0).getBytes()[0], p.getDestinationIP().getBytes()[0]))
//                    && (Objects.equals(routers.get(i).getInterfaceAddrs().get(0).getBytes()[1], p.getDestinationIP().getBytes()[1]))
//                    && (Objects.equals(routers.get(i).getInterfaceAddrs().get(0).getBytes()[2], p.getDestinationIP().getBytes()[2]))) {
//                receiverRouterId = i;
//                break;
//            }
//        }
//        System.out.println("receiverRouterId " + (receiverRouterId + 1));
//        routingPat = routingPat + (copy + 1);
//        while (copy != receiverRouterId) {
//            int copy2 = copy;    //copy2=router r
//            copy = routers.get(copy).getRoutingTable().get(receiverRouterId).getGatewayRouterId() - 1;  //copy=router x
//            System.out.println("next hop: " + (copy + 1));
//            System.out.println("receiver: " + (receiverRouterId + 1));
//            if(copy==-1){
//                return false;[
//            }
//            if (Objects.equals(copy, -2)) {
//
//                return false;
//            }
//            System.out.println("----------------------------------------------------------------------");
//            System.out.println("3A state: " + routers.get(copy).getState());
//            System.out.println("3B state: " + routers.get(copy).getRoutingTable().get(copy2).getDistance());
//            
//            System.out.println("----------------------------------------------------------------------");
//            if (routers.get(copy).getState() == false) {
//                routers.get(copy2).getRoutingTable().get(copy).setDistance(Constants.INFTY);
//                routers.get(copy2).getRoutingTable().get(copy).setGatewayRouterId(-1);
//
//                try {
//                    stateChanger.wait();
//
//                } catch (Exception e) {
//
//                }
//
//                DVR(routers.get(copy2).getRouterId());
//                simpleDVR(routers.get(copy2).getRouterId() - 1);
//                try {
//                    stateChanger.notify();
//                } catch (Exception e) {
//                }
//                return false;
//            }
//            if (routers.get(copy).getRoutingTable().get(copy2).getDistance() == Constants.INFTY) { //copy=router x copy2=router y
//                routers.get(copy).getRoutingTable().get(copy2).setDistance(1);
//                routers.get(copy).getRoutingTable().get(copy2).setGatewayRouterId(copy2);
//
//                try {
//                    stateChanger.wait();
//                } catch (Exception e) {
//
//                }
//
//                DVR(routers.get(copy).getRouterId());
//                simpleDVR(routers.get(copy).getRouterId() - 1);
//                try {
//                    stateChanger.notify();
//                } catch (Exception e) {
//                }
//            }
//
//            hopCount++;
//            routingPat = routingPat + "-->" + (copy + 1);
//
//        }
//        return true;
//        /*
//        1. Find the router s which has an interface
//                such that the interface and source end device have same network address.
//        2. Find the router d which has an interface
//                such that the interface and destination end device have same network address.
//        3. Implement forwarding, i.e., s forwards to its gateway router x considering d as the destination.
//                similarly, x forwards to the next gateway router y considering d as the destination, 
//                and eventually the packet reaches to destination router d.
//                
//            3(a) If, while forwarding, any gateway x, found from routingTable of router r is in down state[x.state==FALSE]
//                    (i) Drop packet
//                    (ii) Update the entry with distance Constants.INFTY
//                    (iii) Block NetworkLayerServer.stateChanger.t
//                    (iv) Apply DVR starting from router r.
//                    (v) Resume NetworkLayerServer.stateChanger.t
//                            
//            3(b) If, while forwarding, a router x receives the packet from router y, 
//                    but routingTableEntry shows Constants.INFTY distance from x to y,
//                    (i) Update the entry with distance 1
//                    (ii) Block NetworkLayerServer.stateChanger.t
//                    (iii) Apply DVR starting from router x.
//                    (iv) Resume NetworkLayerServer.stateChanger.t
//                            
//        4. If 3(a) occurs at any stage, packet will be dropped, 
//            otherwise successfully sent to the destination router
//         */
//
//    }
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj); //To change body of generated methods, choose Tools | Templates.
    }

}
