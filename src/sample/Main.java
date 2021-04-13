package sample;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Main extends Application {



   @Override
    public void start(Stage primaryStage) throws Exception {

       primaryStage.setTitle("Banking Client");

       // Resesting The Data Base for Debugging Reasons
       try {

           this.ResetDataBase();
       }catch (Exception exception){
           exception.printStackTrace();
           System.err.println(exception.getMessage());
       }

       // Initializing two clients in the data base
       var client1 = new StreamClientAdder("3101361","1234",10000);
       var client2 = new StreamClientAdder("3101343","5678",7000);

       // Creating a new GUI View
       var View = new View();

       // Getting an Instance from the server
       StreamServerAdder server = StreamServerAdder.getInstance();

       server.run(View,client1);

        primaryStage.setScene(new Scene(View,600,300));
        primaryStage.show();

   }

    public static void main(String[] args) {
        launch(args);
    }

    public void ResetDataBase()throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:BankingSystem.db");
        Statement statement = connection.createStatement();
        statement.setQueryTimeout(30);
        statement.executeUpdate("DELETE FROM LOGIN");
        statement.executeUpdate("DELETE FROM sqlite_sequence WHERE NAME = 'LOGIN' ");
        statement.executeUpdate("DELETE FROM BALANCE");
        statement.executeUpdate("DELETE FROM sqlite_sequence WHERE NAME = 'BALANCE' ");
        if(connection != null){
            try {
                connection.close();
            }catch (SQLException E){ E.printStackTrace(); }
        }
    }




}
