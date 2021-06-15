package gr.wind.spectra.cdrdbconsumer;

import javax.xml.ws.WebFault;

/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.9-b130926.1035
 * Generated source version: 2.2
 *
 */
@WebFault(name = "Exception", targetNamespace = "http://web.cdrdb.wind.gr/")
public class Exception_Exception extends java.lang.Exception
{

	/**
	 * Java type that goes as soapenv:Fault detail element.
	 *
	 */
	private gr.wind.spectra.cdrdbconsumer.Exception faultInfo;

	/**
	 *
	 * @param faultInfo
	 * @param message
	 */
	public Exception_Exception(String message, gr.wind.spectra.cdrdbconsumer.Exception faultInfo)
	{
		super(message);
		this.faultInfo = faultInfo;
	}

	/**
	 *
	 * @param faultInfo
	 * @param cause
	 * @param message
	 */
	public Exception_Exception(String message, gr.wind.spectra.cdrdbconsumer.Exception faultInfo, Throwable cause)
	{
		super(message, cause);
		this.faultInfo = faultInfo;
	}

	/**
	 *
	 * @return
	 *     returns fault bean: gr.wind.cdrdb.web.Exception
	 */
	public gr.wind.spectra.cdrdbconsumer.Exception getFaultInfo()
	{
		return faultInfo;
	}

}
