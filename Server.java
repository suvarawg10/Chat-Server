
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

// the server that can be run as a console
public class Server {
	// a unique ID for each connection
	private static int uniqueId;
	// an ArrayList to keep the list of the Client
	private ArrayList<ClientThread> al;
	// to display time
	private SimpleDateFormat sdf;
	// the port number to listen for connection
	private int port;
	// to check if server is running
	private boolean keepGoing;
	// notification
	private String notif = " *** ";
	private ArrayList<String> nameslist = new ArrayList<String>();
	private String admin="None ";
	private static String Qs=" NONE :Ask ADMIN To SET NEW Q";
	//constructor that receive the port to listen to for connection as parameter

	
	public Server(int port) {
		// the port
		this.port = port;
		// to display hh:mm:ss
		sdf = new SimpleDateFormat("HH:mm:ss");
		// an ArrayList to keep the list of the Client
		al = new ArrayList<ClientThread>();
	}
	
	public void start() {
		keepGoing = true;
		//create socket server and wait for connection requests 
		try 
		{
			// the socket used by the server
			ServerSocket serverSocket = new ServerSocket(port);

			// infinite loop to wait for connections ( till server is active )
			while(keepGoing) 
			{
				display("Server waiting for Clients on port " + port + ".");
				
				// accept connection if requested from client
				Socket socket = serverSocket.accept();
				// break if server stoped
				if(!keepGoing)
					break;
				// if client is connected, create its thread
				ClientThread t = new ClientThread(socket);
				//add this client to arraylist
				al.add(t);
				t.start();
			}
			// try to stop the server
			try {
				serverSocket.close();
				for(int i = 0; i < al.size(); ++i) {
					ClientThread tc = al.get(i);
					try {
					// close all data streams and socket
					tc.sInput.close();
					tc.sOutput.close();
					tc.socket.close();
					}
					catch(IOException ioE) {
					}
				}
			}
			catch(Exception e) {
				display("Exception closing the server and clients: " + e);
			}
		}
		catch (IOException e) {
            String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
			display(msg);
		}
	}
	
	// to stop the server
	protected void stop() {
		keepGoing = false;
		try {
			new Socket("localhost", port);
		}
		catch(Exception e) {
		}
	}
	
	// Display an event to the console
	private void display(String msg) {
		String time = sdf.format(new Date()) + " " + msg;
		System.out.println(time);
	}
	
	// to broadcast a message to all Clients
	private synchronized int broadcast(String message) {
		// add timestamp to the message
		String time = sdf.format(new Date());
		
		// to check if message is private i.e. client to client message
		String[] w = message.split(" ",3);
		//System.out.println(w[0]+w[1]+w[2]+admin);
		boolean isPrivate = false;
		boolean setQ=false;
		if(w[1].charAt(0)=='@') 
		{isPrivate=true;}
		if(w[1].equals("SETQ") )
		{ 	
			
			if(w[0].equals(admin+":")){setQ=true;}
			else {return -1;}
		}
		if(setQ == true)
		{
			message=w[2];
			Qs = message +"Since "+time ;
			
		}
		// if private message, send message to mentioned username only
		if(isPrivate==true)
		{
			String tocheck=w[1].substring(1, w[1].length());
			message=w[0]+w[2];
			String messageLf = time + " " + message + "\n";
			boolean found=false;
			// we loop in reverse order to find the mentioned username
			for(int y=al.size()-1;y>=0;y=y-1)
			{
				ClientThread ct1=al.get(y);
				String check=ct1.getUsername();
				//System.out.println(y+" "+check+" "+tocheck);
				if(check.equals(tocheck))
				{
					// try to write to the Client if it fails remove it from the list
					if(!ct1.writeMsg(messageLf)) 
					{
						display("Disconnected Client " + ct1.username + " removed from list.");
						al.remove(y);
						nameslist.remove(ct1.username);
						Collections.sort(nameslist);
						if (ct1.username==admin)
							{
							 try{admin=nameslist.get(0);} catch(Exception e){ admin="NONE";display("ZERO ONLINE");}
							int s= broadcast(notif + ct1.username  + " has left the chat room.  NEW ADMIN:"+ admin+ notif);
							Qs="NONE :Ask ADMIN To SET NEW Q";
							}
						
						
					}
					
					else{	found=true;
					break;}
				}
					// username found and delivered the message
					
			}		
			// mentioned user not found, return false
			if(found!=true)
			{
				return 0; 
			}
			
		}
		// if message is a broadcast message
		else
		{
			String messageLf="";
			if(setQ == true)
			{	messageLf = "NEW QUESTION: " + Qs+ " \n";}
			else
			{ messageLf = time + " " + message+ "\n";}
			// display message
			//System.out.print(messageLf+Qs+"  "+setQ);
			// we loop in reverse order in case we would have to remove a Client
			// because it has disconnected
			for(int i = al.size()-1; i>= 0;i=i-1) 
				{
					ClientThread ct = al.get(i);
					// try to write to the Client if it fails remove it from the list
					if(!ct.writeMsg(messageLf)) 
					{
						al.remove(i);
						nameslist.remove(ct.username);
						Collections.sort(nameslist);
						display("Disconnected Client " + ct.username + " removed from list.");
						if (ct.username==admin){
							try{admin=nameslist.get(0);} catch(Exception e){ admin="NONE";display("ZERO ONLINE");}
							int so=broadcast(notif + ct.username  + " has left the chat room. NEW ADMIN:"+ admin+ notif);
							Qs="NONE :Ask ADMIN To SET NEW Q";
							}
					}
				}
		}
		return 1;
		
		
	}

	// if client sent LOGOUT message to exit
	synchronized void remove(int id) {
		
		String disconnectedClient = "";
		// scan the array list until we found the Id
		for(int i = 0; i < al.size(); ++i) {
			ClientThread ct = al.get(i);
			// if found remove it
			if(ct.id == id) {
				disconnectedClient = ct.getUsername();
				al.remove(i);
				display("Disconnected Client " + ct.username + " removed from list.");
				nameslist.remove(disconnectedClient);
				Collections.sort(nameslist);
				if (disconnectedClient==admin){
					try{admin=nameslist.get(0);} catch(Exception e){ admin="NONE";display("ZERO ONLINE");}
					int soo =broadcast(notif + ct.username  + " has left the chat room,  NEW ADMIN:"+ admin+ notif);
					Qs="NONE :Ask ADMIN To SET NEW Q";
				}
				
				break;
			}
		}

		
	}
	
	/*
	 *  To run as a console application
	 * > java Server
	 * > java Server portNumber
	 * If the port number is not specified 1500 is used
	 */ 
	public static void main(String[] args) {
		// start server on port 1500 unless a PortNumber is specified 
		int portNumber = 1500;
		switch(args.length) {
			case 1:
				try {
					portNumber = Integer.parseInt(args[0]);
				}
				catch(Exception e) {
					System.out.println("Invalid port number.");
					System.out.println("Usage is: > java Server [portNumber]");
					return;
				}
			case 0:
				break;
			default:
				System.out.println("Usage is: > java Server [portNumber]");
				return;
				
		}
		// create a server object and start it
		Server server = new Server(portNumber);
		server.start();
	}

	// One instance of this thread will run for each client
	class ClientThread extends Thread {
		// the socket to get messages from client
		Socket socket;
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		// my unique id (easier for deconnection)
		int id;
		// the Username of the Client
		String username;
		// message object to recieve message and its type
		ChatMessage cm;
		// timestamp
		String date;

		// Constructor
		ClientThread(Socket socket) {
			// a unique id
			id = ++uniqueId;
			this.socket = socket;
			//Creating both Data Stream
			System.out.println("Thread trying to create Object Input/Output Streams");
			try
			{
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput  = new ObjectInputStream(socket.getInputStream());
				// read the username
				username = (String) sInput.readObject();
				int ss=broadcast(notif + username + " has joined the chat room." + notif);
				nameslist.add(username);
				Collections.sort(nameslist);
				admin=nameslist.get(0);
				System.out.println("\n"+nameslist+"ADMIN:  "+admin+"\n");
			}
			catch (IOException e) {
				display("Exception creating new Input/output Streams: " + e);
				return;
			}
			catch (ClassNotFoundException e) {
			}
            date = new Date().toString() + "\n";
		}
		
		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		// infinite loop to read and forward message
		public void run() 
		{
			// to loop until LOGOUT
			boolean keepGoing = true;
			while(keepGoing) 
			{
				// read a String (which is an object)
				try {
					cm = (ChatMessage) sInput.readObject();
				}
				catch (IOException e) {
					display(username + " Exception reading Streams: " + e);
					break;				
				}
				catch(ClassNotFoundException e2) {
					break;
				}
				// get the message from the ChatMessage object received
				String message = cm.getMessage();

				// different actions based on type message
				switch(cm.getType())

				{

					case ChatMessage.MESSAGE:
						int confirmation =  broadcast(username + ": " + message);
						if(confirmation== -1){
						String ms = "You cannot set new question" ;
						writeMsg(ms);
						}
						if(confirmation== 0){
						String msg = notif + "Sorry. No such user exists." + notif;
						writeMsg(msg);
						}
					break;
					case ChatMessage.LOGOUT:
						try{admin=nameslist.get(0);} catch(Exception e){ admin="NONE";display("ZERO ONLINE");}
						display(username + " disconnected with a LOGOUT message.");
						keepGoing = false;
					break;
					case ChatMessage.WHOISIN:
						writeMsg("List of the users connected at " + sdf.format(new Date()) + "\n");
					// send list of active clients
					for(int i = 0; i < al.size(); ++i) {
						ClientThread ct = al.get(i);
						writeMsg((i+1) + ") " + ct.username + " since " + ct.date);
					}
					break;
					case ChatMessage.Q: 
						
						writeMsg("CURRENT QUESTION = "+Qs +" By ADMIN = "+admin);
						
					break;
					case ChatMessage.CHECKADMIN:
						writeMsg("CURRENT ADMIN = "+admin);
					break;
				
				}
			}
			// if out of the loop then disconnected and remove from client list
			remove(id);
			close();
		}
		
		// close everything
		private void close() {
			try {
				if(sOutput != null) sOutput.close();
			}
			catch(Exception e) {}
			try {
				if(sInput != null) sInput.close();
			}
			catch(Exception e) {};
			try {
				if(socket != null) socket.close();
			}
			catch (Exception e) {}
		}

		// write a String to the Client output stream
		private boolean writeMsg(String msg) {
			// if Client is still connected send the message to it
			if(!socket.isConnected()) {
				close();
				return false;
			}
			// write the message to the stream
			try {
				sOutput.writeObject(msg);
			}
			// if an error occurs, do not abort just inform the user
			catch(IOException e) {
				display(notif + "Error sending message to " + username + notif);
				display(e.toString());
			}
			return true;
		}
	}
}

