package gr.wind.spectra.business;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DB_Operations 
{
	Connection conn;
	Statement stmt = null;
	ResultSet rs = null;
	
	public DB_Operations(Connection conn)
	{
		this.conn = conn;
	}
	
	public boolean checkIfStringExistsInSpecificColumn(String table, String columnName, String searchValue) throws SQLException
	{
		boolean found = false;
		
		String sqlString = "SELECT `" + columnName + "` FROM `" + table + "` WHERE `" + columnName + "` = ?";
  	    PreparedStatement pst = conn.prepareStatement(sqlString);
  	    pst.setString(1, searchValue);
  	    pst.execute();
  	    
  	    ResultSet rs = pst.executeQuery();
  	    
  	    while ( rs.next() )
  	    {
  	    	String current = rs.getString(columnName);
  	    	
  	    	if (current.contentEquals(searchValue))
  	    	{
  	    		found = true;
  	    	}
  	    }
  	  return found;
	}
  	
  	public String InsertValuesInTableGetSequence(String table, String[] columnNames, String[] columnValues, String[] types) throws SQLException, ParseException
  	{
  		String autoGeneratedID = "Uninitialized";
  		String sqlString = "INSERT INTO " + table + Help_Func.ColumnsToInsertStatement(columnNames) + Help_Func.ValuesToInsertStatement(columnValues); 
  		System.out.println(sqlString);
  		PreparedStatement pst = conn.prepareStatement(sqlString ,Statement.RETURN_GENERATED_KEYS);

  		for (int i=0;i<columnNames.length;i++)
  		{
  			if (columnValues[i].equals(""))
  			{
  				pst.setNull(i+1, Types.NULL);
  			}
  			else
  			{
  				if (types[i].equals("String"))
  				{
  				pst.setString(i+1, columnValues[i]);
  				}
  				else if (types[i].equals("DateTime"))
  				{
  					LocalDateTime time;
  					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  					LocalDateTime dateTime = LocalDateTime.parse(columnValues[i], formatter);
  					pst.setObject(i+1, dateTime);
  				}
  				else if (types[i].equals("Integer"))
  				{
  					pst.setInt(i+1, Integer.parseInt(columnValues[i]));
  				}
  					
  			}
  		}
  		
  		try
  		{
  			pst.executeUpdate();
  			// Code to get Generated Key
  			ResultSet tableKeys = pst.getGeneratedKeys();
  			tableKeys.next();
  			autoGeneratedID = Integer.toString(tableKeys.getInt(1));
  		}
  		catch (SQLException e)
  		{
  			e.printStackTrace();
  	
  		}
  		
  		return autoGeneratedID;
  	}

  	public List<String> GetOneColumnResultSet(String table, String columnName, String predicate) throws SQLException
  	{
  		// Example: select ID from table where a = 2 and b = 3
  		
  		List<String> myList = new ArrayList<String>();
  		
  		String sqlString = "SELECT `" + columnName + "` FROM `" + table + "` WHERE " + predicate;
  		System.out.println(sqlString);
  		PreparedStatement pst = conn.prepareStatement(sqlString);
  		pst.execute();
  		ResultSet rs = pst.executeQuery();
  		
  		while ( rs.next() )
  	    {
  	    	String current = rs.getString(columnName);
  	    	myList.add(current);
 	    }
  		
  		return myList;
  	}
  	
  	public List<String> GetOneLineResultSet(String table, String[] columnNames, String predicate) throws SQLException
  	{
  		List<String> myList = new ArrayList<String>();
		int numOfColumns = columnNames.length;
  		
		String sqlQuery = "SELECT " + Help_Func.columnsWithCommas(columnNames) + " FROM " + table + " WHERE " + predicate + ";";
		System.out.println(sqlQuery);
		PreparedStatement pst = conn.prepareStatement(sqlQuery);
		pst.execute();
		
		ResultSet rs = pst.executeQuery();
  		
  		while ( rs.next() )
  	    {
  			for (int i = 0; i < numOfColumns; i++)
  			{
  				myList.add(rs.getString(columnNames[i]));
  			}
 	    }
  		
  		return myList;
  	}
  	
  	public String GetOneValue(String table, String columnName, String predicate) throws SQLException
  	{
  		String valueFound = "";
  		String sqlQuery = "SELECT " + columnName + " FROM " + table + " WHERE " + predicate + ";";
		System.out.println(sqlQuery);
		PreparedStatement pst = conn.prepareStatement(sqlQuery);
		pst.execute();
		ResultSet rs = pst.executeQuery();
		rs.next();
		valueFound = rs.getString(columnName);
  		return valueFound;
  	}
  	
  	public List<String> GetOneColumnUniqueResultSet(String table, String columnName, String predicate)
  	{
  		// Example: select DISTINCT ID from table where a = 2 and b = 3
  		
  		List<String> myList = new ArrayList<String>();
  		
  		String sqlString = "SELECT DISTINCT `" + columnName + "` FROM `" + table + "` WHERE " + predicate;
  		System.out.println(sqlString);
  		PreparedStatement pst = null;
		try {
			pst = conn.prepareStatement(sqlString);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  		try {
			pst.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  		ResultSet rs = null;
		try {
			rs = pst.executeQuery();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  		
  		try {
			while ( rs.next() )
			{
				String current = rs.getString(columnName);
				myList.add(current);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  		
  		// Sort list alphabetically
  		java.util.Collections.sort(myList);
  		
  		return myList;
  	}
  	
  	public int UpdateValuesForOneColumn(String table, String setColumnName, String newValue, String predicate) throws SQLException
  	{
  		// Example: update TestTable set `Name` = 100 where Surname = "Kapetanios";
  		
  		String sqlString = "update `" + table + "` set `" + setColumnName + "` = '" + newValue + "' WHERE " + predicate;
  		System.out.println(sqlString);
  		PreparedStatement pst = conn.prepareStatement(sqlString);
  		int rowsAffected = pst.executeUpdate();

  		return rowsAffected;
  		
  	}
  	
  	public String NumberOfRowsFound(String table, String predicate) throws SQLException
  	{
  		int numOfRows = 0;
  		String sqlQuery = "SELECT *" + " FROM " + table + " WHERE " + predicate + ";";
		System.out.println(sqlQuery);
		PreparedStatement pst = conn.prepareStatement(sqlQuery);
		pst.execute();
		ResultSet rs = pst.executeQuery();
  		
  		while ( rs.next() )
  	    {
  			numOfRows++;
 	    }
  		
  		return Integer.toString(numOfRows);
  	}
  	
  	public ResultSet GetRows(String table, String[] columnNames, String predicate) throws SQLException
  	{
  		String sqlQuery = "SELECT " + Help_Func.columnsWithCommas(columnNames) + " FROM " + table + " WHERE " + predicate + ";";
  		System.out.println(sqlQuery);
  		PreparedStatement pst = conn.prepareStatement(sqlQuery);
		pst.execute();
		ResultSet rs = pst.executeQuery();
  		return rs;
  	}
}
