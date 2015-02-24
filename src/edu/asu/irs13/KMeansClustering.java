package edu.asu.irs13;

import java.beans.VetoableChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;

public class KMeansClustering {

	static Map<Integer, Double> simMap,simMapIDF,diMapIDF,qdProductIDF;
	static Map<String,Integer> termAndIDF;
	static ArrayList<Integer> rankIDF,KMeansBaseSet;
	static ArrayList<Integer> list1,list2,list3,checklist1,checklist2,checklist3;
	static ArrayList<Integer> randomNumberList;
	static ArrayList<String> listOfTerms;
	static ArrayList<String> listOfTermsTopK;
	static int termCount = 366533;
	static double matrix[][] ;
	static String queryString[];
	static Map<String, Double> scalarNearestTerm;
	static HashMap<Integer, HashMap<String, Double>> docTermAndFreq;
	static HashMap<String, Double> TermAndFreq;
	static HashMap<String, Double> tempTandF;
	public static void main(String[] args) throws CorruptIndexException, IOException {
		
		
		topKdocs(50);
		//newCalculateKMeans(50,3);
		scalarClusterAnalysis();
	}

	static void scalarClusterAnalysis() throws CorruptIndexException, IOException{
		scalarNearestTerm = new HashMap<>();
		TermAndFreq = new HashMap<>();
		tempTandF = new HashMap<>();
		docTermAndFreq = new HashMap<Integer,HashMap<String,Double>>();
		listOfTermsTopK = new ArrayList<>();
		IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
		TermEnum totalTermsEnum = r.terms();

		double sumFreq=0.0,idfScore=0.0,decide=0.0;
		while(totalTermsEnum.next()) // for each term indexed
		{

			Term te = new Term("contents", totalTermsEnum.term().text());
			TermDocs documentsContainingterm = r.termDocs(te);
			while(documentsContainingterm.next()){

				if(KMeansBaseSet.contains(documentsContainingterm.doc())){

					if(docTermAndFreq.containsKey(documentsContainingterm.doc())){

						sumFreq = documentsContainingterm.freq();
						idfScore = Math.log(r.maxDoc()/r.docFreq(te)); 
						tempTandF = docTermAndFreq.get(documentsContainingterm.doc());
						tempTandF.put(totalTermsEnum.term().text(),sumFreq);
						docTermAndFreq.put(documentsContainingterm.doc(),tempTandF);
						tempTandF = new HashMap<>();
						decide = sumFreq * idfScore;
					}
					else{

						sumFreq = documentsContainingterm.freq();
						idfScore = Math.log(r.maxDoc()/r.docFreq(te));
						tempTandF.put(totalTermsEnum.term().text(),sumFreq);
						docTermAndFreq.put(documentsContainingterm.doc(),tempTandF);
						tempTandF = new HashMap<>();
						decide = sumFreq * idfScore;
					}
					if(decide > 1.5){
						listOfTermsTopK.add(totalTermsEnum.term().text());
					}
					decide = 0.0;
				}
			}
		}
		System.out.println(listOfTermsTopK.size());
		System.out.println(docTermAndFreq.size());

		double TDMatrix[][] = new double[listOfTermsTopK.size()][KMeansBaseSet.size()];
		double DTMatrix[][] = new double[KMeansBaseSet.size()][listOfTermsTopK.size()];
		double TTMatrix[][] = new double[listOfTermsTopK.size()][listOfTermsTopK.size()];
		double tempmatrix [][] = new double[listOfTermsTopK.size()][listOfTermsTopK.size()];
		double vectorValues []= new double[listOfTermsTopK.size()];

		for(int i=0;i<listOfTermsTopK.size();i++){
			for(int j=0;j<KMeansBaseSet.size();j++){
				TDMatrix [i][j] = 0;
			}
		}
		for(int i=0;i<listOfTermsTopK.size();i++){
			for(int j=0;j<KMeansBaseSet.size();j++){
				if((docTermAndFreq.get(KMeansBaseSet.get(j)).get(listOfTermsTopK.get(i)))!=null){
					TDMatrix [i][j] = docTermAndFreq.get(KMeansBaseSet.get(j)).get(listOfTermsTopK.get(i));
				}
			}
		}
		for(int i=0;i<listOfTermsTopK.size();i++){
			for(int j=0;j<KMeansBaseSet.size();j++){
				DTMatrix[i][j] = TDMatrix[j][i];
			}

		}
		for(int i=0;i<listOfTermsTopK.size();i++){
			for(int j=0;j<listOfTermsTopK.size();j++){
				TTMatrix [i][j] = 0.0;
			}
		}
		//   ------------------------------    TT MATRIX --------------------------------

		for(int i=0;i<listOfTermsTopK.size();i++){
			for(int j=0;j<listOfTermsTopK.size();j++){
				for(int k=0;k<KMeansBaseSet.size();k++){
					TTMatrix [i][j] += TDMatrix [i][k] * DTMatrix[k][j];
				}
			}
		}
		TDMatrix = null;DTMatrix = null;
		System.gc();
		//   ------------------------------    Normalized TT MATRIX --------------------------------

		for(int i=0;i<listOfTermsTopK.size();i++){
			for(int j=0;j<listOfTermsTopK.size();j++){
				tempmatrix [i][j] = TTMatrix [i][j] / (TTMatrix[i][i] + TTMatrix[j][j] - TTMatrix[i][j]);
			}
		}
		for(int i=0;i<listOfTermsTopK.size();i++){
			for(int j=0;j<listOfTermsTopK.size();j++){
				for(int k=0;k<listOfTermsTopK.size();k++){

				}
			}
		}
		for(int i=0;i<listOfTermsTopK.size();i++){
			for(int j=0;j<listOfTermsTopK.size();j++){
				TTMatrix [i][j] = tempmatrix[i][j];
			}
		}
		for(int i=0;i<listOfTermsTopK.size();i++){
			for(int j=0;j<listOfTermsTopK.size();j++){
				tempmatrix[i][j]=0.0;
			}
		}

		double sum = 0.0;
		//------------------------ neighborhood matrix ---------------
		for(int i=0;i<listOfTermsTopK.size();i++){
			for(int j=0;j<listOfTermsTopK.size();j++){
				vectorValues[i]+= TTMatrix [i][j] * TTMatrix[i][j];
				for(int k=0;k<3;k++){
					tempmatrix[i][j]  += TTMatrix[i][k] * TTMatrix[j][k];
				}

			}
			vectorValues[i] = Math.sqrt(vectorValues[i]);
		}
		for(int i=0;i<listOfTermsTopK.size();i++){
			for(int j=0;j<listOfTermsTopK.size();j++){
				tempmatrix[i][j] = tempmatrix[i][j] / (vectorValues[i]*vectorValues[j]); 
			}
		}

		System.out.println("final matrix");
		for(int i=0;i<listOfTermsTopK.size();i++){
			for(int j=0;j<listOfTermsTopK.size();j++){
				tempmatrix[i][j] = Math.round(tempmatrix[i][j]*10000.0)/10000.0;
			}
		}
		//------------------------ transferring to TT matrix ---------------
		for(int i=0;i<listOfTermsTopK.size();i++){
			for(int j=0;j<listOfTermsTopK.size();j++){
				TTMatrix [i][j] = tempmatrix[i][j];
				if(listOfTermsTopK.get(i).equals(queryString.toString())){
					scalarNearestTerm.put(listOfTermsTopK.get(j), tempmatrix[i][j]);
				}
			}
		}
		
		scalarNearestTerm = sortByValues(scalarNearestTerm);
		int count=0;
		System.out.println("-------Correlated Terms---------");
		for (Map.Entry entry : scalarNearestTerm.entrySet()) {
			count++;
			String term = (String) entry.getKey();
			System.out.println(term);
			if(count>5){
				break;
			}
		}
		
	}
	 


	

	static void topKdocs(int kResults) throws CorruptIndexException, IOException{
		IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
		Scanner sc = new Scanner(System.in);
		String str = "";
		
		calculateSimilarityQMatrixTFIDF();
		System.out.print("query> ");
		int termsLength=0;
		double idfScore=0,idfTemp=0;

		while(!(str = sc.nextLine()).equals("quit"))
		{
			qdProductIDF = new HashMap<>();
			simMapIDF = new HashMap<>();
			rankIDF = new ArrayList<>();
			KMeansBaseSet = new ArrayList<>();

			String[] terms = str.split("\\s+");
			termsLength = terms.length;
			queryString= new String[termsLength];
			for(int i=0;i< terms.length;i++){
				queryString[i]=terms[i];
			}
			for(String query : terms)
			{
				Term te = new Term("contents", query);
				TermDocs tdocs = r.termDocs(te);

				while(tdocs.next())
				{
					if(qdProductIDF.containsKey(tdocs.doc())){
						idfScore = Math.log(r.maxDoc()/r.docFreq(te));
						double prevTemp =  (qdProductIDF.get(tdocs.doc()) + tdocs.freq()*idfScore);
						qdProductIDF.put(tdocs.doc(),(double) prevTemp);
					}
					else{
						idfTemp = Math.log(r.maxDoc()/r.docFreq(te));
						double temp = tdocs.freq()*idfTemp;
						qdProductIDF.put(tdocs.doc(),temp);

					}

				}
			}
			for (Map.Entry entry : qdProductIDF.entrySet()) {
				double sim =0.0,temp1=0.0,temp2=0.0;
				temp1=(double) entry.getValue();
				int key = (Integer)entry.getKey();
				temp2=((double)termsLength*((double)diMapIDF.get(key)));
				sim = temp1/temp2;
				simMapIDF.put(key, sim);
			}
			simMapIDF = sortByValues(simMapIDF);
			rankIDF.addAll(simMapIDF.keySet());

			int restrictResults=0;
			System.out.println("adding to base set");

			for(Integer s:rankIDF){
				if(restrictResults < kResults){
					String d_url = r.document(s).getFieldable("path").stringValue().replace("%%", "/");
					//System.out.println("docid: "+s+"  url: "+d_url);
					//System.out.println(s);
					KMeansBaseSet.add(s);
					restrictResults++;
				}
			}
			//constructKmeansTermMatrix();
			//newCalculateKMeans();
			System.out.print("query> ");
		}	
	}

	public static void calculateSimilarityQMatrixTFIDF() throws CorruptIndexException, IOException{


		IndexReader r;
		diMapIDF= new HashMap<>();
		listOfTerms = new ArrayList<>();

		System.out.println("------calculating similarity------");
		r = IndexReader.open(FSDirectory.open(new File("index")));
		TermEnum totalTermsEnum = r.terms();
		int totalterms=0;double maxTermCount=0,square=0,temp=0,idfTemp;
		double sumFreq=0.0,idfScore;
		while(totalTermsEnum.next()) // for each term indexed
		{	
			listOfTerms.add(totalTermsEnum.term().text());
			Term term = new Term("contents", totalTermsEnum.term().text());
			TermDocs documentsContainingterm = r.termDocs(term);
			while(documentsContainingterm.next()){

				if(diMapIDF.containsKey(documentsContainingterm.doc())){
					idfScore = Math.log(r.maxDoc()/r.docFreq(term));
					sumFreq = diMapIDF.get(documentsContainingterm.doc()) + (documentsContainingterm.freq()*documentsContainingterm.freq()*idfScore*idfScore);
					diMapIDF.put(documentsContainingterm.doc(),(double) sumFreq);
					//TermAndFreq.put(totalTermsEnum.term().text(),sumFreq);
					//docTermAndFreq.put(documentsContainingterm.doc(),TermAndFreq );
				}
				else{
					idfTemp = Math.log(r.maxDoc()/r.docFreq(term));
					temp = documentsContainingterm.freq()*documentsContainingterm.freq()*idfTemp*idfTemp;
					diMapIDF.put(documentsContainingterm.doc(),(double) temp);
					//TermAndFreq.put(totalTermsEnum.term().text(),sumFreq);
					//docTermAndFreq.put(documentsContainingterm.doc(),TermAndFreq );

				}
			}

		}
		for (Map.Entry entry : diMapIDF.entrySet()) {
			double root =0;
			root = Math.sqrt((double) entry.getValue());
			diMapIDF.put((Integer)entry.getKey(),root);
		}

		System.out.println("-----  Done Calculating Similarity -----------");

	} 
	public static <K extends Comparable,V extends Comparable> Map<K,V> sortByValues(Map<K,V> map){
		List<Map.Entry<K,V>> entries = new LinkedList<Map.Entry<K,V>>(map.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<K,V>>() {
			@Override
			public int compare(Entry<K, V> o1, Entry<K, V> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}
		});
		Map<K,V> sortedMap = new LinkedHashMap<K,V>();
		for(Map.Entry<K,V> entry: entries){
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

	



}	

