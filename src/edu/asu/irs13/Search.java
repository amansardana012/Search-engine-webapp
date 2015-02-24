package edu.asu.irs13;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;

import org.apache.lucene.index.*;
import org.apache.lucene.store.FSDirectory;

import java.awt.*;

    
public class Search  {
	
	private static Map hubscorenew= new HashMap<Integer,Double>();
	 private static Map authscorenew= new HashMap<Integer,Double>();
	 private static HashMap<String, Double> idf = new HashMap<String,Double>();
	 private static Map sorted= new HashMap<Integer,Double>();
	 private static List finalpagerank= new ArrayList();
	 
	// printing relevant documents using TF-IDF
	public static List Printrank(double[] magnitude,
			HashMap<Integer,Double> doc,String str) throws IOException, CorruptIndexException  {
		double Similarity=0.0;
		
		IndexReader r = IndexReader.open(FSDirectory.open(new File("/Users/aman/Documents/workspaceee/newweb/index")));
		List list= new ArrayList();
		LinkAnalysis.numDocs= r.maxDoc();
		LinkAnalysis Auth = new LinkAnalysis();
		
		List finalpagerank= new ArrayList();
	boolean flag=true;
		while(flag)
		{
			String[] terms = str.split("\\s+");
			flag=false;
			for(String word : terms)
			{
				double q = Math.sqrt(terms.length);
			Term term = new Term("contents", word);
				TermDocs tdocs1 = r.termDocs(term);
				
				while(tdocs1.next() )
				{	 
				 Similarity = (tdocs1.freq()*idf.get(word) / (magnitude[tdocs1.doc()]*q));	
			     int d_url = tdocs1.doc();
				 if(doc.containsKey(d_url))
					{
						double intermediate = doc.get(d_url);
						Similarity= intermediate + Similarity;
					}
				doc.put(d_url, Similarity);
			     }
			}
		    sorted=sortmap(doc);		
			list = new ArrayList(sorted.keySet());
    	    doc.clear();
		}
		return list;
		}
	//to get sorted relevant documents id and similarity value from vector space similarity 
	 public static Map getsorteddocs()
		{
			return sorted;
			
		}
	 
	// function to create the baseset using the rootset
    public static void baseset(List list)
    {
    	LinkAnalysis Auth = new LinkAnalysis();
    	ArrayList<Integer> baseset= new ArrayList<Integer>();
    	int count=0;
		int K=10;
    	Iterator it = list.iterator();
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
     adjmatrix(baseset,Auth);
		count=0;
  
    }
	
    // function to sort 
	public static <E>Map sortmap(Map doc) {
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
		for (Iterator<E> it = list.iterator(); it.hasNext();) 
		{ 
			Map.Entry entry = (Map.Entry) it.next(); 
		sortedMap.put(entry.getKey(), entry.getValue());
		} 
		return sortedMap; }
	               
	// calculating the adjacency matrix...
	public static void adjmatrix(ArrayList<Integer> baseset,LinkAnalysis Auth)
	{
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
		adjmatrixtrans[j][i]=adjmatrix[i][j];
		}
	}
	
    Scorecalculator(adjmatrix,adjmatrixtrans,baseset,Auth);
	}
  

	
// Authority and hub scores calculator
	public static void Scorecalculator(int[][]adjmatrix,int[][]adjmatrixtrans,ArrayList<Integer> baseset, LinkAnalysis Auth)
  {
	  int count=0;
	double[] auth= new double[baseset.size()];
	double[] authtemp= new double[baseset.size()];
	double[] hub= new double[baseset.size()];
	double[] hubtemp= new double[baseset.size()];
	Map hubscore= new HashMap<Integer,Double>();
	Map authscore= new HashMap<Integer,Double>();
	Arrays.fill(hub, 1.0);
   Arrays.fill(auth, 1.0);
  
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
     hubscorenew=sortmap(hubscore);
     authscorenew=sortmap(authscore);
   
  }
	// function to get the hubscore 
	public static Map gethubscore()
	{
		return hubscorenew ;
	}
	//function to get the authscore 
	public static Map getauthscore()
	{
		return authscorenew ;
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
      return c;
  }
  // function to calculate page rank of the corpus 
  public static List Pagerank() throws IOException, CorruptIndexException 
  {
	  /*
	  if(!finalpagerank.isEmpty())
	  {
		 
		  return finalpagerank;
	  }
	  else
	  {*/
	  IndexReader r = IndexReader.open(FSDirectory.open(new File("/Users/aman/Documents/workspaceee/newweb/index")));
	  LinkAnalysis Auth = new LinkAnalysis();
	  double[] rank= new double[r.maxDoc()];
	  double[] ranknew= new double[r.maxDoc()];
	 
	  Arrays.fill(rank, 1.0/r.maxDoc());
	  double resetvalue = 1.0/(r.maxDoc());
	  Hashtable<Integer,Integer> doc_links_length= new Hashtable<Integer,Integer>();
	  double[] row_rank = new double[r.maxDoc()];
	  List sinknodelist = new ArrayList();
	  List links_list = new ArrayList();
	  double c =0.8 ;
	  
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
	  if(Math.abs(ranknew[N]-rank[N])>0.01)
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
	  return finalpagerank;
	  }
 
  // function to calculate 
  public static double multiplydouble(double[] row_rank, double[] rank) {
      int rowsInA = row_rank.length;
      
      double c =0.0;
      for (int i = 0; i < rowsInA; i++) {
                  c = c + row_rank[i] * rank[i];
          }
      
      return c;    
  }
 
  // function to print the results using pagerank +idf
  public static Map printdocswithrank(Map sorted,List finalpagerank,double w)
  {
	  Map relevantdocs= new HashMap<Integer,Double>();
	  Iterator it = sorted.keySet().iterator();
	 
	  while(it.hasNext())
	  {
		 int key= (int) it.next();
	  double similarity=w*((double)finalpagerank.get(key))+(1-w)*(double)(sorted.get(key));
	  relevantdocs.put(key,similarity);
	  }
  return relevantdocs=sortmap(relevantdocs);
  
  }
  //scaling page rank between 0 & 1
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
     // function to precompute the L-norms of documents 
  public static double[] magnitude() throws IOException {
		int i;
		 
		IndexReader r = IndexReader.open(FSDirectory.open(new File("/Users/aman/Documents/workspaceee/newweb/index")));
	    double[] magnitude = new double[r.maxDoc()];
		
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
			return magnitude;
	}
  
  // function to return the idf values of terms
  public static HashMap<String, Double> getidf()
 {
	return idf;
	 
 }
  // function used in the Gui to return the relevant docs id by vector similarity in a list 
 public static List init(String str, double[] magnitude)
	{
		List list = new ArrayList();
		IndexReader r;
	try {
		r = IndexReader.open(FSDirectory.open(new File("/Users/aman/Documents/workspaceee/newweb/index")));
   
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
		



	



