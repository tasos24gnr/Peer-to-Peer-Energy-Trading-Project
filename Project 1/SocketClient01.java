
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLOutput;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.Scanner;

public class SocketClient01 {
    static private String ip;// = "192.168.1.0 " ;
    static public String username;// = "Energy-User-01 ";
    static private String password;// = "111111111111111";
    static private double kwhs;// = 1500;
    static public double life ;

    public static void main(String[] args) throws IOException {
        IntroduceMySelf();
        my_Listening_Role();
        Socket s = new Socket("169.254.245.250", 2789);
        if (!s.isConnected())
            System.out.println("Socket Connection not established");
        else {
            System.out.println("Socket Connection Established with " + s.getInetAddress());
        }
        Register(s);
        try {
            Circle_Of_Life(s);
            s.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    static private void Register(Socket s) throws IOException {
        DataOutputStream dout = new DataOutputStream(s.getOutputStream());
        int i = 0;
        while (i < 4) {
            dout.writeByte(i);
            if (i == 0) {                //Send ip
                dout.writeUTF(ip);
                dout.flush();
            } else if (i == 1) {         //Send username
                dout.writeUTF(username);
                dout.flush();
            } else if (i == 2) {         //Send password
                dout.writeUTF(password);
                dout.flush();
            } else if (i == 3) {         //Send kwhs
                dout.writeUTF(Double.toString(kwhs));
                dout.flush();
            }
            i++;
        }
    }

    static private boolean IntroduceMySelf() throws UnknownHostException {
        System.out.println("A  wild  Client appeared!!!");

        ip = InetAddress.getLocalHost().getHostAddress();
        System.out.println("\nYour Ip is: " + ip);

        System.out.println("\nEnter your username: ");
        Scanner scanner2 = new Scanner(System.in);
        username = scanner2.nextLine();
        System.out.println("Your username is -> " + username);

        System.out.println("\nEnter your password: ");
        Scanner scanner3 = new Scanner(System.in);
        password = scanner3.nextLine();
        System.out.println("Your password is -> " + password );

        System.out.println("\nEnter your Kwhrs: ");
        Scanner scanner4 = new Scanner(System.in);
        kwhs = Double.parseDouble(scanner4.nextLine());
        System.out.println("Available Kwhrs -> " + kwhs+ "\n");

        return true;
    }

    static public void UpdateMyInfo(Socket s, double updated_life, DataOutputStream dout,String user) throws IOException {
        System.out.println("[#code 10] Message send to Server : Updated Life.");           //Send updated life and username, to server
         dout = new DataOutputStream(s.getOutputStream());
        dout.writeByte(10);                                                              //[#code: 10] Request for update
        dout.writeUTF(user);
        dout.writeUTF(String.valueOf(updated_life));
        dout.flush();
    }

    static private void Circle_Of_Life(Socket s) throws InterruptedException, IOException,EOFException {

        System.out.println("I want to live for ever!");
        DataOutputStream dout=null;
        life = kwhs;

        while (life > 0) {
            System.out.println("Life: " + life);
            life = life - 1;
            TimeUnit.SECONDS.sleep(1);

            if((life >= (0.8*kwhs)) && (life <= (0.809*kwhs)) || (life >= (0.5*kwhs)) && (life <= (0.509*kwhs)) ||(life >= (0.1*kwhs)) && (life <= (0.109*kwhs))){ // Exasa to 20% tou arxikou apo8ematos || to 50% || to 90%
                double help=0.1*kwhs;                       // Zhtw trofodothsh kata 10% tou arxikou apo8ematos
                if((life >= (0.8*kwhs)) && (life <= (0.809*kwhs))){
                    System.out.println("[ system ] LIFE STATUS = 80%");
                    System.out.println("[#code 69] Message send to Server : In need of Energy.");
                }else if((life >= (0.5*kwhs)) && (life <= (0.509*kwhs))){
                    System.out.println("[ system ] LIFE STATUS = 50%");
                    System.out.println("[#code 69] Message send to Server : In need of Energy.");
                }else{
                    System.out.println("[ system ] LIFE STATUS = 10%");
                    System.out.println("[#code 69] Message send to Server : In need of Energy.");
                }
                dout = new DataOutputStream(s.getOutputStream());
                dout.writeByte(69);                       //[#code: 69] gia aithma promi8ias
                dout.writeUTF(username);
                dout.writeUTF(String.valueOf(help));
                dout.flush();

                InputStreamReader streamReader =new InputStreamReader(s.getInputStream());
                BufferedReader reader = new BufferedReader(streamReader);
                String responseMessage  = reader.readLine();
                //reader.close();

                if(responseMessage.equals("fail")){
                    System.out.println("[#code 69] Message received from Server : " +responseMessage+ "to find a Single Supplier.");

                    System.out.println("[#code 69] Message send to Server : Plan A");
                    System.out.println("         |_[#code 69][Plan A] Description: Request a Pair of Suppliers.");
                    dout.writeUTF("A");
                    dout.flush();
                    streamReader =new InputStreamReader(s.getInputStream());
                    BufferedReader reader2 = new BufferedReader(streamReader);
                    String available_supplier_1  = reader2.readLine();
                    String available_supplier_2  = reader2.readLine();
                    if(       !(available_supplier_1.equals("fail")) && !(available_supplier_2.equals("fail"))       ){                  //Plan A succeed, got 2 suppliers!
                        System.out.println("           [#code 69][Plan A] Message received from Server : Success.");
                        System.out.println("           [#code 69][Plan A] Supplier 1: "+available_supplier_1+" , Supplier 2: "+available_supplier_2);


                        //Ready to communicate with both of my Suppliers.
                        //..............................................
                        Socket peer_to_peer1= new Socket("169.254.245.250", port_query(available_supplier_1));
                        if (!peer_to_peer1.isConnected())
                            System.out.println("[#code 36] Socket Connection not established");
                        else {
                            double h_zwh_toy_allou=eisagoges_eksagoges(peer_to_peer1,help/2,available_supplier_1);
                            UpdateMyInfo(s,life,dout,username);
                            UpdateMyInfo(s,h_zwh_toy_allou,dout,available_supplier_1);
                        }
                        /////////////////////////////////////////////////////////////////////////////////////////
                        Socket peer_to_peer2= new Socket("169.254.245.250", port_query(available_supplier_2));
                        if (!peer_to_peer2.isConnected())
                            System.out.println("[#code 36] Socket Connection not established");
                        else {
                            double h_zwh_toy_allou=eisagoges_eksagoges(peer_to_peer2,help/2,available_supplier_2);
                            UpdateMyInfo(s,life,dout,username);
                            UpdateMyInfo(s,h_zwh_toy_allou,dout,available_supplier_2);
                        }


                    }else{          // GO for plan B
                        System.out.println("           [#code 69][Plan A] Message received from Server : Dismiss.");
                        System.out.println("           [#code 69] Message send to Server : Plan B");
                        System.out.println("                    |_[#code 69][Plan B] Description: Request for a spot in server's waiting list (for 5 seconds).");
                        dout.writeUTF("5");
                        dout.flush();

                        streamReader =new InputStreamReader(s.getInputStream());
                        reader2 = new BufferedReader(streamReader);
                        available_supplier_1  = reader2.readLine();
                        available_supplier_2  = reader2.readLine();
                        if(!(available_supplier_1.equals("fail") && available_supplier_2.equals("fail"))) {
                            System.out.println("                      [#code 69][Plan B] Message received from Server : Finally a Pair of Suppliers is available. Supplier 1: " + available_supplier_1 + " , Supplier 2: " + available_supplier_2);


                            //Ready to communicate with both of my Suppliers.
                            //..............................................
                            Socket peer_to_peer1= new Socket("169.254.245.250", port_query(available_supplier_1));
                            if (!peer_to_peer1.isConnected())
                                System.out.println("[#code 36] Socket Connection not established");
                            else {
                                double h_zwh_toy_allou=eisagoges_eksagoges(peer_to_peer1,help/2,available_supplier_1);
                                UpdateMyInfo(s,life,dout,username);
                                UpdateMyInfo(s,h_zwh_toy_allou,dout,available_supplier_1);
                            }
                            /////////////////////////////////////////////////////////////////////////////////////////
                            Socket peer_to_peer2= new Socket("169.254.245.250", port_query(available_supplier_2));
                            if (!peer_to_peer2.isConnected())
                                System.out.println("[#code 36] Socket Connection not established");
                            else {
                                double h_zwh_toy_allou=eisagoges_eksagoges(peer_to_peer2,help/2,available_supplier_2);
                                UpdateMyInfo(s,life,dout,username);
                                UpdateMyInfo(s,h_zwh_toy_allou,dout,available_supplier_2);
                            }





                        }else{
                            System.out.println("                      [#code 69][Plan B - FAILED] BAD NEWS received from Server  ...I am all alone now.");
                            System.out.println("[Crisis] Frozen for 10 seconds.");
                            TimeUnit.SECONDS.sleep(10);
                        }

                    }
                }
                else{
                    System.out.println("[#code 69] Message received from Server : " +responseMessage+  " will be my Single Supplier.");
                    //Ready to communicate with my Single Supplier.
                    //..............................................
                    System.out.println("[#code 36] Attempting to connect with Supplier "+responseMessage+" via port "+port_query(responseMessage));
                    Socket peer_to_peer = new Socket("169.254.245.250", port_query(responseMessage));
                    if (!peer_to_peer.isConnected())
                        System.out.println("[#code 36] Socket Connection not established");
                    else {
                        double h_zwh_toy_allou=eisagoges_eksagoges(peer_to_peer,help,responseMessage);
                        UpdateMyInfo(s,life,dout,username);
                        UpdateMyInfo(s,h_zwh_toy_allou,dout,responseMessage);
                    }

                }

            }
            if(life==1){
                dout = new DataOutputStream(s.getOutputStream());
                dout.writeByte(66);                                                              //[#code: 66] Death.
                dout.flush();
                s.close();
            }
        }

        System.out.println("\nKsoflisa...\nGame Over");
    }

    public static void my_Listening_Role(){
        Listener lucy= new Listener();
        Thread tr= new Thread(lucy);
        tr.start();
    }

    static int port_query(String user){
        int his_port=0;
        final Scanner scanner;
        try {
            scanner = new Scanner(new File("Listeners.txt"));
            while (scanner.hasNextLine()) {
                final String lineFromFile = scanner.nextLine();

                if(lineFromFile.contains(user)) {
                    // a match!
                    his_port=Integer.parseInt(scanner.nextLine());
                    System.out.println("I found " +user+ " in Listeners file with port = "+his_port);
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return his_port;
    }

    static double eisagoges_eksagoges(Socket sock,double dosolipsia,String supplier) throws IOException {
        System.out.println("[#code 36] Socket Connection Established with my Supplier: " + supplier);
        DataOutputStream dout_help = new DataOutputStream(sock.getOutputStream());
        dout_help.writeUTF(String.valueOf(dosolipsia));
        dout_help.flush();
        DataInputStream din_help = new DataInputStream(sock.getInputStream());
        double h_zwh_toy_allou= Double.parseDouble(din_help.readUTF());
        life=life + dosolipsia;
        System.out.println("[#code 36] Successfully received "+dosolipsia+" kwhrs from  my Single Supplier. New Life: " + life);
        return h_zwh_toy_allou;
    }
}

//A client is also a listener, because of his possible Supplier role.
class Listener implements Runnable{
    public void run() {
        Socket s = null;
        ServerSocket s2 = null;
        Random random = new Random();                                         // Choose a random port for listening
        int listenerport =random.nextInt((10000 - 5000) + 1) + 5000;  //   Between :   5000-10000

        try {
            Listener_Helper(listenerport);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Client "+ SocketClient01.username +" has ears too... (on port:"+listenerport +")");

        try {
            s2 = new ServerSocket(listenerport);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String client_help;
        while (true) {
            try {
                s = s2.accept();
                // CLIENT-CLIENT
                System.out.println("[#code36] Connection achieved with Client.  ");
                DataInputStream din_help = new DataInputStream(s.getInputStream());
                client_help= din_help.readUTF();
                System.out.println("[#code36] Client choose me as a Supplier and asked for "+client_help+" kwhrs.");
                SocketClient01.life= SocketClient01.life - Double.parseDouble(client_help);
                double remaining_life=SocketClient01.life;

                DataOutputStream dout_help = new DataOutputStream(s.getOutputStream());
                dout_help.writeUTF(String.valueOf(remaining_life));
                dout_help.flush();
                System.out.println("[#code36] "+ client_help+" kwhrs successfully sent to client. Remaining life: "+remaining_life);


            }catch(Exception e){
                try {
                    s2.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    //Update file
    private void Listener_Helper(int port) throws IOException {
        BufferedWriter out = null;
        try {
            FileWriter fstream = new FileWriter("Listeners.txt", true); //true tells to append data.
            out = new BufferedWriter(fstream);
            out.write(SocketClient01.username);     // Signature username and listening port
            out.newLine();
            out.write(Integer.toString(port));
            out.newLine();
        }
        catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
        finally {
            if(out != null) {
                out.close();
            }
        }
    }
}


