import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Scanner;



import java.io.ByteArrayInputStream;
        import java.io.ByteArrayOutputStream;
        import java.io.DataInputStream;
        import java.io.DataOutputStream;

public class DNS_resolver
{

    static ByteArrayInputStream byteArrayInputStream;
    static ByteArrayOutputStream byteArrayOutputStream;
    static DataInputStream dataInputStream;
    static DataOutputStream dataOutputStream;

    static String RootServer;
    static String DOMAIN_NAME;
    static  String NameServer;
    static String calculatedIP;
    static String rootServer[] = {"192.5.5.241","198.41.0.4","199.9.14.201","192.33.4.12","199.7.91.13","192.203.230.10","192.112.36.4","198.97.190.53","192.36.148.17","192.58.128.30","193.0.14.129","199.7.83.42","202.12.27.33"};


    public static void main(String args[])
    {
        //Scanner scanner = new Scanner(System.in);
        if(args.length==0){
            System.out.println("Error ");

        }

        DOMAIN_NAME = args[0];
        NameServer=DOMAIN_NAME;

       for(int i=0;i<rootServer.length;i++){
           String IP=ResolveIPaddress(DOMAIN_NAME,rootServer[i],i);  // iterative query for DOMAIN_NAME
           if(!(IP.equals("time out")) ){
               break;
           }
       }


    }

    static String ResolveIPaddress(String domain,String server,int serverno)
    {
        int count = 0;
        byteArrayOutputStream = new ByteArrayOutputStream();
        dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        try
        {
            /// header section
            /// ID - An arbitrary 16 bit request identifier

            dataOutputStream.writeShort(0x1222);

            /// Query parameters
            /// flag set to 0000 to restrict of doing
            dataOutputStream.writeShort(0x0000);
            /// Question count -  number of questions 1
            dataOutputStream.writeShort(0x0001);
            /// Answer count - number of answers is 0
            dataOutputStream.writeShort(0x0000);
            /// Number of authority records -
            dataOutputStream.writeShort(0x0000);
            /// Number of Additional Records
            dataOutputStream.writeShort(0x0000);

            /// Question - it has three sections . 1.QNAME 2.QTYPE 3.QCLASS

            /// Question Name - This contains the URL whose IP address we want to find . It is encoded as labels. Each label corresponds to a section of URL

            //  splitting the   hostname
            String[] hostname_levels = domain.split("\\.");
            int length = hostname_levels.length;

            for (int i = 0; i < length; i++)
            {

                byte[] hostname_levels_byte = hostname_levels[i].getBytes();
                //System.out.println(hostname_levels[i] + "  : " + hostname_levels_byte);

                /// at first  length of the level of the URL is written
                dataOutputStream.writeByte(hostname_levels_byte.length);
                /// then the bytes of the level are added
                dataOutputStream.write(hostname_levels_byte);
            }

            // The QNAME section is terminated by 00
            dataOutputStream.writeByte(0x00);

            /// QTYPE -Here we are looking up for the IP address of a hostname and for thisquestion, the  Question Type is 'A' whose value is 1
            dataOutputStream.writeShort(0x0001);

            /// OCLASS - Here we are using Interent class , IN and the value is 1
            dataOutputStream.writeShort(0x0001);

            byte[] DNSframe = byteArrayOutputStream.toByteArray();

            // sending DNS request frame
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket DNSrequestPacket = new DatagramPacket(DNSframe, DNSframe.length, InetAddress.getByName(server), 53);
            // send the packet
            socket.send(DNSrequestPacket);
            // set time out 1s
            socket.setSoTimeout(1000);

            ///****************************receive part***********************************************
            byte response[] = new byte[1024];
            try {
                DatagramPacket receivedpacket = new DatagramPacket(response, response.length);
                socket.receive(receivedpacket);
            }
            catch (SocketTimeoutException e)  { //if time is out , then the getIP() method will return a message "time out"
                return "time out";
            }
        /// DNS reply message is sotred in response[] array
            byteArrayInputStream = new ByteArrayInputStream(response);
            dataInputStream = new DataInputStream(byteArrayInputStream);


            dataInputStream.readShort(); // transaction ID
            dataInputStream.readShort(); /// flags
            dataInputStream.readShort(); // Questions
            short answerRRs= dataInputStream.readShort(); // Answer RRs
            short authorityRRs=dataInputStream.readShort(); // Authority RRs
            short additionalRRs=dataInputStream.readShort(); // Additional RRs


        // QUery Section

            String QueriedDomainName = ResolveDomainName(response); // query domain name
            dataInputStream.readShort(); // Query Type
            dataInputStream.readShort(); // Class IN

   // loop thourgh all answer RRs

        for(int i=1;i<=answerRRs;i++){

                String domName = ResolveDomainName(response);
            String IpAddress="";
                //savedomain = domName;
                String Type = "";

                Type = GetQueryType(dataInputStream.readShort());
                dataInputStream.readShort(); /// Class IN
              int ttl=  dataInputStream.readInt(); // Time To Live
                dataInputStream.readShort();// Data Lenght

                if (Type.equals("A"))
                {

                    int w = dataInputStream.readByte();
                    w = w & 0x000000ff;
                    int x = dataInputStream.readByte();
                    x = x & 0x000000ff;
                    int y = dataInputStream.readByte();
                    y = y & 0x000000ff;
                    int z = dataInputStream.readByte();
                    z = z & 0x000000ff;

                     IpAddress = String.format("%d.%d.%d.%d", w, x, y, z);

                    if(domain.equals(DOMAIN_NAME))
                    {
                        System.out.println(DOMAIN_NAME + "     "+ ttl + "    IN   "+Type + "        "+IpAddress );
                    }


                }
                else if (Type.equals("AAAA"))
                {
                    int a = dataInputStream.readShort();
                    int b = dataInputStream.readShort();
                    int c = dataInputStream.readShort();
                    int d = dataInputStream.readShort();
                    int e = dataInputStream.readShort();
                    int f = dataInputStream.readShort();
                    int g = dataInputStream.readShort();
                    int h = dataInputStream.readShort();
                    String ipv6Address = String.format("%x:%x:%x:%x:%x:%x:%x:%x", a, b, c, d, e, f, g, h);
                    //System.out.println("IPV6 Address : "+ipv6Address);
                    if(domName.equals(DOMAIN_NAME))
                        System.out.println(DOMAIN_NAME + "     "+ ttl + "    IN   "+Type + "        "+ipv6Address );


                }
                else if(Type.equals("CNAME"))
                {
                    String cname = ResolveDomainName(response);
                    System.out.println(DOMAIN_NAME + "     "+ ttl + "    IN   "+Type + "        "+cname );
                    if(domName.equals(DOMAIN_NAME)){
                        DOMAIN_NAME=cname;
                        return ResolveIPaddress(DOMAIN_NAME,rootServer[serverno],serverno);
                    }
                }

                else if(Type.equals("SOA")){
                    System.out.println("          " +domain + "  : Does not exist"); // SOA means doesnot exist
                    return "Does not exist" ;

                }

             if(i==answerRRs)
                 return IpAddress;

            }

        /// loop thorough all authority RRs
            for(int i=0;i<authorityRRs;i++){

                String domName = ResolveDomainName(response);

                String Type = "";

                Type = GetQueryType(dataInputStream.readShort());
                dataInputStream.readShort(); /// Class IN
                dataInputStream.readInt(); // Time To Live
                dataInputStream.readShort();// Data Lenght
                if(Type.equals("CNAME")) {
                    String cname=ResolveDomainName(response);
                    return  ResolveIPaddress(cname,rootServer[serverno],serverno);

                }
                else if(Type.equals("NS")){
                    String nameserver =ResolveDomainName(response);
                    if(additionalRRs==0) {
                        String nameserverip= ResolveIPaddress(nameserver, rootServer[serverno],serverno);
                        String ip = ResolveIPaddress(domain,nameserverip,serverno);
                        if(!(ip.equals("time out")) ){
                            return  ip;
                        }
                    }
                }
                else if(Type.equals("SOA")){
                    System.out.println("          " +domain + "  : Does not exist"); // SOA means doesnot exist
                    return "Does not exist" ;

                }
            }

            /// loop through all additional RRs
            for(int i=0;i<additionalRRs;i++){
                String domName = ResolveDomainName(response);

                String Type = "";

                Type = GetQueryType(dataInputStream.readShort());
                dataInputStream.readShort(); /// Class IN
                dataInputStream.readInt(); // Time To Live
                dataInputStream.readShort();// Data Length

                if (Type.equals("A"))
                {

                    int w = dataInputStream.readByte();
                    w = w & 0x000000ff;
                    int x = dataInputStream.readByte();
                    x = x & 0x000000ff;
                    int y = dataInputStream.readByte();
                    y = y & 0x000000ff;
                    int z = dataInputStream.readByte();
                    z = z & 0x000000ff;

                 String   IpAddress = String.format("%d.%d.%d.%d", w, x, y, z);

                    if(domName.equals(domain))
                        return IpAddress;
                    String ip=ResolveIPaddress(domain,IpAddress,serverno);
                    if(!(ip.equals("time out") ) )
                    return  ip;

                }

                else if (Type.equals("AAAA"))
                {
                    int a = dataInputStream.readShort();
                    int b = dataInputStream.readShort();
                    int c = dataInputStream.readShort();
                    int d = dataInputStream.readShort();
                    int e = dataInputStream.readShort();
                    int f = dataInputStream.readShort();
                    int g = dataInputStream.readShort();
                    int h = dataInputStream.readShort();
                    String ipv6Address = String.format("%x:%x:%x:%x:%x:%x:%x:%x", a, b, c, d, e, f, g, h);
                    //System.out.println("IPV6 Address : "+ipv6Address);

                }

            }


        }

        catch (Exception exception)
        {
            System.out.println("Error occured : ");
            System.out.println(exception);
        }
        //System.out.println("checking ip: "+IpAddress);
        return ResolveIPaddress(NameServer,rootServer[serverno],serverno);
    }






    static String ResolveDomainName(byte answer[]) throws Exception
    {

        /// domain name e convert korar somoy check korte thakbo 00 or c0 read hoise ki na. jodi 00 read hoy tahole bujha jabe domain name read kora shesh. ar c0 read kora holeo bujha jay domain name read kora shesh, kintu sekhetre aro 16 byte extra pore read korte hoy.


        byte Domarray[] = new byte[1024];
        int i = 0;

        while(true)
        {
            byte b = dataInputStream.readByte();

            if(b == 0)
                break;

            if(String.format("%x",b).equals("c0"))
            {
                int index = dataInputStream.readByte();
                index = index & 0x000000ff;
                int j = answer[index];
                index++;
                while(j > 0)
                {
                    if(i>0)
                        Domarray[i++] = '.';

                    for(int l=0; l<j; l++)
                    {
                        Domarray[i++] = answer[index];
                        index++;
                    }
                    j = answer[index];
                    index++;
                }
                break;

            }
            if (i > 0)
                Domarray[i++] = '.';

            for(int j = 0; j < b; j++)
            {
                byte y = dataInputStream.readByte();
                Domarray[i] = y;
                i++;
            }
        }

        String DomName = new String(Domarray, 0, i);
        return DomName;
    }

    static String GetQueryType(int x)
    {
        if(x == 1)
            return "A";
        if(x == 2)
            return "NS";
        if(x == 5)
            return "CNAME";
        if (x == 28)
            return "AAAA";
        if(x==6)
            return "SOA" ;
        return "Unknown";
    }

}

