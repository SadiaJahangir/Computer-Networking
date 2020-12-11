//#include<iostream>
#include <stdio.h>
#include <sys/socket.h>
#include <stdlib.h>
#include <netinet/in.h>
#include <string.h>
#include <unistd.h>
#include<time.h>
#include <sys/socket.h>

#include <sys/types.h>

#include <netinet/in.h>

#include <netdb.h>

#include <stdio.h>

#include <string.h>

#include <stdlib.h>

#include <unistd.h>

#include <errno.h>
//using namespace std;

int file_exists(const char * filename){
    /* try to open file to read */
    FILE *file;
    if (file = fopen(filename, "r")){
        fclose(file);
        return 1;
    }
    return 0;
}


int main(int argc, char* argv[])
{
      char mail_user[300];

    if(argc!=4)
    {
        printf("Incorrect number of arguments\n");
        exit(0);

    }
    /// check whether the given input file name exists or not .
    int z=file_exists(argv[3]);
if(z==0)
{
    printf("File name doesn't exist!\n");
    return 0;

}

    char username[90],a[122],host[122],user[100],port_string[10];

    int ok=0,ok2=0,val=0,j=0,k=0,q=0;

    /// jokhn colon pabo tkhn ok2 er value 0 thakbe na
    /// jkhn ok=1 tar mne ami @ peye gechi ekhn @ er ag pojnto holo user name ar @ er por theke colon er ag porjnto host name ( ok2=0)
    strcpy(a,argv[1]);

    int l=strlen(argv[1]);
    for(int i=0; i<l; i++)
    {
        if(a[i]==':')
        {
            ok2=i;
            break;
        }
        if(ok==1)
        {
            host[j]=a[i];
            j++;
        }
        if(a[i]=='@')
        {
            ok=1;
            val=i ;
        }
        if(ok!=1) user[k++]=a[i];
        username[i]=a[i];

    }
    host[j]='\0';
    username[ok2]='\0';


    if(ok2==0 || username[0]=='@' || ok==0)
    {

        printf("Invalid input. Please give the first input in this format - user@host:port  \n");
        exit(0);
    }

    //printf("HOST name : %s \n",host);

    /// host name validity check
    struct hostent *checkhost;
    checkhost=gethostbyname(host);

    if(checkhost==NULL)
    {
        printf("Error host name");
        exit(0);

    }


    /// port number

    int portnumber=0;
    char *token=strtok(a,":");
    while(token!=NULL)
    {
        //  cout<<token<<endl;
        portnumber=atoi(token);

        token=strtok(NULL,":");
    }

    //  cout<<portnumber<<endl;


    struct sockaddr_in serv_addr;
    struct hostent *server;
    int sockfd;
    ///socket creation & verification
    sockfd=socket(AF_INET,SOCK_STREAM,0);
    if(sockfd<0)
    {
        printf("Socket creation failed\n");

    }
    else
        printf("Socket successfully created\n");
    bzero((char *) &serv_addr, sizeof(serv_addr));


    bzero(&serv_addr, sizeof(serv_addr));

    // assign IP, PORT
    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = inet_addr("127.0.0.1");
    serv_addr.sin_port = htons(portnumber);

    // connect the client socket to server socket
    if (connect(sockfd, (struct sockaddr*)&serv_addr, sizeof(serv_addr)) != 0)
    {
        printf("connection with the server failed...\n");
        exit(0);
    }
    else
        printf("connected to the server..\n");


    char buffer[256];
    int valread= read(sockfd,buffer,255);
    if(valread<0)
    {
        error("ERROR reading from socket");
    }

/// HELO request
    char input[1009];
    gets(input);
    write(sockfd, input, sizeof(input));
    bzero(buffer,0);
    valread= read(sockfd,buffer,255);
    printf("%s\n",buffer);
    if(buffer[0]=='5')
    {
     return 0;
    }

    /// mail _ from
     /// give an email after MAIL FROM . I have considered that the user will give valid email format input.


    char mail_from[300];
    gets(mail_from);

     l=strlen(mail_from);
    j=0;
    for(int i=10;i<l;i++){

        if(mail_from[i]!=' ') mail_user[j++]=mail_from[i];

    }
    mail_user[j]='\0';

    //printf("USER = %s\n",mail_user);
    char mail[100];
    strcpy(mail,mail_user);

    char host_name[1000];
    gethostname(host_name,sizeof (host_name));
    strcat(mail_from," , host name of the linux environment is : ");
    strcat(mail_from,host_name);


    send(sockfd, mail_from, 300, 0);

     bzero(buffer, 1024);
    valread = read(sockfd, buffer, 100);
    if(buffer[0]=='2')
    {

     printf("%s\n",buffer);
    }
    else
    {
        printf("%s\n",buffer);
       return 0;
    }

    /// RCPT TO
    /// give the input in this way: RCPT TO user@gmail.com ( just same as you gave input at first)
    char rcpt_to[300];
    gets(rcpt_to);

    char rcpt_user[300];

    l=strlen(rcpt_to);
    j=0;
    for(int i=8;i<l;i++)
    {
        if(rcpt_to[i]!=' '){ rcpt_user[j++]=rcpt_to[i]; }
    }

    rcpt_user[j]='\0' ;
    //printf("RCPT : %s\n",rcpt_user);

    send(sockfd,rcpt_to,300,0);

     bzero(buffer, 1024);
    valread = read(sockfd, buffer, 100);
    if(buffer[0]=='2')
    {

     printf("%s\n",buffer);
    }
    else
    {
        printf("%s\n",buffer);
       return 0;
    }

    ///  DATA REQUEST /////////////////////////
  char data[300];
    gets(data);

    send(sockfd,data,300,0);

     bzero(buffer, 1024);
    valread = read(sockfd, buffer, 100);
    if(buffer[0]=='2')
    {

     printf("%s\n",buffer);
    }
    else
    {
        printf("%s\n",buffer);
      return 0;
    }


printf("Sending the MAIL ....\n");

/// time of sending the mail
char date[30];
    time_t now = time(NULL);
    struct tm *t = localtime(&now);


strftime(date, sizeof(date)-1, "DATE %d-%m-%Y %H:%M", t);
/// header part

 // printf("C: %s\r\n",date);
char header[2000];
    strcat(header,"To: ");
    strcat(header,rcpt_user);
    //printf("%s\n",rcpt_user);
    strcat(header,"\n");
    strcat(header,"From: ");
    strcat(header,mail);
   // printf("%s\n",mail_user);
    strcat(header,"\n");
    strcat(header,"Subject: ");
    strcat(header,argv[2]);
    strcat(header,"\n");
    strcat(header,date);
    strcat(header,"\n");
       strcat(header,"\n");

       /// read line by line from file
FILE* fp;


fp = fopen(argv[3], "r");

while(fgets(buffer, 255, (FILE*) fp)) {
//if(buffer[0]=='.') break;
strcat(header,buffer);
    //printf("%s\n", buffer);


}


fclose(fp);


//printf("%s\n",header);

  send(sockfd,header,2000,0);

/// . INPUT
   gets(buffer);

    send(sockfd, buffer, 300, 0);

/// QUIT INPUT
    gets(buffer);
    send(sockfd, buffer, 300, 0);

    valread = read(sockfd, buffer, 100);
    printf("STATUS: %s  ,CONNECTION CLOSED\n",buffer);

    if(buffer[0] != '2')
    {
        return 0;
    }


     return 0;


}
