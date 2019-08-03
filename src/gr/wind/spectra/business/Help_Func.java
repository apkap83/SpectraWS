package gr.wind.spectra.business;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gr.wind.spectra.web.InvalidInputException;

import java.awt.List;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.text.ParseException;
import java.text.SimpleDateFormat;


public class Help_Func 
{
	public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
	
	public static String now() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		return sdf.format(cal.getTime());
		}
	
	public static String ColumnsToInsertStatement(String[] columns)
	{
		int numOfFields = columns.length;
		
		String mystring = " (`";
		
		for (int i=0; i<numOfFields ; i ++)
   		{
   			mystring += columns[i] + "`";
   			
   			if (i < numOfFields - 1)
   			{
   				mystring += ", `";
   			}
   			else
   			{
   				mystring += ") VALUES"; 
   			}
   		}
		
		return mystring;
	}
	
	public static String ValuesToInsertStatement(String[] values)

	{
		int numOfFields = values.length;
		
		String mystring = " (";
		
		for (int i=0; i<numOfFields ; i ++)
   		{
   			mystring += "?";
   			
   			if (i < numOfFields - 1)
   			{
   				mystring += ", ";
   			}
   			else
   			{
   				mystring += ")"; 
   			}
   		}
		
		return mystring;
	}

	public static String GetTimeStamp()
	{
		Date date= new Date();
		long time = date.getTime();
		Timestamp ts = new Timestamp(time);
		
		String Output = ts.toString() + " ";
		return Output;
	}
	
	public static String AssignSimilarANDPredicates(String[] predicates, String[] values)
	{
		
		int numOfFields = predicates.length;
		String mystring = "";
		for (int i=0; i<numOfFields; i++)
		{
			// "OltElementName="+ OltElementName + "
			if (i < numOfFields -1)
			{
				mystring += predicates[i] + "='" + values[i] + "' AND "; 
			}
			else
			{
				mystring += predicates[i] + "='" + values[i] + "'";
			}
		}
		
		
		return mystring;
	}
	
	
	public static void ValidateDateTimeFormat(String dateInput) throws ParseException, InvalidInputException
	{
		try
		{
			Date date1=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateInput);
		} catch (ParseException e)
		{
			e.printStackTrace();
			throw new InvalidInputException("Invalid DateTime Input", "DateTime field is not in expected format \"yyyy-MM-dd HH:mm:ss\"");
		}
			
	}
	
	public static ArrayList<String> HierarchyStringToANDPredicates(String hierarchySelected)
	{
		// hierarchySelected = FTTX=1|OLTElementName=Tolis
		
		String SQLExpression = "";
		String technology;
		ArrayList<String> elementHierarchy = new ArrayList<String>();
		
		String[] parts = hierarchySelected.split("->");

		// Pick first element e.g FTTX		
		technology = parts[0].split("=")[0];
		elementHierarchy.add(technology);		
		int numOfParts = parts.length;

		System.out.println("Number of Parts: " + numOfParts);
		for (int i = 0; i < numOfParts ; i ++ )
		{
			if (i == 0 ) 
			{  
				continue; 
			}
			else
			{
				if (i < numOfParts -1)
				{
					String[] keyValuePair = parts[i].split("=");
					SQLExpression = SQLExpression + keyValuePair[0] + " = " + "'" + keyValuePair[1] + "'" + " AND ";
				}
				else
				{
					String[] keyValuePair = parts[i].split("=");
					SQLExpression = SQLExpression + keyValuePair[0] + " = " + "'" + keyValuePair[1] + "'";
				}
			}
			
		}
		
		elementHierarchy.add(SQLExpression);
		
		
		
		System.out.println("Part I: " + elementHierarchy.get(0) );
		System.out.println("Part II: " + elementHierarchy.get(1) );
		
		return elementHierarchy;
	}
	
	public static String columnsWithCommas(String[] columns)
	{
		int numOfColumns = columns.length;
		String myString = "";
		for (int i=0; i < numOfColumns; i++)
		{
			if (i < numOfColumns -1)
			{
				myString += columns[i] + ",";
			}
			else
			{
				myString += columns[i];
			}
		}
		return myString;
	}
	
	public static boolean HierarchyHasMultipleSelections(String Hierarchy)
	{
		String[] initialParts = Hierarchy.split("\\|");
		if (initialParts.length > 1)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	// FTTX=1->OLTElementName=ATHOKRDLOLT01->OltSlot=1|OltSlot=2&&OLT=1->OLTElementName=ATHOKRDLOLT01->OltSlot=3|OltSlot=4
	public static java.util.List<String> GetHierarchySelections(String Hierarchy)
	{
		
		java.util.List<String> myList = new ArrayList<String>();
		String[] initialParts = Hierarchy.split("%"); // FTTX=1->OLTElementName=ATHOKRDLOLT01->OltSlot=1|OltSlot=2  AND OLT=1->OLTElementName=ATHOKRDLOLT01->OltSlot=3|OltSlot=4
		if (initialParts.length > 1)
		{
			
			//System.out.println("Part I " +  initialParts[0]);
			//System.out.println("Part II " +  initialParts[1]);
			for (int i=0; i<initialParts.length;i++)
			{
				String UniqueHierarchy = "";
				String[] items = initialParts[i].split("->"); // FTTX=1 AND OLTElementName=ATHOKRDLOLT01 AND OltSlot=1|OltSlot=2
				
				//System.out.println("Part 1 " +  items[0]);
				//System.out.println("Part 2 " +  items[1]);
				//System.out.println("Part 3 " +  items[2]);
				
				int itemsNum = items.length;
				
				String[] multipleItems = items[itemsNum-1].split("\\|"); // OltSlot=1 AND OltSlot=2
				
				//System.out.println("Part 4 " +  multipleItems[0]);
				//System.out.println("Part 5 " +  multipleItems[1]);
				
				for (int j=0;j<items.length-1;j++)
				{
					UniqueHierarchy += items[j] + "->";
					//System.out.println("Part 6 " +  UniqueHierarchy);
				}
				
				for (int k=0;k<multipleItems.length;k++)
				{
					//System.out.println("Part 7 " +  UniqueHierarchy + multipleItems[k]);
					myList.add(UniqueHierarchy + multipleItems[k]);
				}
				
			}
		
		}
		else
		{
			String UniqueHierarchy = "";
			String[] items = Hierarchy.split("->"); // FTTX=1 AND OLTElementName=ATHOKRDLOLT01 AND OltSlot=1|OltSlot=2
			
			//System.out.println("Part 1 " +  items[0]);
			//System.out.println("Part 2 " +  items[1]);
			//System.out.println("Part 3 " +  items[2]);
			
			int itemsNum = items.length;
			
			String[] multipleItems = items[itemsNum-1].split("\\|"); // OltSlot=1 AND OltSlot=2
			
			//System.out.println("Part 4 " +  multipleItems[0]);
			//System.out.println("Part 5 " +  multipleItems[1]);
			
			for (int j=0;j<items.length-1;j++)
			{
				UniqueHierarchy += items[j] + "->";
				//System.out.println("Part 6 " +  UniqueHierarchy);
			}
			
			for (int k=0;k<multipleItems.length;k++)
			{
				//System.out.println("Part 7 " +  UniqueHierarchy + multipleItems[k]);
				myList.add(UniqueHierarchy + multipleItems[k]);
			}			
		}
		
		for (String item : myList)
		{
			System.out.println(item);
		}
		
		return myList;
	}
	
	public static String HierarchyToPredicate(String input)
	{
		String myPredicate ="";
		String[] initialParts = input.split("->");
		
		if (initialParts.length == 1)
		{
			return "1 = 1";
		}
		
		
		for (int i=1;i<initialParts.length;i++)
		{
			String[] secondaryParts = initialParts[i].split("=");
			
			if (i < initialParts.length-1)
			{
				myPredicate += secondaryParts[0] + " = '" + secondaryParts[1] + "' AND ";
			}
			else
			{
				myPredicate += secondaryParts[0] + " = '" + secondaryParts[1] + "'";
			}
		}
		return myPredicate;
	}
	
	public static String GetRootHierarchyNode(String input)
	{
		String node;
		String[] initialParts = input.split("->");
		
		// Only 1 elements without "->"
		if (initialParts.length == 1)
		{
			return input;
		}
		else
		{
			String[] secondaryParts = initialParts[0].split("=");
			return secondaryParts[0];
		}
	}
	
	public static ArrayList<String> SplitHierarchy(String Hierarchy)
	{
		
		
		return null;
	}
	
	public static String ConCatHierarchy(String[] nodeNames, String[] nodeValues, String[] hierarchyFullPathList )
	{
		String UniqueCharSequence = "->";
		int numOfFields = nodeNames.length;
		String mystring = "";
		
		
		if (numOfFields < 1)
		{
			return "None";
		}
		else if (numOfFields == 1)
		{
			return nodeNames[0] + UniqueCharSequence + hierarchyFullPathList[0] + "=";
		}
		else if (numOfFields > 1)
		{
			for (int i=0; i<numOfFields; i++)
			{
				if (i == 0)
				{
					mystring += nodeNames[i] + UniqueCharSequence;
				}
				else if (i < numOfFields -1)
				{
					mystring += nodeNames[i] + "=" + nodeValues[i] + UniqueCharSequence; 
				}
				else
				{
					if (i == hierarchyFullPathList.length)
					{
						mystring += nodeNames[i] + "=" + nodeValues[i];
					}
					else
					{
						mystring += nodeNames[i] + "=" + nodeValues[i] + UniqueCharSequence + hierarchyFullPathList[i] + "=";
					}
				}
			}
		}
		
		
		return mystring;
	}
	
	public static String ReplaceHierarchyForSubscribersAffected(String hierarchy, String[] subsHierarchy)
	{
		String[] hierarchyItems = hierarchy.split("->");
		String rootElement = "";
		String outputHierarchy = "";
		
		for(int i=0; i < hierarchyItems.length; i++)
		{
			if (i == 0) 
			{
				rootElement = hierarchyItems[0];
				continue;
			} //root element
			
			String[] keyValuePair = hierarchyItems[i].split("=");
			//System.out.println("A: " + keyValuePair[0]);
			//System.out.println("B: " + subsHierarchy[i-1]);
			if (keyValuePair[0].equals(subsHierarchy[i-1]))
			{
				if (i < hierarchyItems.length-1)
				{
					outputHierarchy += keyValuePair[0] + "=" + keyValuePair[1] + "->";
				}
				else
				{
					outputHierarchy += keyValuePair[0] + "=" + keyValuePair[1];
				}
			}
			else
			{
				if (i < hierarchyItems.length-1)
				{
					outputHierarchy += subsHierarchy[i-1] + "=" + keyValuePair[1] + "->";
				}
				else
				{
					outputHierarchy += subsHierarchy[i-1] + "=" + keyValuePair[1];
				}
			}
		}
		
		outputHierarchy = rootElement + "->" + outputHierarchy;
		
		//System.out.println("Output: " + outputHierarchy);
		return outputHierarchy;
	}

	
	public static void main(String[] args)
	{
		
		//System.out.println(Help_Func.ReplaceHierarchyForSubscribersAffected("FTTX->OltElementName=LAROAKDMOLT01->OltSlot=1->OltPort=0->Onu=0->ElementName=LAROAKDMOFLND010H11", new String[] {"OltElementName","OltSlot","OltPort","Onu","ActiveElement","Slot"}));
		
		
		//HierarchyStringToANDPredicates("FTTX=1->OLTElementName=ATHOKRDLOLT01->OltSlot=4");
		//System.out.println("Starting...");
		//GetHierarchySelections("FTTX=1->OLTElementName=ATHOKRDLOLT01->OltSlot=1|OltSlot=2%OLT=1->OLTElementName=ATHOKRDLOLT01->OltSlot=3|OltSlot=4");
		//FTTX=1->OLTElementName=ATHOKRDLOLT01->OltSlot=1
		//String out = HierarchyToPredicate("FTTX=1->OLTElementName=ATHOKRDLOLT01->OltSlot=1");
		//System.out.println(out);
		//System.out.println(Help_Func.HierarchyToPredicate("FTTX->OltElementName=ak->something=3"));

		/*
		ArrayList<String> nodeNamesArrayList = new ArrayList<String>();
		ArrayList<String> nodeValuesArrayList = new ArrayList<String>();
		
		nodeNamesArrayList.add("FTTX");
		nodeValuesArrayList.add("1");
				
		nodeNamesArrayList.add("OltElementName");
		nodeValuesArrayList.add("Somethin1");

		nodeNamesArrayList.add("OltSlot");
		nodeValuesArrayList.add("Something2");
		*/
		//FTTX->OltElementName=LAROAKDMOLT01->OltSlot=1->OltPort=0->Onu=0->ElementName=LAROAKDMOFLND010H11->Slot=4
		//String[] hierarchyFullPathList = {"OltElementName", "OltSlot", "OltPort", "Onu", "ElementName", "Slot"};
		//String[] nodeNamesArrayList = {"FTTX","OltElementName","OltSlot","OltPort","Onu","ElementName", "Slot"};
		//String[] nodeValuesArrayList = {"1","LAROAKDMOLT01","1","0","0","LAROAKDMOFLND010H11", "5"};
		//System.out.println(Help_Func.ConCatHierarchy(nodeNamesArrayList, nodeValuesArrayList, hierarchyFullPathList));
		
		
				
		
	}
	
}
