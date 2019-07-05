package gr.wind.spectra.web;

import java.util.List;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

//@WebService
//@SOAPBinding(style=Style.DOCUMENT)
public interface WebSpectraInterface {
	
	//@WebMethod
	public abstract List<String> getRootElements();
	
	/*
	@WebMethod
	List<String> getFTTXHierarchy(@WebParam(name="OltElementName") String OltElementName, 
			@WebParam(name="GponSlotNo") String GponSlotNo,
			@WebParam(name="PonNo") String PonNo,
			@WebParam(name="OnuPortNo") String OnuPortNo,
			@WebParam(name="NgaElementName") String NgaElementName
			);

	@WebMethod
	int Add(int a, int b);
	*/
}