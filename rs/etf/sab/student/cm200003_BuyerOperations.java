/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.etf.sab.student;

import java.math.BigDecimal;
import java.util.List;
import rs.etf.sab.operations.BuyerOperations;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author milic
 */
public class cm200003_BuyerOperations implements BuyerOperations {

    private Connection connection = DB.getInstance().getConnection();

    @Override
    public int createBuyer(String string, int i) {

        int id = -1;

        try ( PreparedStatement ps = connection.prepareStatement("insert into Buyer (name, cityId, credit) values (?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, string);
            ps.setInt(2, i);
            ps.setDouble(3, 0);

            ps.executeUpdate();
            try ( ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    id = rs.getInt(1);
                }
            } catch (SQLException ex) {
                Logger.getLogger(cm200003_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (SQLException ex) {
            Logger.getLogger(cm200003_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return id;
    }

    @Override
    public int setCity(int i, int i1) {

        int id = -1;

        try ( PreparedStatement ps = connection.prepareStatement("update Buyer set cityId = ? where buyerId = ?")) {
            ps.setInt(1, i1);
            ps.setInt(2, i);
            ps.executeUpdate();
            id = 1;
        } catch (SQLException ex) {
            Logger.getLogger(cm200003_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return id;
    }

    @Override
    public int getCity(int i) {

        int id = -1;

        try ( PreparedStatement ps = connection.prepareStatement("select cityId\n"
                + "from Buyer\n"
                + "where buyerId = ?")) {
            ps.setInt(1, i);

            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    id = rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(cm200003_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return id;

    }

    @Override
    public BigDecimal increaseCredit(int i, BigDecimal bd) {

        BigDecimal count = BigDecimal.ZERO;

        try ( PreparedStatement ps = connection.prepareStatement("update Buyer set credit = credit + ? where buyerId = ?")) {
            ps.setBigDecimal(1, bd);
            ps.setInt(2, i);
            ps.executeUpdate();
            count = getCredit(i);
        } catch (SQLException ex) {
            Logger.getLogger(cm200003_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return count;
    }

    @Override
    public int createOrder(int i) {

        int id = -1;

        try ( PreparedStatement ps = connection.prepareStatement("insert into OrderDone (state, buyerId) values (?,?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, "created");
            ps.setInt(2, i);
            ps.executeUpdate();

            try ( ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    id = rs.getInt(1);
                }
            } catch (SQLException ex) {
                Logger.getLogger(cm200003_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (SQLException ex) {
            Logger.getLogger(cm200003_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return id;
    }

    @Override
    public List<Integer> getOrders(int i) {

        List<Integer> orders = new ArrayList<>();

        try ( PreparedStatement ps = connection.prepareStatement("select orderId\n"
                + "from OrderDone\n"
                + "where buyerId = ?")) {
            ps.setInt(1, i);
            try ( ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(rs.getInt(1));
                }
            } catch (SQLException ex) {
                Logger.getLogger(cm200003_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SQLException ex) {
            Logger.getLogger(cm200003_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return orders;
    }

    @Override
    public BigDecimal getCredit(int i) {

        BigDecimal count = BigDecimal.ZERO;

        try ( PreparedStatement ps = connection.prepareStatement("select credit\n"
                + "from Buyer\n"
                + "where buyerId = ?")) {
            ps.setInt(1, i);
            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    count = rs.getBigDecimal(1);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(cm200003_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return count;
    }

}
