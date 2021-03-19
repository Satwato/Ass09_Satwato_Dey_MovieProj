package MoviesPack;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class dbConnection {
    public static Connection getConnection() {
		Connection conn = null;
		
		try (FileReader propFile = new FileReader(new File("src/MoviesPack/dbConn.properties"))){
			
			Properties prop = new Properties();
			prop.load(propFile);
			
			Class.forName(prop.getProperty("driver"));
			String url = prop.getProperty("url");
			String user = prop.getProperty("user");
			String password = prop.getProperty("password");
			conn = DriverManager.getConnection(url, user, password);
			
		} catch (SQLException | ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		
		return conn;
	}
	
	public static void main(String[] args) {
		Connection testConn = getConnection();
		System.out.println("Test connection : " + testConn);
	}
}
