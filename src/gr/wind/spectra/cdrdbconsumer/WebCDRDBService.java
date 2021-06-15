package gr.wind.spectra.cdrdbconsumer;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;

/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.9-b130926.1035
 * Generated source version: 2.2
 *
 */
@WebServiceClient(name = "WebCDRDBService", targetNamespace = "http://web.cdrdb.wind.gr/", wsdlLocation = "http://10.10.18.120:8080/CDR_DB_WS/WebCDRDBService?wsdl")
public class WebCDRDBService extends Service
{

	private final static URL WEBCDRDBSERVICE_WSDL_LOCATION;
	private final static WebServiceException WEBCDRDBSERVICE_EXCEPTION;
	private final static QName WEBCDRDBSERVICE_QNAME = new QName("http://web.cdrdb.wind.gr/", "WebCDRDBService");

	static
	{
		URL url = null;
		WebServiceException e = null;
		try
		{
			url = new URL("http://10.10.18.120:8080/CDR_DB_WS/WebCDRDBService?wsdl");
		} catch (MalformedURLException ex)
		{
			e = new WebServiceException(ex);
		}
		WEBCDRDBSERVICE_WSDL_LOCATION = url;
		WEBCDRDBSERVICE_EXCEPTION = e;
	}

	public WebCDRDBService()
	{
		super(__getWsdlLocation(), WEBCDRDBSERVICE_QNAME);
	}

	public WebCDRDBService(WebServiceFeature... features)
	{
		super(__getWsdlLocation(), WEBCDRDBSERVICE_QNAME, features);
	}

	public WebCDRDBService(URL wsdlLocation)
	{
		super(wsdlLocation, WEBCDRDBSERVICE_QNAME);
	}

	public WebCDRDBService(URL wsdlLocation, WebServiceFeature... features)
	{
		super(wsdlLocation, WEBCDRDBSERVICE_QNAME, features);
	}

	public WebCDRDBService(URL wsdlLocation, QName serviceName)
	{
		super(wsdlLocation, serviceName);
	}

	public WebCDRDBService(URL wsdlLocation, QName serviceName, WebServiceFeature... features)
	{
		super(wsdlLocation, serviceName, features);
	}

	/**
	 * 
	 * @return
	 *     returns InterfaceWebCDRDB
	 */
	@WebEndpoint(name = "WebCDRDBPort")
	public InterfaceWebCDRDB getWebCDRDBPort()
	{
		return super.getPort(new QName("http://web.cdrdb.wind.gr/", "WebCDRDBPort"), InterfaceWebCDRDB.class);
	}

	/**
	 * 
	 * @param features
	 *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
	 * @return
	 *     returns InterfaceWebCDRDB
	 */
	@WebEndpoint(name = "WebCDRDBPort")
	public InterfaceWebCDRDB getWebCDRDBPort(WebServiceFeature... features)
	{
		return super.getPort(new QName("http://web.cdrdb.wind.gr/", "WebCDRDBPort"), InterfaceWebCDRDB.class, features);
	}

	private static URL __getWsdlLocation()
	{
		if (WEBCDRDBSERVICE_EXCEPTION != null)
		{
			throw WEBCDRDBSERVICE_EXCEPTION;
		}
		return WEBCDRDBSERVICE_WSDL_LOCATION;
	}

}
