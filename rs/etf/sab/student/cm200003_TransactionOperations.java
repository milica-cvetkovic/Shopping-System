/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.etf.sab.student;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.TransactionOperations;
import java.sql.ResultSet;
import java.util.ArrayList;

/**
 *
 * @author milic
 */
public class cm200003_TransactionOperations implements TransactionOperations {

    private Connection connection = DB.getInstance().getConnection();

    @Override
    public BigDecimal getBuyerTransactionsAmmount(int i) {

        BigDecimal id = BigDecimal.valueOf(-1);

        try ( PreparedStatement ps = connection.prepareStatement("select coalesce(SUM(amount),0)\n"
                + "from TransactionDone join TransactionDoneBuyer on TransactionDone.transactionId = TransactionDoneBuyer.transactionId\n"
                + "where TransactionDoneBuyer.buyerId = ?")) {
            ps.setInt(1, i);

            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    id = rs.getBigDecimal(1);
                }
            } catch (SQLException ex) {
                Logger.getLogger(cm200003_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
                id = BigDecimal.valueOf(-1);
            }

        } catch (SQLException ex) {
            Logger.getLogger(cm200003_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
            id = BigDecimal.valueOf(-1);
        }

        id = id.setScale(3);
        return id;

    }

    @Override
    public BigDecimal getShopTransactionsAmmount(int i) {

        BigDecimal id = BigDecimal.valueOf(-1);

        try ( PreparedStatement ps = connection.prepareStatement("select coalesce(SUM(amount),0)\n"
                + "from TransactionDone join TransactionDoneShop on TransactionDone.transactionId = TransactionDoneShop.transactionId\n"
                + "where TransactionDoneShop.shopId = ?")) {
            ps.setInt(1, i);

            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    id = rs.getBigDecimal(1);
                }
            } catch (SQLException ex) {
                Logger.getLogger(cm200003_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
                id = BigDecimal.valueOf(-1);
            }

        } catch (SQLException ex) {
            Logger.getLogger(cm200003_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
            id = BigDecimal.valueOf(-1);
        }

        id = id.setScale(3);
        return id;

    }

    @Override
    public List<Integer> getTransationsForBuyer(int i) {

        List<Integer> transactions = new ArrayList<>();

        try ( PreparedStatement ps = connection.prepareStatement("select TransactionDone.transactionId\n"
                + "from TransactionDone join TransactionDoneBuyer on TransactionDone.transactionId = TransactionDoneBuyer.transactionId\n"
                + "where TransactionDoneBuyer.buyerId = ?")) {
            ps.setInt(1, i);

            try ( ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transactions.add(rs.getInt(1));
                }
            } catch (SQLException ex) {
                Logger.getLogger(cm200003_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
                transactions = null;
            }

        } catch (SQLException ex) {
            Logger.getLogger(cm200003_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
            transactions = null;
        }

        if(transactions.size() == 0)
            return null;
        return transactions;

    }

    @Override
    public int getTransactionForBuyersOrder(int i) {

        int id = -1;

        try ( PreparedStatement ps = connection.prepareStatement("select TransactionDone.transactionId\n"
                + "from TransactionDone join TransactionDoneBuyer on TransactionDone.transactionId = TransactionDoneBuyer.transactionId\n"
                + "where TransactionDone.orderId = ?")) {
            ps.setInt(1, i);

            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    id = rs.getInt(1);
                }
            } catch (SQLException ex) {
                Logger.getLogger(cm200003_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (SQLException ex) {
            Logger.getLogger(cm200003_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return id;

    }

    @Override
    public int getTransactionForShopAndOrder(int i, int i1) {

        int id = -1;

        try ( PreparedStatement ps = connection.prepareStatement("select TransactionDone.transactionId\n"
                + "from TransactionDone join TransactionDoneShop on TransactionDone.transactionId = TransactionDoneShop.transactionId\n"
                + "where TransactionDone.orderId = ? and TransactionDoneShop.shopId = ?")) {
            ps.setInt(1, i);
            ps.setInt(2, i1);

            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    id = rs.getInt(1);
                }
            } catch (SQLException ex) {
                Logger.getLogger(cm200003_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (SQLException ex) {
            Logger.getLogger(cm200003_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return id;

    }

    @Override
    public List<Integer> getTransationsForShop(int i) {

        List<Integer> transactions = new ArrayList<>();

        try ( PreparedStatement ps = connection.prepareStatement("select TransactionDone.transactionId\n"
                + "from TransactionDone join TransactionDoneShop on TransactionDone.transactionId = TransactionDoneShop.transactionId\n"
                + "where TransactionDoneShop.shopId = ?")) {
            ps.setInt(1, i);

            try ( ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transactions.add(rs.getInt(1));
                }
            } catch (SQLException ex) {
                Logger.getLogger(cm200003_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
                transactions = null;
            }

        } catch (SQLException ex) {
            Logger.getLogger(cm200003_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
            transactions = null;
        }
    
        if(transactions.size() == 0)
            return null;
        return transactions;

    }

    @Override
    public Calendar getTimeOfExecution(int i) {
        Calendar time = Calendar.getInstance();

        try ( PreparedStatement ps = connection.prepareStatement("select dateDone\n"
                + "from TransactionDone\n"
                + "where transactionId = ?")) {
            ps.setInt(1, i);

            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    time.setTime(rs.getDate(1));
                }else{
                    time = null;
                }
            } catch (SQLException ex) {
                Logger.getLogger(cm200003_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
                time = null;
            }

        } catch (SQLException ex) {
            Logger.getLogger(cm200003_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
            time = null;
        }
        return time;
    }

    @Override
    public BigDecimal getAmmountThatBuyerPayedForOrder(int i) {

        // mozda treba 0
        BigDecimal id = BigDecimal.valueOf(-1);

        try ( PreparedStatement ps = connection.prepareStatement("select amount\n"
                + "from TransactionDone join TransactionDoneBuyer on TransactionDone.transactionId = TransactionDoneBuyer.transactionId\n"
                + "where TransactionDone.orderId = ?")) {
            ps.setInt(1, i);

            try ( ResultSet rs = ps.executeQuery()) {
                id = rs.getBigDecimal(1);
            } catch (SQLException ex) {
                Logger.getLogger(cm200003_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (SQLException ex) {
            Logger.getLogger(cm200003_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return id;

    }

    @Override
    public BigDecimal getAmmountThatShopRecievedForOrder(int i, int i1) {

        // mozda treba 0
        BigDecimal id = BigDecimal.valueOf(-1);

        try ( PreparedStatement ps = connection.prepareStatement("select amount\n"
                + "from TransactionDone join TransactionDoneShop on TransactionDone.transactionId = TransactionDoneShop.transactionId\n"
                + "where TransactionDone.orderId = ? and TransactionDoneShop.shopId = ?")) {

            ps.setInt(1, i1);
            ps.setInt(2, 1);

            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    id = rs.getBigDecimal(1);
                }
            } catch (SQLException ex) {
                Logger.getLogger(cm200003_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (SQLException ex) {
            Logger.getLogger(cm200003_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return id;

    }

    @Override
    public BigDecimal getTransactionAmount(int i) {

        BigDecimal id = BigDecimal.valueOf(-1);

        try ( PreparedStatement ps = connection.prepareStatement("select amount\n"
                + "from TransactionDone\n"
                + "where transactionId = ?")) {
            ps.setInt(1, i);

            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    id = rs.getBigDecimal(1);
                }
            } catch (SQLException ex) {
                Logger.getLogger(cm200003_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SQLException ex) {
            Logger.getLogger(cm200003_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return id;
    }

    @Override
    public BigDecimal getSystemProfit() {
        
        BigDecimal id = BigDecimal.ZERO;
        
        try(PreparedStatement ps = connection.prepareStatement("select (SUM(ArticlesInOrder.COUNT * Article.articlePrice) - (SUM(ArticlesInOrder.COUNT * Article.articlePrice)*Shop.discount / 100)) * (\n" +
"		select case \n" +
"		when coalesce(SUM(amount),0) > 10000 then 3\n" +
"		else 5\n" +
"		end\n" +
"		from TransactionDone join TransactionDoneBuyer on TransactionDone.transactionId = TransactionDoneBuyer.transactionId\n" +
"		where DATEDIFF(DAY, dateDone, receivedTime) between 0 and 30\n" +
"		and TransactionDoneBuyer.buyerId = OrderDone.buyerId\n" +
"	) / 100\n" +
"	from OrderDone join ArticlesInOrder on OrderDone.orderId = ArticlesInOrder.orderId\n" +
"	join Article on ArticlesInOrder.articleId = Article.articleId\n" +
"	join Sells on Article.articleId = Sells.articleId\n" +
"	join Shop on Shop.shopId = Sells.shopId\n" +
"	where OrderDone.receivedTime is not null and OrderDone.state='arrived'\n" +
"	group by Shop.discount, Shop.shopId, OrderDone.receivedTime, OrderDone.buyerId")){
            
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    id = id.add(rs.getBigDecimal(1));
                }
            }catch (SQLException ex) {
            Logger.getLogger(cm200003_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
            
        } catch (SQLException ex) {
            Logger.getLogger(cm200003_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        id = id.setScale(3);
        
        return id;
    }

}
