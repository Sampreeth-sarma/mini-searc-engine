import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BooleanRetrieval {
	
	HashMap<String, Set<Integer>> invIndex;
	int [][] docs;
	HashSet<String> vocab;
	HashMap<Integer, String> map;  // int -> word
	HashMap<String, Integer> i_map; // inv word -> int map
	public BooleanRetrieval() throws Exception{
		// Initialize variables and Format the data using a pre-processing class and set up variables
		invIndex = new HashMap<String, Set<Integer>>();
		DatasetFormatter formater = new DatasetFormatter();
		formater.textCorpusFormatter("./all.txt");
		docs = formater.getDocs();
		vocab = formater.getVocab();
		map = formater.getVocabMap();
		i_map = formater.getInvMap();
	}

	void createPostingList(){
		//Initialze the inverted index with a SortedSet (so that the later additions become easy!)
		for(String s:vocab){
			invIndex.put(s, new TreeSet<Integer>());
		}
		//for each doc
		for(int i=0; i<docs.length; i++){
			//for each word of that doc
			for(int j=0; j<docs[i].length; j++){
				//Get the actual word in position j of doc i
				String w = map.get(docs[i][j]);
				if(invIndex.containsKey(w)){
					invIndex.get(w).add(i+1);
				}				
				/* TO-DO:
				Get the existing posting list for this word w and add the new doc in the list. 
				Keep in mind doc indices start from 1, we need to add 1 to the doc index , i
				 */
			}

		}
	}

	Set<Integer> intersection(Set<Integer> a, Set<Integer> b){
		/*
		First convert the posting lists from sorted set to something we 
		can iterate easily using an index. I choose to use ArrayList<Integer>.
		Once can also use other enumerable.
		 */
		ArrayList<Integer> PostingList_a = new ArrayList<Integer>(a);
		ArrayList<Integer> PostingList_b = new ArrayList<Integer>(b);
		TreeSet<Integer> result = new TreeSet<Integer>();

		//Set indices to iterate two lists. I use i, j
		int i = 0,j=0;

			while(i!=PostingList_a.size() && j!=PostingList_b.size()){
				if( (int)PostingList_a.get(i) > (int)PostingList_b.get(j)){
					j++;
				}
				else if((int)PostingList_a.get(i)==(int)PostingList_b.get(j)){
					result.add(PostingList_a.get(i));
					i++;
					j++;
				}
				else{
					i++;
				}
					
			}
		
		return result;
	}

	Set <Integer> evaluateANDQuery(String a, String b){
		return intersection(invIndex.get(a), invIndex.get(b));
	}

	Set<Integer> union(Set<Integer> a, Set<Integer> b){
		/*
		 * IMP note: you are required to implement OR and cannot use Java Collections methods directly, e.g., .addAll whcih solves union in 1 line!
		 * TO-DO: Figure out how to perform union extending the posting list intersection method discussed in class?
		 */
		TreeSet<Integer> result = new TreeSet<Integer>(a);
		// Implement Union here
		Iterator<Integer> iterator = b.iterator();
		while(iterator.hasNext()){
			result.add(iterator.next());
		}
		iterator = a.iterator();
		while(iterator.hasNext()){
			result.add(iterator.next());
		}
		return result;
	}

	Set <Integer> evaluateORQuery(String a, String b){
		return union(invIndex.get(a), invIndex.get(b));
	}
	
	Set<Integer> not(Set<Integer> a){
//		TreeSet<Integer> result = new TreeSet<Integer>(map.keySet());
		TreeSet<Integer> result = new TreeSet<Integer>();
		/*
		 Hint:
		 NOT is very simple. I traverse the sorted posting list between i and i+1 index
		 and add the other (NOT) terms in this posting list between these two pointers
		 First convert the posting lists from sorted set to something we 
		 can iterate easily using an index. I choose to use ArrayList<Integer>.
		 Once can also use other enumerable.
		 
		 
		 */
//		Iterator<Integer> iterator = a.iterator();
//		while(iterator.hasNext()){
//			result.remove(iterator.next());
//		}
		Set<Integer> vocab1 = map.keySet();
		Iterator<Integer> vocabIterator = vocab1.iterator();
		Iterator<Integer> setIterator = a.iterator();
		int vocabValue=0,setValue=0;
		if(setIterator.hasNext())
			 setValue =(int) setIterator.next();
		if(vocabIterator.hasNext())
			 vocabValue =(int) vocabIterator.next();
		while(true){
			if(setValue>vocabValue){
				result.add(vocabValue);
				if(vocabIterator.hasNext())
					vocabValue = (int)vocabIterator.next();
				else 
					break;
			}
			else if(setValue == vocabValue){
				if(vocabIterator.hasNext())
					vocabValue = (int)vocabIterator.next();
				else 
					break;
				
			}
			else{
				if(setIterator.hasNext())
					setValue =(int) setIterator.next();
				else if(vocabIterator.hasNext())
					vocabValue = (int)vocabIterator.next();
				else 
					break;
			}
			
			
		}
		// TO-DO: Implement the not method using above idea or anything you find better!
		return result;
	}

	Set <Integer> evaluateNOTQuery(String a){
		return not(invIndex.get(a));
	}
	
	Set <Integer> evaluateAND_NOTQuery(String a, String b){
		return intersection(invIndex.get(a), not(invIndex.get(b)));
	}
	public static void main(String[] args) throws Exception {
		long start_time = System.nanoTime();
		String query_type = args[0];
		FileWriter fw=null;

		//Initialize parameters
		BooleanRetrieval model = new BooleanRetrieval();

		//Generate posting lists
		model.createPostingList();

		//Print the posting lists from the inverted index
		
		
		String query_string;
		PrintWriter pw=null;
		switch(query_type){
		case "PLIST":
					query_string = args[1];
					fw = new FileWriter(args[2]);
					pw= new PrintWriter(fw);
					pw.println(query_string + " -> " + model.invIndex.get(query_string));
					break;
		case "AND":
					query_string = args[1]+" "+args[2]+" "+args[3];
					fw = new FileWriter(args[4]);
					pw= new PrintWriter(fw);
					pw.println(query_string + " -> " + model.evaluateANDQuery(args[1].toLowerCase(), args[3].toLowerCase()));
					break;
		case "OR":					
					query_string = args[1]+" "+args[2]+" "+args[3];
					fw = new FileWriter(args[4]);
					pw= new PrintWriter(fw);
					pw.println(query_string + " -> " + model.evaluateORQuery(args[1].toLowerCase(), args[3].toLowerCase()));
					break;
		case "AND-NOT":
					query_string = args[1]+" "+args[2]+" "+args[3]+" "+args[4];
					fw = new FileWriter(args[5]);
					pw= new PrintWriter(fw);
					pw.println(query_string + " -> " + model.evaluateAND_NOTQuery(args[1].toLowerCase(), args[4].substring(0, args[4].length()-1).toLowerCase()));
					break;
		default:	
					System.out.println("entered query type is invalid");
					break;
					
		}
		pw.close();
		fw.close();
//		long start_time = System.nanoTime();
		long end_time = System.nanoTime();
		System.out.println((end_time-start_time)/(Math.pow(10, 9))+" seconds");

	}
	}