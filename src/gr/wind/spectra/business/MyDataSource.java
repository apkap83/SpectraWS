package gr.wind.spectra.business;

import java.sql.Connection;
import java.util.ResourceBundle;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class MyDataSource
{
	private static String DATABASE_URL;// = "jdbc:mysql://172.16.142.124:3306/SmartOutageDB?";
	private static String USERNAME;// = "root";
	private static String PASSWORD;// = "vQaSx4iVipDPLKfmdVDc";

	private static HikariConfig config = new HikariConfig();
	private static HikariDataSource ds;

	static
	{
		DATABASE_URL = ResourceBundle.getBundle("database").getString("DATABASE_URL");
		USERNAME = ResourceBundle.getBundle("database").getString("USERNAME");
		PASSWORD = ResourceBundle.getBundle("database").getString("PASSWORD");

		config.setJdbcUrl(DATABASE_URL);
		config.setDriverClassName("com.mysql.cj.jdbc.Driver");
		config.setUsername(USERNAME);
		config.setPassword(PASSWORD);
		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "250");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		ds = new HikariDataSource(config);
	}

	public MyDataSource()
	{
	}

	public int hello(int a)
	{
		return 3;
	}

	public static Connection getConnection() throws Exception
	{
		return ds.getConnection();
	}
}