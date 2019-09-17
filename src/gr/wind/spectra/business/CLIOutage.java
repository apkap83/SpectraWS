package gr.wind.spectra.business;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import gr.wind.spectra.model.ProductOfNLUActive;
import gr.wind.spectra.web.InvalidInputException;

public class CLIOutage
{
	private DB_Operations dbs;
	private String requestID;

	public CLIOutage(DB_Operations dbs, String requestID) throws Exception
	{
		this.dbs = dbs;
		this.requestID = requestID;
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

	public ArrayList<ProductOfNLUActive> checkCLIOutage(String CLIProvided, String ServiceType)
			throws SQLException, InvalidInputException
	{
		boolean foundAtLeastOneCLIAffected = false;
		ArrayList<ProductOfNLUActive> mylist = new ArrayList<ProductOfNLUActive>();

		// Check if we have at least one OPEN incident
		boolean weHaveOpenIncident = dbs.checkIfStringExistsInSpecificColumn("SubmittedIncidents", "IncidentStatus",
				"OPEN");

		// Split ServiceType
		String delimiterCharacter = "\\|";
		String[] ServiceTypeSplitted = ServiceType.split(delimiterCharacter);

		// If We have at least one openned incident...
		if (weHaveOpenIncident)
		{
//			System.out.println("We currently have open incidents");

			for (String service : ServiceTypeSplitted)
			{
				ResultSet rs = null;
				rs = dbs.getRows("SubmittedIncidents",
						new String[] { "IncidentID", "OutageID", "HierarchySelected", "Priority", "AffectedServices",
								"Scheduled", "Duration", "EndTime", "Impact" },
						new String[] { "IncidentStatus" }, new String[] { "OPEN" }, new String[] { "String" });

				while (rs.next())
				{
					if (service.equals("Voice"))
					{
						String IncidentID = rs.getString("IncidentID");
						int OutageID = rs.getInt("OutageID");
						String HierarchySelected = rs.getString("HierarchySelected");
						String Priority = rs.getString("Priority");
						String AffectedServices = rs.getString("AffectedServices");
						String Scheduled = rs.getString("Scheduled");
						String Duration = rs.getString("Duration");
						String EndTime = rs.getString("EndTime");
						String Impact = rs.getString("Impact");

						System.out.println(IncidentID + " " + OutageID + " " + HierarchySelected + " " + Priority + " "
								+ AffectedServices + " " + Scheduled + " " + Duration + " " + EndTime + " " + Impact);

						// Replace Hierarchy keys from the correct column names of Hierarchy Subscribers
						// table
						HierarchySelected = this.replaceHierarchyColumns(HierarchySelected, "Voice");

						// Add CLI Value in Hierarcy
						HierarchySelected += "->CliValue=" + CLIProvided;

						// Get root hierarchy String
						String rootElementInHierarchy = Help_Func.getRootHierarchyNode(HierarchySelected);

						// Get Hierarchy Table for that root hierarchy
						String table = dbs.getOneValue("HierarchyTablePerTechnology2", "VoiceSubscribersTableName",
								new String[] { "RootHierarchyNode" }, new String[] { rootElementInHierarchy },
								new String[] { "String" });

						String numOfRowsFound = dbs.numberOfRowsFound(table, Help_Func.hierarchyKeys(HierarchySelected),
								Help_Func.hierarchyValues(HierarchySelected),
								Help_Func.hierarchyStringTypes(HierarchySelected));

						// If matched Hierarchy + CLI matches lines (then those CLIs have actually
						// Outage)
						if (Integer.parseInt(numOfRowsFound) > 0 && AffectedServices.equals("Voice"))
						{

							foundAtLeastOneCLIAffected = true;
							mylist.add(new ProductOfNLUActive(this.requestID, CLIProvided, IncidentID, Priority,
									AffectedServices, Scheduled, Duration, EndTime, Impact, "NULL", "NULL", "NULL"));
						}

						System.out.println("Number of rows found ARE: " + numOfRowsFound);
					} else if (service.equals("Data"))
					{
						String IncidentID = rs.getString("IncidentID");
						int OutageID = rs.getInt("OutageID");
						String HierarchySelected = rs.getString("HierarchySelected");
						String Priority = rs.getString("Priority");
						String AffectedServices = rs.getString("AffectedServices");
						String Scheduled = rs.getString("Scheduled");
						String Duration = rs.getString("Duration");
						String EndTime = rs.getString("EndTime");
						String Impact = rs.getString("Impact");

						System.out.println(IncidentID + " " + OutageID + " " + HierarchySelected + " " + Priority + " "
								+ AffectedServices + " " + Scheduled + " " + Duration + " " + EndTime + " " + Impact);

						// Replace Hierarchy keys from the correct column names of Hierarchy Subscribers
						// table
						HierarchySelected = this.replaceHierarchyColumns(HierarchySelected, "Data");

						// Add CLI Value in Hierarcy
						HierarchySelected += "->CliValue=" + CLIProvided;

						// Get root hierarchy String
						String rootElementInHierarchy = Help_Func.getRootHierarchyNode(HierarchySelected);

						// Get Hierarchy Table for that root hierarchy
						String table = dbs.getOneValue("HierarchyTablePerTechnology2", "DataSubscribersTableName",
								new String[] { "RootHierarchyNode" }, new String[] { rootElementInHierarchy },
								new String[] { "String" });

						String numOfRowsFound = dbs.numberOfRowsFound(table, Help_Func.hierarchyKeys(HierarchySelected),
								Help_Func.hierarchyValues(HierarchySelected),
								Help_Func.hierarchyStringTypes(HierarchySelected));

						// If matched Hierarchy + CLI matches lines (then those CLIs have actually
						// Outage)
						if (Integer.parseInt(numOfRowsFound) > 0 && AffectedServices.equals("Data"))
						{
							foundAtLeastOneCLIAffected = true;
							mylist.add(new ProductOfNLUActive(this.requestID, CLIProvided, IncidentID, Priority,
									AffectedServices, Scheduled, Duration, EndTime, Impact, "NULL", "NULL", "NULL"));
						}

//						System.out.println("Number of rows found ARE: " + numOfRowsFound);
					}
				}

				//
				if (!foundAtLeastOneCLIAffected)
				{
					throw new InvalidInputException("No service affection", "Error 425");
					// mylist.add(new ProductOfNLUActive("MyRequestID", CLIProvided, "NULL", "NULL",
					// "NULL", "NULL", "NULL",
					// "NULL", "NULL", "NULL", "NULL", "NULL"));
				}
			}
		} else
		{
			System.out.println("No openned Incidents currently");
		}

		return mylist;
	}

}
