package edu.asu.irs13;

import static edu.asu.irs13.Search.*;
import static edu.asu.irs13.Cluster.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;

public class Scalarcluster {
	private static IndexReader r;
	private static double[] magnitude;
	private static Map<Integer, HashMap<String, Integer>> map = new HashMap<Integer,HashMap<String,Integer>>();
	public static void coorelation(String input)
	{
		try
		{
	 r = IndexReader.open(FSDirectory.open(new File("/Users/aman/Documents/workspaceee/newweb/index")));
		List list = new ArrayList();
		map=getmap();
		List commonwords = new ArrayList<>();
		List listtopk= new ArrayList<>();
		 int index=0;int K=25;
		 ArrayList<Integer>Baselist = new ArrayList<Integer>();
		 ArrayList<String>Comterms = new ArrayList<String>();
		 magnitude= new double[r.maxDoc()];
		 magnitude=magnitude();
		   list=init(input, magnitude);
			if(list.size()>10)
			{
				for(int i=0; i<10;i++)
				{
					Baselist.add(i, (Integer) list.get(i));
				}
			}
			else
			{
				Baselist.addAll(list);
			}
			double [][] DT = new double[Baselist.size()][commonwords.size()];
			double [][] TD = new double[Baselist.size()][commonwords.size()];
			double [][] TT = new double[commonwords.size()][commonwords.size()];
			double [][] Temp = new double[commonwords.size()][commonwords.size()];
			int termindex;
			HashMap<String, Double> idf = new HashMap<String,Double>();
			//System.out.println(Baselist);
				idf=getidf();
				TermEnum t1 = r.terms();
				while(t1.next())
				{
				Term te = new Term("contents", t1.term().text());
				TermDocs td = r.termDocs(te);
				int count=r.docFreq(te);
				while(td.next())
				{
			    if(Baselist.contains(td.doc()))
			    { 
			    if(!commonwords.contains(t1.term().text()))
			    {
			    	commonwords.add(t1.term().text());
			    }
			    }
				}
			 
				}
				for(int i =0; i<Baselist.size();i++)
				{
					for(int j=0;j<commonwords.size(); j++)
					{ 
						if(map.get(i).get(commonwords.get(j))!=null)
						{
						DT[i][j]=map.get(i).get(commonwords.get(j))/idf.get(commonwords.get(j));
						}
					}
				}
				for(int i =0; i<Baselist.size();i++)
				{
					for(int j=0;j<commonwords.size(); j++)
					{
						TD[i][j]=DT[j][i];
					}
				}
				for(int i =0; i<Baselist.size();i++)
				{
					for(int j=0;j<commonwords.size(); j++)
					{
						TT[i][j]=0;
						for(int k=0; k<commonwords.size(); k++)
						{
							TT[i][j]+= TD[i][k]*DT[k][j];
						}
					}
				}
				for(int i =0; i<Baselist.size();i++)
				{
					for(int j=0;j<commonwords.size(); j++)
					{
						Temp[i][j]= TT[i][j]/(TT[i][i]+TT[j][j]+TT[i][j]);
					}
		
				}
	 double[]termposition = new double[commonwords.size()];
	 double[]queryposition = new double[commonwords.size()];
	 HashMap<String, Double> resultmap = new HashMap<String,Double>();
	    String[] terms = input.split("\\s+");
		for(String word : terms)
		{	
			int index_term=0;
			termindex=commonwords.indexOf(word);
			for(int j=0;j<commonwords.size();j++)
			{
				queryposition= TT[termindex];
				termposition= TT[j];
				double value=multiply_sim(termposition, queryposition);
			   resultmap.put((String) commonwords.get(j), value); 		
			}
		}
		
		resultmap=(HashMap<String, Double>) sortmap(resultmap);
		Iterator it = resultmap.keySet().iterator();
		int index_map=0;
		while(it.hasNext()&&index_map<5)
		{
			System.out.println(it.next());
			index_map++;
		}
		}
	catch(Exception ex)
	{
		ex.printStackTrace();
	}
}
	
	public static void main (String[] args) {
	coorelation("fall semester");
	}

}

