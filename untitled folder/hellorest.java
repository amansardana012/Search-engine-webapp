package sourcecode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import edu.asu.irs13.Cluster;
import edu.asu.irs13.Search;
import static edu.asu.irs13.snippetgen.*;
import static edu.asu.irs13.Cluster.*;
@Path("/search")
public class hellorest {
	
	private static double[] magnitude ;
	private static Map hubscore= new HashMap<Integer,Double>();
	private static Map authscore= new HashMap<Integer,Double>();
	private static List finalpagerank= new ArrayList();
@GET
@Produces(MediaType.APPLICATION_JSON)
public void precomputation() throws IOException 
{
	IndexReader r = IndexReader.open(FSDirectory.open(new File("/Users/aman/Documents/workspaceee/newweb/index")));	
	magnitude= new double [r.maxDoc()];
	magnitude=Search.magnitude();
    finalpagerank =Search.Pagerank();
}

@GET
@Path("/Idf/{word}")
@Produces(MediaType.APPLICATION_JSON)

public JSONObject printUserName(@PathParam("word") String searchword) throws IOException {
	IndexReader r;
	JSONArray json= new JSONArray();
	JSONArray json2= new JSONArray();
	JSONArray json3= new JSONArray();
	JSONObject jsonobj= new JSONObject();
	List list = new ArrayList();
	List relatedlist = new ArrayList();
    List listurl = new ArrayList();
	List snippet_list= new ArrayList();
	List finallist = new ArrayList();
	try {
	r= IndexReader.open(FSDirectory.open(new File("/Users/aman/Documents/workspaceee/newweb/index")));		
   
	list=Search.init(searchword,magnitude);
      snippet_list=generator(searchword,list);
      listurl= (List) getresulturl();
	  relatedlist=Cluster.coorelation(searchword);
	json = new JSONArray(snippet_list);
	json2 = new JSONArray(listurl);
	json3 = new JSONArray(relatedlist);
	jsonobj.put("Url", json2);
	jsonobj.put("snippet", json);
   jsonobj.put("sim",json3);
    
	}
	catch (Exception ex)
	  {
		//wordne"not accepted";
	  }
	return jsonobj;
}
@GET
@Path("/pagerank/{word}")
@Produces(MediaType.APPLICATION_JSON)

public JSONObject Rankresults(@PathParam("word") String searchword) throws IOException 
{
	IndexReader r;
	JSONObject jsonobj= new JSONObject();
	try {
	r= IndexReader.open(FSDirectory.open(new File("/Users/aman/Documents/workspaceee/newweb/index")));	
	Map sorted= new HashMap<Integer,Double>();
	Map relevantdocs= new HashMap<Integer,Double>();
	List doclist= new ArrayList();
	List finaldoclist= new ArrayList();
	Search.init(searchword,magnitude);
    sorted=Search.getsorteddocs();
    relevantdocs=Search.printdocswithrank(sorted, finalpagerank,0.4);
    doclist.addAll(relevantdocs.keySet());
    for(int i=0; i<doclist.size(); i++)
    {
    	String d_url1 = r.document((int) doclist.get(i)).getFieldable("path").stringValue().replace("%%", "/");	
       finaldoclist.add(i, d_url1);
    }
    jsonobj.put("Url",finaldoclist);
	}
	catch (Exception ex)
	  {
		//wordne"not accepted";
	  }
	return jsonobj;  
}

	@GET
	@Path("/Auth/{word}")
	@Produces(MediaType.APPLICATION_JSON)

	public JSONObject ResultsAuth(@PathParam("word") String searchword) throws IOException {
		IndexReader r;
		
		JSONObject jsonobj= new JSONObject();
		List list = new ArrayList();
		List authlist = new ArrayList();
		List finalauthlist = new ArrayList();
		List hublist = new ArrayList();
		List finalhublist = new ArrayList();
		try {
		r= IndexReader.open(FSDirectory.open(new File("/Users/aman/Documents/workspaceee/newweb/index")));		
		list=Search.init(searchword,magnitude);
		Search.baseset(list);
		 authscore=Search.getauthscore();
		 hubscore= Search.gethubscore();
		 authlist.addAll(authscore.keySet());
		 hublist.addAll(hubscore.keySet());
		 for(int i=0; i<authlist.size(); i++)
		    {
		    	String d_url = r.document((int) authlist.get(i)).getFieldable("path").stringValue().replace("%%", "/");	
		       finalauthlist.add(i, d_url);
		    }
		 jsonobj.put("Url", finalauthlist);
		 for(int i=0; i<hublist.size(); i++)
		    {
		    	String d_url1 = r.document((int) hublist.get(i)).getFieldable("path").stringValue().replace("%%", "/");	
		       finalhublist.add(i, d_url1);
		    }
		 jsonobj.put("Url2", finalhublist);
		   } 
		catch (Exception ex)
		  {
			//wordne"not accepted";
		  }
		return jsonobj;  

	}
	@GET
	@Path("/cluster/{word}")
	@Produces(MediaType.APPLICATION_JSON)

	public JSONObject Resultcluster(@PathParam("word") String searchword) throws IOException {
		IndexReader r;
		List list = new ArrayList<>();
		JSONObject jsonobj= new JSONObject();
		List resultlist = new ArrayList();
		List resultlist2 = new ArrayList();
		List resultlist3 = new ArrayList();
		JSONArray json= new JSONArray();
		JSONArray json2= new JSONArray();
		JSONArray json3= new JSONArray();
		Map<Integer, Double>[] Cluster_checknew2 = new HashMap[10];
		try {
			r= IndexReader.open(FSDirectory.open(new File("/Users/aman/Documents/workspaceee/newweb/index")));
			Cluster.clustering(searchword);
			Cluster_checknew2=Cluster.Kmeansiteration(3);
				resultlist.addAll(Cluster_checknew2[0].keySet());
				resultlist2.addAll(Cluster_checknew2[1].keySet());
				resultlist3.addAll(Cluster_checknew2[2].keySet());
				json= new JSONArray(resultlist);json2= new JSONArray(resultlist3);json3= new JSONArray(resultlist3);
				jsonobj.put("c1", json);
				jsonobj.put("c2", json2);
				jsonobj.put("c3", json3);
				
		    }
		
	catch (Exception ex)
	{
		
	}
		return jsonobj;	
	}
	
}
