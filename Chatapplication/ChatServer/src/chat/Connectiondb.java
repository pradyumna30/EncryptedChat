package chat;
import java.sql.*;

 
class Connectiondb {
 
    
    Connection con = null;
    Statement stmt;
    public Connection getCon() {
		return con;
	}
	public void setCon(Connection con) {
		this.con = con;
	}
	Connectiondb() throws SQLException, ClassNotFoundException{
    	Class.forName("com.mysql.jdbc.Driver");
    	con =  DriverManager.getConnection("jdbc:mysql://localhost/test", "root", "root");
    	Statement stmt =  con.createStatement();
    	
        
    }
	public Statement getStmt() {
		return stmt;
	}
	public void setStmt(Statement stmt) {
		this.stmt = stmt;
	}
 
 
}