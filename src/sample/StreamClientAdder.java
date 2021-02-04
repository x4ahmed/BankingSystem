package sample;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class StreamClientAdder {

    private String ID;
    private String Password;
    private double Balance;
    private boolean Login;
    private Connection connection = null;
    private static List<StreamClientAdder> clients = new ArrayList<>();

    public StreamClientAdder(String id, String password, double balance){

        try {
            this.StartDataBaseConnection();
            this.InsertDataInLogInTable(id,password);
            this.InsertDataInBalanceTable(id,balance);
        }catch (Exception exception){
            exception.printStackTrace();
            System.err.println(exception.getMessage());
        } finally {
            this.EndDataBaseConnection();
        }

        this.Login = false;
        this.ID = id;
        this.Balance = balance;
        this.Password = password;
        clients.add(this);


    }


    public void StartDataBaseConnection() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:BankingSystem.db");
    }

    public void EndDataBaseConnection(){

        if(connection != null){
            try {
                connection.close();
            }catch (SQLException E){ E.printStackTrace(); }
        }
    }

    public void InsertDataInLogInTable(String id, String password) throws SQLException{

        Statement statement = connection.createStatement();
        statement.setQueryTimeout(30);
        var preparedStatement =connection.prepareStatement("INSERT INTO LOGIN (CUSTOMERID, PASSWORD) VALUES (?, ?)");
        preparedStatement.setString(1,id);
        preparedStatement.setString(2,password);
        preparedStatement.executeUpdate();
    }

    public void InsertDataInBalanceTable(String id, double balance) throws SQLException{

        Statement statement = connection.createStatement();
        statement.setQueryTimeout(30);
        var preparedStatement =connection.prepareStatement("INSERT INTO BALANCE (CUSTOMERID, BALANCE) VALUES (?, ?)");
        preparedStatement.setString(1,id);
        preparedStatement.setDouble(2,balance);
        preparedStatement.executeUpdate();
    }

    public boolean RequestLogIn(){

        try(var socket = new Socket(InetAddress.getLocalHost(),1111);
            var os = socket.getOutputStream();
            var is = socket.getInputStream();
            var oos = new ObjectOutputStream(os);
            var ois = new ObjectInputStream(is)
        ){

            // Send LogIn Request
            oos.writeObject(ProtocolActions.LogInRequest);
            oos.writeBoolean(this.Login);
            System.out.println("LogIn Request Sent");

            // Receive LogIn Response
            var Action = (ProtocolActions)ois.readObject();
            if(Action.equals(ProtocolActions.LogInResponse)) {

                System.out.println("LogIn Response Received");
                this.Login = (boolean) ois.readObject();
            }


        }catch (Exception exception){
            return this.Login;
        }

        return this.Login;
    }

    public boolean isLogin() {
        return Login;
    }

    public Connection getConnection() {
        return connection;
    }

    public String getID() {
        return ID;
    }

    public String getPassword() {
        return Password;
    }

    public static List<StreamClientAdder> getClients() {
        return clients;
    }

    public double RequestGetBalance(String id){

        double response = 0;

        try(var socket = new Socket(InetAddress.getLocalHost(),1111);
            var os = socket.getOutputStream();
            var is = socket.getInputStream();
            var oos = new ObjectOutputStream(os);
            var ois = new ObjectInputStream(is)
        ){

            // Send Balance Request
            oos.writeObject(ProtocolActions.BalanceRequest);
            oos.writeObject(id);
            System.out.println("Balance Request Sent");

            // Receive Balance Response
            var Action = (ProtocolActions)ois.readObject();
            if(Action.equals(ProtocolActions.BalanceResponse)) {
                System.out.println("Balance Response Received");
                response = (double)ois.readObject();
            }


        }catch (Exception exception){
            return response;
        }


        this.Balance = response;
        return response ;
    }

    public boolean RequestLogOut(){

        try(var socket = new Socket(InetAddress.getLocalHost(),1111);
            var os = socket.getOutputStream();
            var is = socket.getInputStream();
            var oos = new ObjectOutputStream(os);
            var ois = new ObjectInputStream(is)
        ){

            // Send LogOut Request
            oos.writeObject(ProtocolActions.LogOutRequest);
            oos.writeBoolean(this.Login);
            System.out.println("LogOut Request Sent");

            // Receive LogOut Response
            var Action = (ProtocolActions)ois.readObject();
            if(Action.equals(ProtocolActions.LogOutResponse)) {

                System.out.println("LogOut Response Received");
                this.Login = (boolean) ois.readObject();
            }


        }catch (Exception exception){
            return this.Login;
        }

        return this.Login;

    }

    public boolean SendMoneyRequest(double amount, String sender, String receiver){
        boolean flag = false;

        try(var socket = new Socket(InetAddress.getLocalHost(),1111);
            var os = socket.getOutputStream();
            var is = socket.getInputStream();
            var oos = new ObjectOutputStream(os);
            var ois = new ObjectInputStream(is)
            ){

            // Sending Money Request
            oos.writeObject(ProtocolActions.SendMoneyRequest);
            oos.writeObject(amount);
            oos.writeObject(sender);
            oos.writeObject(receiver);
            System.out.println("Send Money Request Sent");

            // Receiving Money Request
            var Action = (ProtocolActions)ois.readObject();
            if(Action.equals(ProtocolActions.SendMoneyResponse)){

                System.out.println("Send Money Response Received");
                flag = (boolean)ois.readObject();
                System.out.println(flag);

            }



        }catch (Exception exception){
            return flag;
        }


        return flag;

    }
}
