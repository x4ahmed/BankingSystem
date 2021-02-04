package sample;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.*;

public class ConnectionHandler implements Runnable {

    private Socket Socket;
    private Connection connection;

    public ConnectionHandler(Socket socket) {this.Socket = socket;}


    @Override
    public void run() {
        try(
            var os = Socket.getOutputStream();
            var is = Socket.getInputStream();
            var oos = new ObjectOutputStream(os);
            var ois = new ObjectInputStream(is)){


            while(true) {
                var Action = (ProtocolActions) ois.readObject();


                if (Action.equals(ProtocolActions.LogInRequest)) {

                    System.out.println("LogIn Request Received");
                    oos.writeObject(ProtocolActions.LogInResponse);
                    oos.writeObject(true);
                    System.out.println("LogIn Response Sent");
                    oos.flush();
                    break;
                }
                else if(Action.equals(ProtocolActions.BalanceRequest)){
                    System.out.println("Balance Request Received");
                    oos.writeObject(ProtocolActions.BalanceResponse);
                    System.out.println("Balance Response Sent");
                    try {

                        String id = (String) ois.readObject();
                        this.StartDataBaseConnection();
                        Statement statement = connection.createStatement();
                        ResultSet rs = statement.executeQuery("select * from BALANCE");
                        while (rs.next()){

                            if(rs.getString("CUSTOMERID").equals(id)) {
                                var money = rs.getDouble("BALANCE");
                                System.out.println(money);
                                oos.writeObject(money);
                                break;
                            }

                        }


                        break;


                    }catch (Exception exception){
                        exception.printStackTrace();
                        System.err.println(exception.getMessage());
                    }finally {
                        this.EndDataBaseConnection();
                    }

                }else if(Action.equals(ProtocolActions.LogOutRequest)){

                    System.out.println("LogOut Request Received");
                    oos.writeObject(ProtocolActions.LogOutResponse);
                    oos.writeObject(false);
                    System.out.println("LogOut Response Sent");
                    oos.flush();
                    break;

                }else if(Action.equals(ProtocolActions.SendMoneyRequest)){

                    System.out.println("Send Money Request Received");
                    oos.writeObject(ProtocolActions.SendMoneyResponse);
                    System.out.println("Send Money Response Sent");
                    try {

                        this.StartDataBaseConnection();
                        Statement statement = connection.createStatement();
                        connection.setAutoCommit(false);

                        double amount = (double)ois.readObject();
                        System.out.println(amount);
                        String sender = (String)ois.readObject();
                        String receiver = (String)ois.readObject();
                        double senderAmount = this.GetBalance(sender);
                        double receiverAmount = this.GetBalance(receiver);

                        if(amount > 0){

                            senderAmount = senderAmount - amount;
                            this.setBalance(sender,senderAmount);
                            receiverAmount = receiverAmount + amount;
                            this.setBalance(receiver,receiverAmount);
                            oos.writeObject(true);

                        }else {
                            oos.writeObject(false);
                        }

                        connection.setAutoCommit(true);



                    }catch (Exception exception){
                        exception.printStackTrace();
                        System.err.println(exception.getMessage());
                    }finally {
                        this.EndDataBaseConnection();
                    }



                }

            }





        }catch (Exception exception){

        }
    }

    private void StartDataBaseConnection() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:BankingSystem.db");
    }

    private void EndDataBaseConnection(){

        if(connection != null){
            try {
                connection.close();
            }catch (SQLException E){ E.printStackTrace(); }
        }
    }

    private double GetBalance(String ID) throws SQLException{

        var statement = connection.createStatement();
        var sql =new StringBuilder();

        sql.append("SELECT (BALANCE) FROM BALANCE WHERE CUSTOMERID='");
        sql.append(ID);
        sql.append("'");
        var result = statement.executeQuery(sql.toString());

        if(result.next()){
            return result.getDouble("BALANCE");
        }

        throw new RuntimeException("Invalid Customer ID");

    }

    private void setBalance(String ID, double amount) throws SQLException{

        var preparedstatement = connection.prepareStatement("UPDATE BALANCE SET BALANCE=? WHERE CUSTOMERID=?");
        preparedstatement.setDouble(1,amount);
        preparedstatement.setString(2,ID);
        preparedstatement.executeUpdate();
    }


}
