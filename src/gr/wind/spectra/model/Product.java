package gr.wind.spectra.model;

import java.util.List;

import javax.xml.bind.annotation.*;

import gr.wind.spectra.business.Help_Func;

@XmlRootElement(name = "Element")
@XmlType(name = "basicStruct", propOrder = {"type", "item", "fullHierarchy"})
public class Product {
	
	private String type;
	private List<String> item;
	private String fullHierarchy;
	private String[] nodeNames;
	private String[] nodeValues;
	
	// Empty constructor requirement of JAXB (Java Architecture for XML Binding)
	public Product()
	{
	}
	
	public Product(String type, List<String> valuesList, String[] nodeNames, String[] nodeValues)
	{
		this.type = type;
		this.item = valuesList;
		this.nodeNames = nodeNames;
		this.nodeValues = nodeValues;
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
