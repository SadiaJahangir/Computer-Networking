#include <netdb.h>
#include <netinet/in.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>
#include<stdio.h>

int file_exists(const char * filename){
    /* try to open file to read */
    FILE *file;
    if (file = fopen(filename, "r")){
        fclose(file);
        return 1;
    }
    return 0;
}


int main(int argc,char* argv[])
{
    /// input format -> ./a.out port_number
    if(argc<2)
    {
        printf("please give the port number ");

    }

    //sockaddr_in -> store the socket address of the server and the client.
    struct sockaddr_in servaddr, cli;

    ///socket create and verification


    int sockfd,len;
    sockfd=socket(AF_INET,SOCK_STREAM,0);
    if(sockfd<0)
    {
        printf("Socket connection failed ! ");

    }
    else
    {
        printf("Connection established\n");

    }

    int PORT=atoi(argv[1]);
    bzero(&servaddr, sizeof(servaddr));
    // assign IP, PORT
    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = htonl(INADDR_ANY);
    servaddr.sin_port = htons(PORT);

// Binding newly created socket to given IP and verification
    if ((bind(sockfd, (struct sockaddr*)&servaddr, sizeof(servaddr))) != 0)
    {
        printf("socket bind failed...\n");
        exit(0);
    }
    else
        printf("Socket successfully binded..\n");

Started:

    // Now server is ready to listen and verification
    if ((listen(sockfd, 5)) != 0)
    {
        printf("Listen failed...\n");
        exit(0);
    }
    else
        printf("Server listening..\n");
    len = sizeof(cli);

    // Accept the data packet from client and verification
    int socket_connect;

    socket_connect= accept(sockfd, (struct sockaddr*)&cli, &len);


    if (socket_connect < 0)
    {
        printf("server acccept failed...\n");
        exit(0);
    }
    else
        printf("server acccept the client...\n");


    send(socket_connect, "220 WELCOME\n", 100, 0);

        /// helo request
        /// the input format should be either helo or HELO

    char buffer[2000];
    read(socket_connect, buffer, sizeof(buffer));

    char check[100];
    strncpy ( check, buffer, 4 );
    if(strcmp(check,"helo")==0  || strcmp(check,"HELO")==0  ){
        char s[1000]={0};
        strcat(s,"250  ");
        strcat(s,buffer);
        strcat(s,"\n");

    send(socket_connect, s, 40, 0);

        }
    else
    {

        send(socket_connect, "500 Syntax error, command unrecognised\n", 100, 0);
        goto Started;

    }

    /// mail_from

bzero(buffer, 1024);
    int valread = read(socket_connect, buffer, 300);
  //  printf("%d\n", valread);
   // printf("%s\n",buffer);

    char w[1000];
    for(int i=0;i<9;i++) w[i]=buffer[i];

    w[9]='\0';

    char mail_user[300];
    int j=0;
    int l=strlen(buffer);
    for(int i=10;i<l;i++)
    {
            if(buffer[i]==',') break;

        if(buffer[i]!=' ') mail_user[j++]=buffer[i];
    }
    mail_user[j]='\0';
 //   printf("%s\n",mail_user);
printf("%s\n",buffer);
    if(strcmp(w,"MAIL FROM")==0)
    {

        send(socket_connect,"250 Requested mail action okay, completed\n",50,0);

    }
    else
    {
    send(socket_connect,"500 Syntax error command unrecognized.\n",100, 0);
    goto Started;

    }

    /// RCPT To /////////////////////

    char rcpt_user[300];

    bzero(buffer,1024);
    valread=read(socket_connect,buffer,330);

    l=strlen(buffer);
    j=0;
    /// mail box name extraction
    for(int i=7;i<l;i++){
    if(buffer[i]=='@') break;
        if(buffer[i]!=' ') rcpt_user[j++]=buffer[i];

    }
    rcpt_user[j]='\0';
   // printf("RCPT to : %s\n",rcpt_user);

    char check2[100];
    strncpy(check2,buffer,7);
    if(strcmp(check2,"RCPT TO")==0)
    {
    /// mail box validity check. the mail_box will have .txt extension
       strcat(rcpt_user,".txt");
//printf("%s\n",rcpt_user);
    int z=file_exists(rcpt_user);
    //printf("%d\n",z);

    if(z==0)
    {
        send(socket_connect,"550 Requested action not taken,Mailbox unavailable\n",255, 0);

        goto Started;
    }
        else
        send(socket_connect,"250 Requested mail action okay, completed\n ",90,0);
    }

    else
    {
        send(socket_connect,"500 Syntax error command unrecognized\n",100, 0);
        goto Started;

    }

    /// DATA REQUEST ////////////

    bzero(buffer,1024);
    valread=read(socket_connect,buffer,330);

    char check3[100];
    strncpy(check3,buffer,4);
    if(strcmp(check3,"DATA")==0)
    {
        send(socket_connect,"250 Requested mail action okay, completed\n ",90,0);
    }

    else
    {
        send(socket_connect,"500 Syntax error command unrecognized\n",100, 0);
        goto Started;

    }

        char msg[3000];
      bzero(buffer,3000);
    valread=read(socket_connect,msg,3000);


/// write the header and the message into mail box file
    FILE * fp;

   fp = fopen (rcpt_user,"a");
 int lin=strlen(msg);

   for(int i=0;i<lin;i++){

  fprintf(fp,"%c", msg[i]);
}
   fclose (fp);


/// termination .
    bzero(buffer, 300);
    valread = read(socket_connect, buffer, 300);

    if(strcmp(buffer, ".")==0)
    printf("Message received ...\n");


    /// quit

    bzero(buffer, 300);
    valread = read(socket_connect, buffer, 300);

    if(strcmp(buffer, "QUIT") == 0)
    {
        send(socket_connect, "250 Requested mail action okay, completed", 100, 0);
        goto Started;
    }
    else
    {

    send(socket_connect, "500 ,Syntex error,Command unrecognized", 200, 0);
        goto Started;

    }


goto Started;


    return 0;



}
