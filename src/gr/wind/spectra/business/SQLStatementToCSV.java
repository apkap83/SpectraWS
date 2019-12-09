package gr.wind.spectra.business;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

//Import log4j classes.
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.opencsv.CSVWriter;

public class SQLStatementToCSV extends Thread
{
	// Define a static logger variable so that it references the
	// Logger instance named "DB_Connection".
	private static final Logger logger = LogManager.getLogger(gr.wind.spectra.business.SQLStatementToCSV.class);

	private DB_Connection conObj;
	private Connection conn;

	private String exportedFileName;
	private String[] predicateKeys;
	private String[] predicateValues;
	private String[] predicateTypes;

	String sqlQuery;

	public SQLStatementToCSV(String exportedFileName, String table, String[] columnsForExport, String[] predicateKeys,
			String[] predicateValues, String[] predicateTypes)
	{
		this.exportedFileName = exportedFileName;
		this.predicateKeys = predicateKeys;
		this.predicateValues = predicateValues;
		this.predicateTypes = predicateTypes;

		sqlQuery = "SELECT " + Help_Func.columnsWithCommas(columnsForExport) + " FROM " + table + " WHERE "
				+ Help_Func.generateANDPredicateQuestionMarks(predicateKeys);

		System.out.println("SQLStatementToCSV Query:" + sqlQuery);

	}

	public void establishDBConnection() throws Exception
	{
		try
		{
			this.conObj = new DB_Connection();
			this.conn = this.conObj.connect();
			//			this.dbs = new DB_Operations(conn);
		} catch (Exception ex)
		{
			throw new Exception(ex.getMessage());
		}
	}

	@Override
	public void run()
	{
		try
		{
			try
			{
				this.establishDBConnection();
			} catch (Exception e1)
			{
				e1.printStackTrace();
			}
			logger.info("Thread execution in ResultSetToCSV Class Started!");
			System.out.println("Thread execution in ResultSetToCSV Class Started!");
			try
			{
				System.out.println(sqlQuery);
				PreparedStatement pst = conn.prepareStatement(sqlQuery);
				for (int i = 0; i < predicateKeys.length; i++)
				{
					if (predicateTypes[i].equals("String"))
					{
						pst.setString(i + 1, predicateValues[i]);
					} else if (predicateTypes[i].equals("Integer"))
					{
						pst.setInt(i + 1, Integer.parseInt(predicateValues[i]));
					}
				}

				ResultSet rs = pst.executeQuery();
				CSVWriter csvWriter = new CSVWriter(new FileWriter(exportedFileName), ',');
				csvWriter.writeAll(rs, false);
				csvWriter.close();
				System.out.println("Thread execution in ResultSetToCSV Class Finished!");
			} catch (IOException e)
			{
				e.printStackTrace();
			}

		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public void saveResultSetToCSV() throws SQLException
	{

	}
}
