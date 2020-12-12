
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLOutput;
import java.util.Dictionary;
import java.util.StringTokenizer;


public class Bank {
    public static void main(String[] args) throws Exception {
        if(args.length!=1) { System.out.println("Incorrect Number of Arguments"); return; }
        int port=Integer.parseInt(args[0]);
        ServerSocket serverSocket =new ServerSocket(port);
        while(true){
         System.out.println("Created socket to port " + port);

        System.out.println("Waiting for a client");
            Socket clientsocket = serverSocket.accept();
            System.out.println("Accepted Client request");
         /// take input from client socket
            DataInputStream in = new DataInputStream(new BufferedInputStream(clientsocket.getInputStream()));
            DataOutputStream out =new DataOutputStream(clientsocket.getOutputStream());

            String[] message = new String[5];



            // Accept the message from store
            String line="";
            for(int i=0;i<5;i++){
                message[i] = in.readUTF();
            }

            // checking for valid user
            String credit_card_number= validitycheck(message);
            if(!(credit_card_number.equals("not"))) {

                // valid user
                String reply="Valid user";
                out.writeUTF(reply);

                /// check whether there is sufficient amount of money in user's credit card.

                String card_number=message[3];
                String required_credit = message[4];
                String money_check_reply="Transaction Approved" ;
                int  money_left_in_credit=sufficient_credit_check(required_credit,credit_card_number);
                if(money_left_in_credit==-1){
                System.out.println("Insufficient balance");
                    out.writeUTF("You don't have sufficeint balance");
                    
                }
                else {
                    System.out.println(money_check_reply);
                    out.writeUTF(money_check_reply);
                    out.writeUTF("Purchase has been made with "+message[4]+" amount");
                    out.writeUTF("Amount left in the credit card is : "+ money_left_in_credit);

                }

            }

            else
            {
                // user is not valid
                String reply="402 USER NOT FOUND";
                out.writeUTF(reply);
            }
            clientsocket.close();
            
            }

    }
    
        public static String validitycheck(String messages[]) throws Exception{

        String z="not";
        File file= new File("database.txt");
        BufferedReader br= new BufferedReader(new FileReader(file));
        String line="";
        while((line=br.readLine()) != null ){
           // System.out.println(line);
            StringTokenizer tokenizer=new StringTokenizer(line,",");
            String first_name, family,post,credit_card ;
            first_name= tokenizer.nextToken();
            family=tokenizer.nextToken();
            post=tokenizer.nextToken();
            credit_card=tokenizer.nextToken();
            if ( first_name.equals(messages[0]) && family.equals(messages[1]) && post.equals(messages[2]) && credit_card.equals(messages[3]))
            {
                System.out.println(first_name);
                z=  credit_card;
            }
        }

        return z;
    }

    public static int sufficient_credit_check(String money,String card_number) throws  Exception {


        File file = new File("database.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = "";
        while ((line = br.readLine()) != null) {
            //System.out.println(line);
            StringTokenizer tokenizer = new StringTokenizer(line, ",");
            String first_name, family, post, credit_card, balance, credit;
            first_name = tokenizer.nextToken();
            family = tokenizer.nextToken();
            post = tokenizer.nextToken();
            credit_card = tokenizer.nextToken();
            balance = tokenizer.nextToken();
            credit = tokenizer.nextToken();

            if (credit_card.equals(card_number)) {
                int credit_money = Integer.parseInt(credit);
                int required_money = Integer.parseInt(money);
                int result = credit_money-required_money;

                if (result >= 0) {


                    // money required for buying CDs .

                    // Credit left in the credit card
                    int baki = credit_money - required_money;
                    String baki_string = Integer.toString(baki);
                    // Update credit in the file of the correcsponding user


                    /// add the money deducted from the credit to the balance
                    int balance_int = Integer.parseInt(balance);
                    int add_balance = balance_int + required_money;
                    // convert add_balace into string
                    String add_balace_string = Integer.toString(add_balance);
                    updateDatabase(add_balace_string, credit_card, baki_string);
                    
                   
                    return baki;
                }
            }


        }
        return -1;
    }

 
     public static void updateDatabase(String balance_edit , String credit_card_number,String credit_edit){
     
            try{
                BufferedReader bufferedReader= new BufferedReader(new FileReader("database.txt"));
                String line ="";
                String newline="";
                String old="";
                String oldContent="";
                line=bufferedReader.readLine();
                while(line !=null){
                // process involved in updating database
                    oldContent = oldContent + line + System.lineSeparator();
                    String[] update=line.split(",");
                    if(update.length==1) break;
                    //System.out.println(update.length);
                    // we need to find out the credit card number from which transaction has been made.
                    if(credit_card_number.equals(update[3])){
                        old=String.join(",",update);
                        update[4]=balance_edit;
                        update[5]=credit_edit;

                        String l = String.join(",",update);
                        newline +=l ;
                    }

                    line=bufferedReader.readLine();


                }
                bufferedReader.close();
                String newtext=oldContent.replaceAll(old,newline);
                BufferedWriter bufferedWriter= new BufferedWriter( new FileWriter("database.txt"));
                bufferedWriter.write(newtext);
                bufferedWriter.close();
            }
            catch (Exception e) {
                System.out.println("Prolem in updating database  "+e);
            }
     
     }


}
