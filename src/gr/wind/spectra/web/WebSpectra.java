package gr.wind.spectra.web;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebResult;
import javax.jws.WebService;

import javax.xml.soap.SOAPHeader;

import javax.xml.bind.annotation.XmlElement;

import gr.wind.spectra.web.InvalidInputException;

import gr.wind.spectra.business.DB_Connection;
import gr.wind.spectra.business.DB_Operations;
import gr.wind.spectra.business.Help_Func;
import gr.wind.spectra.model.Product;
import gr.wind.spectra.model.ProductOfGetOutage;
import gr.wind.spectra.model.ProductOfSubmission;


@WebService //(endpointInterface = "gr.wind.spectra.web.WebSpectraInterface")
public class WebSpectra// implements WebSpectraInterface
{
	private static final String hierSep = "->";
	DB_Connection conObj;
	Connection conn;
	DB_Operations dbs;
		
	public WebSpectra() throws InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		if (conObj == null || dbs == null)
		{
			conObj = new DB_Connection();
			try {
				this.conn = conObj.Connect();
			} catch (InvalidInputException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.dbs = new DB_Operations(conn);
			
		}
	}
	
	
	@WebMethod()
	@WebResult(name="Result")
	public List<Product> getHierarchy
	(
			//@WebParam(targetNamespace="http://spectra.wind.gr/handler/", name="UserName", header = true, mode = Mode.IN) @XmlElement( required = true ) String UserName,
			//@WebParam(targetNamespace="http://spectra.wind.gr/handler/", name="Password", header = true) @XmlElement( required = true ) String Password,
			@WebParam(name="UserName", header = true, mode = Mode.IN) String UserName,
			@WebParam(name="Password", header = true, mode = Mode.IN) String Password,
			@WebParam(name="RequestID") @XmlElement( required = true ) String RequestID,
			@WebParam(name="RequestTimestamp") @XmlElement( required = true ) String RequestTimestamp,
			@WebParam(name="SystemID") @XmlElement( required = true ) String SystemID,
			@WebParam(name="UserID") @XmlElement( required = true ) String UserID,
			@WebParam(name="Hierarchy") String Hierarchy
	) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, InvalidInputException, ParseException
	{
		
		/*
        	<internetCustomersAffected>34</potentialCustomersAffected>// unique user names from Internet Resource path
        	<voiceCustomersAffected>34</potentialCustomersAffected>  // unique user names from Voice Resource path
	    	<cLIsAffected>34</potentialCustomersAffected> // unique CLIs from Voice Resource path
		*/
		
		WebSpectra wb = new WebSpectra();
		List<String> ElementsList = new ArrayList<String>();
		List<Product> prodElementsList = new ArrayList<>();
		
		// Check if Authentication credentials are correct.
		if (! wb.dbs.AuthenticateRequest(UserName, Password) ) {throw new InvalidInputException("User name or Password incorrect!", "Error 100");}
		
		// Check if Required fields are empty
		Help_Func.ValidateNotEmpty("RequestID", RequestID);
		Help_Func.ValidateNotEmpty("SystemID", SystemID);
		Help_Func.ValidateNotEmpty("UserID", UserID);

		// Validate Date Formats if the fields are not empty
		if (! Help_Func.checkIfEmpty("RequestTimestamp", RequestTimestamp))	{ Help_Func.ValidateDateTimeFormat("RequestTimestamp", RequestTimestamp); }
		
		// No Hierarchy is given - returns root elements
		if (Hierarchy == null || Hierarchy.equals("") || Hierarchy.equals("?"))
		{
			String rootHierarchySelected = "";
			ElementsList = wb.dbs.GetOneColumnUniqueResultSet("HierarchyTablePerTechnology2", "RootHierarchyNode", "1 = 1");
			String[] nodeNames = new String[] {};
			String[] nodeValues = new String[] {};
			Product pr = new Product(wb.dbs, new String[] {}, new String[] {}, new String[] {}, Hierarchy, "rootElements", ElementsList, nodeNames, nodeValues, RequestID);
			prodElementsList.add(pr);
		}
		else
		{
			ArrayList<String> nodeNamesArrayList = new ArrayList<String>();
			ArrayList<String> nodeValuesArrayList = new ArrayList<String>();

			// Get root hierarchy String
			String rootElementInHierarchy = Help_Func.GetRootHierarchyNode(Hierarchy);

			// Get Hierarchy Table for that root hierarchy
			String table = wb.dbs.GetOneValue("HierarchyTablePerTechnology2", "HierarchyTableName", "RootHierarchyNode = '" + rootElementInHierarchy + "'");
			
			// Get Hierarchy data in style : OltElementName->OltSlot->OltPort->Onu->ElementName->Slot
			String fullHierarchyFromDB = wb.dbs.GetOneValue("HierarchyTablePerTechnology2", "HierarchyTableNamePath", "RootHierarchyNode = '" + rootElementInHierarchy + "'");

			// Check Columns of Hierarchy against fullHierarchy (avoid wrong key values in hierarchy e.g. SiteNa7me=AKADIMIAS)
			Help_Func.CheckColumnsOfHierarchyVSFullHierarchy(Hierarchy, fullHierarchyFromDB);
			
			// Split the hierarchy retrieved from DB into fields
			String[] fullHierarchyFromDBSplit = fullHierarchyFromDB.split("->");			
			
			// Get Full Internet hierarchy in style : OltElementName->OltSlot->OltPort->Onu->ActiveElement->Slot
			String fullDataSubsHierarchyFromDB = wb.dbs.GetOneValue("HierarchyTablePerTechnology2", "DataSubscribersTableNamePath", "RootHierarchyNode = '" + rootElementInHierarchy + "'");

			// Split the Internet hierarchy retrieved from DB into fields
			String[] fullDataSubsHierarchyFromDBSplit = fullDataSubsHierarchyFromDB.split("->");

			// Get Full Voice hierarchy in style : OltElementName->OltSlot->OltPort->Onu->ActiveElement->Slot
			String fullVoiceSubsHierarchyFromDB = wb.dbs.GetOneValue("HierarchyTablePerTechnology2", "VoiceSubscribersTableNamePath", "RootHierarchyNode = '" + rootElementInHierarchy + "'");

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
				ElementsList = wb.dbs.GetOneColumnUniqueResultSet(table, fullHierarchyFromDBSplit[0], " 1 = 1 ");
				String[] nodeNames = new String[] {rootElementInHierarchy};
				String[] nodeValues = new String[] {"1"};
				Product pr = new Product(wb.dbs, fullHierarchyFromDBSplit, fullDataSubsHierarchyFromDBSplit, fullVoiceSubsHierarchyFromDBSplit, Hierarchy, fullHierarchyFromDBSplit[0] , ElementsList, nodeNames, nodeValues, RequestID);
				prodElementsList.add(pr);	
			}
			else
			{	
				// Check if Max hierarchy is used
				// FTTX->OltElementName=LAROAKDMOLT01->OltSlot=1->OltPort=0->Onu=0->ElementName=LAROAKDMOFLND010H11->Slot=4:  7 MAX = FTTX + OltElementName->OltSlot->OltPort->Onu->ElementName->Slot
				if (hierItemsGiven.length < fullHierarchyFromDBSplit.length + 1)
				{
					// If a full hierarchy is given
					for (int i=0; i < hierItemsGiven.length; i++)
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
					
					ElementsList = wb.dbs.GetOneColumnUniqueResultSet(table, fullHierarchyFromDBSplit[hierItemsGiven.length-1], Help_Func.HierarchyToPredicate(Hierarchy));
					String[] nodeNames = nodeNamesArrayList.toArray(new String[nodeNamesArrayList.size()]);  
					String[] nodeValues = nodeValuesArrayList.toArray(new String[nodeValuesArrayList.size()]);
					Product pr = new Product(wb.dbs, fullHierarchyFromDBSplit, fullDataSubsHierarchyFromDBSplit, fullVoiceSubsHierarchyFromDBSplit, Hierarchy, fullHierarchyFromDBSplit[hierItemsGiven.length-1], ElementsList, nodeNames, nodeValues, RequestID);
					prodElementsList.add(pr);
				}
				else
				{	// Max Hierarchy Level
					// If a full hierarchy is given
					for (int i=0; i < hierItemsGiven.length; i++)
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
					Product pr = new Product(wb.dbs, fullHierarchyFromDBSplit, fullDataSubsHierarchyFromDBSplit, fullVoiceSubsHierarchyFromDBSplit, Hierarchy, "MaxLevel", ElementsList, nodeNames, nodeValues, RequestID);
					prodElementsList.add(pr);
				}
			}
		}		
		return prodElementsList;
	}

	@WebMethod
	@WebResult(name="Result")
	public List<ProductOfSubmission> submitOutage
	(
		@WebParam(name="UserName", header = true, mode = Mode.IN) String UserName,
		@WebParam(name="Password", header = true, mode = Mode.IN) String Password,
		@WebParam(name="RequestTimestamp") @XmlElement( required = true ) String RequestTimestamp,
		@WebParam(name="SystemID") @XmlElement( required = true ) String SystemID,
		@WebParam(name="UserID") @XmlElement( required = true ) String UserID,
		// Defines Uniquely The Incident
		@WebParam(name="IncidentID") @XmlElement( required = true ) String IncidentID,
		@WebParam(name="Scheduled") @XmlElement( required = true ) String Scheduled,
		@WebParam(name="StartTime") @XmlElement( required = true ) String StartTime,
		@WebParam(name="EndTime") @XmlElement( required = false ) String EndTime,
		@WebParam(name="Duration") @XmlElement( required = false ) String Duration,
		// TV, VOICE, DATA
		@WebParam(name="AffectedServices") @XmlElement( required = true ) String AffectedServices,
		// Quality, Loss
		@WebParam(name="Impact") @XmlElement( required = true ) String Impact,
		@WebParam(name="Priority") @XmlElement( required = true ) String Priority,
		@WebParam(name="HierarchySelected") @XmlElement( required = true ) String HierarchySelected
	) throws InvalidInputException, ParseException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException
	{	
		WebSpectra wb = new WebSpectra();
		List<ProductOfSubmission> prodElementsList = new ArrayList<>();
		int OutageID_Integer = 0;
		int totalNumberOfCustomersAffectedPerIncident = 0;
		
		// Check if Authentication credentials are correct.
		if (! wb.dbs.AuthenticateRequest(UserName, Password) ) {throw new InvalidInputException("User name or Password incorrect!", "Error 100");}
		
		// Check if Required fields are not empty and they contain the desired values
		Help_Func.ValidateNotEmpty("RequestTimestamp", RequestTimestamp);
		if (! Help_Func.checkIfEmpty("RequestTimestamp", RequestTimestamp))	{ Help_Func.ValidateDateTimeFormat("RequestTimestamp", RequestTimestamp); }
		
		Help_Func.ValidateNotEmpty("StartTime", StartTime);
		if (! Help_Func.checkIfEmpty("StartTime", StartTime)) { Help_Func.ValidateDateTimeFormat("StartTime", StartTime); }
		if (! Help_Func.checkIfEmpty("EndTime", EndTime)) { Help_Func.ValidateDateTimeFormat("EndTime", EndTime); }
		
		Help_Func.ValidateNotEmpty("SystemID", SystemID);
		Help_Func.ValidateNotEmpty("UserID", UserID);
		Help_Func.ValidateNotEmpty("IncidentID", IncidentID);
		
		Help_Func.ValidateNotEmpty("Scheduled", Scheduled);
		Help_Func.ValidateAgainstPredefinedValues("Scheduled", Scheduled, new String[] {"Yes", "No"});
		
		Help_Func.ValidateIntegerOrEmptyValue("Duration", Duration);
		
		Help_Func.ValidateNotEmpty("AffectedServices", AffectedServices);
		Help_Func.ValidateDelimitedValues("AffectedServices", AffectedServices, "\\|", new String[] {"Voice", "Internet", "IPTV"});
		
		Help_Func.ValidateNotEmpty("Impact", Impact);
		Help_Func.ValidateAgainstPredefinedValues("Impact", Impact, new String[] {"QoS", "LoS"});
		
		Help_Func.ValidateNotEmpty("Priority", Priority);
		Help_Func.ValidateAgainstPredefinedValues("Priority", Priority, new String[] {"Critical", "Medium", "Low"});
		
		Help_Func.ValidateNotEmpty("HierarchySelected", HierarchySelected);


		// Split to % and to | the hierarchy provided
		java.util.List myHier = Help_Func.GetHierarchySelections(HierarchySelected);		

		// Get Max Outage ID (type int)
		OutageID_Integer = wb.dbs.GetMaxIntegerValue("SubmittedIncidents", "OutageID");

		// Services affected
		String [] servicesAffected = AffectedServices.split("\\|");

		// Calculate Total number per Indicent, of customers affected per incident
		int incidentDataCustomersAffected = 0;
		int incidentVoiceCustomersAffected = 0;
		int incidentCLIsAffected = 0;
		for (String service : servicesAffected)
		{	
			for(int i=0;i<myHier.size();i++)
			{
				// Check Hierarchy Format Key_Value Pairs
				Help_Func.checkHierarchyFormatKeyValuePairs(myHier.get(i).toString());
				
				// Firstly determine the hierarchy table that will be used based on the root hierarchy provided 
				String rootHierarchySelected = Help_Func.GetRootHierarchyNode(myHier.get(i).toString());

				// Get Hierarchy data in style : OltElementName->OltSlot->OltPort->Onu->ElementName->Slot
				String fullHierarchyFromDB = wb.dbs.GetOneValue("HierarchyTablePerTechnology2", "HierarchyTableNamePath", "RootHierarchyNode = '" + rootHierarchySelected + "'");

				// Check Columns of Hierarchy against fullHierarchy (avoid wrong key values in hierarchy e.g. SiteNa7me=AKADIMIAS)
				Help_Func.CheckColumnsOfHierarchyVSFullHierarchy(myHier.get(i).toString(), fullHierarchyFromDB);
			
				// Determine Tables for Data/Voice subscribers
				String dataSubsTable =  wb.dbs.GetOneValue("HierarchyTablePerTechnology2", "DataSubscribersTableName", "RootHierarchyNode = '" + rootHierarchySelected + "'");
				String voiceSubsTable =  wb.dbs.GetOneValue("HierarchyTablePerTechnology2", "VoiceSubscribersTableName", "RootHierarchyNode = '" + rootHierarchySelected + "'");
					
				// Get Hierarchies for Data/Voice Tables
				String fullDataHierarchyPath = wb.dbs.GetOneValue("HierarchyTablePerTechnology2", "DataSubscribersTableNamePath", "RootHierarchyNode = '" + rootHierarchySelected + "'");
				String[] fullDataHierarchyPathSplit = fullDataHierarchyPath.split("->"); 

				String fullVoiceHierarchyPath = wb.dbs.GetOneValue("HierarchyTablePerTechnology2", "VoiceSubscribersTableNamePath", "RootHierarchyNode = '" + rootHierarchySelected + "'");
				String[] fullVoiceHierarchyPathSplit = fullVoiceHierarchyPath.split("->");
				
				// Count distinct values of Usernames or CliVlaues in the respective columns
				String dataCustomersAffected = dbs.CountDistinctRowsForSpecificColumn(dataSubsTable, "Username", Help_Func.HierarchyToPredicate(Help_Func.ReplaceHierarchyForSubscribersAffected(myHier.get(i).toString(), fullDataHierarchyPathSplit)));
				String voiceCustomersAffected = dbs.CountDistinctRowsForSpecificColumns(voiceSubsTable, new String[] {"ActiveElement","Subrack","Slot","Port","PON"}, Help_Func.HierarchyToPredicate(Help_Func.ReplaceHierarchyForSubscribersAffected(myHier.get(i).toString(), fullVoiceHierarchyPathSplit)));
				
				// For Voice no data customers are affected and vice versa
				if (service.equals("Voice"))
				{
					dataCustomersAffected = "0";
				}
				else if (service.equals("Internet"))
				{
					voiceCustomersAffected = "0";
				}
				
				incidentDataCustomersAffected += Integer.parseInt(dataCustomersAffected);
				incidentVoiceCustomersAffected += Integer.parseInt(voiceCustomersAffected);
				
				System.out.println("incidentDataCustomersAffected = " + incidentDataCustomersAffected);
				System.out.println("incidentVoiceCustomersAffected = " + incidentVoiceCustomersAffected);
			}
		}
		
		// Check if for the same Incident ID, Service & Hierarchy - We have already an entry
		for (String service : servicesAffected)
		{	
			for(int i=0;i<myHier.size();i++)
			{
				boolean incidentAlreadyExists = wb.dbs.CheckIfCriteriaExists("SubmittedIncidents", new String[] {"IncidentStatus", "IncidentID", "AffectedServices", "HierarchySelected"}, "IncidentStatus='OPEN' AND IncidentID = '" + IncidentID + "' AND AffectedServices = '" + service + "' AND HierarchySelected = '" + myHier.get(i).toString() + "'"); 
				if (incidentAlreadyExists)
				{
					throw new InvalidInputException("There is already an openned incident (" + IncidentID + ") that defines outage for AffectedService = " + service + " and HierarchySelected = " + myHier.get(i).toString(), "Error 195" );
				}
			}
		}

		for (String service : servicesAffected)
		{	
			for(int i=0; i < myHier.size(); i++)
			{
				// Add One
				OutageID_Integer += 1;
				
				// Check Hierarchy Format Key_Value Pairs
				Help_Func.checkHierarchyFormatKeyValuePairs(myHier.get(i).toString());				
				
				// Firstly determine the hierarchy table that will be used based on the root hierarchy provided
				String rootHierarchySelected = Help_Func.GetRootHierarchyNode(myHier.get(i).toString());
				
				// Determine Tables for Data/Voice subscribers
				String dataSubsTable =  wb.dbs.GetOneValue("HierarchyTablePerTechnology2", "DataSubscribersTableName", "RootHierarchyNode = '" + rootHierarchySelected + "'");
				String voiceSubsTable =  wb.dbs.GetOneValue("HierarchyTablePerTechnology2", "VoiceSubscribersTableName", "RootHierarchyNode = '" + rootHierarchySelected + "'");
				
				// Get Hierarchies for Data/Voice Tables
				String fullDataHierarchyPath = wb.dbs.GetOneValue("HierarchyTablePerTechnology2", "DataSubscribersTableNamePath", "RootHierarchyNode = '" + rootHierarchySelected + "'");
				String[] fullDataHierarchyPathSplit = fullDataHierarchyPath.split("->"); 
				String fullVoiceHierarchyPath = wb.dbs.GetOneValue("HierarchyTablePerTechnology2", "VoiceSubscribersTableNamePath", "RootHierarchyNode = '" + rootHierarchySelected + "'");
				String[] fullVoiceHierarchyPathSplit = fullVoiceHierarchyPath.split("->");
				
				// Count distinct values of Usernames or CliVlaues the respective columns
				String dataCustomersAffected = dbs.CountDistinctRowsForSpecificColumn(dataSubsTable, "Username", Help_Func.HierarchyToPredicate(Help_Func.ReplaceHierarchyForSubscribersAffected(myHier.get(i).toString(), fullDataHierarchyPathSplit)));
				String voiceCustomersAffected = dbs.CountDistinctRowsForSpecificColumns(voiceSubsTable, new String[] {"ActiveElement","Subrack","Slot","Port","PON"}, Help_Func.HierarchyToPredicate(Help_Func.ReplaceHierarchyForSubscribersAffected(myHier.get(i).toString(), fullVoiceHierarchyPathSplit)));
				String CLIsAffected = dbs.CountDistinctRowsForSpecificColumn(voiceSubsTable, "CliValue", Help_Func.HierarchyToPredicate(Help_Func.ReplaceHierarchyForSubscribersAffected(myHier.get(i).toString(), fullVoiceHierarchyPathSplit)));
				
				// For Voice no data customers are affected and vice versa
				if (service.equals("Voice"))
				{
					dataCustomersAffected = "0";
				}
				else if (service.equals("Internet"))
				{
					voiceCustomersAffected = "0";
					CLIsAffected = "0";
				}

				// Convert it to String (only for the sake of the below method (InsertValuesInTableGetSequence) - In the database it is still an integer
				String OutageID_String = Integer.toString(OutageID_Integer);
				
				// Insert Values in Database
				wb.dbs.InsertValuesInTable("SubmittedIncidents", 
				new String[] {"DateTime", "OutageID", "IncidentStatus", "RequestTimestamp", "SystemID", "UserID", "IncidentID", 
						"Scheduled", "StartTime", "EndTime", "Duration", "AffectedServices", "Impact", "Priority", "HierarchySelected", "AffectedVoiceCustomers",
						"AffectedDataCustomers", "AffectedCLICustomers", "IncidentAffectedVoiceCustomers", "IncidentAffectedDataCustomers" },
				new String[] {
						Help_Func.now(),
						OutageID_String,
						"OPEN",
						RequestTimestamp,
						SystemID,
						UserID,
						IncidentID,
						Scheduled,
						StartTime,
						EndTime,
						Duration,
						service,
						Impact,
						Priority,
						myHier.get(i).toString(),
						voiceCustomersAffected,
						dataCustomersAffected,
						CLIsAffected,
						Integer.toString(incidentVoiceCustomersAffected),
						Integer.toString(incidentDataCustomersAffected)
				},
				new String[] {"DateTime", "Integer", "String", "DateTime", "String", "String", "String", "String", "DateTime", "DateTime", 
						"String", "String", "String", "String", "String", "Integer", "Integer", "Integer", "Integer", "Integer" }
				);
		
				if (Integer.parseInt(OutageID_String) > 0)
				{
					ProductOfSubmission ps = new ProductOfSubmission(OutageID_String, 
							IncidentID, 
							voiceCustomersAffected, 
							dataCustomersAffected, 
							CLIsAffected, 
							Integer.toString(incidentVoiceCustomersAffected),
							Integer.toString(incidentDataCustomersAffected), 
							"1", 
							service, 
							myHier.get(i).toString(), 
							"Submitted Successfully");
					
					prodElementsList.add(ps);
				}
			}
		}

		wb.conObj.closeDBConnection();
		return prodElementsList;
	}

	@WebMethod
	@WebResult(name="Result")
	public List<ProductOfGetOutage> getOutageStatus
	(
		@WebParam(name="UserName", header = true, mode = Mode.IN) String UserName,
		@WebParam(name="Password", header = true, mode = Mode.IN) String Password,
		// Defines Uniquely The Incident
		@WebParam(name="IncidentID") @XmlElement( required = true ) String IncidentID,
		@WebParam(name="IncidentStatus") @XmlElement( required = true ) String IncidentStatus
	) throws SQLException, InvalidInputException, InstantiationException, IllegalAccessException, ClassNotFoundException
	{	
		WebSpectra wb = new WebSpectra();
		List<ProductOfGetOutage> prodElementsList = new ArrayList<>();
		
		
		// Check if Authentication credentials are correct.
		if (! wb.dbs.AuthenticateRequest(UserName, Password) ) {throw new InvalidInputException("User name or Password incorrect!", "Error 100");}
		
		// Number of rows that will be returned
		String numOfRows = wb.dbs.NumberOfRowsFound("SubmittedIncidents", "IncidentID = 'Incident1' AND IncidentStatus = 'OPEN'");
		
		ResultSet rs = wb.dbs.GetRows("SubmittedIncidents", new String[] {"outageID", "requestID", "incidentStatus", 
				"requestTimestamp", "systemID", "userID", "incidentID", "scheduled", "startTime", "endTime", "duration", 
				"affectedServices", "impact", "priority", "hierarchyselected"}, 
				"IncidentID = '" + IncidentID + "' AND " + "IncidentStatus = '" + IncidentStatus + "';");
		
		if (Integer.parseInt(numOfRows) == 0)
		{
			throw new InvalidInputException("No Results found", "No Results found according to your predicates");
		}
		else
		{
			while ( rs.next() )
			{
				ProductOfGetOutage pg = new ProductOfGetOutage(
				rs.getString("outageID"),
				rs.getString("requestID"),
				rs.getString("incidentStatus"),
				rs.getString("requestTimestamp"),
				rs.getString("systemID"),
				rs.getString("userID"),
				rs.getString("incidentID"),
				rs.getString("scheduled"),
				rs.getString("startTime"),
				rs.getString("endTime"),
				rs.getString("duration"),
				rs.getString("affectedServices"),
				rs.getString("impact"),
				rs.getString("priority"),
				rs.getString("hierarchyselected")
				);
				prodElementsList.add(pg);
			}
		}

		wb.conObj.closeDBConnection();
		
		return prodElementsList;
	}	
	
	@WebMethod
	@WebResult(name="Result")
	public List<ProductOfSubmission> modifyOutage
	(
		@WebParam(name="UserName", header = true, mode = Mode.IN) String UserName,
		@WebParam(name="Password", header = true, mode = Mode.IN) String Password,
		@WebParam(name="OutageID") @XmlElement( required = true ) String OutageID,
		@WebParam(name="RequestTimestamp") @XmlElement( required = true ) String RequestTimestamp,
		@WebParam(name="SystemID") @XmlElement( required = true ) String SystemID,
		@WebParam(name="UserID") @XmlElement( required = true ) String UserID,
		@WebParam(name="IncidentID") @XmlElement( required = true ) String IncidentID,
		@WebParam(name="StartTime") @XmlElement( required = false ) String StartTime,
		@WebParam(name="EndTime") @XmlElement( required = false ) String EndTime,
		@WebParam(name="Duration") @XmlElement( required = false ) String Duration,
		// Voice|Internet|IP TV
		@WebParam(name="AffectedServices") @XmlElement( required = false ) String AffectedServices,
		// Quality, Loss
		@WebParam(name="Impact") @XmlElement( required = false ) String Impact,
		@WebParam(name="HierarchySelected") @XmlElement( required = false ) String HierarchySelected
	) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, InvalidInputException
	{	
		WebSpectra wb = new WebSpectra();
		List<ProductOfSubmission> prodElementsList = new ArrayList<>();
		
		// Check if Authentication credentials are correct.
		if (! wb.dbs.AuthenticateRequest(UserName, Password) ) {throw new InvalidInputException("User name or Password incorrect!", "Error 100");}
		
		// Check if Required fields are empty
		Help_Func.ValidateNotEmpty("OutageID", OutageID);
		Help_Func.ValidateNotEmpty("RequestTimestamp", RequestTimestamp);
		Help_Func.ValidateNotEmpty("SystemID", SystemID);
		Help_Func.ValidateNotEmpty("UserID", UserID);
		Help_Func.ValidateNotEmpty("IncidentID", IncidentID);
		
	//	ProductOfSubmission ps = new ProductOfSubmission(OutageID, IncidentID, "Unknown", "1", "Modified Successfully");
	//	prodElementsList.add(ps);
		
		wb.conObj.closeDBConnection();
	//	return prodElementsList;
		return null;
	}

	@WebMethod
	@WebResult(name="Result")
	public List<ProductOfSubmission> closeOutage
	(
		@WebParam(name="UserName", header = true, mode = Mode.IN) String UserName,
		@WebParam(name="Password", header = true, mode = Mode.IN) String Password,
		@WebParam(name="RequestID") @XmlElement( required = true ) String RequestID,
		@WebParam(name="RequestTimestamp") @XmlElement( required = true ) String RequestTimestamp,
		@WebParam(name="SystemID") @XmlElement( required = true ) String SystemID,
		@WebParam(name="UserID") @XmlElement( required = true ) String UserID,
		
		// Defines Uniquely The Incident
		@WebParam(name="IncidentID") @XmlElement( required = true ) String IncidentID,
		//@WebParam(name="Scheduled") @XmlElement( required = true ) String Scheduled,
		//@WebParam(name="StartTime") @XmlElement( required = true ) String StartTime,
		@WebParam(name="EndTime") @XmlElement( required = false ) String EndTime
		//@WebParam(name="Duration") @XmlElement( required = false ) String Duration,
		// TV, VOICE, DATA
		//@WebParam(name="AffectedServices") @XmlElement( required = false ) String AffectedServices,
		// Quality, Loss
		//@WebParam(name="Impact") @XmlElement( required = false ) String Impact,
		//@WebParam(name="Priority") @XmlElement( required = true ) String Priority,
		// @WebParam(name="Type") @XmlElement( required = true ) String Type,
		//LLU||Elementname||/slot||3##4$$
		//@WebParam(name="HieararchySelected") @XmlElement( required = true ) String HieararchySelected
	) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, InvalidInputException
	{
		//try {
		//	boolean result = wb.dbs.InsertValuesInTable("SubmittedIncidents", new String[] {"RequestID", "UserID"}, new String[] {RequestID, UserID});
		//} catch (SQLException e) {
		//	// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
		WebSpectra wb = new WebSpectra();
		wb.conObj.closeDBConnection();
		
		// Check if Authentication credentials are correct.
		if (! wb.dbs.AuthenticateRequest(UserName, Password) ) {throw new InvalidInputException("User name or Password incorrect!", "Error 100");}
		
		
		return null;
	}
	
	
	/*
	public static void main(String args[]) throws SQLException
	{
		WebSpectraInterface ws = new WebSpectra();
		List<String> myList = new ArrayList<String>();
		
		myList = ws.getFTTXHierarchy("ATHOARTMBOLT01", null, null, null, null);
		
		for (String item : myList)
		{
			System.out.println(item);
		}

	}
	 */
}


