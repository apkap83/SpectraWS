package gr.wind.spectra.model;

import java.sql.SQLException;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import gr.wind.spectra.business.DB_Operations;
import gr.wind.spectra.business.Help_Func;
import gr.wind.spectra.web.InvalidInputException;

@XmlRootElement(name = "Element")
@XmlType(name = "basicStruct", propOrder = { "requestID", "type", "item", "hierarchySelected",
		"internetCustomersAffected", "voiceCustomersAffected", "clisAffected", "activeDataCustomersAffected",
		"tvCustomersAffected" })
public class ProductOfGetHierarchy
{

	private String type;
	private List<String> items;
	private String internetCustomersAffected = "none";
	private String voiceCustomersAffected = "none";
	private String CLIsAffected = "none";

	private String activeDataCustomersAffected = "none";
	private String tvCustomersAffected = "none";

	private String requestID;
	private String hierarchyProvided;
	private String[] nodeNames;
	private String[] nodeValues;
	private String[] hierarchyFullPathList;
	private String[] hierElements;

	// Empty constructor requirement of JAXB (Java Architecture for XML Binding)
	public ProductOfGetHierarchy()
	{
	}
	// Product(wb.dbs, fullHierarchyFromDBSplit, fullDataSubsHierarchyFromDBSplit,
	// fullVoiceSubsHierarchyFromDBSplit, Hierarchy, fullHierarchyFromDBSplit[0] ,
	// ElementsList, nodeNames, nodeValues, RequestID);

	public ProductOfGetHierarchy(DB_Operations dbs, String[] hierarchyFullPathList, String[] fullDataHierarchyPath,
			String[] fullVoiceHierarchyPath, String hierarchyProvided, String type, List<String> items,
			String[] nodeNames, String[] nodeValues, String requestID) throws SQLException, InvalidInputException
	{

		this.hierarchyProvided = hierarchyProvided;
		this.type = type;
		this.items = items;
		this.nodeNames = nodeNames;
		this.nodeValues = nodeValues;
		this.requestID = requestID;
		this.hierarchyFullPathList = hierarchyFullPathList;
		this.hierElements = hierarchyProvided.split("->");

		// If hierarchyProvided is null then return only values provided
		if (this.hierarchyProvided == null || this.hierarchyProvided.equals("") || this.hierarchyProvided.equals("?"))
		{
			// System.out.println("APOSTOLIS PRODUCT HERE 1");
		} else
		{
			// If hierarchyProvided is not null and has > 1 level hierarchy e.g.
			// FTTX->OltElementName
			if (this.hierElements.length > 1)
			{
				// Get Root element from hierarchy
				String rootElement = Help_Func.GetRootHierarchyNode(this.hierarchyProvided);

				// Firstly determine the hierarchy table that will be used based on the root
				// hierarchy provided
				String dataSubsTable = dbs.GetOneValue("HierarchyTablePerTechnology2", "DataSubscribersTableName",
						new String[] { "RootHierarchyNode" }, new String[] { rootElement }, new String[] { "String" });

				String voiceSubsTable = dbs.GetOneValue("HierarchyTablePerTechnology2", "VoiceSubscribersTableName",
						new String[] { "RootHierarchyNode" }, new String[] { rootElement }, new String[] { "String" });

				// Calculate Internet Customers Affected but replace column names in order to
				// search table for customers affected
//				String internetCustomersAffected = dbs.CountDistinctRowsForSpecificColumn(dataSubsTable, "Username",
//						Help_Func.HierarchyToPredicate(Help_Func.ReplaceHierarchyForSubscribersAffected(
//								this.hierarchyProvided, fullDataHierarchyPath)));

				String internetCustomersAffected = dbs.CountDistinctRowsForSpecificColumn(dataSubsTable, "Username",
						Help_Func.HierarchyKeys(Help_Func.ReplaceHierarchyForSubscribersAffected(this.hierarchyProvided,
								fullDataHierarchyPath)),
						Help_Func.HierarchyValues(Help_Func
								.ReplaceHierarchyForSubscribersAffected(this.hierarchyProvided, fullDataHierarchyPath)),
						Help_Func.HierarchyStringTypes(Help_Func.ReplaceHierarchyForSubscribersAffected(
								this.hierarchyProvided, fullDataHierarchyPath)));

				this.internetCustomersAffected = internetCustomersAffected;

				// Calculate Voice Customers Affected but replace column names in order to
				// search table for customers affected
				String voiceCustomersAffected = dbs.CountDistinctRowsForSpecificColumns(voiceSubsTable,
						new String[] { "ActiveElement", "Subrack", "Slot", "Port", "PON" },
						Help_Func.HierarchyKeys(Help_Func.ReplaceHierarchyForSubscribersAffected(this.hierarchyProvided,
								fullVoiceHierarchyPath)),
						Help_Func.HierarchyValues(Help_Func.ReplaceHierarchyForSubscribersAffected(
								this.hierarchyProvided, fullVoiceHierarchyPath)),
						Help_Func.HierarchyStringTypes(Help_Func.ReplaceHierarchyForSubscribersAffected(
								this.hierarchyProvided, fullVoiceHierarchyPath)));

				this.voiceCustomersAffected = voiceCustomersAffected;

				// Calculate CLIs Affected but replace column names in order to search table for
				// customers affected
				String CLIsAffected = dbs.CountDistinctRowsForSpecificColumn(voiceSubsTable, "CliValue",
						Help_Func.HierarchyKeys(Help_Func.ReplaceHierarchyForSubscribersAffected(this.hierarchyProvided,
								fullVoiceHierarchyPath)),
						Help_Func.HierarchyValues(Help_Func.ReplaceHierarchyForSubscribersAffected(
								this.hierarchyProvided, fullVoiceHierarchyPath)),
						Help_Func.HierarchyStringTypes(Help_Func.ReplaceHierarchyForSubscribersAffected(
								this.hierarchyProvided, fullVoiceHierarchyPath)));
				this.CLIsAffected = CLIsAffected;

				// Calculate this
				this.activeDataCustomersAffected = "0";
				this.tvCustomersAffected = "0";
			}
		}
	}

	@XmlElement(name = "activeDataCustomersAffected")
	public String getactiveDataCustomersAffected()
	{
		return activeDataCustomersAffected;
	}

	public void setactiveDataCustomersAffected(String activeDataCustomersAffected)
	{
		this.activeDataCustomersAffected = activeDataCustomersAffected;
	}

	@XmlElement(name = "tvCustomersAffected")
	public String gettvCustomersAffected()
	{
		return tvCustomersAffected;
	}

	public void settvCustomersAffected(String tvCustomersAffected)
	{
		this.tvCustomersAffected = tvCustomersAffected;
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

	@XmlElement(name = "internetCustomersAffected")
	public String getinternetCustomersAffected()
	{
		return internetCustomersAffected;
	}

	public void setinternetCustomersAffected(String internetCustomersAffected)
	{
		this.internetCustomersAffected = internetCustomersAffected;
	}

	@XmlElement(name = "voiceCustomersAffected")
	public String getvoiceCustomersAffected()
	{
		return voiceCustomersAffected;
	}

	public void setvoiceCustomersAffected(String voiceCustomersAffected)
	{
		this.voiceCustomersAffected = voiceCustomersAffected;
	}

	@XmlElement(name = "clisAffected")
	public String getclisAffected()
	{
		return this.CLIsAffected;
	}

	public void setclisAffected(String clisAffected)
	{
		this.CLIsAffected = clisAffected;
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
		} else
		{
			// root element provided only
			if (this.hierElements.length == 0)
			{
				output = "None";
			} else if (this.hierElements.length >= 1)
			{
				// return this.hierarchyProvided + "->" + this.hierarchyFullPathList[0] + "=";
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
