package edu.asu.irs13;
import static edu.asu.irs13.Cluster.getmap;
import static edu.asu.irs13.Search.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;
public class Cluster {
	private static IndexReader r;
	private static double[] magnitude;
	private static ArrayList<Integer> Baselist1= new ArrayList<Integer>();
	private static Map<Integer, HashMap<String, Double>> map = new HashMap<Integer,HashMap<String,Double>>();
	private static HashMap<String, Double> hm = new HashMap<String,Double>();
	private static List list= new ArrayList();
	private static List list_topk= new ArrayList();
	
public static void clustering(String input)
{
	int K=50;
	Map<Integer, Double>[] Cluster_checknew = new HashMap[10];
	
	try {
		 r = IndexReader.open(FSDirectory.open(new File("/Users/aman/Documents/workspaceee/newweb/index")));
		 magnitude=Search.magnitude();
		 list=Search.init(input, magnitude);
		if(list.size()>K)
		{
			for(int i=0; i<50;i++)
			{
				Baselist1.add(i, (Integer) list.get(i));
			}
		}
		else
		{
			Baselist1.addAll(list);
		}
		//System.out.println(Baselist);
		 TermEnum t1 = r.terms();
		
		int index=0;
	
		while(t1.next())
		{
			//System.out.println(list_terms.get(terms));
			Term te = new Term("contents", t1.term().text());
			TermDocs td = r.termDocs(te);
			int count=r.docFreq(te);
			//System.out.println((r.maxDoc()/count);
			while(td.next())
			{
            if(Baselist1.contains(td.doc()))
            { 
            	Double idf=0.0;
            	if(map.containsKey(Baselist1.indexOf(td.doc())))
            	{
            	 idf = td.freq()*Math.log((double)r.maxDoc()/count);
            	hm= map.get(Baselist1.indexOf(td.doc()));
            	hm.put(t1.term().text(),idf); 
            	map.put((Integer)Baselist1.indexOf(td.doc()),hm);
            	hm= new HashMap<>();
            	 if(!list_topk.contains(t1.term().text()))
            	 {
            	 list_topk.add(t1.term().text());
            	 }
            	}
            	else
            	{
            		 idf = td.freq()*Math.log((double)r.maxDoc()/count);
            		hm.put(t1.term().text(),idf); 
                	map.put((Integer)Baselist1.indexOf(td.doc()),hm);
                	hm= new HashMap<>();
                	if(!list_topk.contains(t1.term().text()))
                	{
                	list_topk.add(t1.term().text());
                	}
            	}
           
            	}
            }
			}
		Cluster_checknew=Kmeansiteration(3);
}	 
	catch (Exception ex) {
		ex.printStackTrace();
	}

}

public static Map getmap()
{
	return map;
}

public static Map<Integer, Double>[] Kmeansiteration(int no_clusters)
{
	int cluster_no=0;
	ArrayList<Integer>[] Cluster= new ArrayList[no_clusters];
	ArrayList<String>[] Centroid_key= new ArrayList[no_clusters];
	Map<Integer, Double>[] Cluster_check = new HashMap[no_clusters];
	ArrayList<Integer>[] Cluster_prev = new ArrayList[no_clusters];
	for (int i = 0; i < no_clusters; i++)
	{
		Cluster[i] = new ArrayList<>();
		Cluster_prev[i]= new ArrayList<>();
	Cluster_check[i]= new HashMap<>();
	}
	Random rand = new Random();
	int iteration = 0;
	Integer [] centroid_1= new Integer[no_clusters];
	for(int i=0; i<no_clusters;i++)
	{
	centroid_1[i]=rand.nextInt(Baselist1.size());
	}
	Map<String,Double>[] re_centroid= new HashMap[no_clusters];
	for (int i = 0; i < no_clusters; i++)
	{
		re_centroid[i] = new HashMap();
	}
	
	for(int i=0; i<no_clusters;i++)
	{
		re_centroid[i].clear();
	}	
	double [] norm_C1= new double[no_clusters];
	int index=0;
	for(int i =0 ; i<no_clusters;i++)
	{
	  norm_C1[i]=magnitude[(int) Baselist1.get(centroid_1[i])];
	}

	double [] C1_similar=new double[no_clusters];

	for(int document_no : Baselist1)
     {
		for(int j =0; j<list_topk.size();j++)
    	 {
    		for(int i=0 ; i<no_clusters;i++)
    		{
			if((map.get(centroid_1[i]).get(list_topk.get(j)))!=null&&(map.get((int)Baselist1.indexOf(document_no)).get(list_topk.get(j))!=null))
    		 {
    	     C1_similar[i]=C1_similar[i]+ (map.get((int)Baselist1.indexOf(document_no)).get(list_topk.get(j)) * (map.get(centroid_1[i]).get(list_topk.get(j))));
    		 }
    		}
    	}
    	// System.out.println(C1_similar+"   "+C2_similar+"  "+C3_similar);
		double max=0.0;
     	for(int i =0; i<no_clusters;i++)
     	{  
     		
    	 	C1_similar[i]= C1_similar[i]/(norm_C1[i]*magnitude[document_no]);
     	  if(max<C1_similar[i])
     	  {
     		  max=C1_similar[i];
     	     cluster_no= i;
     	  }
     	}
     	Cluster[cluster_no].add(document_no);
     	Cluster_check[cluster_no].put(document_no, max);
     	Arrays.fill(C1_similar, 0.0);
     }
	/*for(int i=0; i<no_clusters;i++)
    {
    System.out.println(Cluster[i]);
    System.out.println("*****************");
    }*/

	int count=0;Boolean flag=true;
while(flag)
{
	 flag=false;
	 
	//double []new_idf= new double[no_clusters];
	double []norm_C1_new=new double[no_clusters];
	
	for(int i=0; i<no_clusters;i++)
    {
	for(int cluster_doc=0; cluster_doc< Cluster[i].size();cluster_doc++)
	{
	    for(int i_j =0; i_j<list_topk.size();i_j++)
	   	 {
			if((map.get(cluster_doc).get(list_topk.get(i_j)))!=null)
			{
				if(re_centroid[i].containsKey((String) list_topk.get(i_j)))
				{
					
					re_centroid[i].put((String) list_topk.get(i_j),re_centroid[i].get((String) list_topk.get(i_j))+(map.get(cluster_doc).get(list_topk.get(i_j))/Cluster[i].size()));
				}
				else
				{
					re_centroid[i].put((String) list_topk.get(i_j),(map.get(cluster_doc).get(list_topk.get(i_j))/Cluster[i].size()));
				}
			}
	    }	
	 }	
 }

for(int i =0; i<no_clusters;i++)
{
Iterator it = re_centroid[i].keySet().iterator();
while(it.hasNext())
{
	String data= (String) it.next();
	norm_C1_new[i] = norm_C1_new[i]+(re_centroid[i].get(data)*re_centroid[i].get(data));
}
norm_C1_new[i]= Math.sqrt(norm_C1_new[i]);
}
for(int i =0 ; i<no_clusters; i++)
{
 Cluster[i].clear();
	Cluster_check[i].clear();
}
	flag= true;

	for(int document_no : Baselist1)
     {
		for(int j =0; j<list_topk.size();j++)
    	 {
    		for(int i=0 ; i<no_clusters;i++)
    		{
			if((re_centroid[i].get(list_topk.get(j)))!=null&&(map.get((int)Baselist1.indexOf(document_no)).get(list_topk.get(j))!=null))
    		 {
    	     C1_similar[i]=C1_similar[i]+ (map.get((int)Baselist1.indexOf(document_no)).get(list_topk.get(j)) * ((re_centroid[i]).get(list_topk.get(j))));
    		 }
    		}
    	 }
		double maximum=0;
		for(int i =0; i<no_clusters;i++)
     	{  
     		
    	 	C1_similar[i]= C1_similar[i]/(norm_C1_new[i]*magnitude[document_no]);
     	  if(maximum<C1_similar[i])
     	  {
     		  maximum=C1_similar[i];
     	     cluster_no= i;
     	  }
     	}
		Cluster[cluster_no].add(document_no);
		Cluster_check[cluster_no].put(document_no, maximum);
     	Arrays.fill(C1_similar, 0.0);
     }
	    flag=true;	
    for(int i =0; i<no_clusters; i++)
    {
    		if(Cluster_prev[i].containsAll(Cluster[i]))
    		{
    		count = count +1;
    		}
    if(count==(no_clusters-1))
    {
    	flag=false;
    }
    }
    
	for(int i=0; i<no_clusters;i++)
	{
	Cluster_prev[i].clear();
	Cluster_prev[i].addAll(Cluster[i]);
	}
		
}
for(int i=0; i<no_clusters;i++)
{
	Cluster_check[i]=sortmap(Cluster_check[i]);
	System.out.println(Cluster_check[i]);
	System.out.println("****************");
}	
return Cluster_check;
}	 

public static List coorelation(String input)
{
	String [] block={"!--","DOCTYPE","a","i","nbsp","p7defmark","width","abbr","acronym","http","height","address","applet","area","article","aside","audio","b","base","basefont","bdi","bdo","big","blockquote","body","br","button","canvas","caption","center","cite","code","col","colgroup","datalist","dd","del","details","dfn","dialog","dir","div","dl","dt","em","embed","fieldset","figcaption","figure","font","footer","form","frame","frameset","head","header","h1","h6","hr","html","i","iframe","img","input","ins","kbd","keygen","label","legend","li","link","main","map","mark","menu","menuitem","meta","meter","nav","noframes","noscript","object","ol","optgroup","option","output","p","param","pre","progress","q","rp","rt","ruby","s","samp","script","section","select","small","source","span","strike","strong","style","sub","summary","sup","table","tbody","td","textarea","tfoot","th","thead","time","title","tr","track","tt","u","ul","var","video","wbr"};
	List finalscale = new ArrayList<>();
	try
	{
 r = IndexReader.open(FSDirectory.open(new File("/Users/aman/Documents/workspaceee/newweb/index")));
	List list = new ArrayList();
	//map=getmap();
	magnitude=Search.magnitude();
	list=Search.init(input, magnitude);
	List commonwords = new ArrayList<>();
	List listtopk= new ArrayList<>();
	 int index=0;int K=25;
	 ArrayList<Integer>Baselist = new ArrayList<Integer>();
	 ArrayList<String>Comterms = new ArrayList<String>();
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
		
		int termindex;
		HashMap<String, Double> idf = new HashMap<String,Double>();
		//System.out.println(Baselist);
			idf=getidf();
			TermEnum t2 = r.terms();
			while(t2.next())
			{
			Term te = new Term("contents", t2.term().text());
			TermDocs td = r.termDocs(te);
			int count=r.docFreq(te);
			while(td.next())
			{
		    if(Baselist.contains(td.doc()))
		    { 
		    if(!commonwords.contains(t2.term().text()))
		    {
		    	commonwords.add(t2.term().text());
		    }
		    }
			}
			
			}
			double [][] DT = new double[commonwords.size()][commonwords.size()];
			double [][] TD = new double[commonwords.size()][commonwords.size()];
			double [][] TT = new double[commonwords.size()][commonwords.size()];
			double [][] Temp = new double[commonwords.size()][commonwords.size()];
			TermEnum t3 = r.terms();
			while(t3.next())
			{
			Term te1 = new Term("contents", t3.term().text());
			TermDocs td1 = r.termDocs(te1);
			int count=r.docFreq(te1);
			while(td1.next())
			{
		    if(Baselist.contains(td1.doc()))
		    { 
		    if(commonwords.contains(t3.term().text()))
		    {
		    	int index_term = commonwords.indexOf(t3.term().text());
		    	int index_doc = Baselist.indexOf(td1.doc());
		    	//System.out.println(index_term+"  "+index_doc);
		    	DT[index_doc][index_term]=td1.freq();
		    }
		    }
			}
		 
			}
			for(int i =0; i<DT.length;i++)
			{
				for(int j=0;j<DT[0].length; j++)
				{
					TD[i][j]=DT[j][i];
				}
			}
			for(int i =0; i<commonwords.size();i++)
			{
				for(int j=0;j<commonwords.size(); j++)
				{ int sum=0;
					for(int k=0; k<Baselist.size(); k++)
					{
						sum+= TD[i][k]*DT[k][j];
					}
				TT[i][j]= sum;
				}
			}
			for(int i =0; i<commonwords.size();i++)
			{
				for(int j=0;j<commonwords.size(); j++)
				{
					Temp[i][j]= (TT[i][j]/(TT[i][i]+TT[j][j]-TT[i][j]));
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
			queryposition= Temp[termindex];
			termposition= Temp[j];
			double value=multiply_sim(termposition, queryposition);
		    
		   resultmap.put((String) commonwords.get(j), value); 		
		}
	}
	resultmap=(HashMap<String, Double>) sortmap(resultmap);
	Iterator it = resultmap.keySet().iterator();
	int index_map=0;
	System.out.println("the related possible searches are");
	while(it.hasNext())
	{
		
		if(index_map<5)
		{
			String data= (String) it.next();
		 if(!(Arrays.asList(block).contains(data))&&(idf.get(data)>1.0)&&data.matches("[A-Za-z]+"))
		 {
		System.out.println(data);
		index_map++;
		  }
		}
    }
	 
}
catch(Exception ex)
{
	ex.printStackTrace();
}
return finalscale;
}


public static void main(String []args)
{
	System.out.println("enter the query to get query elaboration and clustering results");
	Scanner sc = new Scanner(System.in);
	String str = "";
	str=sc.nextLine();
	//long start=System.currentTimeMillis();
     clustering(str);
     System.out.println("enter the query to get query elaboration");
     Scanner sc1 = new Scanner(System.in);
 	String str1 = "";
 	str=sc.nextLine();
	System.out.println("***************************");
	//long end=System.currentTimeMillis();
	//System.out.println(end-start);
	coorelation(str);
}
public static double multiply_sim(double[] termposition, double[] queryposition)
{
	double similar=0.0;double mag=0.0;double mag2=0.0;
	for(int i =0; i<termposition.length;i++)
	{
	 similar += termposition[i]*queryposition[i];
	}
	for(int i =0; i<termposition.length;i++)
	{
	 mag+= termposition[i]*termposition[i];
	 mag2+= queryposition[i]*queryposition[i];
	}
mag= Math.sqrt(mag); mag2= Math.sqrt(mag2);
similar= (similar/(mag*mag2));
return similar;
}
}
