/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.etf.sab.student;

import java.util.Calendar;
import rs.etf.sab.operations.GeneralOperations;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;
import rs.etf.sab.operations.BuyerOperations;
import rs.etf.sab.operations.CityOperations;
import rs.etf.sab.operations.OrderOperations;

/**
 *
 * @author milic
 */
public class cm200003d_GeneralOperations implements GeneralOperations {

    private Connection connection = DB.getInstance().getConnection();

    public static Calendar time = Calendar.getInstance();

    @Override
    public void setInitialTime(Calendar clndr) {
        this.time.setTimeInMillis(clndr.getTimeInMillis());
    }

    @Override
    public Calendar time(int i) {
        this.time.add(Calendar.DATE, i);

        try ( PreparedStatement psAssembled = connection.prepareStatement("select assembled, orderId, waitAssemble\n"
                + "from OrderDone\n"
                + "where state = 'sent'")) {

            try ( ResultSet rsAssembled = psAssembled.executeQuery()) {
                while (rsAssembled.next()) {

                    if (rsAssembled.getInt(1) == 0) {

                        // nije jos asembliran, smanjujem waitAssemble
                        if (i < rsAssembled.getInt(3)) {
                            // samo smanjuje
                            try ( PreparedStatement psDecrease = connection.prepareStatement("update OrderDone\n"
                                    + "set waitAssemble = waitAssemble - ?\n"
                                    + "where orderId = ?")) {
                                psDecrease.setInt(1, i);
                                psDecrease.setInt(2, rsAssembled.getInt(2));

                                psDecrease.executeUpdate();

                            } catch (SQLException ex) {
                                Logger.getLogger(cm200003d_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        } else if (i == rsAssembled.getInt(3)) {
                            // smanjuje i prebacuje da je assembled i stavljas max waitAssemble za taj grad ne umanjujes

                            try ( PreparedStatement psAssemble = connection.prepareStatement("update OrderDone\n"
                                    + "set assembled = ?\n"
                                    + "where orderId = ?")) {
                                psAssemble.setInt(1, 1);
                                psAssemble.setInt(2, rsAssembled.getInt(2));

                                psAssemble.executeUpdate();

                            } catch (SQLException ex) {
                                Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            int city = -1;
                            try ( PreparedStatement psCity = connection.prepareStatement("select currentCity\n"
                                    + "from OrderDone\n"
                                    + "where orderId = ?")) {
                                psCity.setInt(1, rsAssembled.getInt(2));

                                try ( ResultSet rsCity = psCity.executeQuery()) {
                                    if (rsCity.next()) {
                                        city = rsCity.getInt(1);
                                    }
                                } catch (SQLException ex) {
                                    Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                                }

                            } catch (SQLException ex) {
                                Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            // ne moze findNextCity jer se jos ne prebacuje na sledeci grad
                            int newDistance = findNextDistance(rsAssembled.getInt(2), city);

                            try ( PreparedStatement psAssemble = connection.prepareStatement("update OrderDone\n"
                                    + "set waitAssemble = ?\n"
                                    + "where orderId = ?")) {
                                psAssemble.setInt(1, newDistance);
                                psAssemble.setInt(2, rsAssembled.getInt(2));

                                psAssemble.executeUpdate();

                            } catch (SQLException ex) {
                                Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        } else if (i > rsAssembled.getInt(3)) {
                            // smanjuje, prebacuje da je assembled i salje dalje, mozda u drugi grad moras da radis putanju

                            int num = i - rsAssembled.getInt(3);
                            try ( PreparedStatement psAssemble = connection.prepareStatement("update OrderDone\n"
                                    + "set assembled = ?\n"
                                    + "where orderId = ?")) {
                                psAssemble.setInt(1, 1);
                                psAssemble.setInt(2, rsAssembled.getInt(2));

                                psAssemble.executeUpdate();

                            } catch (SQLException ex) {
                                Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            // updateuj city i waitAssemble
                            int city = -1;
                            try ( PreparedStatement psCity = connection.prepareStatement("select currentCity\n"
                                    + "from OrderDone\n"
                                    + "where orderId = ?")) {
                                psCity.setInt(1, rsAssembled.getInt(2));

                                try ( ResultSet rsCity = psCity.executeQuery()) {
                                    if (rsCity.next()) {
                                        city = rsCity.getInt(1);
                                    }
                                } catch (SQLException ex) {
                                    Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                                }

                            } catch (SQLException ex) {
                                Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            int newDistance = findNextDistance(rsAssembled.getInt(2), city);

                            if (num < newDistance) {

                                try ( PreparedStatement psAssemble = connection.prepareStatement("update OrderDone\n"
                                        + "set waitAssemble = ?\n"
                                        + "where orderId = ?")) {
                                    psAssemble.setInt(1, newDistance - num);
                                    psAssemble.setInt(2, rsAssembled.getInt(2));

                                    psAssemble.executeUpdate();

                                } catch (SQLException ex) {
                                    Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                                }

                            } else {

                                // mozda je problem kad bude 0
                                Pair newPair = findNextCity(rsAssembled.getInt(2), city);

                                int found = 0;

                                while (num >= (Integer) newPair.getRight()) {
                                    num -= (Integer) newPair.getRight();
                                    city = (Integer) newPair.getLeft();
                                    newPair = findNextCity(rsAssembled.getInt(2), (Integer) newPair.getLeft());

                                    if ((Integer) newPair.getRight() == 0) {
                                        found = 1;
                                        city = (Integer) newPair.getLeft();
                                        break;
                                    }
                                }

                                if (found == 1) {

                                    try ( PreparedStatement psAssemble = connection.prepareStatement("update OrderDone\n"
                                            + "set state = 'arrived'\n"
                                            + "where orderId = ?")) {
                                        psAssemble.setInt(1, rsAssembled.getInt(2));

                                        psAssemble.executeUpdate();

                                    } catch (SQLException ex) {
                                        Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                                    }

                                    try ( PreparedStatement psDate = connection.prepareStatement("update OrderDone\n"
                                            + "set receivedTime = ?\n"
                                            + "where orderId = ?")) {

                                        Calendar receive = getCurrentTime();
                                        receive.add(Calendar.DATE, -num);
                                        psDate.setDate(1, new java.sql.Date(receive.getTimeInMillis()));
                                        psDate.setInt(2, rsAssembled.getInt(2));

                                        psDate.executeUpdate();

                                    } catch (SQLException ex) {
                                        Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                                    }

                                    try ( PreparedStatement psCity = connection.prepareStatement("update OrderDone\n"
                                            + "set currentCity = ?\n"
                                            + "where orderId = ?")) {
                                        psCity.setInt(1, city);
                                        psCity.setInt(2, rsAssembled.getInt(2));

                                        psCity.executeUpdate();

                                    } catch (SQLException ex) {
                                        Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    
                                    continue;
                                }

                                try ( PreparedStatement psAssemble = connection.prepareStatement("update OrderDone\n"
                                        + "set waitAssemble = ?\n"
                                        + "where orderId = ?")) {
                                    psAssemble.setInt(1, (Integer) newPair.getRight() - num);
                                    psAssemble.setInt(2, rsAssembled.getInt(2));

                                    psAssemble.executeUpdate();

                                } catch (SQLException ex) {
                                    Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                                }

                                try ( PreparedStatement psCity = connection.prepareStatement("update OrderDone\n"
                                        + "set currentCity = ?\n"
                                        + "where orderId = ?")) {
                                    psCity.setInt(1, city);
                                    psCity.setInt(2, rsAssembled.getInt(2));

                                    psCity.executeUpdate();

                                } catch (SQLException ex) {
                                    Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }

                        }

                    } else if (rsAssembled.getInt(1) == 1) {
                        
                        // asembliran, moram da pomeram i grad i da racunam putanju
                        
                        if (i < rsAssembled.getInt(3)) {
                            // samo smanjuje
                            try ( PreparedStatement psDecrease = connection.prepareStatement("update OrderDone\n"
                                    + "set waitAssemble = waitAssemble - ?\n"
                                    + "where orderId = ?")) {
                                psDecrease.setInt(1, i);
                                psDecrease.setInt(2, rsAssembled.getInt(2));

                                psDecrease.executeUpdate();

                            } catch (SQLException ex) {
                                Logger.getLogger(cm200003d_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        } else if (i == rsAssembled.getInt(3)) {
                            
                            // smanjuje i prebacuje da je assembled i  stavljas max waitAssemble za taj grad ne umanjujes

                            int city = -1;
                            try ( PreparedStatement psCity = connection.prepareStatement("select currentCity\n"
                                    + "from OrderDone\n"
                                    + "where orderId = ?")) {
                                psCity.setInt(1, rsAssembled.getInt(2));

                                try ( ResultSet rsCity = psCity.executeQuery()) {
                                    if (rsCity.next()) {
                                        city = rsCity.getInt(1);
                                    }
                                } catch (SQLException ex) {
                                    Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                                }

                            } catch (SQLException ex) {
                                Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            Pair newPair = findNextCity(rsAssembled.getInt(2), city);
                            city = (Integer) newPair.getLeft();

                            try ( PreparedStatement psAssemble = connection.prepareStatement("update OrderDone\n"
                                    + "set waitAssemble = ?\n"
                                    + "where orderId = ?")) {
                                psAssemble.setInt(1, (Integer) newPair.getRight());
                                psAssemble.setInt(2, rsAssembled.getInt(2));

                                psAssemble.executeUpdate();

                            } catch (SQLException ex) {
                                Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            try ( PreparedStatement psCity = connection.prepareStatement("update OrderDone\n"
                                    + "set currentCity = ?\n"
                                    + "where orderId = ?")) {
                                psCity.setInt(1, city);
                                psCity.setInt(2, rsAssembled.getInt(2));

                                psCity.executeUpdate();

                            } catch (SQLException ex) {
                                Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        } else if (i > rsAssembled.getInt(3)) {
                            
                            // smanjuje, prebacuje da je assembled i salje dalje, mozda u drugi grad moras da radis putanju

                            int num = i - rsAssembled.getInt(3);

                            // updateuj city i waitAssemble
                            int city = -1;
                            try ( PreparedStatement psCity = connection.prepareStatement("select currentCity\n"
                                    + "from OrderDone\n"
                                    + "where orderId = ?")) {
                                psCity.setInt(1, rsAssembled.getInt(2));

                                try ( ResultSet rsCity = psCity.executeQuery()) {
                                    if (rsCity.next()) {
                                        city = rsCity.getInt(1);
                                    }
                                } catch (SQLException ex) {
                                    Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                                }

                            } catch (SQLException ex) {
                                Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            Pair newPair = findNextCity(rsAssembled.getInt(2), city);
                            city = (Integer) newPair.getLeft();

                            OrderOperations orderOperations = new cm200003_OrderOperations();
                            if (city == orderOperations.getBuyer(rsAssembled.getInt(2))) {

                                try ( PreparedStatement psAssemble = connection.prepareStatement("update OrderDone\n"
                                        + "set state = 'arrived'\n"
                                        + "where orderId = ?")) {
                                    psAssemble.setInt(1, rsAssembled.getInt(2));

                                    psAssemble.executeUpdate();

                                } catch (SQLException ex) {
                                    Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                                }

                                try ( PreparedStatement psDate = connection.prepareStatement("update OrderDone\n"
                                        + "set receivedTime = ?\n"
                                        + "where orderId = ?")) {

                                    Calendar receive = getCurrentTime();
                                    receive.add(Calendar.DATE, -num);
                                    psDate.setDate(1, new java.sql.Date(receive.getTimeInMillis()));
                                    psDate.setInt(2, rsAssembled.getInt(2));

                                    psDate.executeUpdate();

                                } catch (SQLException ex) {
                                    Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                                }

                                try ( PreparedStatement psCity = connection.prepareStatement("update OrderDone\n"
                                        + "set currentCity = ?\n"
                                        + "where orderId = ?")) {
                                    psCity.setInt(1, city);
                                    psCity.setInt(2, rsAssembled.getInt(2));

                                    psCity.executeUpdate();

                                } catch (SQLException ex) {
                                    Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                                }

                                continue;

                            }

                            int found = 0;

                            while (num >= (Integer) newPair.getRight()) {

                                num -= (Integer) newPair.getRight();
                                city = (Integer) newPair.getLeft();

                                newPair = findNextCity(rsAssembled.getInt(2), (Integer) newPair.getLeft());

                                if ((Integer) newPair.getRight() == 0) {
                                    found = 1;
                                    city = (Integer) newPair.getLeft();
                                    break;
                                }

                            }

                            if (found == 1) {

                                try ( PreparedStatement psAssemble = connection.prepareStatement("update OrderDone\n"
                                        + "set state = 'arrived'\n"
                                        + "where orderId = ?")) {
                                    psAssemble.setInt(1, rsAssembled.getInt(2));

                                    psAssemble.executeUpdate();

                                } catch (SQLException ex) {
                                    Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                                }

                                try ( PreparedStatement psDate = connection.prepareStatement("update OrderDone\n"
                                        + "set receivedTime = ?\n"
                                        + "where orderId = ?")) {

                                    Calendar receive = getCurrentTime();
                                    receive.add(Calendar.DATE, -num);
                                    psDate.setDate(1, new java.sql.Date(receive.getTimeInMillis()));
                                    psDate.setInt(2, rsAssembled.getInt(2));

                                    psDate.executeUpdate();

                                } catch (SQLException ex) {
                                    Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                                }

                                try ( PreparedStatement psCity = connection.prepareStatement("update OrderDone\n"
                                        + "set currentCity = ?\n"
                                        + "where orderId = ?")) {
                                    psCity.setInt(1, city);
                                    psCity.setInt(2, rsAssembled.getInt(2));

                                    psCity.executeUpdate();

                                } catch (SQLException ex) {
                                    Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                                }

                                continue;
                            }
                            
                            try ( PreparedStatement psAssemble = connection.prepareStatement("update OrderDone\n"
                                    + "set waitAssemble = ?\n"
                                    + "where orderId = ?")) {
                                psAssemble.setInt(1, (Integer) newPair.getRight() - num);
                                psAssemble.setInt(2, rsAssembled.getInt(2));

                                psAssemble.executeUpdate();

                            } catch (SQLException ex) {
                                Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            try ( PreparedStatement psCity = connection.prepareStatement("update OrderDone\n"
                                    + "set currentCity = ?\n"
                                    + "where orderId = ?")) {
                                psCity.setInt(1, city);
                                psCity.setInt(2, rsAssembled.getInt(2));

                                psCity.executeUpdate();

                            } catch (SQLException ex) {
                                Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        }

                    }

                }
            } catch (SQLException ex) {
                Logger.getLogger(cm200003d_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (SQLException ex) {
            Logger.getLogger(cm200003d_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return this.time;
    }

    @Override
    public Calendar getCurrentTime() {
        return this.time;
    }

    @Override
    public void eraseAll() {

        try ( PreparedStatement stmt = connection.prepareStatement("delete from ArticlesInOrder")) {
            stmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(cm200003d_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        try ( PreparedStatement stmt = connection.prepareStatement("delete from Sells")) {
            stmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(cm200003d_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        try ( PreparedStatement stmt = connection.prepareStatement("delete from TransactionDoneBuyer")) {
            stmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(cm200003d_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        try ( PreparedStatement stmt = connection.prepareStatement("delete from TransactionDoneShop")) {
            stmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(cm200003d_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        try ( PreparedStatement stmt = connection.prepareStatement("delete from TransactionDone")) {
            stmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(cm200003d_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        try ( PreparedStatement stmt = connection.prepareStatement("delete from Line")) {
            stmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(cm200003d_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        try ( PreparedStatement stmt = connection.prepareStatement("delete from OrderDone")) {
            stmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(cm200003d_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        try ( PreparedStatement stmt = connection.prepareStatement("delete from Article")) {
            stmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(cm200003d_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        try ( PreparedStatement stmt = connection.prepareStatement("delete from Buyer")) {
            stmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(cm200003d_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        try ( PreparedStatement stmt = connection.prepareStatement("delete from Shop")) {
            stmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(cm200003d_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        try ( PreparedStatement stmt = connection.prepareStatement("delete from City")) {
            stmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(cm200003d_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public int findNextDistance(int i, int currentCity) {

        CityOperations cityOperations = new cm200003_CityOperations();
        BuyerOperations buyerOperations = new cm200003_BuyerOperations();
        OrderOperations orderOperations = new cm200003_OrderOperations();

        int buyerCity = buyerOperations.getCity(orderOperations.getBuyer(i));

        List<Integer> allCities = cityOperations.getCities();

        Map<Integer, Node> nodes = new HashMap<>();

        for (Integer city : allCities) {
            nodes.put(city, new Node(city));
        }

        for (Integer city : allCities) {

            List<Integer> connected = cityOperations.getConnectedCities(city);
            for (Integer cityConnected : connected) {
                nodes.get(city).addAdjacentNode(nodes.get(cityConnected), getDistance(city, cityConnected));
            }
        }

        Graph graph = new Graph();

        for (Integer city : allCities) {
            graph.add(nodes.get(city));
        }

        graph = Dijkstra.shortestPath(graph, nodes.get(buyerCity));

        Set<Node> nodes1 = graph.getNodes();

        int newDistance = -1;

        for (Node n : nodes1) {
            if (n.getCityId() == currentCity) {

                List<Node> shortestPath = n.getShortestPath();

                if (shortestPath.size() - 1 != -1) {
                    newDistance = n.getDistance() - shortestPath.get(shortestPath.size() - 1).getDistance();
                } else {
                    newDistance = 0;
                }

                break;
            }
        }

        return newDistance;

    }

    public Pair<Integer, Integer> findNextCity(int i, int currentCity) {

        CityOperations cityOperations = new cm200003_CityOperations();
        BuyerOperations buyerOperations = new cm200003_BuyerOperations();
        OrderOperations orderOperations = new cm200003_OrderOperations();

        int buyerCity = buyerOperations.getCity(orderOperations.getBuyer(i));

        List<Integer> allCities = cityOperations.getCities();

        Map<Integer, Node> nodes = new HashMap<>();

        for (Integer city : allCities) {
            nodes.put(city, new Node(city));
        }

        for (Integer city : allCities) {

            List<Integer> connected = cityOperations.getConnectedCities(city);
            for (Integer cityConnected : connected) {
                nodes.get(city).addAdjacentNode(nodes.get(cityConnected), getDistance(city, cityConnected));
            }
        }

        Graph graph = new Graph();

        for (Integer city : allCities) {
            graph.add(nodes.get(city));
        }

        graph = Dijkstra.shortestPath(graph, nodes.get(buyerCity));

        Set<Node> nodes1 = graph.getNodes();

        int newCity = -1;
        int newDistance = -1;

        for (Node n : nodes1) {
            if (n.getCityId() == currentCity) {

                List<Node> shortestPath = n.getShortestPath();

                if (shortestPath.size() - 1 != -1) {
                    newCity = shortestPath.get(shortestPath.size() - 1).getCityId();
                    newDistance = shortestPath.get(shortestPath.size() - 1).getDistance();
                } else {
                    newCity = currentCity;
                    newDistance = 0;
                    return new Pair<Integer, Integer>(newCity, newDistance);
                }

                break;
            }
        }

        for (Node n : nodes1) {
            if (n.getCityId() == newCity) {

                List<Node> shortestPath = n.getShortestPath();

                if (shortestPath.size() - 1 != -1) {
                    newDistance -= shortestPath.get(shortestPath.size() - 1).getDistance();
                } else {
                    newDistance -= 0;
                }

                break;
            }
        }

        return new Pair<Integer, Integer>(newCity, newDistance);

    }

    public int getDistance(int city1, int city2) {

        int id = -1;

        try ( PreparedStatement ps = connection.prepareStatement("select distance\n"
                + "from Line\n"
                + "where (cityId1 = ? and cityId2 = ?) \n"
                + "or (cityId1 = ? and cityId2 = ?)")) {
            ps.setInt(1, city1);
            ps.setInt(2, city2);
            ps.setInt(3, city2);
            ps.setInt(4, city1);

            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    id = rs.getInt(1);
                }
            } catch (SQLException ex) {
                Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (SQLException ex) {
            Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return id;
    }

}
