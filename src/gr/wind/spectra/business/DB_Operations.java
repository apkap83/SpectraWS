package gr.wind.spectra.business;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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
  	
  	public boolean InsertValuesInTable(String table, String[] columnNames, String[] columnValues) throws SQLException
  	{
  		
  		int numOfFields = columnNames.length;
  		String sqlString = "INSERT INTO " + table + Help_Func.ColumnsToInsertStatement(columnNames) + Help_Func.ValuesToInsertStatement(columnNames); 
  		System.out.println(sqlString);
  		PreparedStatement pst = conn.prepareStatement(sqlString);
  		
  		for(int i=0; i<numOfFields;i++)
  		{
  			pst.setString(i+1, columnValues[i]);
  		}
  		
  		try
  		{
  			pst.executeUpdate();
  		}
  		catch (SQLException e)
  		{
  			return false;
  		}
  		
  		return true;
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
}
