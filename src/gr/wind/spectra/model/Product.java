package gr.wind.spectra.model;

import java.sql.SQLException;
import java.util.List;

import javax.xml.bind.annotation.*;

import gr.wind.spectra.business.DB_Operations;
import gr.wind.spectra.business.Help_Func;

@XmlRootElement(name = "Element")
@XmlType(name = "basicStruct", propOrder = {"type", "item", "fullHierarchy", "potentialCustomersAffected"})
public class Product {
	
	private String type;
	private List<String> item;
	private String fullHierarchy;
	private String[] nodeNames;
	private String[] nodeValues;
	private String potentialCustomersAffected = "None";
	
	// Empty constructor requirement of JAXB (Java Architecture for XML Binding)
	public Product()
	{
	}
	
	public Product(DB_Operations dbs, String type, List<String> valuesList, String[] nodeNames, String[] nodeValues) throws SQLException
	{
		this.type = type;
		this.item = valuesList;
		this.nodeNames = nodeNames;
		this.nodeValues = nodeValues;

		if (nodeNames.length > 1)
		{
			// Firstly determine the hierarchy table that will be used based on the root hierarchy provided 
			String rootHierarchySelected = Help_Func.GetRootHierarchyNode(getfullHierarchy());
			String table =  dbs.GetOneValue("HierarchyTablePerTechnology", "TableName", "RootHierarchyNode = '" + rootHierarchySelected + "'");
			String customersAffected = dbs.NumberOfRowsFound(table, Help_Func.HierarchyToPredicate(getfullHierarchy()));
			this.potentialCustomersAffected = customersAffected;
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
		return item;
	}
	
	public void setitem(List<String> valuesList)
	{
		this.item = valuesList;
	}
	
	@XmlElement(name = "HieararchySelected")
	public String getfullHierarchy()
	{
		return Help_Func.ConCatHierarchy(nodeNames, nodeValues);
	}
	
	public void setfullHierarchy(String fullHierarchy)
	{
		this.fullHierarchy = fullHierarchy;  
	}
	
}
