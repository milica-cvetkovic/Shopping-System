/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.etf.sab.student;

import java.util.List;
import rs.etf.sab.operations.CityOperations;
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
public class cm200003_CityOperations implements CityOperations {

    private Connection connection = DB.getInstance().getConnection();

    @Override
    public int createCity(String string) {

        int id = -1;

        try ( PreparedStatement psCheck = connection.prepareStatement("select cityName from City where cityName = ?")) {
            psCheck.setString(1, string);
            try ( ResultSet rsCheck = psCheck.executeQuery()) {
                if (rsCheck.next()) {
                    if (rsCheck.getString(1).equals(string)) {
                        return -1;
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(cm200003_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (SQLException ex) {
            Logger.getLogger(cm200003_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        try ( PreparedStatement ps = connection.prepareStatement("insert into City(cityName) values (?)", PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, string);
            ps.executeUpdate();
            try ( ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    id = rs.getInt(1);
                }
            } catch (SQLException ex) {
                Logger.getLogger(cm200003_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (SQLException ex) {
            Logger.getLogger(cm200003_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return id;
    }

    @Override
    public List<Integer> getCities() {

        List<Integer> cities = new ArrayList<>();

        try ( PreparedStatement ps = connection.prepareStatement("select * from City")) {

            try ( ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cities.add(rs.getInt(1));
                }
            } catch (SQLException ex) {
                Logger.getLogger(cm200003_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }

        } catch (SQLException ex) {
            Logger.getLogger(cm200003_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

        return cities;
    }

    @Override
    public int connectCities(int i, int i1, int i2) {

        int id = -1;

        try ( PreparedStatement psCheck = connection.prepareStatement("select distance\n"
                + "from Line\n"
                + "where (cityId1 = ? and cityId2 = ?) or (cityId1 = ? and cityId2 = ?)")) {
            psCheck.setInt(1, i);
            psCheck.setInt(2, i1);
            psCheck.setInt(3, i1);
            psCheck.setInt(4, i);

            try ( ResultSet rsCheck = psCheck.executeQuery()) {
                if (rsCheck.next()) {
                    return -1;
                }
            } catch (SQLException ex) {
                Logger.getLogger(cm200003_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (SQLException ex) {
            Logger.getLogger(cm200003_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        try ( PreparedStatement ps = connection.prepareStatement("insert into Line(cityId1, cityId2, distance) values (?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, i);
            ps.setInt(2, i1);
            ps.setInt(3, i2);
            ps.executeUpdate();
            try ( ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    id = rs.getInt(1);
                }
            } catch (SQLException ex) {
                Logger.getLogger(cm200003_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (SQLException ex) {
            Logger.getLogger(cm200003_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return id;
    }

    @Override
    public List<Integer> getConnectedCities(int i) {

        List<Integer> connections = new ArrayList<>();

        try ( PreparedStatement ps = connection.prepareStatement("select cityId2\n"
                + "from Line\n"
                + "where cityId1 = ?")) {
            ps.setInt(1, i);

            try ( ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    connections.add(rs.getInt(1));
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(cm200003_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        try ( PreparedStatement ps = connection.prepareStatement("select cityId1\n"
                + "from Line\n"
                + "where cityId2 = ?")) {
            ps.setInt(1, i);

            try ( ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    connections.add(rs.getInt(1));
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(cm200003_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return connections;

    }

    @Override
    public List<Integer> getShops(int i) {

        List<Integer> shops = new ArrayList<>();

        try ( PreparedStatement ps = connection.prepareStatement("select shopId from Shop where cityId = ?")) {
            ps.setInt(1, i);
            try ( ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    shops.add(rs.getInt(1));
                }
            } catch (SQLException ex) {
                Logger.getLogger(cm200003_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        } catch (SQLException ex) {
            Logger.getLogger(cm200003_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

        return shops;

    }

}
