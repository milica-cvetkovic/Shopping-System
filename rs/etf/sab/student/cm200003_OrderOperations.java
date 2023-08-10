/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.etf.sab.student;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import rs.etf.sab.operations.OrderOperations;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;
import rs.etf.sab.operations.BuyerOperations;
import rs.etf.sab.operations.CityOperations;
import rs.etf.sab.operations.GeneralOperations;
import rs.etf.sab.operations.TransactionOperations;

/**
 *
 * @author milic
 */
public class cm200003_OrderOperations implements OrderOperations {

    private Connection connection = DB.getInstance().getConnection();

    @Override
    public int addArticle(int i, int i1, int i2) {

        int id = -1;

        try ( PreparedStatement psCount = connection.prepareStatement("select articleCount\n"
                + "from Sells\n"
                + "where articleId = ?")) {
            psCount.setInt(1, i1);

            try ( ResultSet rsCount = psCount.executeQuery()) {
                if (rsCount.next()) {

                    if (i2 <= rsCount.getInt(1)) {

                        try ( PreparedStatement psExisting = connection.prepareStatement("select articleId\n"
                                + "from ArticlesInOrder where articleId = ?")) {
                            psExisting.setInt(1, i1);
                            try ( ResultSet rsExisting = psExisting.executeQuery()) {

                                if (rsExisting.next()) {

                                    try ( PreparedStatement psUpdate = connection.prepareStatement("update ArticlesInOrder\n"
                                            + "set count = count + ?\n"
                                            + "where orderId = ? and articleId = ?")) {
                                        psUpdate.setInt(1, i2);
                                        psUpdate.setInt(2, i);
                                        psUpdate.setInt(3, i1);
                                        psUpdate.executeUpdate();

                                    } catch (SQLException ex) {
                                        Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                                    }

                                } else {

                                    try ( PreparedStatement psInsert = connection.prepareStatement("insert into ArticlesInOrder (orderId, articleId, count) values (?,?,?)")) {
                                        psInsert.setInt(1, i);
                                        psInsert.setInt(2, i1);
                                        psInsert.setInt(3, i2);

                                        psInsert.executeUpdate();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                                    }

                                }

                                try ( PreparedStatement psFind = connection.prepareStatement("select count\n"
                                        + "from ArticlesInOrder\n"
                                        + "where orderId = ? and articleId = ?")) {
                                    psFind.setInt(1, i);
                                    psFind.setInt(2, i1);

                                    try ( ResultSet rsFind = psFind.executeQuery()) {
                                        if (rsFind.next()) {
                                            id = rsFind.getInt(1);
                                        }
                                    } catch (SQLException ex) {
                                        Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                                    }

                                } catch (SQLException ex) {
                                    Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                                }

                            } catch (SQLException ex) {
                                Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        } catch (SQLException ex) {
                            Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }

                }
            } catch (SQLException ex) {
                Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SQLException ex) {
            Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return id;
    }

    @Override
    public int removeArticle(int i, int i1) {
        int id = -1;

        try ( PreparedStatement ps = connection.prepareStatement("delete from ArticlesInOrder where orderId = ? and articleId = ?")) {
            ps.setInt(1, i);
            ps.setInt(2, i1);

            ps.executeQuery();

            id = 1;

        } catch (SQLException ex) {
            Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return id;
    }

    @Override
    public List<Integer> getItems(int i) {

        List<Integer> items = new ArrayList<>();

        try ( PreparedStatement ps = connection.prepareStatement("select articleId\n"
                + "from ArticlesInOrder\n"
                + "where orderId = ?")) {
            ps.setInt(1, i);

            try ( ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(rs.getInt(1));
                }
            } catch (SQLException ex) {
                Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (SQLException ex) {
            Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return items;
    }

    @Override
    public int completeOrder(int i) {

        int id = -1;

        try ( PreparedStatement psCheckCities = connection.prepareStatement("select distinct cityName, City.cityId as cityId\n"
                + "from OrderDone join ArticlesInOrder on OrderDone.orderId = ArticlesInOrder.orderId\n"
                + "join Article on Article.articleId = ArticlesInOrder.articleId\n"
                + "join Sells on Sells.articleId = Article.articleId\n"
                + "join Shop on Shop.shopId = Sells.shopId\n"
                + "join City on Shop.cityId = City.cityId\n"
                + "where OrderDone.orderId = ?")) {
            psCheckCities.setInt(1, i);

            try ( ResultSet rsCheckCities = psCheckCities.executeQuery()) {
                int found = 0;
                List<Integer> cities = new LinkedList<>();
                while (rsCheckCities.next()) {
                    found++;
                    cities.add(rsCheckCities.getInt(2));
                }
                if (found > 1) {

                    List<Integer> shopCities = new ArrayList<>();
                    try ( PreparedStatement psCityShop = connection.prepareStatement("select City.cityId\n"
                            + "from Shop join City on Shop.cityId = City.cityId")) {

                        try ( ResultSet rsCityShop = psCityShop.executeQuery()) {

                            while (rsCityShop.next()) {
                                shopCities.add(rsCityShop.getInt(1));
                            }

                        } catch (SQLException ex) {
                            Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    } catch (SQLException ex) {
                        Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    CityOperations cityOperations = new cm200003_CityOperations();
                    BuyerOperations buyerOperations = new cm200003_BuyerOperations();

                    int buyerCity = buyerOperations.getCity(getBuyer(i));

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

                    int minDistance = -1;
                    int minCity = -1;
                    for (Integer city : shopCities) {
                        for (Node n : nodes1) {
                            if (city == n.getCityId()) {
                                if (minDistance == -1) {
                                    minDistance = n.getDistance();
                                    minCity = n.getCityId();
                                } else {
                                    if (minDistance > n.getDistance()) {
                                        minCity = n.getCityId();
                                        minDistance = n.getDistance();
                                    }
                                }
                            }

                        }
                    }

                    // sad kad si pronasla grad za asembliranje, racunas ponovo koliko je svima potrebno da dodju u ovaj grad i uzimas max od toga, svima stavljas max i u time ga smanjujes do 0
                    graph = new Graph();

                    for (Integer city : allCities) {
                        graph.add(nodes.get(city));
                    }

                    graph = Dijkstra.shortestPath(graph, nodes.get(minCity));

                    Set<Node> nodes2 = graph.getNodes();

                    int maxDistance = -1;
                    for (Integer city : shopCities) {
                        if (city != minCity) {
                            for (Node n : nodes2) {
                                if (city == n.getCityId()) {
                                    if (maxDistance == -1) {
                                        maxDistance = n.getDistance();
                                    } else {
                                        if (maxDistance < n.getDistance()) {
                                            maxDistance = n.getDistance();
                                        }
                                    }
                                }
                            }
                        }

                    }

                    // nasli smo maxDistance, stavljamo ga u OrderDone
                    try ( PreparedStatement psAssembled = connection.prepareStatement("update OrderDone\n"
                            + "set waitAssemble = ?\n"
                            + "where orderId = ?")) {
                        psAssembled.setInt(1, maxDistance);
                        psAssembled.setInt(2, i);

                        psAssembled.executeUpdate();

                    } catch (SQLException ex) {
                        Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    // postavi flag da li je asembliran ili ne
                    try ( PreparedStatement psAssemble = connection.prepareStatement("update OrderDone\n"
                            + "set assembled = ?\n"
                            + "where orderId = ?")) {
                        psAssemble.setInt(1, 0);
                        psAssemble.setInt(2, i);

                        psAssemble.executeUpdate();

                    } catch (SQLException ex) {
                        Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    // currentCity
                    try ( PreparedStatement psCity = connection.prepareStatement("update OrderDone\n"
                            + "set currentCity = ?\n"
                            + "where orderId = ?")) {
                        psCity.setInt(1, minCity);
                        psCity.setInt(2, i);

                        psCity.executeUpdate();

                    } catch (SQLException ex) {
                        Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    // pozovi transakciju za kupca
                } else if (found == 1) {

                    // nadji najkraci pa to stavi u waitAssemble
                    int city = cities.get(0);
                    int newDistance = findNextDistance(i, city);
                    // Pair newPair = findNextCity(i, city);

                    try ( PreparedStatement psAssembled = connection.prepareStatement("update OrderDone\n"
                            + "set waitAssemble = ?\n"
                            + "where orderId = ?")) {
                        psAssembled.setInt(1, newDistance);
                        psAssembled.setInt(2, i);

                        psAssembled.executeUpdate();

                    } catch (SQLException ex) {
                        Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    // postavi flag da li je asembliran ili ne
                    try ( PreparedStatement psAssemble = connection.prepareStatement("update OrderDone\n"
                            + "set assembled = ?\n"
                            + "where orderId = ?")) {
                        psAssemble.setInt(1, 1);
                        psAssemble.setInt(2, i);

                        psAssemble.executeUpdate();

                    } catch (SQLException ex) {
                        Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    // currentCity
                    try ( PreparedStatement psCity = connection.prepareStatement("update OrderDone\n"
                            + "set currentCity = ?\n"
                            + "where orderId = ?")) {
                        psCity.setInt(1, city);
                        psCity.setInt(2, i);

                        psCity.executeUpdate();

                    } catch (SQLException ex) {
                        Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } else {
                    return -1;
                }

                // promeni state
                try ( PreparedStatement psState = connection.prepareStatement("update OrderDone\n"
                        + "set state = 'sent'\n"
                        + "where orderId = ?")) {
                    psState.setInt(1, i);

                    psState.executeUpdate();

                } catch (SQLException ex) {
                    Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                }

                // postavi sent
                try ( PreparedStatement psDate = connection.prepareStatement("update OrderDone\n"
                        + "set sentTime = ?\n"
                        + "where orderId = ?")) {
                    psDate.setDate(1, new java.sql.Date(cm200003d_GeneralOperations.time.getTimeInMillis()));
                    psDate.setInt(2, i);

                    psDate.executeUpdate();

                } catch (SQLException ex) {
                    Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                }

                List<Integer> articles = new ArrayList<>();
                List<Integer> articlesCount = new ArrayList<>();
                //dohvati sve artikle
                try ( PreparedStatement psArticles = connection.prepareStatement("select ArticlesInOrder.articleId, ArticlesInOrder.count\n"
                        + "from OrderDone join ArticlesInOrder on OrderDone.orderId = ArticlesInOrder.orderId\n"
                        + "where OrderDone.orderId = ?")) {
                    psArticles.setInt(1, i);

                    try ( ResultSet rsArticles = psArticles.executeQuery()) {
                        while (rsArticles.next()) {
                            articles.add(rsArticles.getInt(1));
                            articlesCount.add(rsArticles.getInt(2));
                        }
                    } catch (SQLException ex) {
                        Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } catch (SQLException ex) {
                    Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                }

                // smanji kolicinu u Sells (mozda treba da se doda Shop.id?)
                int cnt = 0;
                for (Integer articleId : articles) {
                    try ( PreparedStatement psSells = connection.prepareStatement("update Sells\n"
                            + "set articleCount = articleCount - ?\n"
                            + "where articleId = ?")) {
                        psSells.setInt(1, articlesCount.get(cnt));
                        psSells.setInt(2, articleId);

                        psSells.executeUpdate();

                    } catch (SQLException ex) {
                        Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    cnt++;
                }

                // transakcija za kupca
                TransactionOperations transactionOperations = new cm200003_TransactionOperations();
                OrderOperations orderOperations = new cm200003_OrderOperations();

                int transactionId = -1;
                try ( PreparedStatement psTransaction = connection.prepareStatement("insert into TransactionDone (orderId, amount, dateDone) values (?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
                    psTransaction.setInt(1, i);
                    psTransaction.setBigDecimal(2, getFinalPrice(i));
                    psTransaction.setDate(3, new java.sql.Date(cm200003d_GeneralOperations.time.getTimeInMillis()));
                    psTransaction.executeUpdate();

                    try ( ResultSet rsTransacition = psTransaction.getGeneratedKeys()) {
                        if (rsTransacition.next()) {
                            transactionId = rsTransacition.getInt(1);
                        }
                    } catch (SQLException ex) {
                        Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } catch (SQLException ex) {
                    Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                }

                try ( PreparedStatement psBuyer = connection.prepareStatement("insert into TransactionDoneBuyer (transactionId, buyerId) values (?,?)")) {

                    psBuyer.setInt(1, transactionId);
                    psBuyer.setInt(2, orderOperations.getBuyer(i));
                    psBuyer.executeUpdate();

                } catch (SQLException ex) {
                    Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                }

                id = 1;

            } catch (SQLException ex) {
                Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (SQLException ex) {
            Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return id;

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
                    newCity = 0;
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
                    newCity = 0;
                    newDistance = 0;
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

    @Override
    public BigDecimal getFinalPrice(int i) {

        BigDecimal price = BigDecimal.ZERO;
        int buyerId = getBuyer(i);

        try ( PreparedStatement ps = connection.prepareStatement("select state from OrderDone where orderId = ?")) {
            ps.setInt(1, i);
            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (rs.getString(1).equals("created")) {
                        return BigDecimal.valueOf(-1);
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                price = BigDecimal.valueOf(-1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
            price = BigDecimal.valueOf(-1);
        }

        try ( CallableStatement cs = connection.prepareCall("{call SP_FINAL_PRICE(?, ?, ?, ?)}")) {
            cs.registerOutParameter(4, java.sql.Types.DECIMAL);
            cs.setInt(1, i);
            cs.setBigDecimal(2, getFullPrice(i));
            cs.setBigDecimal(3, getDiscountSum(i));

            cs.execute();

            price = cs.getBigDecimal(4);

        } catch (SQLException ex) {
            Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
            price = BigDecimal.valueOf(-1);
        }

        price = price.setScale(3);

        return price;

    }

    public BigDecimal getFullPrice(int i) {

        BigDecimal price = BigDecimal.ZERO;

        try ( PreparedStatement ps = connection.prepareStatement("select SUM(articlePrice*COUNT)\n"
                + "from ArticlesInOrder join Article on ArticlesInOrder.articleId = Article.articleId\n"
                + "where orderId = ?")) {
            ps.setInt(1, i);

            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    price = BigDecimal.valueOf(rs.getDouble(1));
                }
            } catch (SQLException ex) {
                Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (SQLException ex) {
            Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return price;

    }

    @Override
    public BigDecimal getDiscountSum(int i) {

        BigDecimal discount = BigDecimal.ZERO;

        List<Integer> discounts = new ArrayList<>();
        List<Integer> shops = new ArrayList<>();

        try ( PreparedStatement psStatus = connection.prepareStatement("select state\n"
                + "from OrderDone\n"
                + "where orderId = ?")) {
            psStatus.setInt(1, i);

            try ( ResultSet rsStatus = psStatus.executeQuery()) {
                if (rsStatus.next()) {
                    if (rsStatus.getString(1).equals("sent")) {

                        GeneralOperations generalOperations = new cm200003d_GeneralOperations();

                        int buyerId = getBuyer(i);
                        if (buyerId == -1) {
                            return BigDecimal.valueOf(-1);
                        }

                        try ( PreparedStatement psGet = connection.prepareStatement("select coalesce(SUM(amount),0)\n"
                                + "from TransactionDone join TransactionDoneBuyer on TransactionDone.transactionId = TransactionDoneBuyer.transactionId\n"
                                + "where DATEDIFF(DAY, dateDone, ?) between 0 and 30 \n"
                                + "and TransactionDoneBuyer.buyerId = ?")) {
                            psGet.setDate(1, new java.sql.Date(generalOperations.getCurrentTime().getTimeInMillis()));
                            psGet.setInt(2, buyerId);

                            try ( ResultSet rsGet = psGet.executeQuery()) {
                                if (rsGet.next()) {
                                    if (rsGet.getBigDecimal(1).compareTo(BigDecimal.valueOf(10000.000)) == 1) {

                                        BigDecimal temp = getFullPrice(i);
                                        temp = temp.multiply(BigDecimal.TWO);

                                        temp = temp.divide(BigDecimal.valueOf(100));

                                        discount = discount.add(temp);
                                    }
                                }
                            } catch (SQLException ex) {
                                Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                                discount = BigDecimal.valueOf(-1);
                            }
                        } catch (SQLException ex) {
                            Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                            discount = BigDecimal.valueOf(-1);
                        }

                        try ( PreparedStatement psDiscount = connection.prepareStatement("select distinct discount, Shop.shopId as shopId\n"
                                + "from OrderDone join ArticlesInOrder on OrderDone.orderId = ArticlesInOrder.orderId\n"
                                + "join Article on Article.articleId = ArticlesInOrder.articleId\n"
                                + "join Sells on Sells.articleId = Article.articleId\n"
                                + "join Shop on Shop.shopId = Sells.shopId\n"
                                + "where OrderDone.orderId = ?")) {
                            psDiscount.setInt(1, i);

                            try ( ResultSet rsDiscount = psDiscount.executeQuery()) {
                                while (rsDiscount.next()) {
                                    discounts.add(rsDiscount.getInt(1));
                                    shops.add(rsDiscount.getInt(2));
                                }
                            } catch (SQLException ex) {
                                Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                                discount = BigDecimal.valueOf(-1);
                            }

                        } catch (SQLException ex) {
                            Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                            discount = BigDecimal.valueOf(-1);
                        }

                        for (int j = 0; j < discounts.size(); j++) {

                            try ( PreparedStatement psFind = connection.prepareStatement("select articlePrice, count\n"
                                    + "from Article join ArticlesInOrder on Article.articleId = ArticlesInOrder.articleId\n"
                                    + "join Sells on Sells.articleId = Article.articleId \n"
                                    + "join Shop on Shop.shopId = Sells.shopId\n"
                                    + "where orderId = ? and Shop.shopId = ?")) {
                                psFind.setInt(1, i);
                                psFind.setInt(2, shops.get(j));

                                try ( ResultSet rsFind = psFind.executeQuery()) {
                                    while (rsFind.next()) {
                                        BigDecimal temp = rsFind.getBigDecimal(1).multiply(BigDecimal.valueOf(discounts.get(j)));
                                        temp = temp.divide(BigDecimal.valueOf(100));
                                        temp = temp.multiply(BigDecimal.valueOf(rsFind.getInt(2)));
                                        discount = discount.add(temp);
                                    }
                                } catch (SQLException ex) {
                                    Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                                    discount = BigDecimal.valueOf(-1);
                                }

                            } catch (SQLException ex) {
                                Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                                discount = BigDecimal.valueOf(-1);
                            }

                        }

                    } else {
                        discount = BigDecimal.valueOf(-1);
                    }

                }
            } catch (SQLException ex) {
                Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                discount = BigDecimal.valueOf(-1);
            }

        } catch (SQLException ex) {
            Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
            discount = BigDecimal.valueOf(-1);
        }

        discount = discount.setScale(3);

        return discount;
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

    @Override
    public String getState(int i) {

        String state = null;

        try ( PreparedStatement ps = connection.prepareStatement("select state\n"
                + "from OrderDone\n"
                + "where orderId = ?");) {
            ps.setInt(1, i);

            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    state = rs.getString(1);
                }
            } catch (SQLException ex) {
                Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SQLException ex) {
            Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return state;

    }

    @Override
    public Calendar getSentTime(int i) {

        Calendar time = Calendar.getInstance();

        try ( PreparedStatement ps = connection.prepareStatement("select sentTime\n"
                + "from OrderDone\n"
                + "where orderId = ?");) {
            ps.setInt(1, i);
            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (rs.getDate(1) != null) {
                        time.setTimeInMillis(rs.getDate(1).getTime());
                    } else {
                        time = null;
                    }

                }
            } catch (SQLException ex) {
                Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SQLException ex) {
            Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return time;
    }

    @Override
    public Calendar getRecievedTime(int i) {

        Calendar time = Calendar.getInstance();

        try ( PreparedStatement ps = connection.prepareStatement("select receivedTime\n"
                + "from OrderDone\n"
                + "where orderId = ?");) {
            ps.setInt(1, i);
            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (rs.getDate(1) != null) {
                        time.setTimeInMillis(rs.getDate(1).getTime());
                    } else {
                        time = null;
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SQLException ex) {
            Logger.getLogger(cm200003_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return time;

    }

    @Override
    public int getBuyer(int i) {

        int id = -1;

        try ( PreparedStatement ps = connection.prepareStatement("select buyerId\n"
                + "from OrderDone\n"
                + "where orderId = ?");) {
            ps.setInt(1, i);
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

    @Override
    public int getLocation(int i) {

        int id = -1;

        String state = getState(i);

        if (state.equals("created")) {
            return id;
        }

        try ( PreparedStatement psCity = connection.prepareStatement("select currentCity\n"
                + "from OrderDone\n"
                + "where orderId = ?")) {
            psCity.setInt(1, i);

            try ( ResultSet rsCity = psCity.executeQuery()) {
                if (rsCity.next()) {
                    id = rsCity.getInt(1);
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
