package gr.wind.spectra.business;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import gr.wind.spectra.web.InvalidInputException;

public class IncidentOutageToCSV
{
	private DB_Operations dbs;
	private s_DB_Operations s_dbs;
	private String incidentID;
	private String outageID;

	public IncidentOutageToCSV(DB_Operations dbs, s_DB_Operations s_dbs, String incidentID, String outageID)
	{
		this.dbs = dbs;
		this.s_dbs = s_dbs;
		this.incidentID = incidentID;
		this.outageID = outageID;
	}

	public String replaceHierarchyColumns(String hierarchyProvided, String technology)
			throws SQLException, InvalidInputException
	{
		String newHierarchyValue = "";

		if (technology.equals("Voice"))
		{
			// Get root hierarchy String
			String rootElementInHierarchy = Help_Func.getRootHierarchyNode(hierarchyProvided);

			String fullVoiceSubsHierarchyFromDB;
			String[] fullVoiceSubsHierarchyFromDBSplit;
			// Get Full Voice hierarchy in style :
			// OltElementName->OltSlot->OltPort->Onu->ActiveElement->Slot
			fullVoiceSubsHierarchyFromDB = dbs.getOneValue("HierarchyTablePerTechnology2",
					"VoiceSubscribersTableNamePath", new String[] { "RootHierarchyNode" },
					new String[] { rootElementInHierarchy }, new String[] { "String" });

			// Split the Data hierarchy retrieved from DB into fields
			fullVoiceSubsHierarchyFromDBSplit = fullVoiceSubsHierarchyFromDB.split("->");

			// Replace Hierarchy Columns from the relevant subscribers table
			newHierarchyValue = Help_Func.replaceHierarchyForSubscribersAffected(hierarchyProvided,
					fullVoiceSubsHierarchyFromDBSplit);
		} else if (technology.equals("Data"))
		{
			// Get root hierarchy String
			String rootElementInHierarchy = Help_Func.getRootHierarchyNode(hierarchyProvided);

			String fullVoiceSubsHierarchyFromDB;
			String[] fullVoiceSubsHierarchyFromDBSplit;
			// Get Full Voice hierarchy in style :
			// OltElementName->OltSlot->OltPort->Onu->ActiveElement->Slot
			fullVoiceSubsHierarchyFromDB = dbs.getOneValue("HierarchyTablePerTechnology2",
					"DataSubscribersTableNamePath", new String[] { "RootHierarchyNode" },
					new String[] { rootElementInHierarchy }, new String[] { "String" });

			// Split the Data hierarchy retrieved from DB into fields
			fullVoiceSubsHierarchyFromDBSplit = fullVoiceSubsHierarchyFromDB.split("->");

			// Replace Hierarchy Columns from the relevant subscribers table
			newHierarchyValue = Help_Func.replaceHierarchyForSubscribersAffected(hierarchyProvided,
					fullVoiceSubsHierarchyFromDBSplit);
		}
		return newHierarchyValue;
	}	
	
	
	public void produceReport() throws SQLException, InvalidInputException
	{
		ResultSet rs = null;
		// Get Lines with IncidentStatus = "OPEN"
		rs = s_dbs.getRows("SubmittedIncidents",
				new String[] { "HierarchySelected", "StartTime", "EndTime", "Scheduled", "Impact", "AffectedServices" },
				new String[] { "incidentID", "outageID" }, new String[] { incidentID, outageID }, new String[] { "String", "String" });

		String HierarchySelected = "";
		Date StartTime;
		Date EndTime;
		String Scheduled = "";
		String Impact = "";
		String outageAffectedService = "";

		while (rs.next())
		{
			HierarchySelected = rs.getString("HierarchySelected");
			StartTime = rs.getTimestamp("StartTime");
			EndTime = rs.getTimestamp("EndTime");
			Scheduled = rs.getString("Scheduled");
			Impact = rs.getString("Impact");
			outageAffectedService = rs.getString("AffectedServices");
		}


		// If the closed incident is a "Data" affected one
		if (outageAffectedService.equals("Data"))
		{
			String exportedFileName = "Spectra_CLIs_Affected_OutageID_" + outageID + "_Data_YYYMMDD.csv";
			
			HierarchySelected = this.replaceHierarchyColumns(HierarchySelected, "Data");
			
			SQLStatementToCSV sCSV = new SQLStatementToCSV(exportedFileName, 
					"Internet_Resource_Path", 
					new String[] {"Username"},
					Help_Func.hierarchyKeys(HierarchySelected),
					Help_Func.hierarchyValues(HierarchySelected), 
					Help_Func.hierarchyStringTypes(HierarchySelected)
					);
			sCSV.start();
		}
		// If the closed incident is a "Voice" affected one
		else if (outageAffectedService.equals("Voice"))
		{
			String exportedFileName = "Spectra_CLIs_Affected_OutageID_" + outageID + "_Voice_YYYMMDD.csv";
			
			HierarchySelected = this.replaceHierarchyColumns(HierarchySelected, "Voice");
			
			SQLStatementToCSV sCSV = new SQLStatementToCSV(exportedFileName, 
					"Voice_Resource_Path", 
					new String[] {"Username"},
					Help_Func.hierarchyKeys(HierarchySelected),
					Help_Func.hierarchyValues(HierarchySelected), 
					Help_Func.hierarchyStringTypes(HierarchySelected)
					);
			sCSV.start();
		}
	}
}
