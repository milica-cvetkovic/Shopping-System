package rs.etf.sab.student;

import java.math.BigDecimal;
import rs.etf.sab.operations.*;
import org.junit.Test;
import rs.etf.sab.student.*;
import rs.etf.sab.tests.TestHandler;
import rs.etf.sab.tests.TestRunner;

import java.util.Calendar;

public class StudentMain {

    public static void main(String[] args) {

        ArticleOperations articleOperations = new cm200003_ArticleOperations(); // Change this for your implementation (points will be negative if interfaces are not implemented).
        BuyerOperations buyerOperations = new cm200003_BuyerOperations();
        CityOperations cityOperations = new cm200003_CityOperations();
        GeneralOperations generalOperations = new cm200003d_GeneralOperations();
        OrderOperations orderOperations = new cm200003_OrderOperations();
        ShopOperations shopOperations = new cm200003_ShopOperations();
        TransactionOperations transactionOperations = new cm200003_TransactionOperations();

        
        TestHandler.createInstance(
                articleOperations,
                buyerOperations,
                cityOperations,
                generalOperations,
                orderOperations,
                shopOperations,
                transactionOperations
        );

        TestRunner.runTests();

        
    }
}
