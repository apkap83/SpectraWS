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
		try {
			conn = conObj.Connect();
		} catch (InvalidInputException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
			rootElementsList = dbs.GetOneColumnUniqueResultSet("FTTX_NetworkElementHierarchy", "OltSlot", "1 = 1");
			Product pr = new Product("OltSlot", rootElementsList);
			prodElementsList.add(pr);
		}
		else if (Type.equals("FTTX") && ( Level3 == null || Level3.equals("") || Level3.equals("?") ))
		{
			String predicates = Help_Func.AssignSimilarANDPredicates(new String[] {"OltElementName", "OltSlot"}, new String[] { Level1, Level2 });
			rootElementsList =  dbs.GetOneColumnUniqueResultSet("FTTX_NetworkElementHierarchy", "OltPort", predicates);
			Product pr = new Product("OltPort", rootElementsList);
			prodElementsList.add(pr);
		}
		else if (Type.equals("FTTX") && ( Level4 == null || Level4.equals("") || Level4.equals("?") ))
		{				
			String predicates = Help_Func.AssignSimilarANDPredicates(new String[] {"OltElementName", "OltSlot", "OltPort"}, new String[] { Level1, Level2, Level3 });
			rootElementsList =  dbs.GetOneColumnUniqueResultSet("FTTX_NetworkElementHierarchy", "Onu", predicates);
			Product pr = new Product("Onu", rootElementsList);
			prodElementsList.add(pr);
		}
		else if (Type.equals("FTTX") && ( Level5 == null || Level5.equals("") || Level5.equals("?") ))
		{
			String predicates = Help_Func.AssignSimilarANDPredicates(new String[] {"OltElementName", "OltSlot", "OltPort", "Onu"}, new String[] { Level1, Level2, Level3, Level4 });
			rootElementsList =  dbs.GetOneColumnUniqueResultSet("FTTX_NetworkElementHierarchy", "ElementName", predicates);
			Product pr = new Product("ElementName", rootElementsList);
			prodElementsList.add(pr);
		}
		else if (Type.equals("FTTX") && ( Level6 == null || Level6.equals("") || Level6.equals("?") ))
		{
			String predicates = Help_Func.AssignSimilarANDPredicates(new String[] {"OltElementName", "OltSlot", "OltPort", "Onu", "ElementName"}, new String[] { Level1, Level2, Level3, Level4, Level5 });
			rootElementsList =  dbs.GetOneColumnUniqueResultSet("FTTX_NetworkElementHierarchy", "Slot", predicates);
			Product pr = new Product("Slot", rootElementsList);
			prodElementsList.add(pr);
		}
		else if (Type.equals("FTTX") && ( Level6 != null || ! Level6.equals("") || ! Level6.equals("?") ))
		{
			throw new InvalidInputException("Invalid Input", "There is no Level 7 for FTTX type");
		}
		else if (Type.equals("LLU") && ( Level1 == null || Level1.equals("") || Level1.equals("?") ))
		{
			rootElementsList = dbs.GetOneColumnUniqueResultSet("LLU_All_NetworkElementHierarchy", "ElementName", "1 = 1");
			Product pr = new Product("ElementName", rootElementsList);
			prodElementsList.add(pr);
		}
		else if (Type.equals("LLU") && ( Level2 == null || Level2.equals("") || Level2.equals("?") ))
		{
			String predicates = Help_Func.AssignSimilarANDPredicates(new String[] {"ElementName"}, new String[] { Level1 });
			rootElementsList =  dbs.GetOneColumnUniqueResultSet("LLU_All_NetworkElementHierarchy", "Subrack", predicates);
			Product pr = new Product("Subrack", rootElementsList);
			prodElementsList.add(pr);
		}
		else if (Type.equals("LLU") && ( Level3 == null || Level3.equals("") || Level3.equals("?") ))
		{
			String predicates = Help_Func.AssignSimilarANDPredicates(new String[] {"ElementName", "Subrack"}, new String[] { Level1, Level2 });
			rootElementsList =  dbs.GetOneColumnUniqueResultSet("LLU_All_NetworkElementHierarchy", "Slot", predicates);
			Product pr = new Product("Slot", rootElementsList);
			prodElementsList.add(pr);
		}
		else if (Type.equals("LLU") && ( Level4 != null || ! Level4.equals("") || ! Level4.equals("?") ))
		{
			throw new InvalidInputException("Invalid Input", "There is no Level 4 for LLU type");
		}
		else if (Type.equals("OLT") && ( Level1 == null || Level1.equals("") || Level1.equals("?") ))
		{
			rootElementsList = dbs.GetOneColumnUniqueResultSet("FTTH_NetworkElementHierarchy", "OltElementName", "1 = 1");
			Product pr = new Product("OltElementName", rootElementsList);
			prodElementsList.add(pr);
		}
		else if (Type.equals("OLT") && ( Level2 == null || Level2.equals("") || Level2.equals("?") ))
		{
			String predicates = Help_Func.AssignSimilarANDPredicates(new String[] {"OltElementName"}, new String[] { Level1 });
			rootElementsList =  dbs.GetOneColumnUniqueResultSet("FTTH_NetworkElementHierarchy", "OltSlot", predicates);
			Product pr = new Product("OltSlot", rootElementsList);
			prodElementsList.add(pr);			
		}
		else if (Type.equals("OLT") && ( Level3 == null || Level3.equals("") || Level3.equals("?") ))
		{
			String predicates = Help_Func.AssignSimilarANDPredicates(new String[] {"OltElementName", "OltSlot"}, new String[] { Level1, Level2 });
			rootElementsList =  dbs.GetOneColumnUniqueResultSet("FTTH_NetworkElementHierarchy", "OltPon", predicates);
			Product pr = new Product("OltPon", rootElementsList);
			prodElementsList.add(pr);	
		}
		else if (Type.equals("OLT") && ( Level4 == null || Level4.equals("") || Level4.equals("?") ))
		{
			String predicates = Help_Func.AssignSimilarANDPredicates(new String[] {"OltElementName", "OltSlot", "OltPon"}, new String[] { Level1, Level2, Level3 });
			rootElementsList =  dbs.GetOneColumnUniqueResultSet("FTTH_NetworkElementHierarchy", "OnuID", predicates);
			Product pr = new Product("OnuID", rootElementsList);
			prodElementsList.add(pr);	
		}
		else if (Type.equals("OLT") && ( Level5 == null || Level5.equals("") || Level5.equals("?") ))
		{
			String predicates = Help_Func.AssignSimilarANDPredicates(new String[] {"OltElementName", "OltSlot", "OltPon", "OnuID"}, new String[] { Level1, Level2, Level3, Level4 });
			rootElementsList =  dbs.GetOneColumnUniqueResultSet("FTTH_NetworkElementHierarchy", "BepPortNo", predicates);
			Product pr = new Product("BepPortNo", rootElementsList);
			prodElementsList.add(pr);	
		}
		else if (Type.equals("OLT") && ( Level6 != null || ! Level6.equals("") || ! Level6.equals("?") ))
		{
			throw new InvalidInputException("Invalid Input", "There is no Level 6 for OLT type");
		}
		else if (Type.equals("BRAS") && ( Level2 == null || Level2.equals("") || Level2.equals("?") ))
		{
			rootElementsList = dbs.GetOneColumnUniqueResultSet("IPBB_NetworkElementHierarchy", "Brasname", "1 = 1");
			Product pr = new Product("Brasname", rootElementsList);
			prodElementsList.add(pr);
		}
		else if (Type.equals("BRAS") && ( Level1 != null || ! Level1.equals("") || ! Level1.equals("?") ))
		{
			throw new InvalidInputException("Invalid Input", "There is no Level 2 for BRAS type");
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


