package gr.wind.spectra.model;

import java.sql.SQLException;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import gr.wind.spectra.web.InvalidInputException;

@XmlRootElement(name = "Element")
@XmlType(name = "basicStruct3", propOrder = { "outageID", "incidentStatus", "requestTimestamp", "systemID", "userID",
		"incidentID", "scheduled", "startTime", "endTime", "duration", "affectedServices", "impact", "priority",
		"hierarchyselected" })
public class ProductOfGetOutage
{

	private String outageID = "NULL";
	private String incidentStatus = "NULL";
	private String requestTimestamp = "NULL";
	private String systemID = "NULL";
	private String userID = "NULL";
	private String incidentID = "NULL";
	private String scheduled = "NULL";
	private String startTime = "NULL";
	private String endTime = "NULL";
	private String duration = "NULL";
	private String affectedServices = "NULL";
	private String impact = "NULL";
	private String priority = "NULL";
	private String hierarchyselected = "NULL";

	// Empty constructor requirement of JAXB (Java Architecture for XML Binding)
	public ProductOfGetOutage()
	{
	}

	public ProductOfGetOutage(String outageID, String incidentStatus, String requestTimestamp, String systemID,
			String userID, String incidentID, String scheduled, String startTime, String endTime, String duration,
			String affectedServices, String impact, String priority, String hierarchyselected)
			throws SQLException, InvalidInputException
	{
		if (outageID != null)
		{
			this.outageID = outageID;
		}
		if (incidentStatus != null)
		{
			this.incidentStatus = incidentStatus;
		}
		if (requestTimestamp != null)
		{
			this.requestTimestamp = requestTimestamp;
		}
		if (systemID != null)
		{
			this.systemID = systemID;
		}
		if (userID != null)
		{
			this.userID = userID;
		}
		if (incidentID != null)
		{
			this.incidentID = incidentID;
		}
		if (scheduled != null)
		{
			this.scheduled = scheduled;
		}

		if (startTime != null)
		{
			this.startTime = startTime;
		}
		if (endTime != null)
		{
			this.endTime = endTime;
		}
		if (duration != null)
		{
			this.duration = duration;
		}
		if (affectedServices != null)
		{
			this.affectedServices = affectedServices;
		}
		if (impact != null)
		{
			this.impact = impact;
		}
		if (priority != null)
		{
			this.priority = priority;
		}
		if (hierarchyselected != null)
		{
			this.hierarchyselected = hierarchyselected;
		}

	}

	@XmlElement(name = "outageID")
	public String getoutageID()
	{
		return this.outageID;
	}

	public void setoutageID(String outageID)
	{
		this.outageID = outageID;
	}

	@XmlElement(name = "incidentStatus")
	public String getincidentStatus()
	{
		return this.incidentStatus;
	}

	public void setincidentStatus(String incidentStatus)
	{
		this.incidentStatus = incidentStatus;
	}

	@XmlElement(name = "requestTimestamp")
	public String getrequestTimestamp()
	{
		return this.requestTimestamp;
	}

	public void setrequestTimestamp(String requestTimestamp)
	{
		this.requestTimestamp = requestTimestamp;
	}

	@XmlElement(name = "systemID")
	public String getsystemID()
	{
		return this.systemID;
	}

	public void setsystemID(String systemID)
	{
		this.systemID = systemID;
	}

	@XmlElement(name = "userID")
	public String getuserID()
	{
		return this.userID;
	}

	public void setuserID(String userID)
	{
		this.userID = userID;
	}

	@XmlElement(name = "incidentID")
	public String getincidentID()
	{
		return this.incidentID;
	}

	public void setincidentID(String incidentID)
	{
		this.incidentID = incidentID;
	}

	@XmlElement(name = "scheduled")
	public String getscheduled()
	{
		return this.scheduled;
	}

	public void setscheduled(String scheduled)
	{
		this.scheduled = scheduled;
	}

	@XmlElement(name = "startTime")
	public String getstartTime()
	{
		return this.startTime;
	}

	public void setstartTime(String startTime)
	{
		this.startTime = startTime;
	}

	@XmlElement(name = "endTime")
	public String getendTime()
	{
		return this.endTime;
	}

	public void setendTime(String endTime)
	{
		this.endTime = endTime;
	}

	@XmlElement(name = "duration")
	public String getduration()
	{
		return this.duration;
	}

	public void setduration(String duration)
	{
		this.duration = duration;
	}

	@XmlElement(name = "affectedServices")
	public String getaffectedServices()
	{
		return this.affectedServices;
	}

	public void setaffectedServices(String affectedServices)
	{
		this.affectedServices = affectedServices;
	}

	@XmlElement(name = "impact")
	public String getimpact()
	{
		return this.impact;
	}

	public void setimpact(String impact)
	{
		this.impact = impact;
	}

	@XmlElement(name = "priority")
	public String getpriority()
	{
		return this.priority;
	}

	public void setpriority(String priority)
	{
		this.priority = priority;
	}

	@XmlElement(name = "hierarchyselected")
	public String gethierarchyselected()
	{
		return this.hierarchyselected;
	}

	public void sethierarchyselected(String hierarchyselected)
	{
		this.hierarchyselected = hierarchyselected;
	}

}
