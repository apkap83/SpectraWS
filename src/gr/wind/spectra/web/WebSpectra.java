package gr.wind.spectra.web;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElement;

import gr.wind.spectra.business.DB_Connection;
import gr.wind.spectra.business.DB_Operations;
import gr.wind.spectra.business.Help_Func;
import gr.wind.spectra.model.Product;
import gr.wind.spectra.model.ProductOfGetOutage;
import gr.wind.spectra.model.ProductOfModify;
import gr.wind.spectra.model.ProductOfSubmission;

@WebService // (endpointInterface = "gr.wind.spectra.web.WebSpectraInterface")
public class WebSpectra// implements WebSpectraInterface
{
	private static final String hierSep = "->";
	DB_Connection conObj;
	Connection conn;
	DB_Operations dbs;

	public WebSpectra()
	{

	}

	@WebMethod(exclude = true)
	public void establishDBConnection() throws Exception
	{
		try
		{
			this.conObj = new DB_Connection();
			this.conn = this.conObj.Connect();
			this.dbs = new DB_Operations(conn);
		} catch (Exception ex)
		{
			throw new Exception(ex.getMessage());
		}
	}

	@WebMethod()
	@WebResult(name = "Result")
	public List<Product> getHierarchy(
			// @WebParam(targetNamespace="http://spectra.wind.gr/handler/", name="UserName",
			// header = true, mode = Mode.IN) @XmlElement( required = true ) String
			// UserName,
			// @WebParam(targetNamespace="http://spectra.wind.gr/handler/", name="Password",
			// header = true) @XmlElement( required = true ) String Password,
			@WebParam(name = "UserName", header = true, mode = Mode.IN) String UserName,
			@WebParam(name = "Password", header = true, mode = Mode.IN) String Password,
			@WebParam(name = "RequestID") @XmlElement(required = true) String RequestID,
			@WebParam(name = "RequestTimestamp") @XmlElement(required = true) String RequestTimestamp,
			@WebParam(name = "SystemID") @XmlElement(required = true) String SystemID,
			@WebParam(name = "UserID") @XmlElement(required = true) String UserID,
			@WebParam(name = "Hierarchy") String Hierarchy) throws Exception
	{

		/*
		 * <internetCustomersAffected>34</potentialCustomersAffected>// unique user
		 * names from Internet Resource path
		 * <voiceCustomersAffected>34</potentialCustomersAffected> // unique user names
		 * from Voice Resource path <cLIsAffected>34</potentialCustomersAffected> //
		 * unique CLIs from Voice Resource path
		 */

		WebSpectra wb = new WebSpectra();
		try
		{
			wb.establishDBConnection();
			List<String> ElementsList = new ArrayList<String>();
			List<Product> prodElementsList = new ArrayList<>();

			// Check if Authentication credentials are correct.
			if (!wb.dbs.AuthenticateRequest(UserName, Password))
			{
				throw new InvalidInputException("User name or Password incorrect!", "Error 100");
			}

			wb.dbs.start();

			// Check if Required fields are empty
			Help_Func.ValidateNotEmpty("RequestID", RequestID);
			Help_Func.ValidateNotEmpty("SystemID", SystemID);
			Help_Func.ValidateNotEmpty("UserID", UserID);

			// Validate Date Formats if the fields are not empty
			if (!Help_Func.checkIfEmpty("RequestTimestamp", RequestTimestamp))
			{
				Help_Func.ValidateDateTimeFormat("RequestTimestamp", RequestTimestamp);
			}

			// No Hierarchy is given - returns root elements
			if (Hierarchy == null || Hierarchy.equals("") || Hierarchy.equals("?"))
			{
				// ElementsList =
				// wb.dbs.GetOneColumnUniqueResultSet("HierarchyTablePerTechnology2",
				// "RootHierarchyNode",
				// "1 = 1");

				ElementsList = wb.dbs.GetOneColumnUniqueResultSet("HierarchyTablePerTechnology2", "RootHierarchyNode",
						new String[] {}, new String[] {}, new String[] {});

				String[] nodeNames = new String[] {};
				String[] nodeValues = new String[] {};
				Product pr = new Product(wb.dbs, new String[] {}, new String[] {}, new String[] {}, Hierarchy,
						"rootElements", ElementsList, nodeNames, nodeValues, RequestID);
				prodElementsList.add(pr);
			} else
			{
				ArrayList<String> nodeNamesArrayList = new ArrayList<String>();
				ArrayList<String> nodeValuesArrayList = new ArrayList<String>();

				// Get root hierarchy String
				String rootElementInHierarchy = Help_Func.GetRootHierarchyNode(Hierarchy);

				// Get Hierarchy Table for that root hierarchy
				String table = wb.dbs.GetOneValue("HierarchyTablePerTechnology2", "HierarchyTableName",
						new String[] { "RootHierarchyNode" }, new String[] { rootElementInHierarchy },
						new String[] { "String" });

				// Get Hierarchy data in style :
				// OltElementName->OltSlot->OltPort->Onu->ElementName->Slot
				String fullHierarchyFromDB = wb.dbs.GetOneValue("HierarchyTablePerTechnology2",
						"HierarchyTableNamePath", new String[] { "RootHierarchyNode" },
						new String[] { rootElementInHierarchy }, new String[] { "String" });

				// Check Columns of Hierarchy against fullHierarchy (avoid wrong key values in
				// hierarchy e.g. SiteNa7me=AKADIMIAS)
				Help_Func.CheckColumnsOfHierarchyVSFullHierarchy(Hierarchy, fullHierarchyFromDB);

				// Split the hierarchy retrieved from DB into fields
				String[] fullHierarchyFromDBSplit = fullHierarchyFromDB.split("->");

				// Get Full Internet hierarchy in style :
				// OltElementName->OltSlot->OltPort->Onu->ActiveElement->Slot
				String fullDataSubsHierarchyFromDB = wb.dbs.GetOneValue("HierarchyTablePerTechnology2",
						"DataSubscribersTableNamePath", new String[] { "RootHierarchyNode" },
						new String[] { rootElementInHierarchy }, new String[] { "String" });

				// Split the Internet hierarchy retrieved from DB into fields
				String[] fullDataSubsHierarchyFromDBSplit = fullDataSubsHierarchyFromDB.split("->");

				// Get Full Voice hierarchy in style :
				// OltElementName->OltSlot->OltPort->Onu->ActiveElement->Slot
				String fullVoiceSubsHierarchyFromDB = wb.dbs.GetOneValue("HierarchyTablePerTechnology2",
						"VoiceSubscribersTableNamePath", new String[] { "RootHierarchyNode" },
						new String[] { rootElementInHierarchy }, new String[] { "String" });

				// Split the Internet hierarchy retrieved from DB into fields
				String[] fullVoiceSubsHierarchyFromDBSplit = fullVoiceSubsHierarchyFromDB.split("->");

				// Split given hierarchy
				String[] hierItemsGiven = Hierarchy.split(hierSep);

				// Check if max hierarchy level is surpassed
				// Max hierarchy level is fullHieararchyPath.length + 1
				int maxLevelsOfHierarchy = fullHierarchyFromDBSplit.length + 1;
				if (hierItemsGiven.length > maxLevelsOfHierarchy)
				{
					throw new InvalidInputException("More hierarchy levels than expected", "Error 120");
				}

				// If only root Hierarchy is given
				if (hierItemsGiven.length == 1)
				{
					// ElementsList = wb.dbs.GetOneColumnUniqueResultSet(table,
					// fullHierarchyFromDBSplit[0], " 1 = 1 ");

					ElementsList = wb.dbs.GetOneColumnUniqueResultSet(table, fullHierarchyFromDBSplit[0],
							new String[] {}, new String[] {}, new String[] {});

					String[] nodeNames = new String[] { rootElementInHierarchy };
					String[] nodeValues = new String[] { "1" };
					Product pr = new Product(wb.dbs, fullHierarchyFromDBSplit, fullDataSubsHierarchyFromDBSplit,
							fullVoiceSubsHierarchyFromDBSplit, Hierarchy, fullHierarchyFromDBSplit[0], ElementsList,
							nodeNames, nodeValues, RequestID);
					prodElementsList.add(pr);
				} else
				{
					// Check if Max hierarchy is used
					// FTTX->OltElementName=LAROAKDMOLT01->OltSlot=1->OltPort=0->Onu=0->ElementName=LAROAKDMOFLND010H11->Slot=4:
					// 7 MAX = FTTX + OltElementName->OltSlot->OltPort->Onu->ElementName->Slot
					if (hierItemsGiven.length < fullHierarchyFromDBSplit.length + 1)
					{
						// If a full hierarchy is given
						for (int i = 0; i < hierItemsGiven.length; i++)
						{
							if (i == 0)
							{
								nodeNamesArrayList.add(rootElementInHierarchy);
								nodeValuesArrayList.add("1");
								continue;
							}

							String[] keyValue = hierItemsGiven[i].split("=");
							nodeNamesArrayList.add(keyValue[0]);
							nodeValuesArrayList.add(keyValue[1]);
						}

						// ElementsList = wb.dbs.GetOneColumnUniqueResultSet(table,
						// fullHierarchyFromDBSplit[hierItemsGiven.length - 1],
						// Help_Func.HierarchyToPredicate(Hierarchy));

						ElementsList = wb.dbs.GetOneColumnUniqueResultSet(table,
								fullHierarchyFromDBSplit[hierItemsGiven.length - 1],
								Help_Func.HierarchyKeys(fullHierarchyFromDBSplit[hierItemsGiven.length - 1]),
								Help_Func.HierarchyValues(fullHierarchyFromDBSplit[hierItemsGiven.length - 1]),
								Help_Func.HierarchyStringTypes(fullHierarchyFromDBSplit[hierItemsGiven.length - 1]));

						String[] nodeNames = nodeNamesArrayList.toArray(new String[nodeNamesArrayList.size()]);
						String[] nodeValues = nodeValuesArrayList.toArray(new String[nodeValuesArrayList.size()]);
						Product pr = new Product(wb.dbs, fullHierarchyFromDBSplit, fullDataSubsHierarchyFromDBSplit,
								fullVoiceSubsHierarchyFromDBSplit, Hierarchy,
								fullHierarchyFromDBSplit[hierItemsGiven.length - 1], ElementsList, nodeNames,
								nodeValues, RequestID);
						prodElementsList.add(pr);
					} else
					{ // Max Hierarchy Level
						// If a full hierarchy is given
						for (int i = 0; i < hierItemsGiven.length; i++)
						{
							if (i == 0)
							{
								nodeNamesArrayList.add(rootElementInHierarchy);
								nodeValuesArrayList.add("1");
								continue;
							}

							String[] keyValue = hierItemsGiven[i].split("=");
							nodeNamesArrayList.add(keyValue[0]);
							nodeValuesArrayList.add(keyValue[1]);
						}

						ElementsList = new ArrayList<String>();
						String[] nodeNames = nodeNamesArrayList.toArray(new String[nodeNamesArrayList.size()]);
						String[] nodeValues = nodeValuesArrayList.toArray(new String[nodeValuesArrayList.size()]);
						Product pr = new Product(wb.dbs, fullHierarchyFromDBSplit, fullDataSubsHierarchyFromDBSplit,
								fullVoiceSubsHierarchyFromDBSplit, Hierarchy, "MaxLevel", ElementsList, nodeNames,
								nodeValues, RequestID);
						prodElementsList.add(pr);
					}
				}
			}

			return prodElementsList;
		} finally
		{
			wb.conObj.closeDBConnection();
		}

	}

	@WebMethod
	@WebResult(name = "Result")
	public List<ProductOfSubmission> submitOutage(
			@WebParam(name = "UserName", header = true, mode = Mode.IN) String UserName,
			@WebParam(name = "Password", header = true, mode = Mode.IN) String Password,
			@WebParam(name = "RequestTimestamp") @XmlElement(required = true) String RequestTimestamp,
			@WebParam(name = "SystemID") @XmlElement(required = true) String SystemID,
			@WebParam(name = "UserID") @XmlElement(required = true) String UserID,
			// Defines Uniquely The Incident
			@WebParam(name = "IncidentID") @XmlElement(required = true) String IncidentID,
			@WebParam(name = "Scheduled") @XmlElement(required = true) String Scheduled,
			@WebParam(name = "StartTime") @XmlElement(required = true) String StartTime,
			@WebParam(name = "EndTime") @XmlElement(required = false) String EndTime,
			@WebParam(name = "Duration") @XmlElement(required = false) String Duration,
			// TV, VOICE, DATA
			@WebParam(name = "AffectedServices") @XmlElement(required = true) String AffectedServices,
			// Quality, Loss
			@WebParam(name = "Impact") @XmlElement(required = true) String Impact,
			@WebParam(name = "Priority") @XmlElement(required = true) String Priority,
			@WebParam(name = "HierarchySelected") @XmlElement(required = true) String HierarchySelected)
			throws Exception
	{
		WebSpectra wb = new WebSpectra();

		try
		{
			wb.establishDBConnection();
			List<ProductOfSubmission> prodElementsList;
			prodElementsList = new ArrayList<>();
			int OutageID_Integer = 0;
			// Check if Authentication credentials are correct.
			if (!wb.dbs.AuthenticateRequest(UserName, Password))
			{
				throw new InvalidInputException("User name or Password incorrect!", "Error 100");
			}

			// Check if Required fields are not empty and they contain the desired values
			Help_Func.ValidateNotEmpty("RequestTimestamp", RequestTimestamp);
			if (!Help_Func.checkIfEmpty("RequestTimestamp", RequestTimestamp))
			{
				Help_Func.ValidateDateTimeFormat("RequestTimestamp", RequestTimestamp);
			}

			Help_Func.ValidateNotEmpty("StartTime", StartTime);
			if (!Help_Func.checkIfEmpty("StartTime", StartTime))
			{
				Help_Func.ValidateDateTimeFormat("StartTime", StartTime);
			}
			if (!Help_Func.checkIfEmpty("EndTime", EndTime))
			{
				Help_Func.ValidateDateTimeFormat("EndTime", EndTime);
			}

			Help_Func.ValidateNotEmpty("SystemID", SystemID);
			Help_Func.ValidateNotEmpty("UserID", UserID);
			Help_Func.ValidateNotEmpty("IncidentID", IncidentID);

			Help_Func.ValidateNotEmpty("Scheduled", Scheduled);
			Help_Func.ValidateAgainstPredefinedValues("Scheduled", Scheduled, new String[] { "Yes", "No" });

			Help_Func.ValidateIntegerOrEmptyValue("Duration", Duration);

			Help_Func.ValidateNotEmpty("AffectedServices", AffectedServices);
			Help_Func.ValidateDelimitedValues("AffectedServices", AffectedServices, "\\|",
					new String[] { "Voice", "Internet", "IPTV" });

			Help_Func.ValidateNotEmpty("Impact", Impact);
			Help_Func.ValidateAgainstPredefinedValues("Impact", Impact, new String[] { "QoS", "LoS" });

			Help_Func.ValidateNotEmpty("Priority", Priority);
			Help_Func.ValidateAgainstPredefinedValues("Priority", Priority,
					new String[] { "Critical", "Medium", "Low" });

			Help_Func.ValidateNotEmpty("HierarchySelected", HierarchySelected);

			// Split to % and to | the hierarchy provided
			List<String> myHier = Help_Func.GetHierarchySelections(HierarchySelected);

			// Get Max Outage ID (type int)
			OutageID_Integer = wb.dbs.GetMaxIntegerValue("SubmittedIncidents", "OutageID");

			// Services affected
			String[] servicesAffected = AffectedServices.split("\\|");

			// Calculate Total number per Indicent, of customers affected per incident
			int incidentDataCustomersAffected = 0;
			int incidentVoiceCustomersAffected = 0;
			for (String service : servicesAffected)
			{
				for (int i = 0; i < myHier.size(); i++)
				{
					// If the sumbission contains only root hierarchy then STOP submission
					if (!myHier.get(i).contains("="))
					{
						throw new InvalidInputException("Cannot submit Incident for an invalid/root only hierarchy",
								"Error 900");
					}

					// Check Hierarchy Format Key_Value Pairs
					Help_Func.checkHierarchyFormatKeyValuePairs(myHier.get(i).toString());

					// Firstly determine the hierarchy table that will be used based on the root
					// hierarchy provided
					String rootHierarchySelected = Help_Func.GetRootHierarchyNode(myHier.get(i).toString());

					// Get Hierarchy data in style :
					// OltElementName->OltSlot->OltPort->Onu->ElementName->Slot
					String fullHierarchyFromDB = wb.dbs.GetOneValue("HierarchyTablePerTechnology2",
							"HierarchyTableNamePath", new String[] { "RootHierarchyNode" },
							new String[] { rootHierarchySelected }, new String[] { "String" });

					// Check Columns of Hierarchy against fullHierarchy (avoid wrong key values in
					// hierarchy e.g. SiteNa7me=AKADIMIAS)
					Help_Func.CheckColumnsOfHierarchyVSFullHierarchy(myHier.get(i).toString(), fullHierarchyFromDB);

					// Determine Tables for Data/Voice subscribers
					String dataSubsTable = wb.dbs.GetOneValue("HierarchyTablePerTechnology2",
							"DataSubscribersTableName", new String[] { "RootHierarchyNode" },
							new String[] { rootHierarchySelected }, new String[] { "String" });

					String voiceSubsTable = wb.dbs.GetOneValue("HierarchyTablePerTechnology2",
							"VoiceSubscribersTableName", new String[] { "RootHierarchyNode" },
							new String[] { rootHierarchySelected }, new String[] { "String" });

					// Get Hierarchies for Data/Voice Tables
					String fullDataHierarchyPath = wb.dbs.GetOneValue("HierarchyTablePerTechnology2",
							"DataSubscribersTableNamePath", new String[] { "RootHierarchyNode" },
							new String[] { rootHierarchySelected }, new String[] { "String" });

					String[] fullDataHierarchyPathSplit = fullDataHierarchyPath.split("->");

					String fullVoiceHierarchyPath = wb.dbs.GetOneValue("HierarchyTablePerTechnology2",
							"VoiceSubscribersTableNamePath", new String[] { "RootHierarchyNode" },
							new String[] { rootHierarchySelected }, new String[] { "String" });

					String[] fullVoiceHierarchyPathSplit = fullVoiceHierarchyPath.split("->");

					// Count distinct values of Usernames or CliVlaues in the respective columns
					String dataCustomersAffected = wb.dbs.CountDistinctRowsForSpecificColumn(dataSubsTable, "Username",
							Help_Func.HierarchyKeys(Help_Func.ReplaceHierarchyForSubscribersAffected(
									myHier.get(i).toString(), fullDataHierarchyPathSplit)),
							Help_Func.HierarchyValues(Help_Func.ReplaceHierarchyForSubscribersAffected(
									myHier.get(i).toString(), fullDataHierarchyPathSplit)),
							Help_Func.HierarchyStringTypes(Help_Func.ReplaceHierarchyForSubscribersAffected(
									myHier.get(i).toString(), fullDataHierarchyPathSplit)));

					String voiceCustomersAffected = wb.dbs.CountDistinctRowsForSpecificColumns(voiceSubsTable,
							new String[] { "ActiveElement", "Subrack", "Slot", "Port", "PON" },
							Help_Func.HierarchyKeys(Help_Func.ReplaceHierarchyForSubscribersAffected(
									myHier.get(i).toString(), fullVoiceHierarchyPathSplit)),
							Help_Func.HierarchyValues(Help_Func.ReplaceHierarchyForSubscribersAffected(
									myHier.get(i).toString(), fullVoiceHierarchyPathSplit)),
							Help_Func.HierarchyStringTypes(Help_Func.ReplaceHierarchyForSubscribersAffected(
									myHier.get(i).toString(), fullVoiceHierarchyPathSplit)));

					// For Voice no data customers are affected and vice versa
					if (service.equals("Voice"))
					{
						dataCustomersAffected = "0";
					} else if (service.equals("Internet"))
					{
						voiceCustomersAffected = "0";
					}

					incidentDataCustomersAffected += Integer.parseInt(dataCustomersAffected);
					incidentVoiceCustomersAffected += Integer.parseInt(voiceCustomersAffected);

					System.out.println("incidentDataCustomersAffected = " + incidentDataCustomersAffected);
					System.out.println("incidentVoiceCustomersAffected = " + incidentVoiceCustomersAffected);
				}
			}

			// Check if for the same Incident ID, Service & Hierarchy - We have already an
			// entry
			for (String service : servicesAffected)
			{
				for (int i = 0; i < myHier.size(); i++)
				{
					boolean incidentAlreadyExists = wb.dbs.CheckIfCriteriaExists("SubmittedIncidents",
							new String[] { "IncidentStatus", "IncidentID", "AffectedServices", "HierarchySelected" },
							new String[] { "OPEN", IncidentID, service, myHier.get(i).toString() },
							new String[] { "String", "String", "String", "String" });

					if (incidentAlreadyExists)
					{
						throw new InvalidInputException("There is already an openned incident (" + IncidentID
								+ ") that defines outage for AffectedService = " + service + " and HierarchySelected = "
								+ myHier.get(i).toString(), "Error 195");
					}
				}
			}

			// Calculate Sum of Voice/Data Customers affected for potentially already
			// openned
			// same incident
			String numberOfVoiceCustAffectedFromPreviousIncidents = "0";
			String numberOfDataCustAffectedFromPreviousIncidents = "0";

			if (wb.dbs.CheckIfCriteriaExists("SubmittedIncidents", new String[] { "IncidentID" },
					new String[] { IncidentID }, new String[] { "String" }))
			{
				numberOfVoiceCustAffectedFromPreviousIncidents = wb.dbs.MaxNumberOfCustomersAffected(
						"SubmittedIncidents", "IncidentAffectedVoiceCustomers", new String[] { "IncidentID" },
						new String[] { IncidentID });
				numberOfDataCustAffectedFromPreviousIncidents = wb.dbs.MaxNumberOfCustomersAffected(
						"SubmittedIncidents", "IncidentAffectedDataCustomers", new String[] { "IncidentID" },
						new String[] { IncidentID });

				System.out.println("numberOfVoiceCustAffectedFromPreviousIncidents = "
						+ numberOfVoiceCustAffectedFromPreviousIncidents);
				System.out.println("numberOfDataCustAffectedFromPreviousIncidents = "
						+ numberOfDataCustAffectedFromPreviousIncidents);
			}

			for (String service : servicesAffected)
			{
				for (int i = 0; i < myHier.size(); i++)
				{
					// Add One
					OutageID_Integer += 1;

					// Check Hierarchy Format Key_Value Pairs
					Help_Func.checkHierarchyFormatKeyValuePairs(myHier.get(i).toString());

					// Firstly determine the hierarchy table that will be used based on the root
					// hierarchy provided
					String rootHierarchySelected = Help_Func.GetRootHierarchyNode(myHier.get(i).toString());

					// Determine Tables for Data/Voice subscribers
					String dataSubsTable = wb.dbs.GetOneValue("HierarchyTablePerTechnology2",
							"DataSubscribersTableName", new String[] { "RootHierarchyNode" },
							new String[] { rootHierarchySelected }, new String[] { "String" });

					String voiceSubsTable = wb.dbs.GetOneValue("HierarchyTablePerTechnology2",
							"VoiceSubscribersTableName", new String[] { "RootHierarchyNode" },
							new String[] { rootHierarchySelected }, new String[] { "String" });

					// Get Hierarchies for Data/Voice Tables
					String fullDataHierarchyPath = wb.dbs.GetOneValue("HierarchyTablePerTechnology2",
							"DataSubscribersTableNamePath", new String[] { "RootHierarchyNode" },
							new String[] { rootHierarchySelected }, new String[] { "String" });

					String[] fullDataHierarchyPathSplit = fullDataHierarchyPath.split("->");
					String fullVoiceHierarchyPath = wb.dbs.GetOneValue("HierarchyTablePerTechnology2",
							"VoiceSubscribersTableNamePath", new String[] { "RootHierarchyNode" },
							new String[] { rootHierarchySelected }, new String[] { "String" });

					String[] fullVoiceHierarchyPathSplit = fullVoiceHierarchyPath.split("->");

					// Count distinct values of Usernames or CliVlaues the respective columns
					String dataCustomersAffected = wb.dbs.CountDistinctRowsForSpecificColumn(dataSubsTable, "Username",
							Help_Func.HierarchyKeys(Help_Func.ReplaceHierarchyForSubscribersAffected(
									myHier.get(i).toString(), fullDataHierarchyPathSplit)),
							Help_Func.HierarchyValues(Help_Func.ReplaceHierarchyForSubscribersAffected(
									myHier.get(i).toString(), fullDataHierarchyPathSplit)),
							Help_Func.HierarchyStringTypes(Help_Func.ReplaceHierarchyForSubscribersAffected(
									myHier.get(i).toString(), fullDataHierarchyPathSplit)));

					String voiceCustomersAffected = wb.dbs.CountDistinctRowsForSpecificColumns(voiceSubsTable,
							new String[] { "ActiveElement", "Subrack", "Slot", "Port", "PON" },
							Help_Func.HierarchyKeys(Help_Func.ReplaceHierarchyForSubscribersAffected(
									myHier.get(i).toString(), fullVoiceHierarchyPathSplit)),
							Help_Func.HierarchyValues(Help_Func.ReplaceHierarchyForSubscribersAffected(
									myHier.get(i).toString(), fullVoiceHierarchyPathSplit)),
							Help_Func.HierarchyStringTypes(Help_Func.ReplaceHierarchyForSubscribersAffected(
									myHier.get(i).toString(), fullVoiceHierarchyPathSplit)));

					String CLIsAffected = wb.dbs.CountDistinctRowsForSpecificColumn(voiceSubsTable, "CliValue",
							Help_Func.HierarchyKeys(Help_Func.ReplaceHierarchyForSubscribersAffected(
									myHier.get(i).toString(), fullVoiceHierarchyPathSplit)),
							Help_Func.HierarchyValues(Help_Func.ReplaceHierarchyForSubscribersAffected(
									myHier.get(i).toString(), fullVoiceHierarchyPathSplit)),
							Help_Func.HierarchyStringTypes(Help_Func.ReplaceHierarchyForSubscribersAffected(
									myHier.get(i).toString(), fullVoiceHierarchyPathSplit)));

					// For Voice no data customers are affected and vice versa
					if (service.equals("Voice"))
					{
						dataCustomersAffected = "0";
					} else if (service.equals("Internet"))
					{
						voiceCustomersAffected = "0";
						CLIsAffected = "0";
					}

					// Convert it to String (only for the sake of the below method
					// (InsertValuesInTableGetSequence) - In the database it is still an integer
					String OutageID_String = Integer.toString(OutageID_Integer);

					// Sum Customers Affected from Previous but same Incidents that were inserted in
					// the
					// past
					int totalVoiceIncidentAffected = incidentVoiceCustomersAffected
							+ Integer.parseInt(numberOfVoiceCustAffectedFromPreviousIncidents);
					int totalDataIncidentAffected = incidentDataCustomersAffected
							+ Integer.parseInt(numberOfDataCustAffectedFromPreviousIncidents);

					// Insert Values in Database
					wb.dbs.InsertValuesInTable("SubmittedIncidents",
							new String[] { "DateTime", "OutageID", "IncidentStatus", "RequestTimestamp", "SystemID",
									"UserID", "IncidentID", "Scheduled", "StartTime", "EndTime", "Duration",
									"AffectedServices", "Impact", "Priority", "HierarchySelected",
									"AffectedVoiceCustomers", "AffectedDataCustomers", "AffectedCLICustomers",
									"ActiveDataCustomersAffected", "TVCustomersAffected",
									"IncidentAffectedVoiceCustomers", "IncidentAffectedDataCustomers" },
							new String[] { Help_Func.now(), OutageID_String, "OPEN", RequestTimestamp, SystemID, UserID,
									IncidentID, Scheduled, StartTime, EndTime, Duration, service, Impact, Priority,
									myHier.get(i).toString(), voiceCustomersAffected, dataCustomersAffected,
									CLIsAffected, "0", "0", Integer.toString(totalVoiceIncidentAffected),
									Integer.toString(totalDataIncidentAffected) },
							new String[] { "DateTime", "Integer", "String", "DateTime", "String", "String", "String",
									"String", "DateTime", "DateTime", "String", "String", "String", "String", "String",
									"Integer", "Integer", "Integer", "Integer", "Integer", "Integer", "Integer" });

					if (Integer.parseInt(OutageID_String) > 0)
					{
						ProductOfSubmission ps = new ProductOfSubmission(OutageID_String, IncidentID,
								voiceCustomersAffected, dataCustomersAffected, CLIsAffected,
								Integer.toString(totalVoiceIncidentAffected),
								Integer.toString(totalDataIncidentAffected), "1", service, myHier.get(i).toString(),
								"Submitted Successfully");

						prodElementsList.add(ps);
					}
				}
			}

			return prodElementsList;

		} finally
		{
			// wb.conObj.closeDBConnection();
		}
	}

	@WebMethod
	@WebResult(name = "Result")
	public List<ProductOfGetOutage> getOutageStatus(
			@WebParam(name = "UserName", header = true, mode = Mode.IN) String UserName,
			@WebParam(name = "Password", header = true, mode = Mode.IN) String Password,
			// Defines Uniquely The Incident
			@WebParam(name = "IncidentID") @XmlElement(required = true) String IncidentID,
			@WebParam(name = "IncidentStatus") @XmlElement(required = true) String IncidentStatus) throws Exception
	{
		WebSpectra wb = new WebSpectra();

		try
		{
			wb.establishDBConnection();
			List<ProductOfGetOutage> prodElementsList;
			prodElementsList = new ArrayList<>();

			// Check if fields are empty
			Help_Func.ValidateNotEmpty("IncidentID", IncidentID);
			Help_Func.ValidateNotEmpty("IncidentStatus", IncidentStatus);

			// Check if Authentication credentials are correct.
			if (!wb.dbs.AuthenticateRequest(UserName, Password))
			{
				throw new InvalidInputException("User name or Password incorrect!", "Error 100");
			}

			String numOfRows = "0";
			ResultSet rs = null;
			if (IncidentID.equals("*"))
			{
				// Number of rows that will be returned
				// numOfRows = wb.dbs.NumberOfRowsFound("SubmittedIncidents", "IncidentStatus =
				// '" + IncidentStatus + "'");

				numOfRows = wb.dbs.NumberOfRowsFound("SubmittedIncidents", new String[] { "IncidentStatus" },
						new String[] { IncidentStatus }, new String[] { "String" });

				rs = wb.dbs.GetRows("SubmittedIncidents",
						new String[] { "OutageID", "IncidentStatus", "RequestTimestamp", "SystemID", "UserID",
								"IncidentID", "Scheduled", "StartTime", "EndTime", "Duration", "AffectedServices",
								"Impact", "Priority", "Hierarchyselected", "AffectedVoiceCustomers",
								"AffectedDataCustomers", "AffectedCLICustomers", "ActiveDataCustomersAffected",
								"TVCustomersAffected", "IncidentAffectedVoiceCustomers",
								"IncidentAffectedDataCustomers" },
						new String[] { "IncidentStatus" }, new String[] { IncidentStatus }, new String[] { "String" });
			} else
			{
				numOfRows = wb.dbs.NumberOfRowsFound("SubmittedIncidents",
						new String[] { "IncidentID", "IncidentStatus" }, new String[] { IncidentID, IncidentStatus },
						new String[] { "String", "String" });

				rs = wb.dbs.GetRows("SubmittedIncidents",
						new String[] { "OutageID", "IncidentStatus", "RequestTimestamp", "SystemID", "UserID",
								"IncidentID", "Scheduled", "StartTime", "EndTime", "Duration", "AffectedServices",
								"Impact", "Priority", "Hierarchyselected", "AffectedVoiceCustomers",
								"AffectedDataCustomers", "AffectedCLICustomers", "ActiveDataCustomersAffected",
								"TVCustomersAffected", "IncidentAffectedVoiceCustomers",
								"IncidentAffectedDataCustomers" },
						new String[] { "IncidentID", "IncidentStatus" }, new String[] { IncidentID, IncidentStatus },
						new String[] { "String", "String" });
			}
			if (Integer.parseInt(numOfRows) == 0)
			{
				throw new InvalidInputException("No Results found", "No Results found according to your criteria");
			} else
			{
				while (rs.next())
				{
					ProductOfGetOutage pg = new ProductOfGetOutage(rs.getString("OutageID"),
							rs.getString("IncidentStatus"), rs.getString("RequestTimestamp"), rs.getString("SystemID"),
							rs.getString("UserID"), rs.getString("IncidentID"), rs.getString("Scheduled"),
							rs.getString("StartTime"), rs.getString("EndTime"), rs.getString("Duration"),
							rs.getString("AffectedServices"), rs.getString("Impact"), rs.getString("Priority"),
							rs.getString("Hierarchyselected"), rs.getString("AffectedVoiceCustomers"),
							rs.getString("AffectedDataCustomers"), rs.getString("AffectedCLICustomers"),
							rs.getString("ActiveDataCustomersAffected"), rs.getString("TVCustomersAffected"),
							rs.getString("IncidentAffectedVoiceCustomers"),
							rs.getString("IncidentAffectedDataCustomers")

					);
					prodElementsList.add(pg);
				}
			}
			return prodElementsList;
		} finally
		{
			wb.conObj.closeDBConnection();
		}
	}

	@WebMethod
	@WebResult(name = "Result")
	public ProductOfModify modifyOutage(@WebParam(name = "UserName", header = true, mode = Mode.IN) String UserName,
			@WebParam(name = "Password", header = true, mode = Mode.IN) String Password,
			@WebParam(name = "RequestTimestamp") @XmlElement(required = true) String RequestTimestamp,
			@WebParam(name = "SystemID") @XmlElement(required = true) String SystemID,
			@WebParam(name = "UserID") @XmlElement(required = true) String UserID,
			@WebParam(name = "IncidentID") @XmlElement(required = true) String IncidentID,
			@WebParam(name = "OutageID") @XmlElement(required = true) String OutageID,
			@WebParam(name = "StartTime") @XmlElement(required = false) String StartTime,
			@WebParam(name = "EndTime") @XmlElement(required = false) String EndTime,
			@WebParam(name = "Duration") @XmlElement(required = false) String Duration,
			// Quality, Loss
			@WebParam(name = "Impact") @XmlElement(required = false) String Impact) throws Exception
	{
		WebSpectra wb = new WebSpectra();

		try
		{
			wb.establishDBConnection();
			// Check if Authentication credentials are correct.
			if (!wb.dbs.AuthenticateRequest(UserName, Password))
			{
				throw new InvalidInputException("User name or Password incorrect!", "Error 100");
			}

			ProductOfModify pom = null;

			// Check if Required fields are empty
			Help_Func.ValidateNotEmpty("RequestTimestamp", RequestTimestamp);
			Help_Func.ValidateDateTimeFormat("RequestTimestamp", RequestTimestamp);
			Help_Func.ValidateNotEmpty("SystemID", SystemID);
			Help_Func.ValidateNotEmpty("UserID", UserID);
			Help_Func.ValidateNotEmpty("IncidentID", IncidentID);
			Help_Func.ValidateNotEmpty("OutageID", OutageID);

			// if Start Time Value Exists
			if (!Help_Func.checkIfEmpty("StartTime", StartTime))
			{
				// Check if it has the appropriate format
				Help_Func.ValidateDateTimeFormat("StartTime", StartTime);
			}
			// if End Time Value Exists
			if (!Help_Func.checkIfEmpty("EndTime", EndTime))
			{
				// Check if it has the appropriate format
				Help_Func.ValidateDateTimeFormat("EndTime", EndTime);
			}

			// if Impact Value Exists
			if (!Help_Func.checkIfEmpty("Impact", Impact))
			{
				// Check if it has the appropriate format
				Help_Func.ValidateAgainstPredefinedValues("Impact", Impact, new String[] { "QoS", "LoS" });
			}

			// if Duration Value Exists
			if (!Help_Func.checkIfEmpty("Duration", Duration))
			{
				// Check if it has the appropriate format
				Help_Func.ValidateIntegerOrEmptyValue("Duration", Duration);
			}

			// Check if the combination of IncidentID & OutageID exists
			boolean incidentPlusOutageExists = wb.dbs.CheckIfCriteriaExists("SubmittedIncidents",
					new String[] { "IncidentID", "OutageID" }, new String[] { IncidentID, OutageID },
					new String[] { "String", "String" });

			if (incidentPlusOutageExists)
			{
				// Check if the combination of IncidentID & OutageID refers to a scheduled
				// Incident (Scheduled = "Yes")
				boolean incidentIsScheduled = wb.dbs.CheckIfCriteriaExists("SubmittedIncidents",
						new String[] { "IncidentID", "OutageID", "Scheduled" },
						new String[] { IncidentID, OutageID, "Yes" }, new String[] { "String", "String", "String" });
				// Create a new list with the updated columns - based on what is empty or not
				List<String> listOfColumnsForUpdate = new ArrayList<>();
				List<String> listOfValuesForUpdate = new ArrayList<>();
				List<String> listOfDataTypesForUpdate = new ArrayList<>();

				if (!Help_Func.checkIfEmpty("StartTime", StartTime))
				{
					listOfColumnsForUpdate.add("StartTime");
					listOfValuesForUpdate.add(StartTime);
					listOfDataTypesForUpdate.add("Date");
				}

				if (!Help_Func.checkIfEmpty("EndTime", EndTime))
				{
					listOfColumnsForUpdate.add("EndTime");
					listOfValuesForUpdate.add(EndTime);
					listOfDataTypesForUpdate.add("Date");
				}

				if (!Help_Func.checkIfEmpty("Impact", Impact))
				{
					listOfColumnsForUpdate.add("Impact");
					listOfValuesForUpdate.add(Impact);
					listOfDataTypesForUpdate.add("String");
				}

				if (!Help_Func.checkIfEmpty("Duration", Duration))
				{
					listOfColumnsForUpdate.add("Duration");
					listOfValuesForUpdate.add(Duration);
					listOfDataTypesForUpdate.add("Integer");
				}

				String[] arrayOfColumnsForUpdate = listOfColumnsForUpdate
						.toArray(new String[listOfColumnsForUpdate.size()]);
				String[] arrayOfValuesForUpdate = listOfValuesForUpdate
						.toArray(new String[listOfValuesForUpdate.size()]);
				String[] arrayOfDataTypesForUpdate = listOfDataTypesForUpdate
						.toArray(new String[listOfDataTypesForUpdate.size()]);

				System.out.println("incidentIsScheduled = " + incidentIsScheduled);

				// Update Start/End Times ONLY for Scheduled Incidents
				if (!incidentIsScheduled && (!Help_Func.checkIfEmpty("StartTime", StartTime)
						|| (!Help_Func.checkIfEmpty("EndTime", EndTime))))
				{
					throw new InvalidInputException(
							"The fields of 'Star Time'/'End Time' cannot be modified on non scheduled Outages (Incident: "
									+ IncidentID + ", OutageID " + OutageID + " is not a scheduled incident)",
							"Error 385");
				}

				// Update Operation
				int numOfRowsUpdated = wb.dbs.UpdateColumnOnSpecificCriteria("SubmittedIncidents",
						arrayOfColumnsForUpdate, arrayOfValuesForUpdate, arrayOfDataTypesForUpdate,
						new String[] { "IncidentID", "OutageID" }, new String[] { IncidentID, OutageID },
						new String[] { "String", "Integer" });

				if (numOfRowsUpdated == 1)
				{
					pom = new ProductOfModify(
							"Successfully Modified Incident: " + IncidentID + " - Outage ID: " + OutageID);
				} else
				{
					pom = new ProductOfModify("Error modifying incident!");
				}
			} else
			{
				throw new InvalidInputException("The combination of IncidentID: " + IncidentID + " and OutageID: "
						+ OutageID + " does not exist!", "Error 550");
			}

			// Return instance of class ProductOfModify
			return pom;
		} finally
		{
			// Close DB Connection
			wb.conObj.closeDBConnection();
		}
	}

	@WebMethod
	@WebResult(name = "Result")
	public List<ProductOfSubmission> closeOutage(
			@WebParam(name = "UserName", header = true, mode = Mode.IN) String UserName,
			@WebParam(name = "Password", header = true, mode = Mode.IN) String Password,
			@WebParam(name = "RequestID") @XmlElement(required = true) String RequestID,
			@WebParam(name = "RequestTimestamp") @XmlElement(required = true) String RequestTimestamp,
			@WebParam(name = "SystemID") @XmlElement(required = true) String SystemID,
			@WebParam(name = "UserID") @XmlElement(required = true) String UserID,

			// Defines Uniquely The Incident
			@WebParam(name = "IncidentID") @XmlElement(required = true) String IncidentID,
			@WebParam(name = "EndTime") @XmlElement(required = false) String EndTime

	) throws InstantiationException, IllegalAccessException, ClassNotFoundException, Exception, InvalidInputException
	{
		// try {
		// boolean result = wb.dbs.InsertValuesInTable("SubmittedIncidents", new
		// String[] {"RequestID", "UserID"}, new String[] {RequestID, UserID});
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		WebSpectra wb = new WebSpectra();
		wb.conObj.closeDBConnection();

		// Check if Authentication credentials are correct.
		if (!wb.dbs.AuthenticateRequest(UserName, Password))
		{
			throw new InvalidInputException("User name or Password incorrect!", "Error 100");
		}

		return null;
	}

	/*
	 * public static void main(String args[]) throws Exception { WebSpectraInterface
	 * ws = new WebSpectra(); List<String> myList = new ArrayList<String>();
	 *
	 * myList = ws.getFTTXHierarchy("ATHOARTMBOLT01", null, null, null, null);
	 *
	 * for (String item : myList) { System.out.println(item); }
	 *
	 * }
	 */
}
