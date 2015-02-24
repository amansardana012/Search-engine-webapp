package edu.asu.irs13;
import static edu.asu.irs13.Search.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class snippetgen {
	private static IndexReader r;
	private static double[] magnitude;
	private static List result_url = new ArrayList();
	public static List generator(String input,List list)
	{
		List resultlist = new ArrayList();
		try
		{
	 r = IndexReader.open(FSDirectory.open(new File("/Users/aman/Documents/workspaceee/newweb/index")));
		//List list = new ArrayList();
		 int index=0;
		List finallist = new ArrayList();
		List filesentences = new ArrayList();
		
		ArrayList filelist = new ArrayList();
		   
		   for(int i =0; i<list.size()&&i<10 ; i++)
		   {
			   String d_url = r.document((int) list.get(i)).getFieldable("path").stringValue();
			   finallist.add(i, d_url);
			  
		   }
		   
		  for(int itr=0; itr<list.size()&& itr<10 ;itr++)
		   {
			  filelist.clear();
			  filesentences.clear();
			
			  Map word_count= new HashMap<String,Integer>();
			
			  File file = new File("/Users/aman/Downloads/irs13/Projectclass/result3/"+finallist.get(itr));
			  //System.out.println(file.toString());
			  Document doc = Jsoup.parse(file, "UTF-8");
			  String file_data = doc.body().text();
			int file_length=file_data.length()/40;
			  
			for(int i=0 ; i<file_length; i++)
			{
		filesentences.add(file_data.substring(i*40, (i*40)+40));
			index++;
			}
			  int itr_index=0;
			 String[] terms = input.split("\\s+");
			
			 for(String word : terms)
				{
				 for(int j=0 ; j<filesentences.size();j++)
			       {
					String[] data=((String) filesentences.get(j)).split("\\s+");
					int count=0;
					
				
					for(int k=0; k<data.length;k++)
					 {
				     
						if(data[k].matches(word))
					
						{
						
						word_count.put(filesentences.get(j),++count);
						}
					 }
			      }
			   }	
		      word_count=sortmap(word_count);
		      	filelist.add(word_count.keySet());
		      	
		      	result_url.add(finallist.get(itr));
		      	resultlist.add(filelist.get(0));

		   }
		} 

		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	return resultlist;
	}
	
	public static List getresulturl()
	{
		return result_url;
		
	}
	//sort the map
	public static <E>Map sortmap(Map doc) {
		LinkedList list = new LinkedList(doc.entrySet()); 
		Collections.sort(list, new Comparator<Map.Entry<String,Integer>>()
		{

			@Override
			public int compare(Entry<String, Integer> e1,
					Entry<String,Integer> e2) {
				
				return e2.getValue().compareTo(e1.getValue());

			} 
			

		});
		Map sortedMap = new LinkedHashMap(); 
		for (Iterator<E> it = list.iterator(); it.hasNext();) 
		{ 
			Map.Entry entry = (Map.Entry) it.next(); 
		sortedMap.put(entry.getKey(), entry.getValue());
		} 
		return sortedMap; 
	}
	

}
