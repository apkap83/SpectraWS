package gr.wind.spectra.model;

import java.util.List;

import javax.xml.bind.annotation.*;

import gr.wind.spectra.business.Help_Func;

@XmlRootElement(name = "Element")
@XmlType(name = "basicStruct2", propOrder = {"requestID", "outageID", "incidentID", "numOfCustomerssAffected", "statusCode", "description"})
public class ProductOfSubmission {
	
	private String requestID;
	private String outageID;
	private String incidentID;
	private String numOfCustomerssAffected;
	private String statusCode;
	private String description;
	
	// Empty constructor requirement of JAXB (Java Architecture for XML Binding)
	public ProductOfSubmission()
	{
	}
	
	public ProductOfSubmission(String requestID, String outageID, String incidentID, String numOfCustomerssAffected, String statusCode, String description)
	{
		this.incidentID = incidentID;
		this.outageID = outageID;
		this.numOfCustomerssAffected = numOfCustomerssAffected;
		this.statusCode = statusCode;
		this.description = description;
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

	@XmlElement(name = "outageID")
	public String getoutageID()
	{
		return this.outageID;
	}
	
	public void setoutageID(String outageID)
	{
		this.requestID = outageID;
	}
	
	@XmlElement(name = "IncidentID")
	public String getIncidentID()
	{
		return this.incidentID;
	}
	
	public void setIncidentID(String incidentID)
	{
		this.incidentID = incidentID;
	}
	
	@XmlElement(name = "numOfCustomerssAffected")
	public String getnumOfCustomerssAffected()
	{
		return this.numOfCustomerssAffected;
	}
	
	public void setnumOfCustomerssAffected(String numOfClis)
	{
		this.numOfCustomerssAffected = numOfClis;
	}

	@XmlElement(name = "statusCode")
	public String getstatusCode()
	{
		return this.statusCode;
	}
	
	public void setstatusCode(String statusCode)
	{
		this.statusCode = statusCode;
	}
	
	@XmlElement(name = "description")
	public String getdescription()
	{
		return this.description;
	}
	
	public void setdescription(String description)
	{
		this.description = description;
	}
	
}
