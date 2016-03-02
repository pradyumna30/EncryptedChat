package chat;

import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Arrays;
import java.awt.Component;
import java.awt.GridLayout;
import java.io.*;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
// for jssl 
import javax.net.ssl.*;
import com.sun.net.ssl.*;
import com.sun.net.ssl.internal.ssl.Provider;



public class Client{
		
		/* Input Output */ 
		public ObjectInputStream sIn;     		
		/* To read from the Socket */
		public ObjectOutputStream sOut, sOut2;			
		/* To write into socket */
		private Socket socket;
		private SSLSocket sslSocket;
		private ClientGUI cGUI;     			
		/* It is used for Graphical user interface*/
		public String server,username,password;
		private int port;
		public volatile boolean awayFlag;
		private String userpass;
		private int sign = 0;
		
		/* calls the common constructor with the GUI set to null */

		Client(String server,int port, String username, ClientGUI cg, String password, int signup){
			this.server=server;
			this.port=port;
			this.username=username;
			cGUI = cg;
			this.password = password;
			awayFlag  = false;
			this.sign = signup;
		}
		Client(String server,int port, String username, ClientGUI cg, String password){
			this.server=server;
			this.port=port;
			this.username=username;
			cGUI = cg;
			this.password = password;
			awayFlag  = false;
			}
		
	
		public boolean start() throws Exception {
			
			if(sign == 1){
			try{	
				Security.addProvider(new Provider());
				System.setProperty("javax.net.ssl.trustStore","client.ks");
				// socket = new Socket(server,port);
				SSLSocketFactory sslsocketfactory = (SSLSocketFactory)SSLSocketFactory.getDefault();
				sslSocket = (SSLSocket)sslsocketfactory.createSocket(server,port);
				//System.out.println("In Start Method");
			}

			catch(Exception e){
				display("Error connection to server" );
				return false;
			}
			
			try
			{   
				/*sOut = new ObjectOutputStream(socket.getOutputStream());
				//sOut2 = new ObjectOutputStream(socket.getOutputStream());
				sIn = new ObjectInputStream(socket.getInputStream());*/
				sOut = new ObjectOutputStream(sslSocket.getOutputStream());
				//sOut2 = new ObjectOutputStream(socket.getOutputStream());
				sIn = new ObjectInputStream(sslSocket.getInputStream());
			}
			catch (IOException e) {
				display("Exception in creating new IO stream ");
				return false;
			}
		
		
			new ListenFromServer().start(); 
			
			try
			{	ChatObject obj = new ChatObject(ChatObject.SIGNUP,username, password);
				//System.out.println(obj.getPassword());
			//	userpass = username + "@" + password;
				sOut.writeObject(obj);
				//sOut2.writeObject(password);
			}
			catch(IOException e)
			{
				display("Exception doing signup ");
				disconnect();
				return false;
			}
			//	 success and informed the person 
		
			return true;
		}
		else{
	
			try{	
			// socket = new Socket(server,port);
			Security.addProvider(new Provider());
			System.setProperty("javax.net.ssl.trustStore","client.ks");
			// socket = new Socket(server,port);
			SSLSocketFactory sslsocketfactory = (SSLSocketFactory)SSLSocketFactory.getDefault();
			sslSocket = (SSLSocket)sslsocketfactory.createSocket(server,port);
			//System.out.println("In Start Method");
			//System.out.println("In Start Method");
			}

			catch(Exception e){
			display("Error connection to server" );
			return false;
			}
			//System.out.println("Above connection accepted");
			//String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
			String msg = "Connection accepted " + sslSocket.getInetAddress() + ":" + sslSocket.getPort();
			display(msg);
			//System.out.println("below connection accepted" + msg);

			/* creating data stream */

			try
			{   
			/*sOut = new ObjectOutputStream(socket.getOutputStream());
			//sOut2 = new ObjectOutputStream(socket.getOutputStream());
			sIn = new ObjectInputStream(socket.getInputStream());*/
			sOut = new ObjectOutputStream(sslSocket.getOutputStream());
			//sOut2 = new ObjectOutputStream(socket.getOutputStream());
			sIn = new ObjectInputStream(sslSocket.getInputStream());
			}
			catch (IOException e) {
				display("Exception in creating new IO stream ");
				return false;
			}
		
			/* Thread creation to listen from server */
			/* username is sent to server as string all other objects are sent as ChatObject object */	
			new ListenFromServer().start(); 
	
			try
				{	ChatObject ob2 = new ChatObject(ChatObject.LOGIN, username, password);
				
					//userpass = username + "@" + password;
					sOut.writeObject(ob2);
					//sOut2.writeObject(password);
				}
			catch(IOException e)
			{
				display("Exception doing login ");
				disconnect();
				return false;
		}
			/* success and informed the person */
			return true;
		}
		}
		
		private void display(String msg){
	
		 cGUI.append(msg + "\n",ChatObject.MESSAGE);
		/* append to message to clientGUI */
		}
		
		void sendMessage(ChatObject msg) throws Exception{
		
		String temp=null;
		String secret = "mysecret";
		byte[] key = (username + password + secret).getBytes("UTF-8");
		MessageDigest sha = MessageDigest.getInstance("SHA-1");
		key = sha.digest(key);
		key = Arrays.copyOf(key, 16);
		
		SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

	    // Instantiate the cipher
	    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
	    cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
	    
	     byte[] encrypted = cipher.doFinal((msg.getMessage()).getBytes());
	   
	     System.out.println("encrypted object string: " + msg.toString().getBytes());
		
	    /*cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
	    byte[] original = cipher.doFinal(encrypted);
	    String originalString = new String(original);*/
	    
	    
		String temp2 =null;
		
		/* sending a private message to a user */
		/* private message is preceded by #[Name of user] [Message] */
		temp = msg.getMessage();
		temp = temp.trim();
		temp = "garbage";
		msg.setMessage(temp);
		msg.setEncMsg(encrypted);
		byte[] encryptedObj = cipher.doFinal(msg.toString().getBytes());
		cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
		byte[] original = cipher.doFinal(encryptedObj);
		
		
		
		if(!temp.isEmpty() || msg.getType()==ChatObject.LOGOUT ){
		try {
				sOut.writeObject(msg);
			
		}
		catch(IOException e){
			display("Exception writing to server");
			}
		  }
		}
		/* If something goes wrong then close all the connection */

		private void disconnect() {
			
		try {
			if(sIn !=null) sIn.close();
		    }
		catch(Exception e) {} 
		/*nothing to be done when sIn has null */
		try {
			if(sOut !=null) sOut.close();
		   }
		catch(Exception e) {} 
		/* nothing to be done */
		try {
			if(socket!= null) socket.close();
			if(sslSocket!= null) sslSocket.close();
		    }
		/* nothing to be done */
		catch(Exception e) {} 
		
			if(cGUI!= null)
			cGUI.connectionFailed();
		}
		
		/* a class that waits for message from the server and append them to JTextPane */
		class ListenFromServer extends Thread {
		 @Override
		public void run() {
			while(true) {
				try {
					String originalmsg;

					//String msg = (String)sIn.readObject();
					//ChatObject obj = (Chatobject)sIn.readObject().g
				//	System.out.println("object type : " + sIn.readObject());
					Object ob = sIn.readObject();
					if(String.valueOf(ob).contains("ChatObject")){
						ChatObject obj = (ChatObject)ob;
						String secret2 = "mysecret";
						byte[] key2 = (username + password + secret2).getBytes("UTF-8");
						MessageDigest sha2 = MessageDigest.getInstance("SHA-1");
						key2 = sha2.digest(key2);
						key2 = Arrays.copyOf(key2, 16);
						Cipher cipher2 = Cipher.getInstance("AES/ECB/PKCS5Padding");
						SecretKeySpec secretKeySpec2 = new SecretKeySpec(key2, "AES");
						cipher2.init(Cipher.DECRYPT_MODE, secretKeySpec2);
						byte[] enc = obj.getEncMsg();
						byte[] original2 = cipher2.doFinal(enc);
						originalmsg = new String(original2);
					}
					else{
						
						originalmsg = (String)ob;
						if(originalmsg.contains("Incorrect Username or password")){
							JOptionPane.showConfirmDialog((Component) null, "Invalid User Name or Password !!",
							        "alert", JOptionPane.OK_CANCEL_OPTION);
							
							new ClientGUI("localhost", 1500);
							cGUI.dispose();
							
						}
						
							
							
					}
					
					
					
				    
				 
					if(awayFlag==true)
					{
						while(awayFlag==true)
						{
							try {
								sleep(2000);
							} catch (InterruptedException e) {
							}
						}
					}
					
		
				/* checking for a normal chatroom message */	
				cGUI.append(originalmsg,ChatObject.MESSAGE);
				}
				
		catch(Exception e) {
		display("Server has closed the connection ");
		e.getStackTrace();
		if(cGUI !=null)
			cGUI.connectionFailed();
		break;
		}
		/* can't happen with a string object but need the catch any how */
		
	   }
	  }
	}
}