package chat;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.Arrays;
import javax.crypto.NoSuchPaddingException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.BadPaddingException;

public class ChatObject implements Serializable{	

	/**
	 * The ChatObject will be exchanged between the server and the client
	 */
	private static final long serialVersionUID = 1L;
	/* Static values for each type of message */
	static final int WHO = 0, MESSAGE =1, LOGOUT =2, INFO = 6,SIGNUP = 7,LOGIN = 8,ENC = 9 ; 
	private String username = "", password = "";
	private int type;
	private String message;
	private byte[] encPass;
	private final String secret = "mysecret";
	private byte[] key;
	String pass;

	private  byte[] encMsg;
	/*
	 * Constructor method
	 * Input:
	 * 		-type of the message
	 * 		-actual message text
	 */
	ChatObject(int type)
	{
		this.type = type;
	}
	public byte[] getEncPass() {
		return encPass;
	}
	public void setEncPass(byte[] encPass) {
		this.encPass = encPass;
	}
	ChatObject(int type,String message)
	{
	 this.type=type;
	 this.message=message;
	}
	ChatObject(int type,String user,String pass) throws Exception
	{
	 this.type=type;
	 this.username=user;
	 this.password=pass;
	 
	 key = (username + password + secret).getBytes("UTF-8");
	 System.out.println(key.toString());
	 MessageDigest sha = MessageDigest.getInstance("SHA-1");
	 key = sha.digest(key);
	 key = Arrays.copyOf(key, 16);
		
	 SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

	    // Instantiate the cipher
    	 Cipher cipher = Cipher.getInstance("AES");
	 cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
	    
	 encPass = cipher.doFinal(password.getBytes());
	 cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
	 byte[] original1 = cipher.doFinal(encPass);
	 
	 String st = new String(original1);
	 System.out.println(st);
	 
	    // byte[] encrypted = cipher.doFinal(msg.);
	     

	}
		void decrpt() throws Exception{
		
	
		//MessageDigest sha = MessageDigest.getInstance("SHA-1");
		/*key = sha.digest(key);
		key = Arrays.copyOf(key, 16);*/
		Cipher cipher = Cipher.getInstance("AES");
		SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
		cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
	    byte[] original1 = cipher.doFinal(encPass);
	    pass = new String(original1);
	  
		
	}

	/*
	 * Getter method for fetching the type of message
	 */
	int getType() {
		return type;
	}
	
	/*
	 * Setter method for setting value of message
	 */
	
	public void setMessage(String message) {
		this.message = message;
	}

	/*
	 * Getter method for fetching the actual message content
	 */
	String getMessage() {
		return message;
	}

	public byte[] getEncMsg() throws Exception {
		return encMsg;
	}

	public void setEncMsg(byte[] encMsg) throws Exception{
		this.encMsg = encMsg;
	}


	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


}
