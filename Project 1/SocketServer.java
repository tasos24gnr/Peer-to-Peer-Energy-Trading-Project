
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

public class SocketServer {
    static Map<String, Double> myMap = new ConcurrentHashMap<String, Double>();
    static Map<String, Integer> WaitingList = new ConcurrentHashMap<String, Integer>();
    public static void main(String[] args) throws IOException {
        Socket s = null;
        System.out.println("Server is listening...");
        ServerSocket s2 = new ServerSocket(2789);

        while (true) {
           try {
               s = s2.accept();
               System.out.println("Connection achieved with client -> " + s);
               DataInputStream din = new DataInputStream(s.getInputStream());
               Thread t_client = new ClientHandler(s, din);
               t_client.start();


           }catch(Exception e){
               s2.close();

           }
        }

    }

    static class ClientHandler extends Thread  {
        Socket my_s;
        DataInputStream my_din;

        public ClientHandler(Socket s, DataInputStream din) {       //Constructor
            my_s = s;
            my_din = din;
        }

        public void  run(){
            try {
                Signin(my_s,my_din);
                Management(my_s,my_din);


            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    static private void Signin(Socket s, DataInputStream din) throws IOException {
        int i = 0;       //counter
        double foo;
        String info;
        String client = null;
        double client_kwhs= 0;
        String client_name=null;
        while (i < 4) {
            byte messageType = din.readByte();
            if (messageType == 0) {
                info = din.readUTF();
                System.out.println("Client's IP: " + info);
                client = info;
            } else if (messageType == 1) {
                info = din.readUTF();
                System.out.println("Client's Username: " + info);
                client = client.concat(info);
                client_name=info;
            } else if (messageType == 2) {
                info = din.readUTF();
                System.out.println("Client's Password: " + info);
                client = client.concat(info);
            } else if (messageType == 3) {
                info = din.readUTF();
                foo = parseDouble(info);
                client_kwhs=foo;
                System.out.println("Client's KWhs: " + foo);
            }
            i++;
        }
        myMap.put(client_name,client_kwhs);  //Save ip and kwhs of each client
        System.out.println("~Registration Completed~");
        for (String l : myMap.keySet()) {
            System.out.println("key: " + l + " value: " + myMap.get(l));
        }
        System.out.println();
    }

    static private void Management(Socket s, DataInputStream din) throws IOException ,EOFException{
        String message, result, the_who;
        byte messageType;
        boolean triedAandfailed;          //Plan B backdoor (:In case, already tried plan A and failed.)
        do{
        triedAandfailed = false;
        messageType = din.readByte();
        if (messageType == 69) {                    // [#code: 69] Aithma promithias apo client
            the_who = din.readUTF();            // Receiving client's Username
            System.out.println();
            message = din.readUTF();
            System.out.println("[#code 69][Request] New Request: Client " + the_who + " in need of " + message + " kwhrs.");
            String temp = message;                          //temp= requested kwhrs
            result = code69_first_step(temp);            //Search for a SINGLE Supplier
            if (!("fail").equals(result)) {                //a SINGLE Supplier found!
                PrintWriter writer = new PrintWriter(s.getOutputStream());
                writer.println(result);
                writer.flush();
                System.out.println("[#code 69][Response] Single Supplier Found: " + result + ". Information, send to requester client. " + "\n===================================================================");
            } else {                                       //Unable to find a SINGLE Supplier
                PrintWriter writer = new PrintWriter(s.getOutputStream());
                writer.println(result);                 // Send to client dah String "fail".
                writer.flush();
                System.out.println("[#code 69][Response] Unable to find a Single Supplier for this client. On hold for new instruction. ");
                message = din.readUTF();                 // Since no single supplier found, client should choose which plan he prefers.
                if (message.equalsIgnoreCase("A")) {
                    System.out.println("                   |___ [#code 69][Request] Client has decided to try plan: " + message);
                    System.out.println("                        [#code 69][Plan A] Description: Client requested a Pair of Suppliers.");
                    //temp=requested kwhrs , the_who=Requester client
                    MyResult rr = code69_plan_A(temp, the_who);
                    if (rr.getFirst().equals("fail") || rr.getSecond().equals("fail")) { // No pair folund
                        System.out.println("                        [#code 69][Response] Abort Plan A. Unable to find a Pair. On hold for new instruction. ");
                        writer = new PrintWriter(s.getOutputStream());          //  Pass
                        writer.println(rr.getFirst());                          //  "fail"
                        writer.println(rr.getSecond());                         //String values
                        writer.flush();
                        triedAandfailed = true;
                    } else {                  // A pair found
                        System.out.println("                        [#code 69][Response] Sending both Suppliers information to requester client. ");
                        writer = new PrintWriter(s.getOutputStream());
                        writer.println(rr.getFirst());
                        writer.println(rr.getSecond());
                        writer.flush();
                    }
                }

                if (message.equalsIgnoreCase("B") || triedAandfailed) {
                    System.out.println("                                           |___ [#code 69][Request] Client has decided to try plan B");
                    message = din.readUTF();
                    System.out.println("                                                [#code 69][Plan B] Description: Client's request will stay in Waiting List for " + message + " secs and then it will re-evaluate for a pair of suppliers.");
                    try {
                        WaitingList.put(the_who, Integer.parseInt(message));
                        TimeUnit.SECONDS.sleep(Integer.parseInt(message));
                        WaitingList.remove(the_who);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    MyResult rr = code69_plan_A(temp, the_who);     //final search for pair of suppliers
                    if (rr.getFirst().equals("fail") || rr.getSecond().equals("fail")) { // No pair folund
                        System.out.println("                                           [#code 69] Plan B Failed !theres no suppliers for u");
                        writer = new PrintWriter(s.getOutputStream());          //  Pass
                        writer.println(rr.getFirst());                          //  "fail"
                        writer.println(rr.getSecond());                         //String values
                        writer.flush();

                    } else {                  // A pair found
                        System.out.println("                                           [#code 69]Plan B Succeed. Finally! A pair of suppliers is available.");
                        writer = new PrintWriter(s.getOutputStream());
                        writer.println(rr.getFirst());
                        writer.println(rr.getSecond());
                        writer.flush();
                    }


                }

                System.out.println("\n=====================================================================================");

            }

        }
        if (messageType == 10) {                   //[#code: 10] Update
            the_who = din.readUTF();            // Receiving client's Username
            //System.out.println();
            message = din.readUTF();            //Receiving client's updated life
            code10_Update_Map(message, the_who);
        }
        if (messageType == 66){
            break;
        }
        }while(true);
    }

    //Search for a SINGLE Supplier : RETURNS supplier's name or String "fail"
    static private String code69_first_step(String m){
        System.out.println("[#code 69][Process/] Searching for a Supplier.");
        Map.Entry<String, Double> maxEntry = null;
        for (Map.Entry<String, Double> entry : myMap.entrySet())
        {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
            {
                maxEntry = entry;
            }
        }
        System.out.println("[#code 69][  ....  ] The Client with the most kwhrs is "+maxEntry.getKey()+" => "+maxEntry.getValue());
            double request= parseDouble(m);
        if ((maxEntry.getValue()*0.1) > request){           //if client's request is less than 10% of prospective supplier's kwhrs --> REQUEST ACCEPTED
            System.out.println("[#code 69][/Process] This User ("+maxEntry.getKey()+") will become a Single Supplier.");
            return maxEntry.getKey();
        }else{                                              // Right now is impossible to find a Single Supplier for this request
            System.out.println("[#code 69][/Process] This User ("+maxEntry.getKey()+") is unable to become a Single Supplier.");
            return "fail";
        }
    }

    //Search for a Pair of Suppliers : RETURNS a MyResult Object (available supplier 1,available supplier 2) or null Object
    static private MyResult code69_plan_A(String t, String who) {
        System.out.println("                        [#code 69][Plan A][Process/] Searching for the two Suppliers.");
        String c1 = null;         //1st Contender Supplier
        String c2 = null;         //2nd Contender Supplier
        int lock= 0;              //flag for dah loop
        double r = parseDouble(t);              //r=requested energy
        for (Map.Entry<String, Double> entry : myMap.entrySet())
        {
            if(!entry.getKey().equals(who)){
                if(((r/2) < (0.1*(entry.getValue())))&& lock==0){                   // Searching for the first Supplier . If requested energy /2 < 10% of prospective supplier's kwhrs --> 1st found
                    System.out.println("                        [#code 69][Plan A][  ....  ] Found 1st Contender Supplier: "+entry.getKey()+" donates "+r/2+" kwhrs to "+who+".");
                    c1=entry.getKey();
                    lock=1;
                } else if(((r/2) < (0.1*(entry.getValue())))&& lock==1){                   // Searching for the second Supplier . If requested energy /2 < 10% of prospective supplier's kwhrs --> 2nd found
                    System.out.println("                        [#code 69][Plan A][  ....  ] Found 2nd Contender Supplier: "+entry.getKey()+" donates "+r/2+" kwhrs to "+who+".");
                    c2=entry.getKey();
                    lock=2;
                    break;                                                             //ready
                }
            }
        }

        if(lock==2){         // A pair of suppliers found.
            System.out.println("                        [#code 69][Plan A][/Process] Pair of Suppliers successfully found.");
            return new MyResult(c1,c2);
        }else{              // Unable to find a pair of suppliers
            System.out.println("                        [#code 69][Plan A][/Process] Unable to find a pair of suppliers.");
            return new MyResult("fail","fail");
        }
    }

    //Update Client's Info
    static private void code10_Update_Map(String t, String who){
        System.out.println("\n[#code 10][Update/] Client "+who+" sent his new life.");
            double up = parseDouble(t);              // client's life
        for (Map.Entry<String, Double> entry : myMap.entrySet())
        {
            if(entry.getKey().equals(who)){
                myMap.replace(who,up);
            }
        }
        System.out.println("[#code 10]******** UPDATED-INFO***********");
        for (String l : myMap.keySet()) {
            System.out.println("[#code 10] key: " + l + " value: " + myMap.get(l));
        }
        //System.out.println();
        System.out.println("[#code 10][/Update] Successfull Update.");

    }
}

final class MyResult {
    private final String first;
    private final String second;
    public MyResult(String first, String second) {      //Constructor
        this.first = first;
        this.second = second;
    }
    public String getFirst() {
        return first;
    }
    public String getSecond() {
        return second;
    }
}