package gr.wind.spectra.business;

import java.util.Date;
import java.sql.Timestamp;

public class Help_Func 
{
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
   			mystring += '?';
   			
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
	
	
	public static String ConCatHierarchy(String[] nodeNames, String[] nodeValues )
	{
		String UniqueCharSequence = "&";
		int numOfFields = nodeNames.length;
		String mystring = "";
		
		
		if (numOfFields == 0)
		{
			return "None";
		}
		
		for (int i=0; i<numOfFields; i++)
		{
			if (i < numOfFields -1)
			{
				mystring += nodeNames[i] + "=" + nodeValues[i] + UniqueCharSequence; 
			}
			else
			{
				mystring += nodeNames[i] + "=" + nodeValues[i];
			}
		}
		
		
		return mystring;
	}
	
}
