package gr.wind.spectra.business;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import gr.wind.spectra.model.ProductOfNLUActive;
import gr.wind.spectra.web.InvalidInputException;

public class CLIOutage
{
	private DB_Operations dbs;
	private String requestID;
	DateFormat dateFormat = new SimpleDateFormat(Help_Func.DATE_FORMAT);

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

	public ProductOfNLUActive checkCLIOutage(String CLIProvided, String ServiceType)
			throws SQLException, InvalidInputException, ParseException
	{
		ProductOfNLUActive ponla = new ProductOfNLUActive();
		boolean foundAtLeastOneCLIAffected = false;
		boolean voiceAffected = false;
		boolean dataAffected = false;

		String allAffectedServices = "";

		// Check if we have at least one OPEN incident
		boolean weHaveOpenIncident = dbs.checkIfStringExistsInSpecificColumn("SubmittedIncidents", "IncidentStatus",
				"OPEN");

		// Split ServiceType
		String delimiterCharacter = "\\|";
		String[] ServiceTypeSplitted = ServiceType.split(delimiterCharacter);

		// If We have at least one opened incident...
		if (weHaveOpenIncident)
		{

			String IncidentID = "";
			int OutageID = 0;
			String HierarchySelected = "";
			String Priority = "";
			String AffectedServices = "";
			String Scheduled = "";
			String Duration = "";
			Date StartTime = null;
			Date EndTime = null;
			String Impact = "";
			String EndTimeString = null;

			for (String service : ServiceTypeSplitted)
			{
				ResultSet rs = null;
				// Get Lines with IncidentStatus = "OPEN"
				rs = dbs.getRows("SubmittedIncidents",
						new String[] { "IncidentID", "OutageID", "HierarchySelected", "Priority", "AffectedServices",
								"Scheduled", "Duration", "StartTime", "EndTime", "Impact" },
						new String[] { "IncidentStatus" }, new String[] { "OPEN" }, new String[] { "String" });

				while (rs.next())
				{
					boolean isOutageWithinScheduledRange = false;

					IncidentID = rs.getString("IncidentID");
					OutageID = rs.getInt("OutageID");
					HierarchySelected = rs.getString("HierarchySelected");
					Priority = rs.getString("Priority");
					AffectedServices = rs.getString("AffectedServices");
					Scheduled = rs.getString("Scheduled");
					Duration = rs.getString("Duration");
					StartTime = rs.getDate("StartTime");
					EndTime = rs.getDate("EndTime");
					Impact = rs.getString("Impact");

					System.out.println("SCHEDULED = " + Scheduled);

					// If it is OPEN & Scheduled & Date(Now) > StartTime then set
					// isOutageWithinScheduledRange to TRUE
					if (Scheduled.equals("Yes"))
					{
						// Get current date
						LocalDateTime now = LocalDateTime.now();

						// Convert StartTime date to LocalDateTime object
						LocalDateTime StartTimeInLocalDateTime = Instant.ofEpochMilli(StartTime.getTime())
								.atZone(ZoneId.systemDefault()).toLocalDateTime();

						// if Start time is after NOW and it is still OPEN
						if (now.isAfter(StartTimeInLocalDateTime))
						{
							isOutageWithinScheduledRange = true;
						} else
						{
							isOutageWithinScheduledRange = false;
						}

						System.out.println("isOutageWithinScheduledRange = " + isOutageWithinScheduledRange);
					}

					// if service given in web request is Voice
					if (AffectedServices.equals("Voice") && service.equals("Voice"))
					{
						System.out.println("Inside VOICE");
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
						if (Integer.parseInt(numOfRowsFound) > 0 && Scheduled.equals("No"))
						{
							foundAtLeastOneCLIAffected = true;
							voiceAffected = true;
						} else if (Integer.parseInt(numOfRowsFound) > 0 && Scheduled.equals("Yes")
								&& isOutageWithinScheduledRange)
						{
							foundAtLeastOneCLIAffected = true;
							voiceAffected = true;
						}

						System.out.println("Number of rows found ARE: " + numOfRowsFound);
					} else if (AffectedServices.equals("Data") && service.equals("Data"))
					{
						System.out.println("Inside DATA");
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

						System.out.println("numOfRowsFound = " + numOfRowsFound);

						// Scheduled No & Rows Found
						if (Integer.parseInt(numOfRowsFound) > 0 && Scheduled.equals("No"))
						{
							foundAtLeastOneCLIAffected = true;
							dataAffected = true;

							// Scheduled Yes & Rows Found & Outage Within Scheduled Range
						} else if (Integer.parseInt(numOfRowsFound) > 0 && Scheduled.equals("Yes")
								&& isOutageWithinScheduledRange)
						{
							foundAtLeastOneCLIAffected = true;
							dataAffected = true;
						}
					}
				}

				// CLI is not affected from outage
				if (!foundAtLeastOneCLIAffected)
				{
					throw new InvalidInputException("No service affection", "Info 425");
				} else
				{
					// Indicate Voice, Data or Voice|Data service affection
					if (voiceAffected && dataAffected)
					{
						allAffectedServices = "Voice|Data";
					} else if (voiceAffected)
					{
						allAffectedServices = "Voice";
					} else if (dataAffected)
					{
						allAffectedServices = "Data";
					}

					ponla = new ProductOfNLUActive(this.requestID, CLIProvided, Priority, allAffectedServices,
							Scheduled, Duration, EndTimeString, Impact, "NULL", "NULL", "NULL");
				}
			}
		} else
		{
			throw new InvalidInputException("No service affection", "Info 425");
		}

		return ponla;
	}

}