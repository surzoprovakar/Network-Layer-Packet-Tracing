/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networklayer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;
import static networklayer.NetworkLayerServer.routers;

/**
 *
 * @author samsung
 */
public class Router implements Serializable{

    private int routerId;
    private int numberOfInterfaces;
    private ArrayList<IPAddress> interfaceAddrs;//list of IP address of all interfaces of the router
    private ArrayList<RoutingTableEntry> routingTable;//used to implement DVR
    private ArrayList<Integer> neighborRouterIds;//Contains both "UP" and "DOWN" state routers
    private Boolean state;//true represents "UP" state and false is for "DOWN" state
    //private Boolean update;

    public Router() {
        interfaceAddrs = new ArrayList<>();
        routingTable = new ArrayList<>();
        neighborRouterIds = new ArrayList<>();

        /**
         * 80% Probability that the router is up
         */
        Random random = new Random();
        double p = random.nextDouble();
        if (p <= 0.80) {
            state = true;
        } else {
            state = false;
        }

        numberOfInterfaces = 0;
        //update = false;
    }

    public Router(int routerId, ArrayList<Integer> neighborRouters, ArrayList<IPAddress> interfaceAddrs) {
        this.routerId = routerId;
        this.interfaceAddrs = interfaceAddrs;
        this.neighborRouterIds = neighborRouters;
        routingTable = new ArrayList<>();

        /**
         * 80% Probability that the router is up
         */
        Random random = new Random();
        double p = random.nextDouble();
        if (p <= 0.80) {
            state = true;
        } else {
            state = false;
        }
        

        numberOfInterfaces = this.interfaceAddrs.size();
        //update = false;
    }

    @Override
    public String toString() {
        String temp = "";
        temp += "Router ID: " + routerId + "\n";
        temp += "Intefaces: \n";
        for (int i = 0; i < numberOfInterfaces; i++) {
            temp += interfaceAddrs.get(i).getString() + "\t";
        }
        temp += "\n";
        temp += "Neighbors: \n";
        for (int i = 0; i < neighborRouterIds.size(); i++) {
            temp += neighborRouterIds.get(i) + "\t";
        }
        return temp;
    }

    /**
     * Initialize the distance(hop count) for each router. for itself,
     * distance=0; for any connected router with state=true, distance=1;
     * otherwise distance=Constants.INFTY;
     */
    public void initiateRoutingTable() {
        routingTable.clear();
        double dis = Constants.INFTY;
        
        RoutingTableEntry entry = null;
        for (int i = 0; i < routers.size(); i++) {
            int flag = 1;
            if (routers.get(i).routerId == this.routerId) {
                dis = 0;
                entry = new RoutingTableEntry(this.routerId, dis, routerId);
            } 
            else {
                for (int j = 0; j < neighborRouterIds.size(); j++) {
                    Integer neighbor = neighborRouterIds.get(j);
                    if (routers.get(i).routerId == neighbor.intValue() && routers.get(i).getState() == true) {
                        dis = 1;
                        entry = new RoutingTableEntry(routers.get(i).routerId, dis, routers.get(i).routerId);
                        flag = 0;
                        break;
                    }
                }
                if (flag == 1) {
                    dis = Constants.INFTY;
                    entry = new RoutingTableEntry(routers.get(i).routerId, dis, -1);
                    

                }
            }

            routingTable.add(entry);
        }

    }

    /**
     * Delete all the routingTableEntry
     */
    public void clearRoutingTable() {
        routingTable.clear();
        for(int i=0;i<routers.size();i++) {
            RoutingTableEntry r=new RoutingTableEntry(routers.get(i).getRouterId(), Constants.INFTY, -1);
            routingTable.add(r);
            
        }
    }

    /**
     * Update the routing table for this router using the entries of Router
     * neighbor
     *
     * @param neighbor
     */
    /*public boolean updateRoutingTable(Router neighbor) {
        boolean update=false;
        double dis = 1;
        double updateDis = 0;
        ArrayList<RoutingTableEntry> neighborTable = neighbor.getRoutingTable();
        ArrayList<RoutingTableEntry> currentTable = this.getRoutingTable();
        if (neighbor.getRoutingTable().isEmpty() == false) {
            for (int i = 0; i < currentTable.size(); i++) {
                updateDis = dis + neighborTable.get(i).getDistance();
                if (currentTable.get(i).getGatewayRouterId() == neighborTable.get(i).getRouterId()
                        || (updateDis < currentTable.get(i).getDistance()
                        && this.getRouterId() != neighborTable.get(i).getGatewayRouterId())) {
                    currentTable.get(i).setDistance(updateDis);
                    currentTable.get(i).setGatewayRouterId(neighborTable.get(i).getRouterId());
                    
                    update = true;
                }
            }
        } else {
            for (int i = 0; i < currentTable.size(); i++) {
                if (currentTable.get(i).getGatewayRouterId() == neighbor.getRouterId()) {
                    currentTable.get(i).setDistance(Constants.INFTY);
                    currentTable.get(i).setGatewayRouterId(-1);
                    //update = true;
                }
            }
        }
        return update;
        
    }*/
    
    public boolean updateRoutingTable(Router neighbor) {
        int numOfRouters = NetworkLayerServer.routers.size();
        boolean bool2 = false;
        if (neighbor.getState() == false) {
            for (int i = 0; i < routingTable.size(); i++) {
                if (routingTable.get(i).getGatewayRouterId() == neighbor.getRouterId() && routingTable.get(i).getDistance() != Constants.INFTY) {
                    routingTable.get(i).setDistance(Constants.INFTY);
                    routingTable.get(i).setGatewayRouterId(-1);
                    bool2 = true;
                }
            }
            return bool2;
        }
        double d;
        boolean bool = false;
        for (int i = 0; i < numOfRouters; i++) {
            d = routingTable.get(neighbor.routerId - 1).getDistance() + neighbor.getRoutingTable().get(i).getDistance();
            if (d > Constants.INFTY) {
                d = Constants.INFTY;
            }
            if (routingTable.get(i).getGatewayRouterId() == neighbor.routerId
                    || (d < routingTable.get(i).getDistance() && routerId != neighbor.getRoutingTable().get(i).getGatewayRouterId())) {
                if (d == routingTable.get(i).getDistance()) {
                    continue;
                }
                routingTable.get(i).setDistance(d);
                routingTable.get(i).setGatewayRouterId(neighbor.routerId);
                bool = true;
            }
        }
        return bool;
    }

    /*public void simpleupdateRoutingTable(Router neighbor) {
        double dis = 1;
        double updateDis = 0;
        ArrayList<RoutingTableEntry> neighborTable = neighbor.getRoutingTable();
        ArrayList<RoutingTableEntry> currentTable = this.getRoutingTable();
        if (neighbor.getRoutingTable().isEmpty() == false) {
            for (int i = 0; i < currentTable.size(); i++) {
                updateDis = dis + neighborTable.get(i).getDistance();
                if (updateDis < currentTable.get(i).getDistance()) {
                    currentTable.get(i).setDistance(updateDis);
                    currentTable.get(i).setGatewayRouterId(neighborTable.get(i).getRouterId());
                    //update = true;
                }
            }
        } else {
            for (int i = 0; i < currentTable.size(); i++) {
                if (currentTable.get(i).getGatewayRouterId() == neighbor.getRouterId()) {
                    currentTable.get(i).setDistance(Constants.INFTY);
                    //update = true;
                }
            }
        }
    }*/
    
    public boolean simpleUpdateRoutingTable(Router neighbor) {
        int numOfRouters = NetworkLayerServer.routers.size();
        boolean bool2 = false;
        if (neighbor.getState() == false) {
            for (int i = 0; i < routingTable.size(); i++) {
                if (routingTable.get(i).getGatewayRouterId() == neighbor.getRouterId() && routingTable.get(i).getDistance() != Constants.INFTY) {
                    routingTable.get(i).setDistance(Constants.INFTY);
                    routingTable.get(i).setGatewayRouterId(-1);
                    bool2 = true;
                }
            }
            return bool2;
        }
        double d;
        boolean bool = false;
        for (int i = 0; i < numOfRouters; i++) {
            d = routingTable.get(neighbor.routerId - 1).getDistance() + neighbor.getRoutingTable().get(i).getDistance();
            if (d > Constants.INFTY) {
                d = Constants.INFTY;
            }
            if (d < routingTable.get(i).getDistance()) {
//                if (d == routingTable.get(i).getDistance()) {
//                    continue;
//                }
                routingTable.get(i).setDistance(d);
                routingTable.get(i).setGatewayRouterId(neighbor.routerId);
                bool = true;
            }
        }
        return bool;
    }



    /**
     * If the state was up, down it; if state was down, up it
     */
    public void revertState() {
        state = !state;
        if (state == true) {
            this.initiateRoutingTable();
        } else {
            this.clearRoutingTable();
        }
    }

    public int getRouterId() {
        return routerId;
    }

    public void setRouterId(int routerId) {
        this.routerId = routerId;
    }

    public int getNumberOfInterfaces() {
        return numberOfInterfaces;
    }

    public void setNumberOfInterfaces(int numberOfInterfaces) {
        this.numberOfInterfaces = numberOfInterfaces;
    }

    public ArrayList<IPAddress> getInterfaceAddrs() {
        return interfaceAddrs;
    }

    public void setInterfaceAddrs(ArrayList<IPAddress> interfaceAddrs) {
        this.interfaceAddrs = interfaceAddrs;
        numberOfInterfaces = this.interfaceAddrs.size();
    }

    public ArrayList<RoutingTableEntry> getRoutingTable() {
        return routingTable;
    }

    public void addRoutingTableEntry(RoutingTableEntry entry) {
        this.routingTable.add(entry);
    }

    public ArrayList<Integer> getNeighborRouterIds() {
        return neighborRouterIds;
    }

    public void setNeighborRouterIds(ArrayList<Integer> neighborRouterIds) {
        this.neighborRouterIds = neighborRouterIds;
    }

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

//    public void setUpdate(Boolean update) {
//        this.update = update;
//    }
//
//    public Boolean getUpdate() {
//        return update;
//    }

}
