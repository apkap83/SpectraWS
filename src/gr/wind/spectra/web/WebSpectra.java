package gr.wind.spectra.web;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import gr.wind.spectra.web.InvalidInputException;

import gr.wind.spectra.business.DB_Connection;
import gr.wind.spectra.business.DB_Operations;
import gr.wind.spectra.business.Help_Func;
import gr.wind.spectra.business.ServiceImplementation;

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
	public List<String> getRootElements()
	{
		List<String> rootElementsList = new ArrayList<String>();
    	try {
			rootElementsList = dbs.GetOneColumnResultSet("rootNetworkElementHierarchy", "ElementType", "1 = 1");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rootElementsList;
	}
	
	@WebMethod
	public List<String> getHierarchy(
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
		
		// FTTX Hierarchy
		if (Type == null || Type.equals("") || Type.equals("?"))
		{
			rootElementsList = dbs.GetOneColumnUniqueResultSet("rootNetworkElementHierarchy", "ElementType", "1 = 1");
		}
		else if (Type.equals("FTTX") && ( Level1 == null || Level1.equals("") || Level1.equals("?") ) )
		{
			rootElementsList = dbs.GetOneColumnUniqueResultSet("FTTX_NetworkElementHierarchy", "OltElementName", "1 = 1");
		}
		else if (Type.equals("FTTX") && ( Level2 == null || Level2.equals("") || Level2.equals("?") ) )
		{
			String predicates = Help_Func.AssignSimilarANDPredicates(new String[] {"OltElementName"}, new String[] { Level1 });
			rootElementsList = dbs.GetOneColumnUniqueResultSet("FTTX_NetworkElementHierarchy", "GponSlotNo", "1 = 1");
		}
		else if (Type.equals("FTTX") && ( Level3 == null || Level3.equals("") || Level3.equals("?") ))
		{
			String predicates = Help_Func.AssignSimilarANDPredicates(new String[] {"OltElementName", "GponSlotNo"}, new String[] { Level1, Level2 });
			rootElementsList =  dbs.GetOneColumnUniqueResultSet("FTTX_NetworkElementHierarchy", "PonNo", predicates);	
		}
		else if (Type.equals("FTTX") && ( Level4 == null || Level4.equals("") || Level4.equals("?") ))
		{
			String predicates = Help_Func.AssignSimilarANDPredicates(new String[] {"OltElementName", "GponSlotNo", "PonNo"}, new String[] { Level1, Level2, Level3 });
			rootElementsList =  dbs.GetOneColumnUniqueResultSet("FTTX_NetworkElementHierarchy", "OnuPortNo", predicates);
		}
		else if (Type.equals("FTTX") && ( Level5 == null || Level5.equals("") || Level5.equals("?") ))
		{
			String predicates = Help_Func.AssignSimilarANDPredicates(new String[] {"OltElementName", "GponSlotNo", "PonNo", "OnuPortNo"}, new String[] { Level1, Level2, Level3, Level4 });
			rootElementsList =  dbs.GetOneColumnUniqueResultSet("FTTX_NetworkElementHierarchy", "NgaElementName", predicates);
		}
		else if (Type.equals("FTTX") && ( Level6 == null || Level6.equals("") || Level6.equals("?") ))
		{
			String predicates = Help_Func.AssignSimilarANDPredicates(new String[] {"OltElementName", "GponSlotNo", "PonNo", "OnuPortNo", "NgaElementName"}, new String[] { Level1, Level2, Level3, Level4, Level5 });
			rootElementsList =  dbs.GetOneColumnUniqueResultSet("FTTX_NetworkElementHierarchy", "NgaElementCardSlotNo", predicates);
		}
		else if (Type.equals("FTTX") && ( Level6 != null || ! Level6.equals("") || ! Level6.equals("?") ))
		{
			throw new InvalidInputException("Invalid Input", "There is no Level 7 for FTTX type");
		}
		// FTTX Hierarchy
		
		
		
		
		return rootElementsList;
	}
	
	
	/*
	public List<String> getFTTXHierarchy(String OltElementName, 
			String GponSlotNo, 
			String PonNo, 
			String OnuPortNo, 
			String NgaElementName)
	{
		List<String> rootElementsList = new ArrayList<String>();
		
		if (OltElementName == null || OltElementName.equals("") || OltElementName.equals("?"))
		{
			rootElementsList = dbs.GetOneColumnUniqueResultSet("FTTX_NetworkElementHierarchy", "OltElementName", "1 = 1");
		}
		else if (GponSlotNo == null || GponSlotNo.equals("") ||GponSlotNo.equals("?"))
		{
			String predicates = Help_Func.AssignSimilarANDPredicates(new String[] {"OltElementName"}, new String[] { OltElementName });
			rootElementsList =  dbs.GetOneColumnUniqueResultSet("FTTX_NetworkElementHierarchy", "GponSlotNo", predicates);
		}
		else if (PonNo == null || PonNo.equals("") || PonNo.equals("?"))
		{
			String predicates = Help_Func.AssignSimilarANDPredicates(new String[] {"OltElementName", "GponSlotNo"}, new String[] { OltElementName, GponSlotNo });
			rootElementsList =  dbs.GetOneColumnUniqueResultSet("FTTX_NetworkElementHierarchy", "PonNo", predicates);			
		}
		else if (OnuPortNo == null || OnuPortNo.equals("") || OnuPortNo.equals("?"))
		{
			String predicates = Help_Func.AssignSimilarANDPredicates(new String[] {"OltElementName", "GponSlotNo", "PonNo"}, new String[] { OltElementName, GponSlotNo, PonNo });
			rootElementsList =  dbs.GetOneColumnUniqueResultSet("FTTX_NetworkElementHierarchy", "OnuPortNo", predicates);	
		}
		else if (NgaElementName == null || NgaElementName.equals("") || NgaElementName.equals("?"))
		{
			String predicates = Help_Func.AssignSimilarANDPredicates(new String[] {"OltElementName", "GponSlotNo", "PonNo", "OnuPortNo"}, new String[] { OltElementName, GponSlotNo, PonNo, OnuPortNo });
			rootElementsList =  dbs.GetOneColumnUniqueResultSet("FTTX_NetworkElementHierarchy", "NgaElementName", predicates);			
		}
		else
		{
			String predicates = Help_Func.AssignSimilarANDPredicates(new String[] {"OltElementName", "GponSlotNo", "PonNo", "OnuPortNo", "NgaElementName"}, new String[] { OltElementName, GponSlotNo, PonNo, OnuPortNo, NgaElementName });
			rootElementsList =  dbs.GetOneColumnUniqueResultSet("FTTX_NetworkElementHierarchy", "NgaElementCardSlotNo", predicates);	
		}
		return rootElementsList;
	}
	
		
	public int Add(int a, int b) 
	{
		return a+b;
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
		
	}
	*/
}


