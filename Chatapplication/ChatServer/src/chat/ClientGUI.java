package chat;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;


import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/* client is able to run on Graphical interface using console */
/*
* 	 
*/

public class ClientGUI extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	/* will first hold username , later on enter message */
	JLabel label, label2;	
	/* to hold the username latter on the message */
	private JTextField tf;
	JPasswordField tf2;
	/* to hold server address and port number */
	private JTextField tfServer, tfPort;
	/* to logout or to get the list of user */
	private JButton login , logout, away, block, unblock;
	/* for chat room */
	private JTextPane chatrum;
	/* it is for connection */
	private boolean connected;
	/* client object */
	private Client client;
	/* defaut port number */
	private int defaultPort;
	private String defaultHost;
	private StyledDocument doc;
	private SimpleAttributeSet aset, aset2;
	JPanel moodPanel, southPanel, northPanel;
	private JButton signup;
	
	// constructor connection receiving socket number
	
	ClientGUI(String host,int port){
		
		
		super("Secure - Client");
		System.out.println("Enter user name Password ");
		defaultPort = port;
		defaultHost=host;
		/* The north panel */
		
		northPanel = new JPanel(new GridLayout(9,1));
		/* the server name and port number */
		JPanel serverAndPort = new JPanel(new GridLayout(1,5,1,3));
		/* the two JTextField with default value for server address and port number */
		tfServer = new JTextField(host);
		tfServer.setEditable(true);
		tfPort = new JTextField("" + port);
		tfPort.setEditable(true);
		tfPort.setHorizontalAlignment(SwingConstants.RIGHT);
		
		serverAndPort.add(new JLabel("Server Address :     "));
		serverAndPort.add(tfServer);
		serverAndPort.add(new JLabel("Port Number:  "));
		serverAndPort.add(tfPort);
		serverAndPort.add(new JLabel(""));
		/* adds the server and port field to the GUI */
		
		northPanel.add(serverAndPort);
		/* Label and Textfield */
		
		label= new JLabel("Enter Your Username Below",SwingConstants.CENTER);
		northPanel.add(label);
		
		tf=new JTextField("anon");
		tf.setForeground(Color.black);
		tf.setBackground(Color.white);
		northPanel.add(tf);
		

		label2= new JLabel("Password",SwingConstants.CENTER);
		northPanel.add(label2);
		
		tf2=new JPasswordField();
		tf2.setForeground(Color.black);
		tf2.setBackground(Color.white);
		northPanel.add(tf2);

		northPanel.add(new JLabel("Your message will be encrpted with AES block cipher, Enjoy chating !!"), BorderLayout.NORTH);
		northPanel.add(new JLabel("Spammers will be blocked for 10 sec"), BorderLayout.NORTH);
		getContentPane().add(northPanel, BorderLayout.NORTH);
		
		/* the centerPanel which is the chat room */
		chatrum = new JTextPane();
	
		JPanel centerPanel = new JPanel(new GridLayout(1, 1));
		centerPanel.add(new JScrollPane(chatrum));
		chatrum.setEditable(false);
		/*for setting text style */
		doc=chatrum.getStyledDocument();
		aset=new SimpleAttributeSet();
		aset2=new SimpleAttributeSet();
		
		/*moodPanel = new JPanel(new BorderLayout());
		centerPanel.add(new JScrollPane(moodPanel));*/
		getContentPane().add(centerPanel, BorderLayout.CENTER);
		
		/* 3 button */
		login = new JButton("Login");
		login.addActionListener(this);
		logout = new JButton("Logout");
		logout.addActionListener(this);
		logout.setEnabled(false);
		/* login is required before logout */
		
		/* user have to login before being able to see who is in */
		southPanel = new JPanel();
		
		signup = new JButton("Sign Up");
		signup.addActionListener(this);
		southPanel.add(signup);
		southPanel.add(login);
		southPanel.add(logout);
	
		getContentPane().add(southPanel, BorderLayout.SOUTH);
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(750,600);
		setVisible(true);
		tf.requestFocus();
		append("Welcome to Chat Room \n",ChatObject.MESSAGE);
	}
	
	/* called by the client to append text in the textpane */
	void append(String str,int Type){
		String str2="", str3="", str4="";
		Font first;
		String arr[];
		first=new Font("Comic Sans MS",Font.PLAIN,13);
		StyleConstants.setFontFamily(aset2, first.getFamily());
		StyleConstants.setFontSize(aset2, first.getSize());
		StyleConstants.setAlignment(aset2, StyleConstants.ALIGN_LEFT);
		StyleConstants.setBold(aset2, false);
		StyleConstants.setItalic(aset2,false);
		StyleConstants.setForeground(aset2, Color.BLACK);
		switch(Type){
		/* checking for broadcast message */
		case ChatObject.MESSAGE:
			
			first=new Font("Comic Sans MS",Font.PLAIN,15);
			StyleConstants.setFontFamily(aset, first.getFamily());
			StyleConstants.setFontSize(aset, first.getSize());
			StyleConstants.setAlignment(aset, StyleConstants.ALIGN_LEFT);
			StyleConstants.setBold(aset, true);
			StyleConstants.setItalic(aset,false);
			StyleConstants.setForeground(aset, Color.blue);
			arr = str.split(",");
			if(arr.length>1)
			{
				str2= arr[1];
				str = arr[0];
			}
			else
			{
				str2=str;
				str="";
			}
			break;
		
		case ChatObject.INFO:
			first=new Font("Comic Sans MS",Font.PLAIN,12);
			StyleConstants.setFontFamily(aset, first.getFamily());
			StyleConstants.setFontSize(aset, first.getSize());
			StyleConstants.setItalic(aset,false);
			StyleConstants.setBold(aset, true);
			StyleConstants.setForeground(aset, Color.CYAN);
			if(str.indexOf('$')!=-1)
			{
				String temp = str.substring(str.indexOf('$')+1);
				str = str.replace('$', ' ');
				temp = temp.replace('*', ' ');
				label.setText("Hi "+temp+ "enter your message below");
			}
			str2=str;
			str="";
			break;
		}
		try{
		doc.setParagraphAttributes(0, 0, aset, false);
		doc.insertString(doc.getLength(), str, aset2);
		doc.insertString(doc.getLength(), str2, aset);
		doc.insertString(doc.getLength(), str3, aset);
		doc.insertString(doc.getLength(), str4, aset2);
		}
		catch(BadLocationException e){
			System.out.println("unable to write to chatrum");
			}
		chatrum.setCaretPosition(doc.getLength()-1);
		
		}
	
	
	/* called by the GUI is the connection failed */
	/* we reset our buttons label textfield */
	void connectionFailed(){
		login.setEnabled(true);
		logout.setEnabled(false);
	
		label.setText("Enter Your Username Below");
		tf.setText("anon");
		/* reset port number and host name */
		tfPort.setText("" + defaultPort);
		tfServer.setText(defaultHost);
		/* let the user change them */
		tfServer.setEditable(true);
		tfPort.setEditable(true);
		/* don't react to a change request after the username */
		tf.removeActionListener(this);
		connected = false;
		}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		/* if it is the Logout button */
		try{
		if(o == logout) {
			client.sendMessage(new ChatObject(ChatObject.LOGOUT, ""));
			tfServer.setEditable(true);
			tfPort.setEditable(true);
			new ClientGUI("localhost", 1500);
			this.dispose();
			return;
		}
		/* if it the who is in button */
				/* ok it is coming from the JTextField */
		if(connected) {
			
			northPanel.remove(label2);
			northPanel.remove(tf2);
			revalidate();
			repaint();
			/* just have to send the message */
			String msg=tf.getText();
			/* checking for private message */
			if(!msg.isEmpty() && msg.length() < 15)
			{
				msg = msg.trim();
				client.sendMessage(new ChatObject(ChatObject.MESSAGE, tf.getText().trim()));				
		
			}
			else{
				JOptionPane.showConfirmDialog((Component) null, "Number of characters is exceeding limit.. Please type small message",
				        "alert", JOptionPane.OK_CANCEL_OPTION);
				
			}
			tf.setText("");
			return;
		}
		if(o == signup){
			String username = tf.getText();
			char[] pass = tf2.getPassword();
			String password = new String(pass);
			if(username.length() == 0)
				return;
			String server = tfServer.getText().trim();
			if(server.length() == 0)
				return;
			/* empty or invalid port number, ignore it */
			String portNumber = tfPort.getText().trim();
			if(portNumber.length() == 0)
				return;
			int port = 0;
			try {
				port = Integer.parseInt(portNumber);
			}
			catch(Exception en) {
				return;  
			/* nothing I can do if port number is not valid */
			}
			int sigup = 1;
		//	client = new Client(server, port, username, this, password);
			client = new Client(server, port, username, this, password, sigup);
			if(!client.start()) 
			{ 
				System.out.println("Client not started");
				return;
			}
			tf.setText("");
			tf2.setText("");
			signup.setEnabled(false);
			login.setEnabled(false);
			this.dispose();
			new ClientGUI("localhost", 1500);
			JOptionPane.showConfirmDialog((Component) null, "Successfully signed up !! You can login to the Chat application",
			        "alert", JOptionPane.OK_CANCEL_OPTION);
			
			
		
			return;
			
		}
		if(o == login) {
			/* ok it is a connection request */
			String username = tf.getText().trim();
			char[] pass = tf2.getPassword();
			String password = new String(pass);
			
			
			/* empty user name ignore it */
			if(username.length() == 0)
				return;
		
			/* empty serverAddress ignore it */
			String server = tfServer.getText().trim();
			if(server.length() == 0)
				return;
			/* empty or invalid port number, ignore it */
			String portNumber = tfPort.getText().trim();
			if(portNumber.length() == 0)
				return;
			int port = 0;
			try {
				port = Integer.parseInt(portNumber);
			}
			catch(Exception en) {
				return;  
			/* nothing I can do if port number is not valid */
			}

			/* try creating a new Client with GUI */
			client = new Client(server, port, username, this, password);
			/* test if we can start the Client */
			if(!client.start()) 
				{ 
					System.out.println("Client not started");
					return;
				}
			tf.setText("");
			tf2.setText("");
			label.setText("Hi "+username+" enter your message below");
			connected = true;
			
			/* disable login button */
			login.setEnabled(false);
			/* enable the 2 buttons */
			logout.setEnabled(true);
			signup.setEnabled(false);
		
			/* disable the Server and Port JTextField */
			tfServer.setEditable(false);
			tfPort.setEditable(false);
			/* Action listener for when the user enter a message */
			tf.addActionListener(this);
			//northPanel.remove(label2);
			//northPanel.remove(tf2);
			northPanel.remove(label2);
			northPanel.remove(tf2);
			revalidate();
			repaint();
		}

		}
		catch(Exception e1){
			e1.printStackTrace();
		}

	}
	
	/* to start the whole thing the server */
	public static void main(String[] args) throws IOException {
		new ClientGUI("localhost", 1500);
		
	
	
	}

}


