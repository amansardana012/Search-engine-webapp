package edu.asu.irs13;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;

import org.apache.lucene.index.*;
import org.apache.lucene.store.FSDirectory;

import java.awt.*;

    
public class basic  {
	

	public static void main(String[] args) throws Exception {
    try{
				
		IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
			
				double [] magnitude= new double [r.maxDoc()];
				HashMap<String, Double> idf= new HashMap<String,Double>();
			magnitude(r, magnitude,idf);
			
			HashMap<Integer, Double> doc= new HashMap<Integer,Double>();
			Printrank(r, magnitude,doc,idf);
			
}			
catch (Exception e)
{
System.out.println(e.toString());
	System.out.println("Invalid input");
	System.out.println("Search Terminated");
	System.exit(0);
}
	}
	
	// printing relevant docs and finding the baseset 
	public static void Printrank(IndexReader r, double[] magnitude,
			HashMap<Integer,Double> doc,HashMap<String, Double> idf) throws IOException, CorruptIndexException  {
		double Similarity=0.0;
		int count=0, set=0;
		int K=10;
		
		Map sorted= new HashMap<Integer,Double>();
		LinkAnalysis.numDocs= r.maxDoc();
		LinkAnalysis Auth = new LinkAnalysis();
		ArrayList<Integer> baseset= new ArrayList<Integer>();
		List finalpagerank= new ArrayList();
		Scanner sc = new Scanner(System.in);
		
		String str = "";
		
		System.out.println("Enter the query to search");
	
		while(!(str = sc.nextLine()).equals("quit"))
		{
			String[] terms = str.split("\\s+");
			
			System.out.println("Searching "+ str );
			for(String word : terms)
			{
				double q = Math.sqrt(terms.length);
			Term term = new Term("contents", word);
				TermDocs tdocs1 = r.termDocs(term);
				
				while(tdocs1.next() )
				{	 
				 Similarity = (tdocs1.freq()*idf.get(word) / (magnitude[tdocs1.doc()]*q));
			// d_url = "Document id is " + tdocs1.doc() +" url -->" +" "+ r.document(tdocs1.doc()).getFieldable("path").stringValue().replace("%%", "/")+"   "+ "Similarity value is ";
				
			int d_url = tdocs1.doc();
				 if(doc.containsKey(d_url))
					{
						double intermediate = doc.get(d_url);
						Similarity= intermediate + Similarity;
					}
				doc.put(d_url, Similarity);
				
					}
					
				}
					 
			
			if(doc.isEmpty())
			{
				System.out.println("NO results found");
				
			}
		sorted=sortmap(doc);
		
		if(sorted.isEmpty() == false)
		{
			System.out.println("Total results found : "+ sorted.size());
			if (sorted.size()>10)
				System.out.println("Showing Top 10 results");
			else 
				System.out.println("Showing " +sorted.size()+ " result(s)");
			print(sorted);
			double start=System.currentTimeMillis();
			Iterator it = sorted.keySet().iterator();
			while (it.hasNext()&& count<K)
			{
				int key=(int)it.next();
			    
				int [] links=Auth.getLinks(key);
		        int [] citations= Auth.getCitations(key);
		       for (Integer pb:links)
		       {
		    	   if (!baseset.contains(pb))
		    	   {
		    		   baseset.add(pb);
		    	   }
		       }
		       for(Integer ct :citations)
		       {
		    	   if (!baseset.contains(ct))
		    	   {
		    		   baseset.add(ct);
		    	   }
		       }
		       if (!baseset.contains(key))
		       {
		    	   baseset.add(key);
		    	   }
		      count++;
			}
			System.out.println(baseset.size());
			 double end=System.currentTimeMillis();
	    	   System.out.println(end- start);
			adjmatrix(baseset,Auth);
    	   		count=0;
    	    baseset.clear();
    	  
    	    finalpagerank=Pagerank(r,Auth);
    	     printdocswithrank(r, Auth, sorted, finalpagerank);
    	    doc.clear();
			sorted.clear();
			
		}
		System.out.println("Enter the query to search ");
		}}

	// sorting function
	
	public static Map sortmap(Map doc) {
		LinkedList list = new LinkedList(doc.entrySet()); 
		Collections.sort(list, new Comparator<Map.Entry<Integer, Double>>()
		{

			@Override
			public int compare(Entry<Integer, Double> e1,
					Entry<Integer, Double> e2) {
				
				return e2.getValue().compareTo(e1.getValue());

			} 
			

		});
		Map sortedMap = new LinkedHashMap(); 
		for (Iterator it = list.iterator(); it.hasNext();) 
		{ 
			Map.Entry entry = (Map.Entry) it.next(); 
		sortedMap.put(entry.getKey(), entry.getValue());
		} 
		return sortedMap; }
	               
	// calculating the adjacency matrix...
	public static void adjmatrix(ArrayList<Integer> baseset, LinkAnalysis Auth)
	{
		double start=System.currentTimeMillis();
		int [][]adjmatrix= new int[baseset.size()][baseset.size()];
		int [][]adjmatrixtrans= new int[baseset.size()][baseset.size()];
		
		for(int i=0; i<baseset.size();i++)
		{
		int[] basesetlinks= Auth.getLinks(baseset.get(i));
		
			for(int j=0; j<basesetlinks.length; j++)
			{
				if(baseset.contains(basesetlinks[j]))
				{
			int index=baseset.indexOf(basesetlinks[j]);
			adjmatrix[i][index]=1;
				}
			}
		}
	for(int i=0;i<baseset.size();i++)
	{
	for(int j=0; j<baseset.size();j++)
		{
		//System.out.print(adjmatrix[i][j]);	
		adjmatrixtrans[j][i]=adjmatrix[i][j];
		}
	}
	double end=System.currentTimeMillis();
	System.out.println(end-start);
    Scorecalculator(adjmatrix,adjmatrixtrans,baseset,Auth);
	}
  
// Authority and hub scores calculator
	public static void Scorecalculator(int[][]adjmatrix,int[][]adjmatrixtrans,ArrayList<Integer> baseset, LinkAnalysis Auth)
  {
		double start=System.currentTimeMillis();
		int count=0;
	
	  double[] auth= new double[baseset.size()];
	double[] authtemp= new double[baseset.size()];
	double[] hub= new double[baseset.size()];
	double[] hubtemp= new double[baseset.size()];
	Arrays.fill(hub, 1.0);
   Arrays.fill(auth, 1.0);
   Map authscore= new HashMap<Integer,Double>();
   Map hubscore= new HashMap<Integer,Double>();
	int[][]AAT=multiply(adjmatrix,adjmatrixtrans);
	int[][]ATA=multiply(adjmatrixtrans,adjmatrix);
	
	boolean flag =true;
		while(flag)
		{
			flag=false;
			double temp=0.0;
			double temp1=0.0;
	
			for (int i=0; i<baseset.size();i++)
			{
				for (int j=0;j<baseset.size();j++)
				{
					temp = temp + ATA[i][j]*auth[j];
					temp1= temp1+ AAT[i][j]*hub[j];
				}
			   authtemp[i]= temp;
			   hubtemp[i] = temp1;
				temp=0.0;temp1=0.0;
		   }
			authtemp=normalize(authtemp);
			hubtemp=normalize(hubtemp);
			for (int N=0;N<baseset.size();N++)
			{
			  if(Math.abs(authtemp[N]-auth[N])>0.00001 && Math.abs(hubtemp[N]-hub[N])>0.00001)
			   {
			     flag= true;
			   }
			}
			
		 if(flag==true)
		 {
	       for (int K=0;K<baseset.size();K++)
	       {
	    	  auth[K]=authtemp[K];
	          hub[K]= hubtemp[K];
	       }
		
		 }
	  }
		for (int p=0;p<baseset.size();p++)
		{
			authscore.put(baseset.get(p),authtemp[p]);
			hubscore.put(baseset.get(p),hubtemp[p]);
		}
     hubscore=sortmap(hubscore);
     authscore=sortmap(authscore);
     double end=System.currentTimeMillis();
     System.out.println("score calc"+(end- start));
     System.out.println("Top 10 hubs are");
     print(hubscore);
    System.out.println("Top 10 authority are");
     print(authscore);
 
 }
	public static List print(Map map)
	{
		List list = new ArrayList(map.keySet());
		System.out.println("x--x---x-x-x-x-x-x--x-x-x-x-x--x--x--x-x-x--x-x--xx-xx-x-x--x----x-x--x-x--xx--x-x-");
		IndexReader r;
		try {
			r = IndexReader.open(FSDirectory.open(new File("index")));
	
		if(list.size()>10)
		{
			for(int i=0 ; i< 10; i++) 
			{
			String d_url = r.document((int) list.get(i)).getFieldable("path").stringValue().replace("%%", "/");
			System.out.println(list.get(i)+"    "+map.get(list.get(i))+"the url is"+ d_url); 
			}
		}
		else
		{
			for(int i=0 ; i< list.size(); i++) 
			{
				System.out.println(list.get(i)+"     "+map.get(list.get(i))); 	
				
			}
		}
		}
		catch (IOException e) {
			
			e.printStackTrace();
		}
		return list;
	}
  
 // normalizing the matrices 
	public static double[] normalize(double[] hubtemp)
  {
	  double magnitude=0.0;
	  for (int i=0; i<hubtemp.length; i++)
	  {
		 magnitude= magnitude + hubtemp[i]*hubtemp[i];
	  }
     magnitude= Math.sqrt(magnitude);
     for (int i=0; i<hubtemp.length; i++)
	  {
		hubtemp[i]=(hubtemp[i]/magnitude);
	  }
     return hubtemp;
  }
	
 // matrix multiplication
  public static int[][] multiply(int[][] a, int[][] b) {
	  double start=System.currentTimeMillis();
	  int rowsInA = a.length;
      int columnsInA = a[0].length; // same as rows in B
      int columnsInB = b[0].length;
      int[][] c = new int[rowsInA][columnsInB];
      for (int i = 0; i < rowsInA; i++) {
          for (int j = 0; j < columnsInB; j++) {
              for (int k = 0; k < columnsInA; k++) {
                  c[i][j] = c[i][j] + a[i][k] * b[k][j];
              }
          }
      }
      double end=System.currentTimeMillis();
      System.out.println(end-start);
      return c;
  }
  public static List Pagerank(IndexReader r,LinkAnalysis Auth)
  {
	  double[] rank= new double[r.maxDoc()];
	  double[] ranknew= new double[r.maxDoc()];
	  //Double[] M_star = new Double[r.maxDoc()];
	 
	  Arrays.fill(rank, 1.0/r.maxDoc());
	  double resetvalue = 1.0/(r.maxDoc());
	  Map doc_links_length= new HashMap<Integer,Integer>();
	  Map sortedrank= new HashMap<Integer,Double>();
	  double[] row_rank = new double[r.maxDoc()];
	  List finalpagerank = new ArrayList();
	  List sinknodelist = new ArrayList();
	  List links_list = new ArrayList();
	  double c =0.8 ;
	  //int MegaBytes = 10241024;
	  /*long freeMemory;
	  long totalMemory;
	  long maxMemory;
	  freeMemory = Runtime.getRuntime().freeMemory()/MegaBytes;
      totalMemory = Runtime.getRuntime().totalMemory()/MegaBytes;
      maxMemory = Runtime.getRuntime().maxMemory()/MegaBytes;

     System.out.println("JVM freeMemory: " + freeMemory);
     System.out.println("JVM totalMemory also equals to initial heap size of JVM : "
                                + totalMemory);
     System.out.println("JVM maxMemory also equals to maximum heap size of JVM: "
                                + maxMemory);*/

	  for (int i=0;i<r.maxDoc();i++)
	  {
		    int[] nodelinks= Auth.getLinks(i);
		  doc_links_length.put(i, nodelinks.length);
		  if (nodelinks.length==0)
		  {
			  sinknodelist.add(i);
		  }
	   }
	  boolean flag= true;
	  int count=0;
	  while(flag==true)
	  {
	  for (int j=0; j<r.maxDoc();j++)
  {
		  Arrays.fill(row_rank,(1-c)*resetvalue);
		 int[] citations= Auth.getCitations(j);
    	for(int k=0; k<sinknodelist.size();k++)
	    {
		  row_rank[(int) sinknodelist.get(k)]+= c*(resetvalue);
        }
    	for(Integer ct :citations)
		 {		       
	      int links_length=(int) doc_links_length.get(ct);
	      row_rank[ct]=row_rank[ct]+ c*(1.0/links_length);
		 }
  
	 ranknew[j]=multiplydouble(row_rank,rank);
	
  }


	 flag=false;
	
	 for (int N=0;N<ranknew.length;N++)
	{
	  if(Math.abs(ranknew[N]-rank[N])>0.00001)
	  {
		 flag= true;
	  }
	}
	 
	 if(flag==true)
	 {
       for (int P=0;P<ranknew.length;P++)
       {
    	  rank[P]=ranknew[P];
       }
	 } 
}
	 
	  for(int i_j=0;i_j<ranknew.length;i_j++)
	  {
		  finalpagerank.add(i_j, ranknew[i_j]);
	  }
	   
	  finalpagerank=scalepagerank(finalpagerank);
	  
	  /*freeMemory = Runtime.getRuntime().freeMemory() / MegaBytes;
      totalMemory = Runtime.getRuntime().totalMemory() / MegaBytes;
      maxMemory = Runtime.getRuntime().maxMemory() / MegaBytes;

      System.out.println("Used Memory in JVM: " + (maxMemory - freeMemory));
      System.out.println("freeMemory in JVM: " + freeMemory);
      System.out.println("totalMemory in JVM shows current size of java heap : "
                                 + totalMemory);
      System.out.println("maxMemory in JVM: " + maxMemory);

System.out.println("mem"+(totalMemory-freeMemory));*/

	  return finalpagerank;
  } 
  public static List scalepagerank(List finalpagerank)
  {
	List pagerank= new ArrayList();
	 double scaled=0.0;
	 double min=100; 
	 double max=-1; 
	 int Arraysize= finalpagerank.size(); 
	 for(int i=0;i<Arraysize;i++)
	 { 
		 if((double)finalpagerank.get(i)<min)
	         { 
			 min=(double)finalpagerank.get(i); 
	         } 
		 if((double)finalpagerank.get(i)>max)
		    { 
			 max=(double)finalpagerank.get(i); 
			}
		 
     } 
	 for(int j=0;j<Arraysize;j++)
	 {  
		 scaled=((double)finalpagerank.get(j)-min)/(max-min); 
        pagerank.add(j, scaled);
	 scaled=0.0;
	 }
	 
  return pagerank;
  }
  public static double multiplydouble(double[] row_rank, double[] rank) {
      int rowsInA = row_rank.length;
      
      double c =0.0;
      for (int i = 0; i < rowsInA; i++) {
                  c = c + row_rank[i] * rank[i];
          }
      
      return c;    
  }
  public static void printdocswithrank(IndexReader r,LinkAnalysis Auth,Map sorted,List finalpagerank)
  {
	  Map relevantdocs= new HashMap<Integer,Double>();
	  double w=0.7;
	  Iterator it = sorted.keySet().iterator();
	 
	  while(it.hasNext())
	  {
		 int key= (int) it.next();
	  double similarity=w*((double)finalpagerank.get(key))+(1-w)*(double)(sorted.get(key));
	  relevantdocs.put(key,similarity);
	  }
  relevantdocs=sortmap(relevantdocs);
  print(relevantdocs);
  }
     
  public static void magnitude(IndexReader r, double[] magnitude,HashMap<String, Double> idf)
			throws IOException {
		int i;
		 
		
	for ( i=0;i<r.maxDoc();i++) 											//initializing the matrix to 0
		{
			magnitude[i]= 0;
		}
		
			TermEnum t = r.terms();
		
			while(t.next())
			{
				Term term1 = new Term("contents", t.term().text() );
				
				TermDocs tdocs = r.termDocs(term1);
					int count=r.docFreq(term1);
					
			    double	Idfvalue = ((double)(r.maxDoc())/count);
			      Idfvalue= Math.log(Idfvalue);
			      
			    while (tdocs.next())
				{
			    	double weight =tdocs.freq()*Idfvalue;
			    	weight= weight*weight;
				magnitude[tdocs.doc()] = magnitude[tdocs.doc()] + weight;
				
				}
				
			    idf.put(t.term().text(),Idfvalue);
			   }
			for ( i=0;i<r.maxDoc();i++) 		
			{
				
					magnitude[i]=(double) Math.sqrt(magnitude[i]);
				
			}
			
	}

			
	}
		



	



