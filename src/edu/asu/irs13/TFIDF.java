package edu.asu.irs13;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

public class TFIDF {
	
    public static List init(String str)
	{
		List list = new ArrayList();
		IndexReader r;
	try {
		r = IndexReader.open(FSDirectory.open(new File("index")));
	
    double [] magnitude= new double [r.maxDoc()];
   
   HashMap<Integer,Double> doc= new HashMap<Integer,Double>();
	
	
	list =Search.Printrank(magnitude,doc,str);
          }
    catch (IOException e) 
        {
	
	e.printStackTrace();
        }
   return list;
	}
}


