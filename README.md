The Quiz game can be implemented in different possible ways.
A) A quiz Group in which only admin can ask Question and all can Answer(CREATOR)
B) A Quiz Group in which all can Ask Question
C) A Quiz Group in which only higher Authority Can Asked Question
In the Third Implementation method we will need to use an election algorithm to
decide Current Higher Authority.
It is a multithreaded client/server ChatServer based on a console which uses Java Socket
programming. A server listens for connection requests from clients across the network or
even from the same machine. Clients know how to connect to the server via an IP address and
port number. After connecting to the server, the client gets to choose his/her username on the
chat room. The client sends a message, the message is sent to the server using
ObjectOutputStream in java. After receiving the message from the client, the server
broadcasts the message if it is not a private message. And if it is a private message which is
detected using ‘@’ followed by a valid username, then send the message only to that user.
Java object serialization to transfer the messages.
Features:
**Client**
To start the Client in console mode use one of the following command
 1. java Client
 2. java Client username
 3. java Client username portNumber
 4. java Client username portNumber serverAddress
at the console prompt
If the portNumber is not specified 1500 is used
If the serverAddress is not specified "localHost" is used
If the username is not specified "Anonymous" is used
**Server**
To run as a console application
 1. java Server
 2. java Server portNumber
If the port number is not specified 1500 is used
**Chat Instructions :**
While in client console:
1. Priority will be decided based on chronological order of member name and only Higher
one can ask Question.
2. Type 'CHECKADMIN' to find Admin username.
3. Type 'Q' to find Admin Question
4. Admin must Type 'SETQ<space>YourQuestion to set question
5. Simply type the message to send broadcast to all active clients
6. Type '@username<space>yourmessage' without quotes to send a message to desired client
7.Type 'WHOISIN' without quotes to see list of active clients.
8. Type 'LOGOUT' without quotes to log off from the server.
