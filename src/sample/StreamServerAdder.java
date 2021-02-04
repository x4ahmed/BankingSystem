package sample;

import java.net.ServerSocket;
import java.sql.ResultSet;
import java.sql.Statement;

public final class StreamServerAdder {

    private static StreamServerAdder Server;
    private StreamClientAdder client;
    private View view;

    private StreamServerAdder(){}

    public static void main(String[] args) {
        try(var serverSocket = new ServerSocket(1111)){

            while (true) {
                var socket = serverSocket.accept();
                new Thread(new ConnectionHandler(socket)).start();
            }

        }catch (Exception exception){
            exception.printStackTrace();
        }
    }

    public static StreamServerAdder getInstance(){

        if(Server == null) {Server = new StreamServerAdder();}

        return Server;

    }

    public void run(View View, StreamClientAdder Client){

        this.client = Client;
        this.view = View;
        view.EventHandler(Server);


    }

    public StreamClientAdder getClient() {
        return client;
    }

    public boolean ExistsInDataBase(String ID , String Password){

        boolean flag = false;
        try {
            this.client.StartDataBaseConnection();

            Statement statement = this.client.getConnection().createStatement();
            ResultSet rs = statement.executeQuery("select * from LOGIN");
            while (rs.next()){
                if(rs.getString("CUSTOMERID").equals(ID) && rs.getString("PASSWORD").equals(Password)){
                    flag = true;
                    for(StreamClientAdder CLIENT : StreamClientAdder.getClients()){

                        if(CLIENT.getID().equals(ID) && CLIENT.getPassword().equals(Password)) {this.client = CLIENT; }
                    }
                }
            }


        }catch (Exception exception){
            exception.printStackTrace();
            System.err.println(exception.getMessage());
        } finally {
            this.client.EndDataBaseConnection();
        }




        return flag;
    }

    public boolean ExistsInDataBase(String ID){

        boolean flag = false;
        try {
            this.client.StartDataBaseConnection();

            Statement statement = this.client.getConnection().createStatement();
            ResultSet rs = statement.executeQuery("select * from BALANCE");
            while (rs.next()){
                if(rs.getString("CUSTOMERID").equals(ID)){
                    System.out.println("Found the ID");
                    flag = true;
                }
            }


        }catch (Exception exception){
            exception.printStackTrace();
            System.err.println(exception.getMessage());
        } finally {
            this.client.EndDataBaseConnection();
        }


        return flag;
    }
}
