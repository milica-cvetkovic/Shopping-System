/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.etf.sab.student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import rs.etf.sab.operations.ArticleOperations;

/**
 *
 * @author milic
 */
public class cm200003_ArticleOperations implements ArticleOperations {

    private Connection connection = DB.getInstance().getConnection();
    
    @Override
    public int createArticle(int i, String string, int i1) {
        
        int id = -1;
        
        try(PreparedStatement ps = connection.prepareStatement("insert into Article(articleName, articlePrice) values (?,?)",PreparedStatement.RETURN_GENERATED_KEYS)){
            ps.setString(1, string);
            ps.setDouble(2, i1);
            
            ps.executeUpdate();
            
            try(ResultSet rs = ps.getGeneratedKeys()){
                if(rs.next()){
                    
                    try(PreparedStatement psSells = connection.prepareStatement("insert into Sells(shopId, articleId, articleCount) values (?,?,?)",PreparedStatement.RETURN_GENERATED_KEYS)){
                       psSells.setInt(1, i);
                       psSells.setInt(2, rs.getInt(1));
                       psSells.setInt(3, 0);
                       
                       psSells.executeUpdate();
                       
                       id = rs.getInt(1);
                       
                    }catch (SQLException ex) {
                        Logger.getLogger(cm200003_ArticleOperations.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                }
            }catch (SQLException ex) {
                Logger.getLogger(cm200003_ArticleOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }catch (SQLException ex) {
            Logger.getLogger(cm200003_ArticleOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return id;
    }
}
