package gr.wind.spectra.model;

import java.sql.SQLException;
import java.util.List;

import javax.xml.bind.annotation.*;

import gr.wind.spectra.business.DB_Operations;
import gr.wind.spectra.business.Help_Func;

@XmlRootElement(name = "Element")
@XmlType(name = "basicStruct", propOrder = {"requestID", "type", "item", "hierarchySelected", "potentialCustomersAffected"})
public class Product {
	
	private DB_Operations dbs;
	private String type;
	private List<String> items;
	private String potentialCustomersAffected = "none";
	private String requestID;
	private String hierarchyProvided;
	private String[] nodeNames;
	private String[] nodeValues;
	private String[] hierarchyFullPathList;
	private String[] subsFullPathList;
	private String[] hierElements;
	
	// Empty constructor requirement of JAXB (Java Architecture for XML Binding)
	public Product()
	{
	}
	
	public Product(DB_Operations dbs, String[] hierarchyFullPath, String[] subsFullPath, String hierarchyProvided ,String type, List<String> items, String[] nodeNames, String[] nodeValues, String requestID) throws SQLException
	{
		
		this.dbs = dbs;
		this.hierarchyProvided = hierarchyProvided;
		this.type = type;
		this.items = items;
		this.nodeNames = nodeNames;
		this.nodeValues = nodeValues;
		this.requestID = requestID;
		this.hierarchyFullPathList = hierarchyFullPath;
		this.subsFullPathList = subsFullPath;
		
		this.hierElements = hierarchyProvided.split("->");

		// If hierarchyProvided is null then return only values provided
		if (this.hierarchyProvided == null || this.hierarchyProvided.equals("") || this.hierarchyProvided.equals("?"))
		{
			// System.out.println("APOSTOLIS PRODUCT HERE 1");
		}
		else
		{
			// If hierarchyProvided is not null and has > 1 level hierarchy e.g. FTTX->OltElementName
			if (this.hierElements.length > 1)
			{
				System.out.println("APOSTOLIS PRODUCT HERE 2");
				// Get Root element from hierarchy
				String rootElement = Help_Func.GetRootHierarchyNode(this.hierarchyProvided);
				// Firstly determine the hierarchy table that will be used based on the root hierarchy provided 
				String table =  dbs.GetOneValue("HierarchyTablePerTechnology", "SubscribersTableName", "RootHierarchyNode = '" + rootElement + "'");

				// Number of rows asks different table
				// Because of that we will use correct hierarchy - replaced hierarchy element
				
				// Calculate customers affected but for the correct columns
				System.out.println("this.hierarchyProvided = "+ this.hierarchyProvided);
				System.out.println("Help_Func.ReplaceHierarchyForSubscribersAffected = "+ Help_Func.ReplaceHierarchyForSubscribersAffected(this.hierarchyProvided, subsFullPath));
				for (String item : subsFullPath)
				{
					System.out.println("Item: " + item);
				}
				
				// Calculate Customers Affected but replace column names in order to search table for customers affected
				String customersAffected = dbs.NumberOfRowsFound(table, Help_Func.HierarchyToPredicate(Help_Func.ReplaceHierarchyForSubscribersAffected(this.hierarchyProvided, subsFullPath)));
				this.potentialCustomersAffected = customersAffected;
				
			}
		}
	}
	
	@XmlElement(name = "elementType")
	public String getType()
	{
		return type;
	}
	
	public void setType(String type)
	{
		this.type = type;
	}

	@XmlElement(name = "requestID")
	public String getrequestID()
	{
		return this.requestID;
	}
	
	public void setrequestID(String requestID)
	{
		this.requestID = requestID;
	}	
	
	
	@XmlElement(name = "potentialCustomersAffected")
	public String getpotentialCustomersAffected()
	{
		return potentialCustomersAffected;
	}
	
	public void setpotentialCustomersAffected(String potentialCustomersAffected)
	{
		this.potentialCustomersAffected = potentialCustomersAffected;
	}
	
	public List<String> getitem()
	{
		return this.items;
	}
	
	public void setitem(List<String> valuesList)
	{
		this.items = valuesList;
	}
	
	@XmlElement(name = "hierarchySelected")
	public String gethierarchySelected()
	{
		String output = "";
		if (this.hierarchyProvided == null || this.hierarchyProvided.equals("") || this.hierarchyProvided.equals("?"))
		{
			output = "none";
		}
		else
		{
			// root element provided only
			if (this.hierElements.length == 0)
			{
				output = "None";
			}
			else if (this.hierElements.length >= 1)
			{
				//return this.hierarchyProvided + "->" + this.hierarchyFullPathList[0] + "=";
				output = Help_Func.ConCatHierarchy(nodeNames, nodeValues, this.hierarchyFullPathList);
			}
		}
		
		return output;
	}
	
	public void sethierarchySelected(String hierarchyProvided)
	{
		this.hierarchyProvided = hierarchyProvided;  
	}
	
}
