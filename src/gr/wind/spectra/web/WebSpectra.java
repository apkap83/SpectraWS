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
	) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, InvalidInputException
	{
		
		WebSpectra wb = new WebSpectra();
		List<String> ElementsList = new ArrayList<String>();
		List<Product> prodElementsList = new ArrayList<>();
		
		// Check if Authentication credentials are correct.
		if (! wb.dbs.AuthenticateRequest(UserName, Password) ) {throw new InvalidInputException("Invalid Credentials", "User name or Password incorrect!");}
		
		// No Hierarchy is given - returns root elements
		if (Hierarchy == null || Hierarchy.equals("") || Hierarchy.equals("?"))
		{
			System.out.println("APOSTOLIS HERE 1");
			String rootHierarchySelected = "";
			ElementsList = wb.dbs.GetOneColumnUniqueResultSet("HierarchyTablePerTechnology", "RootHierarchyNode", "1 = 1");
			String[] nodeNames = new String[] {};
			String[] nodeValues = new String[] {};
			Product pr = new Product(wb.dbs, new String[] {}, new String[] {}, Hierarchy, "rootElements", ElementsList, nodeNames, nodeValues, RequestID);
			prodElementsList.add(pr);
		}
		else
		{
			ArrayList<String> nodeNamesArrayList = new ArrayList<String>();
			ArrayList<String> nodeValuesArrayList = new ArrayList<String>();

			// Get root hierarchy = hierarchy given
			String rootElementInHierarchy = Help_Func.GetRootHierarchyNode(Hierarchy);

			// Get Hierarchy Table for that root hierarchy
			String table = wb.dbs.GetOneValue("HierarchyTablePerTechnology", "HierarchyTableName", "RootHierarchyNode = '" + rootElementInHierarchy + "'");
			
			// Get Full hierarchy from the same table as above in style : OltElementName->OltSlot->OltPort->Onu->ElementName->Slot
			String fullHierarchyFromDB = wb.dbs.GetOneValue("HierarchyTablePerTechnology", "HierarchyTableNamePath", "RootHierarchyNode = '" + rootElementInHierarchy + "'");

			// Split the hierarchy retrieved from DB into fields
			String[] fullHierarchyFromDBSplit = fullHierarchyFromDB.split("->");			
			
			// Get Full hierarchy from the same table as above in style : OltElementName->OltSlot->OltPort->Onu->ActiveElement->Slot
			String subsHierarchyFromDB = wb.dbs.GetOneValue("HierarchyTablePerTechnology", "SubscribersTableNamePath", "RootHierarchyNode = '" + rootElementInHierarchy + "'");

			// Split the hierarchy retrieved from DB into fields
			String[] subsHierarchyFromDBSplit = subsHierarchyFromDB.split("->");

			// Split given hierarchy
			String[] hierItemsGiven = Hierarchy.split(hierSep);

			// If only root Hierarchy is given
			if (hierItemsGiven.length == 1)
			{
				ElementsList = wb.dbs.GetOneColumnUniqueResultSet(table, fullHierarchyFromDBSplit[0], " 1 = 1 ");
				String[] nodeNames = new String[] {rootElementInHierarchy};
				String[] nodeValues = new String[] {"1"};
				Product pr = new Product(wb.dbs, fullHierarchyFromDBSplit, subsHierarchyFromDBSplit, Hierarchy, fullHierarchyFromDBSplit[0] , ElementsList, nodeNames, nodeValues, RequestID);
				prodElementsList.add(pr);	
			}
			else
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
				Product pr = new Product(wb.dbs, fullHierarchyFromDBSplit, subsHierarchyFromDBSplit, Hierarchy, fullHierarchyFromDBSplit[hierItemsGiven.length-1], ElementsList, nodeNames, nodeValues, RequestID);
				prodElementsList.add(pr);
			}
		}
		
		return prodElementsList;
	}
	/*
	@WebMethod
	@WebResult(name="Result")
	public List<Product> getHierarchy(
			@WebParam(name="RequestID") @XmlElement( required = true ) String RequestID,
			@WebParam(name="RequestTimestamp") @XmlElement( required = true ) String RequestTimestamp,
			@WebParam(name="SystemID") @XmlElement( required = true ) String SystemID,
			@WebParam(name="UserID") @XmlElement( required = true ) String UserID,
			@WebParam(name="Type") String Type,
			@WebParam(name="Level1") String Level1,
			@WebParam(name="Level2") String Level2,
			@WebParam(name="Level3") String Level3,
			@WebParam(name="Level4") String Level4,
			@WebParam(name="Level5") String Level5,
			@WebParam(name="Level6") String Level6,
			@WebParam(name="Level7") String Level7,
			@WebParam(name="Level8") String Level8,
			@WebParam(name="Level9") String Level9,
			@WebParam(name="Level10") String Level10,
			@WebParam(name="Level11") String Level11,
			@WebParam(name="Level12") String Level12,
			@WebParam(name="Level13") String Level13,
			@WebParam(name="Level14") String Level14,
			@WebParam(name="Level15") String Level15
			) throws gr.wind.spectra.web.InvalidInputException, SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException
	{	
		WebSpectra wb = new WebSpectra();
		List<String> rootElementsList = new ArrayList<String>();
		List<Product> prodElementsList = new ArrayList<>();
		// FTTX Hierarchy
		if (Type == null || Type.equals("") || Type.equals("?"))
		{
				rootElementsList = wb.dbs.GetOneColumnUniqueResultSet("rootNetworkElementHierarchy", "ElementType", "1 = 1");
				String[] nodeNames = new String[] {};
				String[] nodeValues = new String[] {};;
				Product pr = new Product(wb.dbs, "rootElements", rootElementsList, nodeNames, nodeValues);
				prodElementsList.add(pr);
		}
		else if (Type.equals("FTTX") && ( Level1 == null || Level1.equals("") || Level1.equals("?") ) )
		{
			rootElementsList = wb.dbs.GetOneColumnUniqueResultSet("FTTX_NetworkElementHierarchy", "OltElementName", "1 = 1");
			String[] nodeNames = new String[] {"FTTX"};
			String[] nodeValues = new String[] {"1"};
			Product pr = new Product(wb.dbs, "OltElementName", rootElementsList, nodeNames, nodeValues);
			prodElementsList.add(pr);
		}
		else if (Type.equals("FTTX") && ( Level2 == null || Level2.equals("") || Level2.equals("?") ) )
		{
			rootElementsList = wb.dbs.GetOneColumnUniqueResultSet("FTTX_NetworkElementHierarchy", "OltSlot", "1 = 1");
			String[] nodeNames = new String[] {"FTTX", "OltElementName"};
			String[] nodeValues = new String[] {"1", Level1};
			Product pr = new Product(wb.dbs, "OltSlot", rootElementsList, nodeNames, nodeValues);
			prodElementsList.add(pr);
		}
		else if (Type.equals("FTTX") && ( Level3 == null || Level3.equals("") || Level3.equals("?") ))
		{
			String predicates = Help_Func.AssignSimilarANDPredicates(new String[] {"OltElementName", "OltSlot"}, new String[] { Level1, Level2 });
			rootElementsList =  wb.dbs.GetOneColumnUniqueResultSet("FTTX_NetworkElementHierarchy", "OltPort", predicates);
			String[] nodeNames = new String[] {"FTTX", "OltElementName", "OltSlot"};
			String[] nodeValues = new String[] {"1", Level1, Level2};
			Product pr = new Product(wb.dbs, "OltPort", rootElementsList, nodeNames, nodeValues);
			prodElementsList.add(pr);
		}
		else if (Type.equals("FTTX") && ( Level4 == null || Level4.equals("") || Level4.equals("?") ))
		{				
			String predicates = Help_Func.AssignSimilarANDPredicates(new String[] {"OltElementName", "OltSlot", "OltPort"}, new String[] { Level1, Level2, Level3 });
			rootElementsList =  wb.dbs.GetOneColumnUniqueResultSet("FTTX_NetworkElementHierarchy", "Onu", predicates);
			String[] nodeNames = new String[] {"FTTX", "OltElementName", "OltSlot", "OltPort"};
			String[] nodeValues = new String[] {"1", Level1, Level2, Level3};
			Product pr = new Product(wb.dbs, "Onu", rootElementsList, nodeNames, nodeValues);
			prodElementsList.add(pr);
		}
		else if (Type.equals("FTTX") && ( Level5 == null || Level5.equals("") || Level5.equals("?") ))
		{
			String predicates = Help_Func.AssignSimilarANDPredicates(new String[] {"OltElementName", "OltSlot", "OltPort", "Onu"}, new String[] { Level1, Level2, Level3, Level4 });
			rootElementsList =  wb.dbs.GetOneColumnUniqueResultSet("FTTX_NetworkElementHierarchy", "ElementName", predicates);
			String[] nodeNames = new String[] {"FTTX", "OltElementName", "OltSlot", "OltPort", "Onu"};
			String[] nodeValues = new String[] {"1", Level1, Level2, Level3, Level4};
			Product pr = new Product(wb.dbs, "ElementName", rootElementsList, nodeNames, nodeValues);
			prodElementsList.add(pr);
		}
		else if (Type.equals("FTTX") && ( Level6 == null || Level6.equals("") || Level6.equals("?") ))
		{
			String predicates = Help_Func.AssignSimilarANDPredicates(new String[] {"OltElementName", "OltSlot", "OltPort", "Onu", "ElementName"}, new String[] { Level1, Level2, Level3, Level4, Level5 });
			rootElementsList =  wb.dbs.GetOneColumnUniqueResultSet("FTTX_NetworkElementHierarchy", "Slot", predicates);
			String[] nodeNames = new String[] {"FTTX", "OltElementName", "OltSlot", "OltPort", "Onu", "ElementName"};
			String[] nodeValues = new String[] {"1", Level1, Level2, Level3, Level4, Level5};
			Product pr = new Product(wb.dbs, "Slot", rootElementsList, nodeNames, nodeValues);
			prodElementsList.add(pr);
		}
		else if (Type.equals("FTTX") && ( Level6 != null || ! Level6.equals("") || ! Level6.equals("?") ))
		{
			throw new InvalidInputException("Invalid Input", "There is no Level 7 for FTTX type");
		}
		else if (Type.equals("LLU") && ( Level1 == null || Level1.equals("") || Level1.equals("?") ))
		{
			rootElementsList = wb.dbs.GetOneColumnUniqueResultSet("LLU_All_NetworkElementHierarchy", "ElementName", "1 = 1");
			String[] nodeNames = new String[] {"LLU"};
			String[] nodeValues = new String[] {"1"};
			Product pr = new Product(wb.dbs, "ElementName", rootElementsList, nodeNames, nodeValues);
			prodElementsList.add(pr);
		}
		else if (Type.equals("LLU") && ( Level2 == null || Level2.equals("") || Level2.equals("?") ))
		{
			String predicates = Help_Func.AssignSimilarANDPredicates(new String[] {"ElementName"}, new String[] { Level1 });
			rootElementsList =  wb.dbs.GetOneColumnUniqueResultSet("LLU_All_NetworkElementHierarchy", "Subrack", predicates);
			String[] nodeNames = new String[] {"LLU", "ElementName"};
			String[] nodeValues = new String[] {"1", Level1};
			Product pr = new Product(wb.dbs, "Subrack", rootElementsList, nodeNames, nodeValues);
			prodElementsList.add(pr);
		}
		else if (Type.equals("LLU") && ( Level3 == null || Level3.equals("") || Level3.equals("?") ))
		{
			String predicates = Help_Func.AssignSimilarANDPredicates(new String[] {"ElementName", "Subrack"}, new String[] { Level1, Level2 });
			rootElementsList =  wb.dbs.GetOneColumnUniqueResultSet("LLU_All_NetworkElementHierarchy", "Slot", predicates);
			String[] nodeNames = new String[] {"LLU", "ElementName", "Subrack"};
			String[] nodeValues = new String[] {"1", Level1, Level2};
			Product pr = new Product(wb.dbs, "Slot", rootElementsList, nodeNames, nodeValues);
			prodElementsList.add(pr);
		}
		else if (Type.equals("LLU") && ( Level4 != null || ! Level4.equals("") || ! Level4.equals("?") ))
		{
			throw new InvalidInputException("Invalid Input", "There is no Level 4 for LLU type");
		}
		else if (Type.equals("OLT") && ( Level1 == null || Level1.equals("") || Level1.equals("?") ))
		{
			rootElementsList = wb.dbs.GetOneColumnUniqueResultSet("FTTH_NetworkElementHierarchy", "OltElementName", "1 = 1");
			String[] nodeNames = new String[] {"OLT"};
			String[] nodeValues = new String[] {"1"};
			Product pr = new Product(wb.dbs, "OltElementName", rootElementsList, nodeNames, nodeValues);
			prodElementsList.add(pr);
		}
		else if (Type.equals("OLT") && ( Level2 == null || Level2.equals("") || Level2.equals("?") ))
		{
			String predicates = Help_Func.AssignSimilarANDPredicates(new String[] {"OltElementName"}, new String[] { Level1 });
			rootElementsList =  wb.dbs.GetOneColumnUniqueResultSet("FTTH_NetworkElementHierarchy", "OltSlot", predicates);
			String[] nodeNames = new String[] {"OLT", "OltElementName"};
			String[] nodeValues = new String[] {"1", Level1};
			Product pr = new Product(wb.dbs, "OltSlot", rootElementsList, nodeNames, nodeValues);
			prodElementsList.add(pr);			
		}
		else if (Type.equals("OLT") && ( Level3 == null || Level3.equals("") || Level3.equals("?") ))
		{
			String predicates = Help_Func.AssignSimilarANDPredicates(new String[] {"OltElementName", "OltSlot"}, new String[] { Level1, Level2 });
			rootElementsList =  wb.dbs.GetOneColumnUniqueResultSet("FTTH_NetworkElementHierarchy", "OltPon", predicates);
			String[] nodeNames = new String[] {"OLT", "OltElementName", "OltSlot"};
			String[] nodeValues = new String[] {"1", Level1, Level2};
			Product pr = new Product(wb.dbs, "OltPon", rootElementsList, nodeNames, nodeValues);
			prodElementsList.add(pr);	
		}
		else if (Type.equals("OLT") && ( Level4 == null || Level4.equals("") || Level4.equals("?") ))
		{
			String predicates = Help_Func.AssignSimilarANDPredicates(new String[] {"OltElementName", "OltSlot", "OltPon"}, new String[] { Level1, Level2, Level3 });
			rootElementsList =  wb.dbs.GetOneColumnUniqueResultSet("FTTH_NetworkElementHierarchy", "OnuID", predicates);
			String[] nodeNames = new String[] {"OLT", "OltElementName", "OltSlot", "OltPon"};
			String[] nodeValues = new String[] {"1", Level1, Level2, Level3};
			Product pr = new Product(wb.dbs, "OnuID", rootElementsList, nodeNames, nodeValues);
			prodElementsList.add(pr);	
		}
		else if (Type.equals("OLT") && ( Level5 == null || Level5.equals("") || Level5.equals("?") ))
		{
			String predicates = Help_Func.AssignSimilarANDPredicates(new String[] {"OltElementName", "OltSlot", "OltPon", "OnuID"}, new String[] { Level1, Level2, Level3, Level4 });
			rootElementsList =  wb.dbs.GetOneColumnUniqueResultSet("FTTH_NetworkElementHierarchy", "BepPortNo", predicates);
			String[] nodeNames = new String[] {"OLT", "OltElementName", "OltSlot", "OltPon", "OnuID"};
			String[] nodeValues = new String[] {"1", Level1, Level2, Level3, Level4};
			Product pr = new Product(wb.dbs, "BepPortNo", rootElementsList, nodeNames, nodeValues);
			prodElementsList.add(pr);	
		}
		else if (Type.equals("OLT") && ( Level6 != null || ! Level6.equals("") || ! Level6.equals("?") ))
		{
			throw new InvalidInputException("Invalid Input", "There is no Level 6 for OLT type");
		}
		else if (Type.equals("BRAS") && ( Level2 == null || Level2.equals("") || Level2.equals("?") ))
		{
			rootElementsList = wb.dbs.GetOneColumnUniqueResultSet("IPBB_NetworkElementHierarchy", "Brasname", "1 = 1");
			String[] nodeNames = new String[] {"BRAS"};
			String[] nodeValues = new String[] {"1"};
			Product pr = new Product(wb.dbs, "Brasname", rootElementsList, nodeNames, nodeValues);
			prodElementsList.add(pr);
		}
		else if (Type.equals("BRAS") && ( Level1 != null || ! Level1.equals("") || ! Level1.equals("?") ))
		{
			throw new InvalidInputException("Invalid Input", "There is no Level 2 for BRAS type");
		}
		
		wb.conObj.closeDBConnection();
		return prodElementsList;
	}
	*/
	@WebMethod
	@WebResult(name="Result")
	public List<ProductOfSubmission> submitOutage
	(
		@WebParam(name="UserName", header = true, mode = Mode.IN) String UserName,
		@WebParam(name="Password", header = true, mode = Mode.IN) String Password,
		@WebParam(name="RequestID") @XmlElement( required = true ) String RequestID,
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
		@WebParam(name="AffectedServices") @XmlElement( required = false ) String AffectedServices,
		// Quality, Loss
		@WebParam(name="Impact") @XmlElement( required = false ) String Impact,
		@WebParam(name="Priority") @XmlElement( required = true ) String Priority,
		// @WebParam(name="Type") @XmlElement( required = true ) String Type,
		//LLU||Elementname||/slot||3##4$$
		@WebParam(name="HierarchySelected") @XmlElement( required = true ) String HierarchySelected
	) throws InvalidInputException, ParseException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException
	{	
		WebSpectra wb = new WebSpectra();
		List<ProductOfSubmission> prodElementsList = new ArrayList<>();
		String OutageID;
		
		// Check if Authentication credentials are correct.
		if (! wb.dbs.AuthenticateRequest(UserName, Password) ) {throw new InvalidInputException("Invalid Credentials", "User name or Password incorrect!");}
		
		try {
			/*boolean result = wb.dbs.InsertValuesInTable("SubmittedIncidents", 
					new String[] {"DateTime", "RequestID", "RequestTimestamp", "SystemID", "UserID", "IncidentID", "Scheduled", "StartTime", "EndTime", "Duration", "AffectedServices", "Impact", "Priority", "HierarchySelected"}, 
					new String[] {
							"2019-01-01 00:01:00",
							"R1",
							"2019-01-01 00:01:00",
							"Remedy",
							"akapetan",
							"Incident1",
							"N",
							"2019-01-01 00:01:00",
							"2019-01-01 00:01:00",
							"1",
							"TV",
							"Quality",
							"Major",
							"FTTX=1||OLTElementName=Tolis"
					});
				*/
			
				// Validate
				Help_Func.ValidateDateTimeFormat(RequestTimestamp);
				Help_Func.ValidateDateTimeFormat(StartTime);
				Help_Func.ValidateDateTimeFormat(EndTime);

				java.util.List myHier = Help_Func.GetHierarchySelections(HierarchySelected);
				
				for(int i=0;i<myHier.size();i++)
				{
					// Firstly determine the hierarchy table that will be used based on the root hierarchy provided 
					String rootHierarchySelected = Help_Func.GetRootHierarchyNode(myHier.get(i).toString());
					String table =  wb.dbs.GetOneValue("HierarchyTablePerTechnology", "TableName", "RootHierarchyNode = '" + rootHierarchySelected + "'");
					String customersAffected = wb.dbs.NumberOfRowsFound(table, Help_Func.HierarchyToPredicate(myHier.get(i).toString()));
					
					OutageID = wb.dbs.InsertValuesInTableGetSequence("SubmittedIncidents", 
					new String[] {"DateTime", "RequestID", "IncidentStatus", "RequestTimestamp", "SystemID", "UserID", "IncidentID", 
							"Scheduled", "StartTime", "EndTime", "Duration", "AffectedServices", "Impact", "Priority", "HierarchySelected", "AffectedCustomers" },
					new String[] {
							Help_Func.now(),
							RequestID,
							"OPEN",
							RequestTimestamp,
							SystemID,
							UserID,
							IncidentID,
							Scheduled,
							StartTime,
							EndTime,
							Duration,
							AffectedServices,
							Impact,
							Priority,
							myHier.get(i).toString(),
							customersAffected
					},
					new String[] {"DateTime", "String", "String", "DateTime", "String", "String", "String", "String", "DateTime", "DateTime", 
							"String", "String", "String", "String", "String", "Integer" }
							);
					
					if (Integer.parseInt(OutageID) > 0)
					{
						ProductOfSubmission ps = new ProductOfSubmission(RequestID, OutageID, IncidentID, customersAffected, "1", "Submitted Successfully");
						prodElementsList.add(ps);
					
					}
				}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("ERRRRROR!");
			e.printStackTrace();
		}
		
		// Calculate CLIs affected
		//if (Outcome == true)
		//{
			// Firstly determine the hierarchy table that will be used based on the root hierarchy provided 
			//wb.dbs.GetOneValue("HierarchyTablePerTechnology", "TableName", "RootHierarchyNode = ");
		
			//int rowsAffected = wb.dbs.NumberOfRowsFound(, String predicate)
			//ProductOfSubmission ps = new ProductOfSubmission(RequestID, IncidentID, Integer.toString(rowsAffected), "1", "SUCCESS!");
			//prodElementsList.add(ps);
		//}
		
		//try {
			
		//	if (predicates.get(0).equals("FTTX"))
			//{
			
				//int rowsAffected = wb.dbs.UpdateValuesForOneColumn("Internet_Resource_Path", "OutageID", OutageID, predicates.get(1));	
				//ProductOfSubmission ps = new ProductOfSubmission(RequestID, IncidentID, Integer.toString(rowsAffected), "1", "SUCCESS!");
				//prodElementsList.add(ps);
			//}
		//	System.out.println(predicates.get(0));
			
		//} catch (SQLException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
		//return prodElementsList;
		
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
		if (! wb.dbs.AuthenticateRequest(UserName, Password) ) {throw new InvalidInputException("Invalid Credentials", "User name or Password incorrect!");}
		
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
	public List<Product> modifyOutage
	(
		@WebParam(name="UserName", header = true, mode = Mode.IN) String UserName,
		@WebParam(name="Password", header = true, mode = Mode.IN) String Password,
		@WebParam(name="RequestID") @XmlElement( required = true ) String RequestID,
		@WebParam(name="RequestTimestamp") @XmlElement( required = true ) String RequestTimestamp,
		@WebParam(name="SystemID") @XmlElement( required = true ) String SystemID,
		@WebParam(name="UserID") @XmlElement( required = true ) String UserID,

		// Defines Uniquely The Incident & Should match line in SubmittedIncidents Table
		@WebParam(name="IncidentID") @XmlElement( required = true ) String IncidentID,
		// @WebParam(name="Scheduled") @XmlElement( required = true ) String Scheduled,
		// @WebParam(name="StartTime") @XmlElement( required = true ) String StartTime,
		
		@WebParam(name="EndTime") @XmlElement( required = false ) String EndTime,
		@WebParam(name="Duration") @XmlElement( required = false ) String Duration,
		// TV, VOICE, DATA
		@WebParam(name="AffectedServices") @XmlElement( required = false ) String AffectedServices,
		// Quality, Loss
		@WebParam(name="Impact") @XmlElement( required = false ) String Impact,
		@WebParam(name="Priority") @XmlElement( required = false ) String Priority,
		// @WebParam(name="Type") @XmlElement( required = true ) String Type,
		//LLU||Elementname||/slot||3##4$$
		@WebParam(name="HierarchySelected") @XmlElement( required = true ) String HierarchySelected
	) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, InvalidInputException
	{	
		WebSpectra wb = new WebSpectra();
		wb.conObj.closeDBConnection();
		
		// Check if Authentication credentials are correct.
		if (! wb.dbs.AuthenticateRequest(UserName, Password) ) {throw new InvalidInputException("Invalid Credentials", "User name or Password incorrect!");}
		
		return null;
	}

	@WebMethod
	@WebResult(name="Result")
	public List<Product> closeOutage
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
		if (! wb.dbs.AuthenticateRequest(UserName, Password) ) {throw new InvalidInputException("Invalid Credentials", "User name or Password incorrect!");}
		
		
		return null;
	}
	
	public static void main(String args[]) throws SQLException
	{/*
		WebSpectraInterface ws = new WebSpectra();
		List<String> myList = new ArrayList<String>();
		
		myList = ws.getFTTXHierarchy("ATHOARTMBOLT01", null, null, null, null);
		
		for (String item : myList)
		{
			System.out.println(item);
		}
		*/
	}
}


