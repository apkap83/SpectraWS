package gr.wind.spectra.web;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import gr.wind.spectra.web.InvalidInputException;

import gr.wind.spectra.business.DB_Connection;
import gr.wind.spectra.business.DB_Operations;
import gr.wind.spectra.business.Help_Func;
import gr.wind.spectra.model.Product;

@WebService //(endpointInterface = "gr.wind.spectra.web.WebSpectraInterface")
public class WebSpectra// implements WebSpectraInterface
{
	DB_Connection conObj;
	Connection conn;
	DB_Operations dbs;
		
	public WebSpectra()
	{
		conObj = new DB_Connection();
		conn = conObj.Connect();
		dbs = new DB_Operations(conn);
	}
	
	@WebMethod
	@WebResult(name="Result")
	public List<Product> getHierarchy(
			@WebParam(name="Type") String Type,
			@WebParam(name="Level1") String Level1,
			@WebParam(name="Level2") String Level2,
			@WebParam(name="Level3") String Level3,
			@WebParam(name="Level4") String Level4,
			@WebParam(name="Level5") String Level5,
			@WebParam(name="Level6") String Level6,
			@WebParam(name="Level7") String Level7,
			@WebParam(name="Level8") String Level8,
			@WebParam(name="Level9") String Level9,
			@WebParam(name="Level10") String Level10
			) throws gr.wind.spectra.web.InvalidInputException
	{
		List<String> rootElementsList = new ArrayList<String>();
		List<Product> prodElementsList = new ArrayList<>();
		// FTTX Hierarchy
		if (Type == null || Type.equals("") || Type.equals("?"))
		{
				rootElementsList = dbs.GetOneColumnUniqueResultSet("rootNetworkElementHierarchy", "ElementType", "1 = 1");
				Product pr = new Product("rootElements", rootElementsList);
				prodElementsList.add(pr);
		}
		else if (Type.equals("FTTX") && ( Level1 == null || Level1.equals("") || Level1.equals("?") ) )
		{
			rootElementsList = dbs.GetOneColumnUniqueResultSet("FTTX_NetworkElementHierarchy", "OltElementName", "1 = 1");
			Product pr = new Product("OltElementName", rootElementsList);
			prodElementsList.add(pr);
		}
		else if (Type.equals("FTTX") && ( Level2 == null || Level2.equals("") || Level2.equals("?") ) )
		{
			rootElementsList = dbs.GetOneColumnUniqueResultSet("FTTX_NetworkElementHierarchy", "GponSlotNo", "1 = 1");
			Product pr = new Product("GponSlotNo", rootElementsList);
			prodElementsList.add(pr);
		}
		else if (Type.equals("FTTX") && ( Level3 == null || Level3.equals("") || Level3.equals("?") ))
		{
			String predicates = Help_Func.AssignSimilarANDPredicates(new String[] {"OltElementName", "GponSlotNo"}, new String[] { Level1, Level2 });
			rootElementsList =  dbs.GetOneColumnUniqueResultSet("FTTX_NetworkElementHierarchy", "PonNo", predicates);
			Product pr = new Product("PonNo", rootElementsList);
			prodElementsList.add(pr);
		}
		else if (Type.equals("FTTX") && ( Level4 == null || Level4.equals("") || Level4.equals("?") ))
		{				
			String predicates = Help_Func.AssignSimilarANDPredicates(new String[] {"OltElementName", "GponSlotNo", "PonNo"}, new String[] { Level1, Level2, Level3 });
			rootElementsList =  dbs.GetOneColumnUniqueResultSet("FTTX_NetworkElementHierarchy", "OnuPortNo", predicates);
			Product pr = new Product("OnuPortNo", rootElementsList);
			prodElementsList.add(pr);
		}
		else if (Type.equals("FTTX") && ( Level5 == null || Level5.equals("") || Level5.equals("?") ))
		{
			String predicates = Help_Func.AssignSimilarANDPredicates(new String[] {"OltElementName", "GponSlotNo", "PonNo", "OnuPortNo"}, new String[] { Level1, Level2, Level3, Level4 });
			rootElementsList =  dbs.GetOneColumnUniqueResultSet("FTTX_NetworkElementHierarchy", "NgaElementName", predicates);
			Product pr = new Product("NgaElementName", rootElementsList);
			prodElementsList.add(pr);
		}
		else if (Type.equals("FTTX") && ( Level6 == null || Level6.equals("") || Level6.equals("?") ))
		{
			String predicates = Help_Func.AssignSimilarANDPredicates(new String[] {"OltElementName", "GponSlotNo", "PonNo", "OnuPortNo", "NgaElementName"}, new String[] { Level1, Level2, Level3, Level4, Level5 });
			rootElementsList =  dbs.GetOneColumnUniqueResultSet("FTTX_NetworkElementHierarchy", "NgaElementCardSlotNo", predicates);
			Product pr = new Product("NgaElementCardSlotNo", rootElementsList);
			prodElementsList.add(pr);
		}
		else if (Type.equals("FTTX") && ( Level6 != null || ! Level6.equals("") || ! Level6.equals("?") ))
		{
			throw new InvalidInputException("Invalid Input", "There is no Level 7 for FTTX type");
		}

		return prodElementsList;
	}
	

	public static void main(String args[]) throws SQLException
	{/*
		WebSpectraInterface ws = new WebSpectra();
		List<String> myList = new ArrayList<String>();
		
		myList = ws.getFTTXHierarchy("ATHOARTMBOLT01", null, null, null, null);
		
		for (String item : myList)
		{
			System.out.println(item);
		}
		*/
	}
}


