package gr.wind.spectra.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "Element")
@XmlType(name = "basicStruct5", propOrder = { "status" })
public class ProductOfModify
{
	private String status;

	public ProductOfModify()
	{

	}

	public ProductOfModify(String status)
	{
		this.status = status;
	}

	@XmlElement(name = "status")
	public String getstatus()
	{
		return status;
	}

	public void setstatus(String status)
	{
		this.status = status;
	}

}
