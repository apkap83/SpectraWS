package gr.wind.spectra.model;

import java.util.List;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "Element")
@XmlType(name = "basicStruct", propOrder = {"type", "item"})
public class Product {
	
	private String type;
	private List<String> item;
	
	// Empty constructor requirement of JAXB (Java Architecture for XML Binding)
	public Product()
	{
	}
	
	public Product(String type, List<String> valuesList)
	{
		this.type = type;
		this.item = valuesList;
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
}
