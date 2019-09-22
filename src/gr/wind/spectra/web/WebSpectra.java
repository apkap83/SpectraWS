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

import gr.wind.spectra.business.CLIOutage;
import gr.wind.spectra.business.DB_Connection;
import gr.wind.spectra.business.DB_Operations;
import gr.wind.spectra.business.Help_Func;
import gr.wind.spectra.model.ProductOfCloseOutage;
import gr.wind.spectra.model.ProductOfGetHierarchy;
import gr.wind.spectra.model.ProductOfGetOutage;
import gr.wind.spectra.model.ProductOfModify;
import gr.wind.spectra.model.ProductOfNLUActive;
import gr.wind.spectra.model.ProductOfSubmission;

@WebService(endpointInterface = "gr.wind.spectra.web.InterfaceWebSpectra")
public class WebSpectra implements InterfaceWebSpectra
{
	private static final String hierSep = "->";
	private DB_Connection conObj;
	private Connection conn;
	private DB_Operations dbs;

	// Logger instance
	// private static final Logger logger =
	// LogManager.getLogger(gr.wind.spectra.web.WebSpectra.class.getName());

	public WebSpectra()
	{

	}

	@Override
	@WebMethod(exclude = true)
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

	@Override
	@WebMethod()
	@WebResult(name = "Result")
	public List<ProductOfGetHierarchy> getHierarchy(
			@WebParam(name = "UserName", header = true, mode = Mode.IN) String UserName,
			@WebParam(name = "Password", header = true, mode = Mode.IN) String Password,
			@WebParam(name = "RequestID") @XmlElement(required = true) String RequestID,
			@WebParam(name = "RequestTimestamp") @XmlElement(required = true) String RequestTimestamp,
			@WebParam(name = "SystemID") @XmlElement(required = true) String SystemID,
			@WebParam(name = "UserID") @XmlElement(required = true) String UserID,
			@WebParam(name = "Hierarchy") String Hierarchy) throws Exception, InvalidInputException
	{

		/*
		 * <DataCustomersAffected>34</potentialCustomersAffected>// unique user names
		 * from Data Resource path
		 * <voiceCustomersAffected>34</potentialCustomersAffected> // unique user names
		 * from Voice Resource path <cLIsAffected>34</potentialCustomersAffected> //
		 * unique CLIs from Voice Resource path
		 */

		WebSpectra wb = new WebSpectra();
		try
		{
			wb.establishDBConnection();
			List<String> ElementsList = new ArrayList<String>();
			List<ProductOfGetHierarchy> prodElementsList = new ArrayList<>();

			// Check if Authentication credentials are correct.
			if (!wb.dbs.authenticateRequest(UserName, Password))
			{
				throw new InvalidInputException("User name or Password incorrect!", "Error 100");
			}

			// Check if Required fields are empty
			Help_Func.validateNotEmpty("RequestID", RequestID);
			Help_Func.validateNotEmpty("SystemID", SystemID);
			Help_Func.validateNotEmpty("UserID", UserID);

			// Validate Date Formats if the fields are not empty
			if (!Help_Func.checkIfEmpty("RequestTimestamp", RequestTimestamp))
			{
				Help_Func.validateDateTimeFormat("RequestTimestamp", RequestTimestamp);
			}

			// No Hierarchy is given - returns root elements
			if (Hierarchy == null || Hierarchy.equals("") || Hierarchy.equals("?"))
			{
				// ElementsList =
				// wb.dbs.GetOneColumnUniqueResultSet("HierarchyTablePerTechnology2",
				// "RootHierarchyNode",
				// "1 = 1");

				ElementsList = wb.dbs.getOneColumnUniqueResultSet("HierarchyTablePerTechnology2", "RootHierarchyNode",
						new String[] {}, new String[] {}, new String[] {});

				String[] nodeNames = new String[] {};
				String[] nodeValues = new String[] {};
				ProductOfGetHierarchy pr = new ProductOfGetHierarchy(wb.dbs, new String[] {}, new String[] {},
						new String[] {}, Hierarchy, "rootElements", ElementsList, nodeNames, nodeValues, RequestID,
						"No");
				prodElementsList.add(pr);
			} else
			{
				ArrayList<String> nodeNamesArrayList = new ArrayList<String>();
				ArrayList<String> nodeValuesArrayList = new ArrayList<String>();

				// Get root hierarchy String
				String rootElementInHierarchy = Help_Func.getRootHierarchyNode(Hierarchy);

				// Get Hierarchy Table for that root hierarchy
				String table = wb.dbs.getOneValue("HierarchyTablePerTechnology2", "HierarchyTableName",
						new String[] { "RootHierarchyNode" }, new String[] { rootElementInHierarchy },
						new String[] { "String" });

				// Get Hierarchy data in style :
				// OltElementName->OltSlot->OltPort->Onu->ElementName->Slot
				String fullHierarchyFromDB = wb.dbs.getOneValue("HierarchyTablePerTechnology2",
						"HierarchyTableNamePath", new String[] { "RootHierarchyNode" },
						new String[] { rootElementInHierarchy }, new String[] { "String" });

				// Check Columns of Hierarchy against fullHierarchy (avoid wrong key values in
				// hierarchy e.g. SiteNa7me=AKADIMIAS)
				Help_Func.checkColumnsOfHierarchyVSFullHierarchy(Hierarchy, fullHierarchyFromDB);

				// Split the hierarchy retrieved from DB into fields
				String[] fullHierarchyFromDBSplit = fullHierarchyFromDB.split("->");

				// Get Full Data hierarchy in style :
				// OltElementName->OltSlot->OltPort->Onu->ActiveElement->Slot
				String fullDataSubsHierarchyFromDB = wb.dbs.getOneValue("HierarchyTablePerTechnology2",
						"DataSubscribersTableNamePath", new String[] { "RootHierarchyNode" },
						new String[] { rootElementInHierarchy }, new String[] { "String" });

				// Split the Data hierarchy retrieved from DB into fields
				String[] fullDataSubsHierarchyFromDBSplit = fullDataSubsHierarchyFromDB.split("->");

				// Get Full Voice hierarchy in style :
				// OltElementName->OltSlot->OltPort->Onu->ActiveElement->Slot
				String fullVoiceSubsHierarchyFromDB = wb.dbs.getOneValue("HierarchyTablePerTechnology2",
						"VoiceSubscribersTableNamePath", new String[] { "RootHierarchyNode" },
						new String[] { rootElementInHierarchy }, new String[] { "String" });

				// Split the Data hierarchy retrieved from DB into fields
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

					ElementsList = wb.dbs.getOneColumnUniqueResultSet(table, fullHierarchyFromDBSplit[0],
							new String[] {}, new String[] {}, new String[] {});

					String[] nodeNames = new String[] { rootElementInHierarchy };
					String[] nodeValues = new String[] { "1" };

					ProductOfGetHierarchy pr = new ProductOfGetHierarchy(wb.dbs, fullHierarchyFromDBSplit,
							fullDataSubsHierarchyFromDBSplit, fullVoiceSubsHierarchyFromDBSplit, Hierarchy,
							fullHierarchyFromDBSplit[0], ElementsList, nodeNames, nodeValues, RequestID,
							Help_Func.determineWSAffected(Hierarchy));
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

						ElementsList = wb.dbs.getOneColumnUniqueResultSet(table,
								fullHierarchyFromDBSplit[hierItemsGiven.length - 1], Help_Func.hierarchyKeys(Hierarchy),
								Help_Func.hierarchyValues(Hierarchy), Help_Func.hierarchyStringTypes(Hierarchy));

						String[] nodeNames = nodeNamesArrayList.toArray(new String[nodeNamesArrayList.size()]);
						String[] nodeValues = nodeValuesArrayList.toArray(new String[nodeValuesArrayList.size()]);
						ProductOfGetHierarchy pr = new ProductOfGetHierarchy(wb.dbs, fullHierarchyFromDBSplit,
								fullDataSubsHierarchyFromDBSplit, fullVoiceSubsHierarchyFromDBSplit, Hierarchy,
								fullHierarchyFromDBSplit[hierItemsGiven.length - 1], ElementsList, nodeNames,
								nodeValues, RequestID, Help_Func.determineWSAffected(Hierarchy));
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
						ProductOfGetHierarchy pr = new ProductOfGetHierarchy(wb.dbs, fullHierarchyFromDBSplit,
								fullDataSubsHierarchyFromDBSplit, fullVoiceSubsHierarchyFromDBSplit, Hierarchy,
								"MaxLevel", ElementsList, nodeNames, nodeValues, RequestID,
								Help_Func.determineWSAffected(Hierarchy));
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

	@Override
	@WebMethod
	@WebResult(name = "Result")
	public List<ProductOfSubmission> submitOutage(
			@WebParam(name = "UserName", header = true, mode = Mode.IN) String UserName,
			@WebParam(name = "Password", header = true, mode = Mode.IN) String Password,
			@WebParam(name = "RequestID") @XmlElement(required = true) String RequestID,
			@WebParam(name = "RequestTimestamp") @XmlElement(required = true) String RequestTimestamp,
			@WebParam(name = "SystemID") @XmlElement(required = true) String SystemID,
			@WebParam(name = "UserID") @XmlElement(required = true) String UserID,
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
			throws Exception, InvalidInputException
	{
		WebSpectra wb = new WebSpectra();

		try
		{
			wb.establishDBConnection();
			List<ProductOfSubmission> prodElementsList;
			prodElementsList = new ArrayList<>();
			int OutageID_Integer = 0;
			// Check if Authentication credentials are correct.
			if (!wb.dbs.authenticateRequest(UserName, Password))
			{
				throw new InvalidInputException("User name or Password incorrect!", "Error 100");
			}

			// Check if Required fields are not empty and they contain the desired values
			Help_Func.validateNotEmpty("RequestID", RequestID);
			Help_Func.validateNotEmpty("RequestTimestamp", RequestTimestamp);
			if (!Help_Func.checkIfEmpty("RequestTimestamp", RequestTimestamp))
			{
				Help_Func.validateDateTimeFormat("RequestTimestamp", RequestTimestamp);
			}

			Help_Func.validateNotEmpty("StartTime", StartTime);
			if (!Help_Func.checkIfEmpty("StartTime", StartTime))
			{
				Help_Func.validateDateTimeFormat("StartTime", StartTime);
			}
			if (!Help_Func.checkIfEmpty("EndTime", EndTime))
			{
				Help_Func.validateDateTimeFormat("EndTime", EndTime);
			}

			Help_Func.validateNotEmpty("SystemID", SystemID);
			Help_Func.validateNotEmpty("UserID", UserID);
			Help_Func.validateNotEmpty("IncidentID", IncidentID);

			Help_Func.validateNotEmpty("Scheduled", Scheduled);
			Help_Func.validateAgainstPredefinedValues("Scheduled", Scheduled, new String[] { "Yes", "No" });

			// If the submitted incident is scheduled then it should always has "EndTime"
			/*
			 * if (Scheduled.equals("Yes")) { if (Help_Func.checkIfEmpty("EndTime",
			 * EndTime)) { throw new
			 * InvalidInputException("Scheduled incidents should always contain Start Time and End Time"
			 * , "Error 172"); } }
			 */
			Help_Func.validateIntegerOrEmptyValue("Duration", Duration);

			Help_Func.validateNotEmpty("AffectedServices", AffectedServices);
			Help_Func.validateDelimitedValues("AffectedServices", AffectedServices, "\\|",
					new String[] { "Voice", "Data", "IPTV" });

			Help_Func.validateNotEmpty("Impact", Impact);
			Help_Func.validateAgainstPredefinedValues("Impact", Impact, new String[] { "QoS", "LoS" });

			Help_Func.validateNotEmpty("Priority", Priority);
			Help_Func.validateAgainstPredefinedValues("Priority", Priority,
					new String[] { "Critical", "Medium", "Low" });

			Help_Func.validateNotEmpty("HierarchySelected", HierarchySelected);

			// Split to % and to | the hierarchy provided
			List<String> myHier = Help_Func.getHierarchySelections(HierarchySelected);

			// Get Max Outage ID (type int)
			OutageID_Integer = wb.dbs.getMaxIntegerValue("SubmittedIncidents", "OutageID");

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
					String rootHierarchySelected = Help_Func.getRootHierarchyNode(myHier.get(i).toString());

					// Get Hierarchy data in style :
					// OltElementName->OltSlot->OltPort->Onu->ElementName->Slot
					String fullHierarchyFromDB = wb.dbs.getOneValue("HierarchyTablePerTechnology2",
							"HierarchyTableNamePath", new String[] { "RootHierarchyNode" },
							new String[] { rootHierarchySelected }, new String[] { "String" });

					// Check Columns of Hierarchy against fullHierarchy (avoid wrong key values in
					// hierarchy e.g. SiteNa7me=AKADIMIAS)
					Help_Func.checkColumnsOfHierarchyVSFullHierarchy(myHier.get(i).toString(), fullHierarchyFromDB);

					// Determine Tables for Data/Voice subscribers
					String dataSubsTable = wb.dbs.getOneValue("HierarchyTablePerTechnology2",
							"DataSubscribersTableName", new String[] { "RootHierarchyNode" },
							new String[] { rootHierarchySelected }, new String[] { "String" });

					String voiceSubsTable = wb.dbs.getOneValue("HierarchyTablePerTechnology2",
							"VoiceSubscribersTableName", new String[] { "RootHierarchyNode" },
							new String[] { rootHierarchySelected }, new String[] { "String" });

					// Get Hierarchies for Data/Voice Tables
					String fullDataHierarchyPath = wb.dbs.getOneValue("HierarchyTablePerTechnology2",
							"DataSubscribersTableNamePath", new String[] { "RootHierarchyNode" },
							new String[] { rootHierarchySelected }, new String[] { "String" });

					String[] fullDataHierarchyPathSplit = fullDataHierarchyPath.split("->");

					String fullVoiceHierarchyPath = wb.dbs.getOneValue("HierarchyTablePerTechnology2",
							"VoiceSubscribersTableNamePath", new String[] { "RootHierarchyNode" },
							new String[] { rootHierarchySelected }, new String[] { "String" });

					String[] fullVoiceHierarchyPathSplit = fullVoiceHierarchyPath.split("->");

					// Count distinct values of Usernames or CliVlaues in the respective columns
					String dataCustomersAffected = wb.dbs.countDistinctRowsForSpecificColumn(dataSubsTable, "Username",
							Help_Func.hierarchyKeys(Help_Func.replaceHierarchyForSubscribersAffected(
									myHier.get(i).toString(), fullDataHierarchyPathSplit)),
							Help_Func.hierarchyValues(Help_Func.replaceHierarchyForSubscribersAffected(
									myHier.get(i).toString(), fullDataHierarchyPathSplit)),
							Help_Func.hierarchyStringTypes(Help_Func.replaceHierarchyForSubscribersAffected(
									myHier.get(i).toString(), fullDataHierarchyPathSplit)));

					String voiceCustomersAffected = wb.dbs.countDistinctRowsForSpecificColumns(voiceSubsTable,
							new String[] { "ActiveElement", "Subrack", "Slot", "Port", "PON" },
							Help_Func.hierarchyKeys(Help_Func.replaceHierarchyForSubscribersAffected(
									myHier.get(i).toString(), fullVoiceHierarchyPathSplit)),
							Help_Func.hierarchyValues(Help_Func.replaceHierarchyForSubscribersAffected(
									myHier.get(i).toString(), fullVoiceHierarchyPathSplit)),
							Help_Func.hierarchyStringTypes(Help_Func.replaceHierarchyForSubscribersAffected(
									myHier.get(i).toString(), fullVoiceHierarchyPathSplit)));

					// For Voice no data customers are affected and vice versa
					if (service.equals("Voice"))
					{
						dataCustomersAffected = "0";
					} else if (service.equals("Data"))
					{
						voiceCustomersAffected = "0";
					}

					incidentDataCustomersAffected += Integer.parseInt(dataCustomersAffected);
					incidentVoiceCustomersAffected += Integer.parseInt(voiceCustomersAffected);
				}
			}

			// Check if for the same Incident ID, Service & Hierarchy - We have already an
			// entry
			for (String service : servicesAffected)
			{
				for (int i = 0; i < myHier.size(); i++)
				{
					boolean incidentAlreadyExists = wb.dbs.checkIfCriteriaExists("SubmittedIncidents",
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

			if (wb.dbs.checkIfCriteriaExists("SubmittedIncidents", new String[] { "IncidentID" },
					new String[] { IncidentID }, new String[] { "String" }))
			{
				numberOfVoiceCustAffectedFromPreviousIncidents = wb.dbs.maxNumberOfCustomersAffected(
						"SubmittedIncidents", "IncidentAffectedVoiceCustomers", new String[] { "IncidentID" },
						new String[] { IncidentID });
				numberOfDataCustAffectedFromPreviousIncidents = wb.dbs.maxNumberOfCustomersAffected(
						"SubmittedIncidents", "IncidentAffectedDataCustomers", new String[] { "IncidentID" },
						new String[] { IncidentID });

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
					String rootHierarchySelected = Help_Func.getRootHierarchyNode(myHier.get(i).toString());

					// Determine Tables for Data/Voice subscribers
					String dataSubsTable = wb.dbs.getOneValue("HierarchyTablePerTechnology2",
							"DataSubscribersTableName", new String[] { "RootHierarchyNode" },
							new String[] { rootHierarchySelected }, new String[] { "String" });

					String voiceSubsTable = wb.dbs.getOneValue("HierarchyTablePerTechnology2",
							"VoiceSubscribersTableName", new String[] { "RootHierarchyNode" },
							new String[] { rootHierarchySelected }, new String[] { "String" });

					// Get Hierarchies for Data/Voice Tables
					String fullDataHierarchyPath = wb.dbs.getOneValue("HierarchyTablePerTechnology2",
							"DataSubscribersTableNamePath", new String[] { "RootHierarchyNode" },
							new String[] { rootHierarchySelected }, new String[] { "String" });

					String[] fullDataHierarchyPathSplit = fullDataHierarchyPath.split("->");
					String fullVoiceHierarchyPath = wb.dbs.getOneValue("HierarchyTablePerTechnology2",
							"VoiceSubscribersTableNamePath", new String[] { "RootHierarchyNode" },
							new String[] { rootHierarchySelected }, new String[] { "String" });

					String[] fullVoiceHierarchyPathSplit = fullVoiceHierarchyPath.split("->");

					// Count distinct values of Usernames or CliVlaues the respective columns
					String dataCustomersAffected = wb.dbs.countDistinctRowsForSpecificColumn(dataSubsTable, "Username",
							Help_Func.hierarchyKeys(Help_Func.replaceHierarchyForSubscribersAffected(
									myHier.get(i).toString(), fullDataHierarchyPathSplit)),
							Help_Func.hierarchyValues(Help_Func.replaceHierarchyForSubscribersAffected(
									myHier.get(i).toString(), fullDataHierarchyPathSplit)),
							Help_Func.hierarchyStringTypes(Help_Func.replaceHierarchyForSubscribersAffected(
									myHier.get(i).toString(), fullDataHierarchyPathSplit)));

					String voiceCustomersAffected = wb.dbs.countDistinctRowsForSpecificColumns(voiceSubsTable,
							new String[] { "ActiveElement", "Subrack", "Slot", "Port", "PON" },
							Help_Func.hierarchyKeys(Help_Func.replaceHierarchyForSubscribersAffected(
									myHier.get(i).toString(), fullVoiceHierarchyPathSplit)),
							Help_Func.hierarchyValues(Help_Func.replaceHierarchyForSubscribersAffected(
									myHier.get(i).toString(), fullVoiceHierarchyPathSplit)),
							Help_Func.hierarchyStringTypes(Help_Func.replaceHierarchyForSubscribersAffected(
									myHier.get(i).toString(), fullVoiceHierarchyPathSplit)));

					String CLIsAffected = wb.dbs.countDistinctRowsForSpecificColumn(voiceSubsTable, "CliValue",
							Help_Func.hierarchyKeys(Help_Func.replaceHierarchyForSubscribersAffected(
									myHier.get(i).toString(), fullVoiceHierarchyPathSplit)),
							Help_Func.hierarchyValues(Help_Func.replaceHierarchyForSubscribersAffected(
									myHier.get(i).toString(), fullVoiceHierarchyPathSplit)),
							Help_Func.hierarchyStringTypes(Help_Func.replaceHierarchyForSubscribersAffected(
									myHier.get(i).toString(), fullVoiceHierarchyPathSplit)));

					// For Voice no data customers are affected and vice versa
					if (service.equals("Voice"))
					{
						dataCustomersAffected = "0";
					} else if (service.equals("Data"))
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
					wb.dbs.insertValuesInTable("SubmittedIncidents",
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
						ProductOfSubmission ps = new ProductOfSubmission(RequestID, OutageID_String, IncidentID,
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

	@Override
	@WebMethod
	@WebResult(name = "Result")
	public List<ProductOfGetOutage> getOutageStatus(
			@WebParam(name = "UserName", header = true, mode = Mode.IN) String UserName,
			@WebParam(name = "Password", header = true, mode = Mode.IN) String Password,
			@WebParam(name = "RequestID") @XmlElement(required = true) String RequestID,
			@WebParam(name = "IncidentID") @XmlElement(required = true) String IncidentID,
			@WebParam(name = "IncidentStatus") @XmlElement(required = true) String IncidentStatus)
			throws Exception, InvalidInputException
	{
		WebSpectra wb = new WebSpectra();

		try
		{
			wb.establishDBConnection();
			List<ProductOfGetOutage> prodElementsList;
			prodElementsList = new ArrayList<>();

			// Check if fields are empty
			Help_Func.validateNotEmpty("IncidentID", IncidentID);
			Help_Func.validateNotEmpty("IncidentStatus", IncidentStatus);

			// Check if Authentication credentials are correct.
			if (!wb.dbs.authenticateRequest(UserName, Password))
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

				numOfRows = wb.dbs.numberOfRowsFound("SubmittedIncidents", new String[] { "IncidentStatus" },
						new String[] { IncidentStatus }, new String[] { "String" });

				rs = wb.dbs.getRows("SubmittedIncidents",
						new String[] { "OutageID", "IncidentStatus", "RequestTimestamp", "SystemID", "UserID",
								"IncidentID", "Scheduled", "StartTime", "EndTime", "Duration", "AffectedServices",
								"Impact", "Priority", "Hierarchyselected", "AffectedVoiceCustomers",
								"AffectedDataCustomers", "AffectedCLICustomers", "ActiveDataCustomersAffected",
								"TVCustomersAffected", "IncidentAffectedVoiceCustomers",
								"IncidentAffectedDataCustomers" },
						new String[] { "IncidentStatus" }, new String[] { IncidentStatus }, new String[] { "String" });
			} else
			{
				numOfRows = wb.dbs.numberOfRowsFound("SubmittedIncidents",
						new String[] { "IncidentID", "IncidentStatus" }, new String[] { IncidentID, IncidentStatus },
						new String[] { "String", "String" });

				rs = wb.dbs.getRows("SubmittedIncidents",
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
					ProductOfGetOutage pg = new ProductOfGetOutage(RequestID, rs.getString("OutageID"),
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

	@Override
	@WebMethod
	@WebResult(name = "Result")
	public ProductOfModify modifyOutage(@WebParam(name = "UserName", header = true, mode = Mode.IN) String UserName,
			@WebParam(name = "Password", header = true, mode = Mode.IN) String Password,
			@WebParam(name = "RequestID") @XmlElement(required = true) String RequestID,
			@WebParam(name = "RequestTimestamp") @XmlElement(required = true) String RequestTimestamp,
			@WebParam(name = "SystemID") @XmlElement(required = true) String SystemID,
			@WebParam(name = "UserID") @XmlElement(required = true) String UserID,
			@WebParam(name = "IncidentID") @XmlElement(required = true) String IncidentID,
			@WebParam(name = "OutageID") @XmlElement(required = true) String OutageID,
			@WebParam(name = "StartTime") @XmlElement(required = false) String StartTime,
			@WebParam(name = "EndTime") @XmlElement(required = false) String EndTime,
			@WebParam(name = "Duration") @XmlElement(required = false) String Duration,
			// Quality, Loss
			@WebParam(name = "Impact") @XmlElement(required = false) String Impact)
			throws Exception, InvalidInputException
	{
		WebSpectra wb = new WebSpectra();

		try
		{
			wb.establishDBConnection();
			// Check if Authentication credentials are correct.
			if (!wb.dbs.authenticateRequest(UserName, Password))
			{
				throw new InvalidInputException("User name or Password incorrect!", "Error 100");
			}

			ProductOfModify pom = null;

			// Check if Required fields are empty
			Help_Func.validateNotEmpty("RequestID", RequestID);
			Help_Func.validateNotEmpty("RequestTimestamp", RequestTimestamp);
			Help_Func.validateDateTimeFormat("RequestTimestamp", RequestTimestamp);
			Help_Func.validateNotEmpty("SystemID", SystemID);
			Help_Func.validateNotEmpty("UserID", UserID);
			Help_Func.validateNotEmpty("IncidentID", IncidentID);
			Help_Func.validateNotEmpty("OutageID", OutageID);

			// if Start Time Value Exists
			if (!Help_Func.checkIfEmpty("StartTime", StartTime))
			{
				// Check if it has the appropriate format
				Help_Func.validateDateTimeFormat("StartTime", StartTime);
			}
			// if End Time Value Exists
			if (!Help_Func.checkIfEmpty("EndTime", EndTime))
			{
				// Check if it has the appropriate format
				Help_Func.validateDateTimeFormat("EndTime", EndTime);
			}

			// if Impact Value Exists
			if (!Help_Func.checkIfEmpty("Impact", Impact))
			{
				// Check if it has the appropriate format
				Help_Func.validateAgainstPredefinedValues("Impact", Impact, new String[] { "QoS", "LoS" });
			}

			// if Duration Value Exists
			if (!Help_Func.checkIfEmpty("Duration", Duration))
			{
				// Check if it has the appropriate format
				Help_Func.validateIntegerOrEmptyValue("Duration", Duration);
			}

			// Check if the combination of IncidentID & OutageID exists
			boolean incidentPlusOutageExists = wb.dbs.checkIfCriteriaExists("SubmittedIncidents",
					new String[] { "IncidentID", "OutageID" }, new String[] { IncidentID, OutageID },
					new String[] { "String", "String" });

			if (incidentPlusOutageExists)
			{
				// Check if the combination of IncidentID & OutageID refers to a scheduled
				// Incident (Scheduled = "Yes")
				boolean incidentIsScheduled = wb.dbs.checkIfCriteriaExists("SubmittedIncidents",
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
				int numOfRowsUpdated = wb.dbs.updateColumnOnSpecificCriteria("SubmittedIncidents",
						arrayOfColumnsForUpdate, arrayOfValuesForUpdate, arrayOfDataTypesForUpdate,
						new String[] { "IncidentID", "OutageID" }, new String[] { IncidentID, OutageID },
						new String[] { "String", "Integer" });

				if (numOfRowsUpdated == 1)
				{
					pom = new ProductOfModify(RequestID, IncidentID, OutageID, "930", "Successfully Modified Incident");
				} else
				{
					pom = new ProductOfModify(RequestID, IncidentID, OutageID, "980", "Error modifying incident!");
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

	@Override
	@WebMethod
	@WebResult(name = "Result")
	public ProductOfCloseOutage closeOutage(@WebParam(name = "UserName", header = true, mode = Mode.IN) String UserName,
			@WebParam(name = "Password", header = true, mode = Mode.IN) String Password,
			@WebParam(name = "RequestID") @XmlElement(required = true) String RequestID,
			@WebParam(name = "RequestTimestamp") @XmlElement(required = true) String RequestTimestamp,
			@WebParam(name = "SystemID") @XmlElement(required = true) String SystemID,
			@WebParam(name = "UserID") @XmlElement(required = true) String UserID,
			@WebParam(name = "IncidentID") @XmlElement(required = true) String IncidentID,
			@WebParam(name = "OutageID") @XmlElement(required = true) String OutageID)
			throws Exception, InvalidInputException
	{
		WebSpectra wb = new WebSpectra();

		try
		{
			wb.establishDBConnection();
			// Check if Authentication credentials are correct.
			if (!wb.dbs.authenticateRequest(UserName, Password))
			{
				throw new InvalidInputException("User name or Password incorrect!", "Error 100");
			}

			ProductOfCloseOutage poca = null;

			// Check if Required fields are empty
			Help_Func.validateNotEmpty("RequestID", RequestID);
			Help_Func.validateNotEmpty("RequestTimestamp", RequestTimestamp);
			Help_Func.validateDateTimeFormat("RequestTimestamp", RequestTimestamp);
			Help_Func.validateNotEmpty("SystemID", SystemID);
			Help_Func.validateNotEmpty("UserID", UserID);
			Help_Func.validateNotEmpty("IncidentID", IncidentID);
			Help_Func.validateNotEmpty("OutageID", OutageID);

			// Check if the combination of IncidentID & OutageID exists
			boolean incidentPlusOutageExists = wb.dbs.checkIfCriteriaExists("SubmittedIncidents",
					new String[] { "IncidentID", "OutageID" }, new String[] { IncidentID, OutageID },
					new String[] { "String", "String" });

			if (incidentPlusOutageExists)
			{

				// Check if the combination of IncidentID & OutageID is still OPEN
				boolean incidentPlusOutageIsOpen = wb.dbs.checkIfCriteriaExists("SubmittedIncidents",
						new String[] { "IncidentID", "OutageID", "IncidentStatus" },
						new String[] { IncidentID, OutageID, "OPEN" }, new String[] { "String", "String", "String" });

				// If incident is still in status OPEN
				if (incidentPlusOutageIsOpen)
				{
					// Update Operation
					int numOfRowsUpdated = wb.dbs.updateColumnOnSpecificCriteria("SubmittedIncidents",
							new String[] { "IncidentStatus", "EndTime" }, new String[] { "CLOSED", Help_Func.now() },
							new String[] { "String", "Date" }, new String[] { "IncidentID", "OutageID" },
							new String[] { IncidentID, OutageID }, new String[] { "String", "Integer" });

					if (numOfRowsUpdated == 1)
					{
						poca = new ProductOfCloseOutage(RequestID, IncidentID, OutageID, "990",
								"Successfully Closed Incident");
					} else
					{
						poca = new ProductOfCloseOutage(RequestID, IncidentID, OutageID, "423",
								"Error Closing Incident");
					}
				} else // If incident is not in status OPEN
				{
					String closedTime = wb.dbs.getOneValue("SubmittedIncidents", "EndTime",
							new String[] { "IncidentID", "OutageID" }, new String[] { IncidentID, OutageID },
							new String[] { "String", "String" });

					throw new InvalidInputException("The combination of IncidentID: " + IncidentID + " and OutageID: "
							+ OutageID + " has already been closed since: " + closedTime, "Error 820");
				}
			} else
			{
				throw new InvalidInputException("The combination of IncidentID: " + IncidentID + " and OutageID: "
						+ OutageID + " does not exist!", "Error 950");
			}

			return poca;
		} finally
		{
			wb.conObj.closeDBConnection();
		}
	}

	@Override
	@WebMethod
	@WebResult(name = "Result")
	public ProductOfNLUActive NLU_Active(@WebParam(name = "UserName", header = true, mode = Mode.IN) String UserName,
			@WebParam(name = "Password", header = true, mode = Mode.IN) String Password,
			@WebParam(name = "RequestID") @XmlElement(required = true) String RequestID,
			@WebParam(name = "SystemID") @XmlElement(required = true) String SystemID,
			@WebParam(name = "RequestTimestamp") @XmlElement(required = true) String RequestTimestamp,
			@WebParam(name = "CLI") @XmlElement(required = true) String CLI,
			@WebParam(name = "Service") @XmlElement(required = true) String Service,
			@WebParam(name = "ServiceL2") @XmlElement(required = false) String ServiceL2,
			@WebParam(name = "ServiceL3") @XmlElement(required = false) String ServiceL3)
			throws Exception, InvalidInputException
	{
		WebSpectra wb = new WebSpectra();
		ProductOfNLUActive ponla = null;
		try
		{
			wb.establishDBConnection();
			// Check if Authentication credentials are correct.
			if (!wb.dbs.authenticateRequest(UserName, Password))
			{
				throw new InvalidInputException("User name or Password incorrect!", "Error 100");
			}

			// Check if Required fields are empty
			Help_Func.validateNotEmpty("RequestID", RequestID);
			Help_Func.validateNotEmpty("SystemID", SystemID);
			Help_Func.validateNotEmpty("RequestTimestamp", RequestTimestamp);
			Help_Func.validateDateTimeFormat("RequestTimestamp", RequestTimestamp);
			Help_Func.validateNotEmpty("CLI", CLI);
			// Help_Func.validateNotEmpty("Service", Service);

			// if Impact Value Exists
			if (!Help_Func.checkIfEmpty("Service", Service))
			{
				// Check if it has the appropriate format
				Help_Func.validateDelimitedValues("Service", Service, "\\|", new String[] { "Voice", "Data", "IPTV" });
			}

			CLIOutage co = new CLIOutage(wb.dbs, RequestID);
			ponla = co.checkCLIOutage(CLI, Service);

		} finally
		{
			wb.conObj.closeDBConnection();
		}
		return ponla;
	}

}
