package sample;

import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

public class View extends VBox {

    private Label CustomerID;
    private TextField textField1;
    private Label Password;
    private TextField textField2;
    private Button LogIn;
    private Button LogOut;
    private Slider Balance = new Slider();
    private Label Receiver = new Label("Receiver");
    private TextField textField3;
    private Label Message = new Label();
    private HBox hb3 = new HBox();
    private Button SendMoney = new Button("Send Money");




    public View(){

       //Label1
        CustomerID = new Label("Customer ID");
        CustomerID.setPrefWidth(100);
        textField1 = new TextField();
        textField1.setMinWidth(450);
        var hb1 = new HBox();
        hb1.getChildren().add(CustomerID);
        hb1.getChildren().add(textField1);
        hb1.setSpacing(50);



        // Label2
        Password = new Label("Password");
        Password.setPrefWidth(100);
        textField2 = new TextField();
        textField2.setMinWidth(450);
        var hb2 = new HBox();
        hb2.getChildren().add(Password);
        hb2.getChildren().add(textField2);
        hb2.setSpacing(50);

        LogIn = new Button("Log In");
        LogIn.setMaxWidth(600);

        LogOut = new Button("Log Out");
        LogOut.setMaxWidth(600);

        this.setSpacing(5);
        Message.setLayoutX(200);
        this.getChildren().addAll(hb1,hb2,LogIn,LogOut,Message);

    }

    public void EventHandler(StreamServerAdder server){

        LogIn.setOnAction(e->{

            if(server.getClient().isLogin()) {

                Message.setText("You Must Log Out First");
            }
            else {
                String ID = textField1.getText();
                String Password = textField2.getText();
                System.out.println("ID: " + ID + " " + "Password: " + Password);

                if (server.ExistsInDataBase(ID, Password)) {
                    if (server.getClient().isLogin()) {
                        System.out.println("Client Is Already Logged In");
                        Message.setText("Client Is Already Logged In");

                    } else {
                        if (server.getClient().RequestLogIn()) {
                            System.out.println("LogIn Protocol Is Finished Successfully");
                            System.out.println("The Current Balance is: " + server.getClient().RequestGetBalance(server.getClient().getID()));
                            System.out.println("Balance Protocol Is Finished Successfully");
                            Message.setText("Logged In Successfully");

                            double Money = server.getClient().RequestGetBalance(server.getClient().getID());
                            Balance.setMin(0);
                            Balance.setMax(Money);
                            Balance.setMaxWidth(580);
                            Balance.setShowTickMarks(true);
                            Balance.setShowTickLabels(true);
                            Balance.setMajorTickUnit(1000);
                            Balance.setBlockIncrement(250);

                            Receiver.setPrefWidth(100);
                            textField3 = new TextField();
                            textField3.setMinWidth(450);
                            hb3.getChildren().add(Receiver);
                            hb3.getChildren().add(textField3);
                            hb3.setSpacing(50);

                            SendMoney.setMaxWidth(600);

                            this.getChildren().add(Balance);
                            this.getChildren().add(hb3);
                            this.getChildren().add(SendMoney);

                            SendMoney.setOnAction(b->{
                                double amount = Balance.getValue();
                                String Sender = server.getClient().getID();
                                System.out.println(Sender);
                                String Receiver = textField3.getText();

                                if(server.ExistsInDataBase(Receiver) && (!Receiver.equals(server.getClient().getID()))){

                                 if(server.getClient().SendMoneyRequest(amount,Sender,Receiver)){

                                    System.out.println("Sending Money Protocol Is Done Successfully");

                                    this.getChildren().remove(Balance);
                                    double newAmount = server.getClient().RequestGetBalance(server.getClient().getID());
                                    Balance.setMin(0);
                                    Balance.setMax(newAmount);
                                    Balance.setMaxWidth(580);
                                    Balance.setShowTickMarks(true);
                                    Balance.setShowTickLabels(true);
                                    Balance.setMajorTickUnit(1000);
                                    Balance.setBlockIncrement(250);
                                    this.getChildren().add(5,Balance);
                                    Message.setText("Transfer Completed Successfully");

                                 }else {Message.setText("You Don't Have Enough Money");}


                                }else if(!server.ExistsInDataBase(Receiver)){Message.setText("The Receiver Doesn't Exist In The Data Base");}

                                else if(server.getClient().getID().equals(Receiver)) {Message.setText("You Can't Transfer Money To Yourself");}

                                else {Message.setText("Error in TCP Protocol Occurred");}

                            });

                        } else {
                            System.out.println("Error in TCP Protocol Occurred");
                            Message.setText("Error in TCP Protocol Occurred");
                        }
                    }
                } else {

                    if(ID.isEmpty() || Password.isEmpty()){Message.setText("One or more of the fields are empty");}
                    else {
                    System.out.println("Client Does Not Exist In Data Base");
                    Message.setText("Client Does Not Exist In Data Base");
                        }
                }

            }


        });

        LogOut.setOnAction(e->{

            if(server.getClient().isLogin()){

                if(!server.getClient().RequestLogOut()){

                    Message.setText("Logged Out Successfully");
                    textField1.clear();
                    textField2.clear();
                    textField3.clear();
                    this.getChildren().remove(Balance);
                    this.getChildren().remove(hb3);
                    this.getChildren().remove(SendMoney);
                    hb3.getChildren().remove(Receiver);
                    hb3.getChildren().remove(textField3);

                    System.out.println("LogOut Protocol Is Finished Successfully");

                }else {
                    Message.setText("Error in TCP Protocol Occurred");
                }

            }else { Message.setText("You Must Log In First"); }


        });
    }
}
