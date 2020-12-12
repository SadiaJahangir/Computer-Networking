
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

// Each Client Connection will be managed in a dedicated Thread
public class store implements Runnable {

    static final File WEB_ROOT = new File(".");
    static final String DEFAULT_FILE = "index.html";
    static final String FILE_NOT_FOUND = "404.html";
    static final String METHOD_NOT_SUPPORTED = "not_supported.html";
    // port to listen connection
    static int PORT, bankport;
    static String bankHost;

    // verbose mode
    static final boolean verbose = true;

    // Client Connection via Socket Class
    private Socket connect;

    public store(Socket c) {
        connect = c;
    }

    public static void main(String[] args) {
        try {
            // Now we will check the number of arguments
            if (args.length != 3) {
                System.out.println("Invalid number of arguments. Please try again.");
                return;
            }
            // Getting the port, bank host Ip and bankport 
            PORT = Integer.parseInt(args[0]);
            bankHost = args[1];
            bankport = Integer.parseInt(args[2]);

            // Now we will create a server socket to connect with a client
            ServerSocket serverConnect = new ServerSocket(PORT);
            System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");

            // we listen until user halts server execution
            while (true) {
                store myServer = new store(serverConnect.accept());

                if (verbose) {
                    System.out.println("Connecton opened. (" + new Date() + ")");
                }

                // create dedicated thread to manage the client connection
                Thread thread = new Thread(myServer);
                thread.start();
            }

        } catch (IOException e) {
            System.err.println("Server Connection error : " + e.getMessage());
        }
    }

    @Override
    public void run() {
        // we manage our particular client connection
        BufferedReader in = null;
        PrintWriter out = null;
        BufferedOutputStream dataOut = null;
        String fileRequested = null;

        try {
            // we read characters from the client via input stream on the socket
            in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
            // we get character output stream to client (for headers)
            out = new PrintWriter(connect.getOutputStream());
            // get binary output stream to client (for requested data)
            dataOut = new BufferedOutputStream(connect.getOutputStream());

            // get first line of the request from the client
            String input = in.readLine();
            // we parse the request with a string tokenizer
            StringTokenizer parse = new StringTokenizer(input);
            String method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client
            // we get file requested
            fileRequested = parse.nextToken().toLowerCase();

            // Now we check which method we got to work with

            if (input.contains("POST")) {
                String str = null;
                // Reading the header

                while (((str = in.readLine()).length()) != 0) {
                    System.out.println(in.readLine());
                }
                // Reading payload of the post method
                StringBuilder stringBuilder = new StringBuilder();
                while (in.ready()) {
                    stringBuilder.append((char) in.read());
                }
                String firstname = null;
                String family = null;
                String postcode = null;
                String creditcard = null;
                String itemno = null;
                String quantity = null;

                String buyer_info = stringBuilder.toString();
                // Getting the submitted form value given by the user
                String info[] = buyer_info.split("=");

                for (int i = 0; i < info.length; i++) {
                    String data[] = info[i].split("&");
                    if (i == 1)
                        firstname = data[0];
                    if (i == 2)
                        family = data[0];
                    if (i == 3)
                        postcode = data[0];
                    if (i == 4)
                        creditcard = data[0];
                    if (i == 5)
                        itemno = data[0];
                    if (i == 6)
                        quantity = data[0];

                }

                // Establishing connection with the bank
                Socket socket = new Socket(bankHost, bankport);
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

                /// calculating the purchase value for the CDs of the user want to purchase
                String prize = "", item = null;
                BufferedReader bf = new BufferedReader(new FileReader("store_database.txt"));
                String data = bf.readLine();
                while (data != null) {
                    StringTokenizer tokenizer = new StringTokenizer(data, ":");

                    item = tokenizer.nextToken();
                    prize = tokenizer.nextToken();

                    if (item.equals(itemno))
                        break;

                    else
                        prize = null;
                    data = bf.readLine();
                }

                int count = Integer.parseInt(quantity);
                int amount = Integer.parseInt(prize);
                int purchase = count * amount;
                String total_balance = Integer.toString(purchase);

                // Now we will send the data to bank to varify user and transaction approval

                for (int i = 0; i < 5; i++) {
                    if (i == 0)
                        dataOutputStream.writeUTF(firstname);
                    if (i == 1)
                        dataOutputStream.writeUTF(family);
                    if (i == 2)
                        dataOutputStream.writeUTF(postcode);
                    if (i == 3)
                        dataOutputStream.writeUTF(creditcard);
                    if (i == 4)
                        dataOutputStream.writeUTF(total_balance);
                    dataOutputStream.flush();

                }
                // Getting the response from the bank
                String response = dataInputStream.readUTF();
                if (response.equals("Valid user")) {
                    response = dataInputStream.readUTF();
                    if(response.equals("Transaction Approved")){
                    
                        response = dataInputStream.readUTF();
                        System.out.println("Total cost of purchase :" +response);
                        response = dataInputStream.readUTF();
                        System.out.println("Available credit :" +response);
                         dataOut.write(ProcessHTML("Transaction.html").getBytes());
                          dataOut.flush();
                        
                    }
                    else{
                        System.out.println("Transaction failed" + response);
                        dataOut.write(ProcessHTML("insufficient.html").getBytes());
                        dataOut.flush();
                    }

                } else {
                    System.out.println("Transaction failed "+response);
                    dataOut.write(ProcessHTML("InvalidUser.html").getBytes());
                    dataOut.flush();

                }
                //closing the connection
                dataInputStream.close();
                dataOutputStream.close();
                socket.close();
                connect.close();
                System.out.println("Connection closed.\n");

            }

            else if (!method.equals("GET") && !method.equals("HEAD")) {
                if (verbose) {
                    System.out.println("501 Not Implemented : " + method + " method.");
                }

                // we return the not supported file to the client
                File file = new File(WEB_ROOT, METHOD_NOT_SUPPORTED);
                int fileLength = (int) file.length();
                String contentMimeType = "text/html";
                // read content to return to client
                byte[] fileData = readFileData(file, fileLength);

                // we send HTTP Headers with data to client
                out.println("HTTP/1.1 501 Not Implemented");
                out.println("Server: Java HTTP Server from SSaurel : 1.0");
                out.println("Date: " + new Date());
                out.println("Content-type: " + contentMimeType);
                out.println("Content-length: " + fileLength);
                out.println(); // blank line between headers and content, very important !
                out.flush(); // flush character output stream buffer
                // file
                dataOut.write(fileData, 0, fileLength);
                dataOut.flush();

            } else {
                // GET or HEAD method
                if (fileRequested.endsWith("/")) {
                    fileRequested += DEFAULT_FILE;
                }

                File file = new File(WEB_ROOT, fileRequested);
                int fileLength = (int) file.length();
                String content = getContentType(fileRequested);

                if (method.equals("GET")) { // GET method so we return content
                    byte[] fileData = readFileData(file, fileLength);

                    // send HTTP Headers
                    out.println("HTTP/1.1 200 OK");
                    out.println("Server: Java HTTP Server from SSaurel : 1.0");
                    out.println("Date: " + new Date());
                    out.println("Content-type: " + content);
                    out.println("Content-length: " + fileLength);
                    out.println(); // blank line between headers and content, very important !
                    out.flush(); // flush character output stream buffer

                    dataOut.write(fileData, 0, fileLength);
                    dataOut.flush();
                }

                if (verbose) {
                    System.out.println("File " + fileRequested + " of type " + content + " returned");
                }

            }

        } catch (FileNotFoundException fnfe) {
            try {
                fileNotFound(out, dataOut, fileRequested);
            } catch (IOException ioe) {
                System.err.println("Error with file not found exception : " + ioe.getMessage());
            }

        } catch (IOException ioe) {
            System.err.println("Server error : " + ioe);
        }
        catch(NullPointerException e){
        } 
        
        finally {
            try {
                in.close();
                out.close();
                dataOut.close();
                connect.close(); // we close socket connection
            } catch (Exception e) {
                System.err.println("Error closing stream : " + e.getMessage());
            }
        }

    }

    private byte[] readFileData(File file, int fileLength) throws IOException {
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];

        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        } finally {
            if (fileIn != null)
                fileIn.close();
        }

        return fileData;
    }

    // return supported MIME Types
    private String getContentType(String fileRequested) {
        if (fileRequested.endsWith(".htm") || fileRequested.endsWith(".html"))
            return "text/html";
        else
            return "text/plain";
    }

    private void fileNotFound(PrintWriter out, OutputStream dataOut, String fileRequested) throws IOException {
        File file = new File(WEB_ROOT, FILE_NOT_FOUND);
        int fileLength = (int) file.length();
        String content = "text/html";
        byte[] fileData = readFileData(file, fileLength);

        out.println("HTTP/1.1 404 File Not Found");
        out.println("Server: Java HTTP Server from SSaurel : 1.0");
        out.println("Date: " + new Date());
        out.println("Content-type: " + content);
        out.println("Content-length: " + fileLength);
        out.println(); // blank line between headers and content, very important !
        out.flush(); // flush character output stream buffer

        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();

        if (verbose) {
            System.out.println("File " + fileRequested + " not found");
        }
    }

	public String ProcessHTML(String filename)
    {
        String data="";
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));

            String str = "";
            while ((str = br.readLine()) != null) {
                data += str+"\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String message="";
        message += "HTTP/1.1 200 OK\n";
        message += "Content-Length: " + data.length() + "\n";
        message += "Content-Type: text/html\n";
        message += "Connection: keep-alive\r\n";
        message += "\r\n";
        message += data;
        return message;
    }

}
