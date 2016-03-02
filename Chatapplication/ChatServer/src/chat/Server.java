package chat;
import java.awt.Component;
import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JOptionPane;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.*;

import javax.net.ssl.*;
import com.sun.net.ssl.*;
import com.sun.net.ssl.internal.ssl.Provider;


/*
 * The Server class is the backbone of the chat room.
 * Multiple clients will connect to the server and the 
 * connections will be maintained on individual threads
 */

public class Server 
{
	/* To create unique ID's for each connection */
	private int max_Connection = 0;
	private static int _connId;
	/* List of clients connected */
	private ArrayList<ClientThread> clients;
	/* GUI mode flag */
	private ServerGUI sgui;
	/* To display current time */
	private SimpleDateFormat sdf;
	/* Port number for server to listen */
	private int port;
	/* To stop the server */
	private boolean keepOn;
	/* Backup to file*/
	private static File file = new File("backup.txt");
	private static File file2 = new File("cliconn.txt");
	private static File file3 = new File("userpass.txt");
	private FileWriter fw, fw2, fw3;

	ObjectInputStream ois;
	ObjectOutputStream oos;

	/* Constructor that sets the value of port and GUI object*/
	public Server(int port, ServerGUI sgui)
	{	
		this.sgui = sgui;
		this.port = port;
		sdf = new SimpleDateFormat("HH:mm:ss");
		clients = new ArrayList<ClientThread>();
	}
	
	/* 
	 * Starts the server 
	 */
	public void wakeup() throws SQLException
	{
		keepOn = true;
		try
		{
			if(!file.exists()) 
				file.createNewFile();
			if(!file2.exists())
				file2.createNewFile();
			if(!file3.exists())
				file3.createNewFile();
		}
		catch(Exception e)
		{
			show("Exception "+e);
		}
		
		try
		{
			/* Create the main server socket */
			//ServerSocket servSoc = new ServerSocket(port);
			SSLServerSocket sslServerSocket = null;
			Security.addProvider(new Provider());
			System.setProperty("javax.net.ssl.keyStore","testkeystore.ks");
			System.setProperty("javax.net.ssl.keyStorePassword","cloud123");
			
			SSLServerSocketFactory sslServerSocketfactory = (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
			sslServerSocket = (SSLServerSocket)sslServerSocketfactory.createServerSocket(port);
			while(keepOn)
			{	
				show("Keep on " + keepOn);
				show("Waiting for clients on port "+ port +".");
				/* Accept incoming connection request from client */
			//	Socket connSoc = servSoc.accept();
				
				
				SSLSocket sslSocket = (SSLSocket)sslServerSocket.accept();

				/* Stop if Server is to be stopped */
				
				
				ClientThread clientThread = null;
				/* Set a new thread for the new connection */
				try{
				if(max_Connection >= 5){
					System.out.println(max_Connection);
					JOptionPane.showConfirmDialog((Component) null, "Maximum connection reached !",
					        "alert", JOptionPane.OK_CANCEL_OPTION);
					clientThread = new ClientThread(sslSocket);
					clients.add(clientThread);
					clientThread.start();
					clientThread.sInput.close();
					clientThread.sOutput.close();
					clientThread.connSoc.close();
					clients.remove(clientThread);
				
					
				}
				else{	
					clientThread = new ClientThread(sslSocket);
					clients.add(clientThread);
					clientThread.start();
					
					max_Connection++;
					
				
				}
				
				
				}
				catch(Exception e){
					int result = JOptionPane.showConfirmDialog((Component) null, "Username already existing",
					        "alert", JOptionPane.OK_CANCEL_OPTION);
				//	clientThread.writeMsg("Username already existing !!");
					new ClientGUI("localhost", 1500);
					clients.remove(clientThread);
					System.out.println(e.getMessage());
					//keepOn = true;
					
					
				}
				/* Add the connection to the client list */
				if(!keepOn)
					break;
			}

			/* Stop the server*/
			try
			{
				//servSoc.close();
				sslServerSocket.close();
				
				/* Close all connections to the clients */
				for(int i=0; i< clients.size(); ++i) 
				{
					ClientThread clientThread = clients.get(i);
					try
					{
						clientThread.sInput.close();
						clientThread.sOutput.close();
						clientThread.connSoc.close();
					}
					catch(IOException e)
					{
						/* Do not handle */
					}
				}
			}
			catch(Exception e)
			{
				show("Unable to close the clients ");
			}
			
		}
		catch(IOException e)
		{
			show("Exception on new ServerSocket ");
		}
	}

	/* 
	 * Stops the server 
	 */
	@SuppressWarnings("resource")
	protected void stop()
	{
		keepOn = false;
		/* Try connecting to self as a Client */
		try {
			//new Socket("localhost", port);
		//	new SSLServerSocket("localhost", port);
		}
		catch(Exception e)
		{
			/* Do nothing */
		}
		file.delete();
	}

	/* 
	 * Display an event on the console 
	 * Input
	 * 		-Message to be displayed
	 */
	private void show(String msg)
	{
		String timeMes = sdf.format(new Date()) + " " + msg;
		/* Check if GUI is enabled else output on console */
		sgui.appendEvent(timeMes + "\n");
	}

	/* 
	 * Broadcast a message to all clients
	 * Synchronized is used to make the method thread synchronous. So that
	 * multiple client threads can use the method safely 
	 */
	private synchronized void broadcast(String message)
	{
		String time = sdf.format(new Date());
		String messTime = time + " " + message + "\n";
		String orgMsg = messTime;
		BufferedWriter bw;
		PrintWriter pw;
		byte[] encrypted = null ;
		String enc = null;
		ChatObject obj = new ChatObject(ChatObject.ENC);
		
		sgui.appendRoom(messTime, ChatObject.MESSAGE);
		try{
			/* Backup data to a file */
			fw = new FileWriter(file.getAbsoluteFile(),true);
			if(!messTime.contains("just joined")){
			bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);
			
			pw.print(messTime);
			
			pw.close();
			}
			}
		catch(Exception e)
		{
			System.out.println("Unable to backup data\n");
		}
		
		/* Remove client if write fails and client has disconnected */
		for(int i=clients.size(); --i>=0;)
		{	
			ClientThread clientThread = clients.get(i);
			String user = clientThread.username;
			String pass = clientThread.pass;
			System.out.println(user + pass);
			String secret = "mysecret";
			try{
			byte[] key = (user + pass + secret).getBytes("UTF-8");
			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			key = sha.digest(key);
			key = Arrays.copyOf(key, 16);
			
			SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
			
		    // Instantiate the cipher
		    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		    cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
		    System.out.println(messTime);
		    encrypted = cipher.doFinal((messTime.getBytes()));
		    enc = new String(encrypted);
			//messTime = encrypted.toString();
		    secretKeySpec = new SecretKeySpec(key, "AES");
		  //  byte[] orgi = messTime.getBytes();
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
			byte[] decrypted = cipher.doFinal(encrypted);
			String dec = new String(decrypted);
			System.out.println("De msg" + dec);
		    obj = new ChatObject(ChatObject.ENC);
		    obj.setEncMsg(encrypted);
			
			
			}
			catch(Exception e){
				e.printStackTrace();
			}
			if(!clientThread.blocked)
				if(!clientThread.writeMsg(obj))
				{
					clients.remove(i);
					show("Disconnected client "+ clientThread.username + " removed from list.");
				}			
		}
	}

	/*
	 *  If a client logs off using LOGOUT message 
	 */
	synchronized void remove(int id) 
	{
		for(int i=0; i<clients.size(); ++i)
		{
			ClientThread clientThread = clients.get(i);
			if(clientThread.id == id)
			{
				clients.remove(i);
				return;
			}
		}
	}
	
	synchronized private void fetch(String target, String username) {
		int flag=0;
		String mess="Previous users connected at ";
		String ip;
		for(int i=clients.size(); --i>=0;)
		{
			ClientThread clientThread = clients.get(i);
			if(clientThread.username.equalsIgnoreCase(target))
			{
				if(!clientThread.blocked)
				{
					ip = clientThread.ip;
					mess = mess.concat(ip+" are:\n");
					BufferedReader br = null;
					try {
						br = new BufferedReader(new FileReader(file2.getAbsoluteFile()));
					} catch (FileNotFoundException e1) {}
					String current, ipsrc, usersrc;
					try {
						while((current=br.readLine())!=null)
						{
							ipsrc = current.substring(0, current.indexOf('@'));
							usersrc = current.substring(current.indexOf('@')+1);

							if(ip.equals(ipsrc))
							{
								mess = mess.concat(usersrc+"\n");
							}
						}
					} catch (IOException e) {
					}
					finally {
						try {
							br.close();
						} catch (IOException e) {
						}
					}
					flag=1;
					break;
				}
			}
			
		}
		if(flag==0)
		{
			mess = "No such user\n";
		}

		for(int i=clients.size(); --i>=0;)
		{
			ClientThread clientThread = clients.get(i);
			if(clientThread.username.equalsIgnoreCase(username))
			{
				if(!clientThread.blocked)
					if(!clientThread.writeMsg(mess))
					{
						clients.remove(i);
						show("Disconnected client "+ clientThread.username + " removed from list.");
					}		
			}
		}
	}

	/*synchronized void adder(String username, String ip)
	{
		String time = sdf.format(new Date());
		try{
			//fw3 = new FileWriter(file3.getAbsoluteFile(),true);
			//BufferedWriter bw = new BufferedWriter(fw3);
			//PrintWriter pw = new PrintWriter(bw);
			pw.print(ip+'@'+time+" "+username+"\n");
			pw.close();
		}
		catch(IOException f)
		{
		}
	}
	*/
	/*
	 * Inner class for connecting to the clients
	 */
	class ClientThread extends Thread 
	{
		/* Socket for communication */
		SSLSocket connSoc;
		// SSLSocket sslSocket;
		//SSLServerSocketFactory sslServerSocketfactory;
		//SSLServerSocket sslServerSocket;
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		/* Connection ID */
		int id;
		/* Client username */
		String username, password;
		
		String user = "";
		String pass = "";
		/* Chat message */
		ChatObject chatObject;
		/* Date */
		String date;
		BufferedReader br;
		String ip;
		boolean blocked;
		boolean invalid = false;
		int type;
		
		/* Constructor */
		public ClientThread(SSLSocket connSoc) throws Exception {
			
			
			id = ++_connId;
			blocked = false;
			String userpass;
			this.connSoc = connSoc;
			ip = connSoc.getInetAddress().getHostAddress();
			
			/* Creation of i/o datastreams */
			try
			{
				
				BufferedReader br3;
			
				fw3 = new FileWriter(file3.getAbsoluteFile(),true);
				BufferedWriter bw3 = new BufferedWriter(fw3);
				br3 = new BufferedReader(new FileReader(file3.getAbsoluteFile()));
				PrintWriter pw2  = new PrintWriter(bw3);
				sInput = new ObjectInputStream(connSoc.getInputStream());
				sOutput = new ObjectOutputStream(connSoc.getOutputStream());
				/* fetch the username */
				// userpass has username and password combined as object
				ChatObject ob =  (ChatObject)sInput.readObject();
				username = ob.getUsername();
				// receiving encrpted password for communication
				show("enc pass is " + ob.getEncPass().toString());
				//password = ob.getPassword();	
				ob.decrpt();
				password = ob.pass;
				show("Decr Pass is " + password);
				type = ob.getType();
				String a = ob.pass;
				System.out.println("Printing pass " + a);
				System.out.println("Type "+ type);
				if(type == ChatObject.SIGNUP){
					Connectiondb con1 = new Connectiondb();
					
					Connection con2 = con1.getCon();
					
					PreparedStatement pstmt = con2.prepareStatement("INSERT INTO `register`(uname,pass) VALUES (?, ?)");
					pstmt.setString(1, username);
					pstmt.setString(2, password);
					// Statement stmt =  con1.getStmt();
					int i =pstmt.executeUpdate();
					pw2.print(username + "@" + password + "\n");
					pw2.close();
					this.writeMsg("Successfully Sign up\n");

				}
				//userpass = (String) sInput.readObject();
				//pw2.println(userpass);
				//username = userpass.substring(0, userpass.indexOf('@'));
				//password = userpass.substring(userpass.indexOf('@')+1);
				
				String current2 = "";
				
				try {
					while((current2=br3.readLine())!=null)
					{	show(current2);
						if(current2.equals(""))
							break;
						user = current2.substring(0, current2.indexOf('@'));
						pass = current2.substring(current2.indexOf('@')+1);
						
						if(username.equals(user))
						{
							invalid = true;
							break;
							
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

				
				
				//show(username + " "+ password + " just joined the chat room.");
				//this.writeMsg("Server assigned you the alias: $"+username+"*\n");
				
			}

			catch(IOException e)
			{
				show("Unable to create new I/O streams");
				return;
			} 
			catch (ClassNotFoundException e) {
			}
			date = new Date().toString() + "\n";
			//adder(username, ip);
		}
		
		/* runner */
		public void run() {
			boolean keepOn = true;
			int counter=0;
			long time = 0;
			if(!invalid){
				//broadcast(username+" left the chatroom*");
				this.writeMsg("Incorrect Username or password\n");
				keepOn = false;
				
				show(" Keep on = false ");
			}
			else{
				broadcast(username + " just joined the chat room.*");
				BufferedReader br;
				try {
					br = new BufferedReader(new FileReader(file.getAbsoluteFile()));
				
				
				String current = "";
				while((current = br.readLine()) != null)
				{
					this.writeMsg(current+"\n");
				}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			// checking signup option
			if(type == 7){
				//broadcast(username+" left the chatroom*");
				this.writeMsg("successfully signup");
				keepOn = false;
				show(" Keep on = false ");
			}
		
			while(keepOn)
			{		
				/* Read input as object */
				try {
					chatObject = (ChatObject) sInput.readObject();
				} 
				catch (IOException e) {
					//show(username+" left the chatroom");
					broadcast(username+" left the chatroom*");
					break;
				}
				catch (ClassNotFoundException e)
				{
					e.printStackTrace();
					break;
				}
				
				if(!blocked)
				{
					/* Message handling */
					//String message = chatObject.getMessage();
					String originalMsg = "";
					byte[] encpted;
					String secret = "mysecret";
					try {
					encpted = chatObject.getEncMsg();
					show(pass);
					show(user);
					show(encpted.toString());
					byte[] key = (username + pass + secret).getBytes("UTF-8");
					MessageDigest sha = MessageDigest.getInstance("SHA-1");
					key = sha.digest(key);
					key = Arrays.copyOf(key, 16);
					Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
					SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
					cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
				    byte[] original = cipher.doFinal(encpted);
				    originalMsg = new String(original);
					
					
					
					} catch (Exception e1) {
					
						e1.printStackTrace();
					}
					if(counter == 0)
					{
						time = System.currentTimeMillis();
					}
					counter++;
					if(counter==5)
					{
						time = System.currentTimeMillis()-time;
						if(time<2000)
						{
							this.writeMsg("Please slow down! You have been blocked for 10 sec due to spamming\n");
							this.blocked = true;
							//message = ""; 
							try {
								sleep(10000);
							} catch (InterruptedException e) {
								show("Exception in wait");
							}
							try {
								sInput.reset();
							} catch (IOException e) {
							}
							this.blocked = false;
							this.writeMsg("You have been unblocked\n");
						}
						counter=0;
					}
					
					
					switch (chatObject.getType()) {
					case ChatObject.MESSAGE:
						// broadcast(username + ", " + encpted);
						broadcast(username + ", " + originalMsg);
						break;
						
					case ChatObject.LOGOUT:
						show(username + " disconnected with a LOGOUT message.");
						broadcast(username+" left the chatroom*");
						keepOn = false;
						break;
						
										}
				}
			}
			remove(id);
			close();
		}

		
		/*
		 * Writes a message to the output stream
		 */
		private boolean writeMsg(String msg) {
			/* if Client is still connected send the message to it */
			if(!connSoc.isConnected()) {
				close();
				return false;
			}
			/* write the message to the stream */
			try {
					sOutput.writeObject(msg);
			}
			/* if an error occurs, do not abort just inform the user */
			catch(IOException e) {
				show("Error sending message to " + username);
			}
			return true;
		}
		
		private boolean writeMsg(ChatObject msg) {
			/* if Client is still connected send the message to it */
			if(!connSoc.isConnected()) {
				close();
				return false;
			}
			/* write the message to the stream */
			try {   System.out.println("inside object write");
					sOutput.writeObject(msg);
			}
			/* if an error occurs, do not abort just inform the user */
			catch(IOException e) {
				show("Error sending message to " + username);
			}
			return true;
		}

		private boolean writeMsg(byte[] msg) {
			/* if Client is still connected send the message to it */
			if(!connSoc.isConnected()) {
				close();
				return false;
			}
			/* write the message to the stream */
			try {
					sOutput.writeObject(msg);
			}
			/* if an error occurs, do not abort just inform the user */
			catch(IOException e) {
				show("Error sending message to " + username);
			}
			return true;
		}
		/*
		 * Close all the streams and socket connection
		 */
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
				if(connSoc != null) connSoc.close();
			}
			catch (Exception e) {}
		}
	}
		
}