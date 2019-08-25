
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static java.lang.Double.parseDouble;

public class SocketServer {
    static Map<String, Double> myMap = new ConcurrentHashMap<String, Double>();
    static Map<String, Integer> WaitingList = new ConcurrentHashMap<String, Integer>();
    static Map<String, Double> S_List = new ConcurrentHashMap<String, Double>();                   // Contains: How many kwhrs, each client, SEND to other clients
    static Map<String, Double> B_List = new ConcurrentHashMap<String, Double>();                   // Contains: How many kwhrs, each client, RECEIVED from other suppliers
    static Map<String, Long> T_List = new ConcurrentHashMap<String, Long>();                       // Contains: How much TIME passed since a client had a supplier's role
    static Map<String, Double> SBT_List_Unsorted = new ConcurrentHashMap<String, Double>();        // Contains: Function's Result for each client **UNSORTED**
    static Map<String, Double> SBT_List_wanna_be_Sorted = new ConcurrentHashMap<String, Double>(); // Contains TEMPORALLY: Function's Result for each client
    static Map<String, Double> Suppliers_List = new ConcurrentHashMap<String, Double>();           // Contains TEMPORALLY: Suppliers (name,help units) for each request


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

        S_List.put(client_name,0.0);               // Registered |Client|  SEND    |0 kwhrs| to others
        B_List.put(client_name,0.0);               // Registered |Client| RECEIVED |0 kwhrs| from others
        T_List.put(client_name, Long.valueOf(0));  // Registered |Client| was supplier before |0 secs|
        SBT_List_Unsorted.put(client_name,0.0);             // Registered |Client|      ,   |func result|
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

                    S_List_Update(result, Double.parseDouble(temp));      B_List_Update(the_who, Double.parseDouble(temp));  //Update Concurent HashMaps S&B

                    //SBT_List_use_of_Formula();   //DEN TO XRHSIMOPOIW STH SINGLE

                } else {                                       //Unable to find a SINGLE Supplier
                    PrintWriter writer = new PrintWriter(s.getOutputStream());
                    writer.println(result);                 // Send to client dah String "fail".
                    writer.flush();
                    System.out.println("[#code 69][Response] Unable to find a Single Supplier for this client. On hold for new instruction. ");
                    message = din.readUTF();                 // Since no single supplier found, client should choose which plan he prefers.
                    if (message.equalsIgnoreCase("A")) {
                        System.out.println("                   |___ [#code 69][Request] Client has decided to try plan: " + message);
                        System.out.println("                        [#code 69][Plan A] Description: Client requested Multiple Suppliers.");
                        //temp=requested kwhrs , the_who=Requester client

                        SBT_List_use_of_Formula();
                        int number_of_suppliers_found = code69_plan_A(temp, the_who);

                        if (number_of_suppliers_found==0) { // No multi suppliers found
                            System.out.println("                        [#code 69][Response] Abort Plan A. Unable to find multi-Suppliers. On hold for new instruction. ");
                            writer = new PrintWriter(s.getOutputStream());          //   Pass "fail"
                            writer.println("fail");                                 //  String value
                            writer.flush();
                            triedAandfailed = true;
                        } else {                  // multi-Suppliers found
                            System.out.println("                        [#code 69][Response] Sending multi Suppliers information to requester client. ");
                            writer = new PrintWriter(s.getOutputStream());
                            writer.println(number_of_suppliers_found);
                            writer.flush();

                            // na steilw ston client ta onomata twn suppliers kai to ti 8a parei apo ton ka8ena mesw toy map pou exw
                            for (Map.Entry<String, Double> sup : Suppliers_List.entrySet()) {
                                writer.println(sup.getKey());
                                writer.println(sup.getValue());
                                //
                            }
                            writer.flush();
                            Suppliers_List.clear(); //Clear suppliers map
                        }
                    }

                    if (message.equalsIgnoreCase("B") || triedAandfailed) {
                        System.out.println("                                           |___ [#code 69][Request] Client has decided to try plan B");
                        message = din.readUTF();
                        System.out.println("                                                [#code 69][Plan B] Description: Client's request will stay in Waiting List for " + message + " secs and then it will re-evaluate for multi-suppliers.");
                        try {
                            WaitingList.put(the_who, Integer.parseInt(message));
                            TimeUnit.SECONDS.sleep(Integer.parseInt(message));
                            WaitingList.remove(the_who);

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        SBT_List_use_of_Formula();
                        int number_of_suppliers_found = code69_plan_A(temp, the_who);

                        if (number_of_suppliers_found==0) { // No multi suppliers found
                            System.out.println("                                           [#code 69] Plan B Failed !theres no suppliers for u");                            writer = new PrintWriter(s.getOutputStream());          //   Pass "fail"
                            writer.println("fail");                                 //  String value
                            writer.flush();

                        } else {                  // multi-Suppliers found
                            System.out.println("                                           [#code 69]Plan B Succeed. Finally! Multi-suppliers are available.");
                            writer = new PrintWriter(s.getOutputStream());
                            writer.println(number_of_suppliers_found);
                            writer.flush();

                            // na steilw ston client ta onomata twn suppliers kai to ti 8a parei apo ton ka8ena mesw toy map pou exw
                            for (Map.Entry<String, Double> sup : Suppliers_List.entrySet()) {
                                writer.println(sup.getKey());
                                writer.println(sup.getValue());
                            }
                            writer.flush();
                            Suppliers_List.clear();     //Clear Suppliers List
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
                message = din.readUTF();
                System.out.println("\n\nA loving Client and Supplier left us...");
                System.out.println("          +++++++");
                System.out.println("       ++++++++++++  ");
                System.out.println("    ++++++++++++++++++");
                System.out.println("    |     ~ RIP ~   | ");
                System.out.println("          " +message);
                System.out.println("    |    2019-2019   |");
                System.out.println("    ++++++++++++++++++");
                System.out.println("      +++++++++++++");

                //diagrafh apo ta praktika
                myMap.remove(message);
                S_List.remove(message);
                B_List.remove(message);
                T_List.remove(message);
                SBT_List_Unsorted.remove(message);
                Suppliers_List.remove(message);

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

    //Search for Multiple Suppliers : RETURNS 0 for Failure OR the number of multi-suppliers. FILL UP Suppliers MAP
    static private int code69_plan_A(String t, String who) {
        System.out.println("                        [#code 69][Plan A][Process/] Searching for Multiple Suppliers.");
        double r = parseDouble(t);              //r=requested energy
        double r_backup=parseDouble(t);
        int cuircles_number=1;
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader("Sorted.txt"));
                String text = null;
                while(r>0 && cuircles_number<3) {        //Requested Energy not collected

                    if ((text = reader.readLine()) != null) {
                        //~~~~~~~~~~~ PLAN A ~~~~~~~~~~~~~~
                        //~~~~~~~~~~ SCENARIO ~~~~~~~~~~~~~
                            String current_client = text;
                            double ten_percent;
                            for (Map.Entry<String, Double> entry : myMap.entrySet()) {
                                if(!entry.getKey().equals(who) && !(entry.getValue()<=0)){        // i Dont want my self as a supplier AND i dont want a dead supplier
                                    if(entry.getKey().equals(current_client)){  //I fOUND CURRENT CLIENT
                                        ten_percent = 0.1 * entry.getValue();       //his 10%:
                                        r = r - ten_percent;    //Remaining energy units of request
                                        if (r < 0) {
                                            ten_percent = ten_percent + r;      //FIX NEGATIVITY
                                        }
                                        if(Suppliers_List.containsKey(entry.getKey())){

                                            for (Map.Entry<String, Double> sss : Suppliers_List.entrySet()) {
                                                if (entry.getKey().equals(sss.getKey())) {
                                                    ten_percent = ten_percent + sss.getValue();   // Synoliko poso pou 8a dwsei = to poso tou 1ou kyklou + tenpercent
                                                }
                                            }

                                            System.out.println("                        [#code 69][Plan A][   ...   ]2nd Circle: Supplier "+entry.getKey()+" gives "+ten_percent);
                                            System.out.println("                                                                                            (totally: "+ten_percent+")");
                                            Suppliers_List.replace(entry.getKey(),ten_percent); // |Supplier|,|help units|

                                        }else{
                                            Suppliers_List.put(entry.getKey(), ten_percent); // |Supplier|,|help units|
                                            System.out.println("                        [#code 69][Plan A][   ...   ]1st Circle: Supplier "+entry.getKey()+" gives "+ten_percent);
                                        }
                                    }
                                }
                            }
                        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    }else{
                        reader.close();
                        reader = new BufferedReader(new FileReader("Sorted.txt"));    //Re-initilize the reader to point onto the first line again!
                        text = null;
                        cuircles_number++;
                    }

                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                }
            }

        int number_of_using_suppliers=Suppliers_List.size();    // Now i know the number of using suppliers (0 || ...)
        if(number_of_using_suppliers<2){
            Suppliers_List.clear();
            return 0;
        }else{
            for (Map.Entry<String, Double> entry : Suppliers_List.entrySet()) {
                S_List_Update(entry.getKey(),entry.getValue());
                //System.out.println("Suppliers LIST: KEY:"+entry.getKey()+" VALUE:"+entry.getValue());
            }

            B_List_Update(who,r_backup);
            return number_of_using_suppliers;
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

    //S_List Update: Client send more kwhrs... (->***Updates T_List too****)
    static private void S_List_Update(String supplier,double kwrhs) {
        for (Map.Entry<String, Double> entry : S_List.entrySet()) {
            if (entry.getKey().equals(supplier)) {
                double add= kwrhs + entry.getValue();
                S_List.replace(supplier,add);
            }
        }
        for (Map.Entry<String, Long> entry : T_List.entrySet()) {
            if (entry.getKey().equals(supplier)) {
                T_List.replace(supplier,System.currentTimeMillis()); // Saves the exact time of transaction
            }
        }
    }

    //B_List Update: Client received more kwhrs...
    static private void B_List_Update(String client,double kwrhs) {
        for (Map.Entry<String, Double> entry : B_List.entrySet()) {
            if (entry.getKey().equals(client)) {
                double add= kwrhs + entry.getValue();
                B_List.replace(client,add);
            }
        }
    }

    //
    static private void SBT_List_use_of_Formula() throws IOException {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long current_time= System.currentTimeMillis();      //Current Time.
        long time_result;
        double S=0,B=0,formulas_result;
        for (Map.Entry<String, Long> entry : T_List.entrySet()) {
            String user= entry.getKey();
            for (Map.Entry<String, Double> entry2 : S_List.entrySet()) {
                if (entry2.getKey().equals(user)) {
                    S= entry2.getValue();
                }
            }
            for (Map.Entry<String, Double> entry3 : B_List.entrySet()) {
                if (entry3.getKey().equals(user)) {
                    B= entry3.getValue();
                }
            }
            time_result = current_time - entry.getValue();  //Time_Result = Current_Time - Saved_Time_from_last_transaction
            // +~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~+
            // |  Function Formula: max{S – B, 0) * time_result}  |
            // +~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~+
            formulas_result=  Math.max( S-B,0.0 ) * time_result;
           /* System.out.println("formulas S      --------->"+S);
            System.out.println("formulas B      --------->"+B);
            System.out.println("formulas CT      --------->"+current_time);
            System.out.println("formulas ST      --------->"+entry.getValue());
            System.out.println("formulas timeresult------->"+time_result);
            System.out.println("formulas result----------->"+formulas_result);
            System.out.println("////////////////////////////////////////");*/
            SBT_List_Unsorted.replace(user,formulas_result);
        }
        //1.Copy Unsorted Map to a temporal intermidiate Map called SBT_List_wanna_be_Sorted
        SBT_List_wanna_be_Sorted = new ConcurrentHashMap<>(SBT_List_Unsorted);
        //2.Call Sort Function
        Sort((ConcurrentHashMap<String, Double>) SBT_List_wanna_be_Sorted);
    }

    // SBT_List_Unsorted => SORTED LIST IN THE FILE
    private static void Sort(ConcurrentHashMap<String,Double> map) throws IOException {

        while(!map.isEmpty()) {
            Map.Entry<String, Double> MAXEntry = null;
            for (Map.Entry<String, Double> entry : map.entrySet()) {
                if (MAXEntry == null || entry.getValue().compareTo(MAXEntry.getValue()) > 0) {
                    MAXEntry = entry;
                }
            }
            //System.out.println("MAX ENTRY KEY=================================="+MAXEntry.getKey());
            BufferedWriter outF=null; ;
            try {
                FileWriter fstream = new FileWriter("Sorted.txt", true); //true tells to append data.

                final Scanner scanner;
                try {
                    scanner = new Scanner(new File("Sorted.txt"));
                    while (scanner.hasNextLine()) {
                        final String lineFromFile = scanner.nextLine();
                        if(lineFromFile.contains(MAXEntry.getKey())) {
                            FileWriter fwOb = new FileWriter("Sorted.txt", false);
                            PrintWriter pwOb = new PrintWriter(fwOb, false);
                            pwOb.flush();
                            pwOb.close();
                            fwOb.close();
                            break;
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }


                outF = new BufferedWriter(fstream);
                outF.write(MAXEntry.getKey());     //
                outF.newLine();
            }
            catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
            }
            finally {
                if(outF != null) {
                    outF.close();
                }
            }

            map.remove(MAXEntry.getKey());


        }
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