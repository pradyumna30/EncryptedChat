package chat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class ServerGUI extends JFrame implements ActionListener, WindowListener {

	/**
	 * Creates swing based graphical interface for the application
	 */
	private static final long serialVersionUID = 1L;
	/* Buttons for start and stop */
	private JButton stopstart;
	/* Text area for chat room and event display*/
	private JTextArea eventDisplay;
	/* Port number */
	private JTextField tPortNumber;
	/* Server */
	private Server server;
	private JTextPane chatrum;
	private StyledDocument doc;
	private SimpleAttributeSet aSet;
	
	/* 
	 * GUI Constructor
	 */
	ServerGUI(int port)
	{	
		super("Quack - Server");

		server = null;
		/* Set the top panel */
		JPanel topPan = new JPanel();
		topPan.add(new JLabel("Port number: "));
		tPortNumber = new JTextField(" " + port);
		topPan.add(tPortNumber);
		/* Put the start/stop button */
		stopstart = new JButton("START");
		stopstart.addActionListener(this);
		topPan.add(stopstart);
		add(topPan, BorderLayout.NORTH);
		
		/* Chat room and events */
		JPanel midPan = new JPanel(new GridLayout(2, 1));
		chatrum = new JTextPane();
		chatrum.setEditable(false);
		/* Fetch the document and the attribute set for 
		 * modifying display based on message type */
		doc = chatrum.getStyledDocument();
        aSet = new SimpleAttributeSet();
		appendRoom("Chat history.\n",ChatObject.MESSAGE);
		/* Add the chat panel to the midPan and make it scroll */
		midPan.add(new JScrollPane(chatrum));
		eventDisplay = new JTextArea(80,80);
		eventDisplay.setEditable(false);
		appendEvent("Event log.\n");
		midPan.add(new JScrollPane(eventDisplay));
		/* Add the midPan to the Frame*/
		add(midPan);
		
		/* For the close button */
		addWindowListener(this);
		setSize(400,600);
		setVisible(true);
	}

	/*
	 * Appends a message to the chat room
	 * Inputs:
	 * 		-message text to be added to the chat room
	 * 		-type of the message
	 */
	void appendRoom(String str, int type) {
		
		Font firstFont;
		/* Set formatting on the basis of the type of the message*/
		switch(type)
		{
		case ChatObject.MESSAGE:
			firstFont = new Font("Comic Sans MS", Font.PLAIN, 13);
			StyleConstants.setFontFamily(aSet, firstFont.getFamily());
	        StyleConstants.setFontSize(aSet, firstFont.getSize());
	        StyleConstants.setItalic(aSet, false);
	        StyleConstants.setBold(aSet, false);
	        StyleConstants.setForeground(aSet, Color.blue);
	        StyleConstants.setAlignment(aSet, StyleConstants.ALIGN_LEFT);
			break;
		
		}
		
		/* Sets the paragraph attributes */
        doc.setParagraphAttributes(0, 0, aSet, false);
        
        try {
        	/* Insert string at the end of the document */
			doc.insertString(doc.getLength(), str, aSet);
		} catch (BadLocationException e) {
			System.out.println("Chat room limit exhausted");
		}
        
        /* Controls the scroll down event */
        chatrum.setCaretPosition(doc.getLength());
	}
	
	/*
	 * Appends an event to the event room
	 * Input:
	 * 		-Event string to be added 
	 */
	void appendEvent(String str) {
		eventDisplay.append(str);
		eventDisplay.setCaretPosition(eventDisplay.getText().length() - 1);
	}
	
	/*
	 * If the user clicks the X button to close the application
	 * the connection with the server needs to be closed to free the port
	 */
	@Override
	public void windowClosing(WindowEvent arg0) {
		/* If my Server exist*/
		if(server != null) {
			try {
				/* Ask the server to close the connection */
				server.stop();
			}
			catch(Exception eClose) {
			}
			server = null;
		}
		/* dispose the frame */
		dispose();
		System.exit(0);
	}
	
	/*
	 * Starts the server on click of button
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) 
	{
		/* if running we have to stop */
		if(server != null) {
			server.stop();
			server = null;
			tPortNumber.setEditable(true);
			stopstart.setText("START");
			return;
		}
		/* Start the server */	
		int port;
		try {
			port = Integer.parseInt(tPortNumber.getText().trim());
		}
		catch(Exception er) {
			appendEvent("Invalid port number");
			return;
		}	
		/* Create a new Server */
		server = new Server(port, this);
		/* Start the server as a thread */
		new ServerRunning().start();
		
		/* Change label for the button */
		stopstart.setText("STOP");
		tPortNumber.setEditable(false);
	}
	
	
	/*
	 * Main method
	 */
	public static void main (String args[])
	{
		/* Call server constructor with port 1500 as default */
		new ServerGUI(1500);
	}

	/*
	 * A thread to run the Server
	 */
	class ServerRunning extends Thread {
		public void run() {
			/* should execute until if fails */
			try {
				server.wakeup();
			} catch (Exception e) {
				
				e.printStackTrace();
			}         
			/* the server failed */
			stopstart.setText("START");
			tPortNumber.setEditable(true);
			appendEvent("Server crashed\n");
			server = null;
		}
	}
	
	/*
	 * Unchanged methods
	 */
	@Override
	public void windowDeactivated(WindowEvent arg0) {}
	@Override
	public void windowDeiconified(WindowEvent arg0) {}
	@Override
	public void windowIconified(WindowEvent arg0) {}
	@Override
	public void windowOpened(WindowEvent arg0) {}
	@Override
	public void windowActivated(WindowEvent arg0) {}
	@Override
	public void windowClosed(WindowEvent arg0) {}
	
}