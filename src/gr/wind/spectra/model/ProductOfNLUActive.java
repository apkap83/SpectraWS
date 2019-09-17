package gr.wind.spectra.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "Element")
@XmlType(name = "basicStruct8", propOrder = { "requestID", "CLI", "incidentID", "problem_severity", "affected_services",
		"scheduled", "duration", "end_time", "impact", "flag1", "flag2", "flag3" })
public class ProductOfNLUActive
{

	private String requestID;
	private String CLI;
	private String incidentID = "NULL";
	private String problem_severity = "NULL";
	private String affected_services = "NULL";
	private String scheduled = "NULL";
	private String duration = "NULL";
	private String end_time = "NULL";
	private String impact = "NULL";
	private String flag1 = "NULL";
	private String flag2 = "NULL";
	private String flag3 = "NULL";

	public ProductOfNLUActive(String requestID, String CLI, String incidentID, String problem_severity,
			String affected_services, String scheduled, String duration, String end_time, String impact, String flag1,
			String flag2, String flag3)
	{
		this.requestID = requestID;
		this.CLI = CLI;
		this.incidentID = incidentID;
		this.problem_severity = problem_severity;
		this.affected_services = affected_services;
		this.scheduled = scheduled;
		this.duration = duration;
		this.end_time = end_time;
		this.impact = impact;
		this.flag1 = flag1;
		this.flag2 = flag2;
		this.flag3 = flag3;
	}

	@XmlElement(name = "requestID")
	public String getRequestID()
	{
		return requestID;
	}

	public void setRequestID(String requestID)
	{
		this.requestID = requestID;
	}

	@XmlElement(name = "CLI")
	public String getCLI()
	{
		return CLI;
	}

	public void setCLI(String cLI)
	{
		CLI = cLI;
	}

	@XmlElement(name = "incidentID")
	public String getIncidentID()
	{
		return incidentID;
	}

	public void setIncidentID(String incidentID)
	{
		this.incidentID = incidentID;
	}

	@XmlElement(name = "problem_severity")
	public String getProblem_severity()
	{
		return problem_severity;
	}

	public void setProblem_severity(String problem_severity)
	{
		this.problem_severity = problem_severity;
	}

	@XmlElement(name = "affected_services")
	public String getAffected_services()
	{
		return affected_services;
	}

	public void setAffected_services(String affected_services)
	{
		this.affected_services = affected_services;
	}

	@XmlElement(name = "scheduled")
	public String getScheduled()
	{
		return scheduled;
	}

	public void setScheduled(String scheduled)
	{
		this.scheduled = scheduled;
	}

	@XmlElement(name = "duration")
	public String getDuration()
	{
		return duration;
	}

	public void setDuration(String duration)
	{
		this.duration = duration;
	}

	@XmlElement(name = "end_time")
	public String getEnd_time()
	{
		return end_time;
	}

	public void setEnd_time(String end_time)
	{
		this.end_time = end_time;
	}

	@XmlElement(name = "impact")
	public String getImpact()
	{
		return impact;
	}

	public void setImpact(String impact)
	{
		this.impact = impact;
	}

	@XmlElement(name = "flag1")
	public String getFlag1()
	{
		return flag1;
	}

	public void setFlag1(String flag1)
	{
		this.flag1 = flag1;
	}

	@XmlElement(name = "flag2")
	public String getFlag2()
	{
		return flag2;
	}

	public void setFlag2(String flag2)
	{
		this.flag2 = flag2;
	}

	@XmlElement(name = "flag3")
	public String getFlag3()
	{
		return flag3;
	}

	public void setFlag3(String flag3)
	{
		this.flag3 = flag3;
	}

}