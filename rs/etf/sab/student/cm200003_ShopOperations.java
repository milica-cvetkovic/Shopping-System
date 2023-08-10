/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.etf.sab.student;

import java.util.List;
import rs.etf.sab.operations.ShopOperations;
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
public class cm200003_ShopOperations implements ShopOperations {

    private Connection connection = DB.getInstance().getConnection();

    @Override
    public int createShop(String string, String string1) {

        int id = -1;

        try ( PreparedStatement psName = connection.prepareStatement("select name from Shop where name = ?")) {
            psName.setString(1, string);
            try ( ResultSet rsName = psName.executeQuery()) {
                if (rsName.next()) {
                    if (rsName.getString(1).equals(string)) {
                        return -1;
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(cm200003_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SQLException ex) {
            Logger.getLogger(cm200003_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        try ( PreparedStatement ps = connection.prepareStatement("select top 1 cityId\n"
                + "from City\n"
                + "where cityName = ?")) {

            ps.setString(1, string1);

            try ( ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    int cityId = rs.getInt(1);
                    try ( PreparedStatement psInsert = connection.prepareStatement("insert into Shop (name, cityId, discount) values(?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
                        psInsert.setString(1, string);
                        psInsert.setInt(2, cityId);
                        psInsert.setInt(3, 0);
                        psInsert.executeUpdate();
                        try ( ResultSet rsInsert = psInsert.getGeneratedKeys()) {
                            if (rsInsert.next()) {
                                id = rsInsert.getInt(1);
                            }
                        } catch (SQLException ex) {
                            Logger.getLogger(cm200003_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    } catch (SQLException ex) {
                        Logger.getLogger(cm200003_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            } catch (SQLException ex) {
                Logger.getLogger(cm200003_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (SQLException ex) {
            Logger.getLogger(cm200003_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return id;
    }

    @Override
    public int setCity(int i, String string) {

        int id = 1;
        try ( PreparedStatement ps = connection.prepareStatement("select top 1 cityId\n"
                + "from City\n"
                + "where cityName = ?")) {

            ps.setString(1, string);

            try ( ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    int cityId = rs.getInt(1);
                    try ( PreparedStatement psUpdate = connection.prepareStatement("update Shop set cityId = ? where shopId = ?")) {
                        psUpdate.setInt(1, cityId);
                        psUpdate.setInt(2, i);
                        
                        psUpdate.executeUpdate();
                        id = 1;
                    } catch (SQLException ex) {
                        id = -1;
                        Logger.getLogger(cm200003_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }

            } catch (SQLException ex) {
                id = -1;
                Logger.getLogger(cm200003_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (SQLException ex) {
            id = -1;
            Logger.getLogger(cm200003_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return id;
    }

    @Override
    public int getCity(int i) {
        int id = -1;
        try ( PreparedStatement ps = connection.prepareStatement("select cityId\n"
                + "from Shop\n"
                + "where shopId = ?")) {
            ps.setInt(1, i);

            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    id = rs.getInt(1);
                }
            } catch (SQLException ex) {
                Logger.getLogger(cm200003_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (SQLException ex) {
            Logger.getLogger(cm200003_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return id;
    }

    @Override
    public int setDiscount(int i, int i1) {

        int id = -1;

        try ( PreparedStatement ps = connection.prepareStatement("update Shop set discount = ? where shopId = ?")) {
            ps.setInt(1, i1);
            ps.setInt(2, i);
            ps.executeUpdate();
            id = 1;
        } catch (SQLException ex) {
            id = -1;
            Logger.getLogger(cm200003_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return id;
    }

    @Override
    public int increaseArticleCount(int i, int i1) {
        
        int count = -1;

        try ( PreparedStatement ps = connection.prepareStatement("update Sells set articleCount = articleCount + ? where articleId = ?")) {
            ps.setInt(1, i1);
            ps.setInt(2, i);
            ps.executeUpdate();
            count = getArticleCount(i);
        } catch (SQLException ex) {
            count = -1;
            Logger.getLogger(cm200003_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return count;

    }

    @Override
    public int getArticleCount(int i) {

        int id = 0;

        try ( PreparedStatement ps = connection.prepareStatement("select articleCount\n"
                + "from Sells\n"
                + "where articleId = ?")) {
            ps.setInt(1, i);
            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    id = rs.getInt(1);
                }
            } catch (SQLException ex) {
                Logger.getLogger(cm200003_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SQLException ex) {
            Logger.getLogger(cm200003_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return id;
    }

    @Override
    public List<Integer> getArticles(int i) {

        List<Integer> articles = new ArrayList<>();

        try ( PreparedStatement ps = connection.prepareStatement("select articleId\n"
                + "from Sells\n"
                + "where shopId = ?")) {
            ps.setInt(1, i);

            try ( ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    articles.add(rs.getInt(1));
                }
            } catch (SQLException ex) {
                Logger.getLogger(cm200003_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (SQLException ex) {
            Logger.getLogger(cm200003_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return articles;
    }

    @Override
    public int getDiscount(int i) {
        int id = -1;

        try ( PreparedStatement ps = connection.prepareStatement("select discount \n"
                + "from Shop\n"
                + "where shopId = ?")) {
            ps.setInt(1, i);

            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    id = rs.getInt(1);
                }
            } catch (SQLException ex) {
                Logger.getLogger(cm200003_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (SQLException ex) {
            Logger.getLogger(cm200003_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return id;
    }

}
