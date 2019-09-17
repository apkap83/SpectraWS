package gr.wind.spectra.business;

import java.sql.Connection;
import java.sql.SQLException;

public class CLIOutage
{
	private DB_Connection conObj;
	private Connection conn;
	private DB_Operations dbs;

	public CLIOutage(String CLIProvided) throws Exception
	{
	}

	public void establishDBConnection() throws Exception
	{
		try
		{
			this.conObj = new DB_Connection();
			this.conn = this.conObj.connect();
			this.dbs = new DB_Operations(conn);
		} catch (Exception ex)
		{
			throw new Exception(ex.getMessage());
		}
	}

	public void checkCLIOutage() throws SQLException
	{
		boolean weHaveOpenIncident = dbs.checkIfStringExistsInSpecificColumn("SubmittedIncidents", "IncidentStatus",
				"OPEN");

		System.out.println("weHaveOpenIncident = " + weHaveOpenIncident);
	}

	public static void main(String[] args) throws Exception
	{
		CLIOutage cl = new CLIOutage("2102012739");
		cl.checkCLIOutage();
	}
}
