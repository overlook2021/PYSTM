package Corpus;

import java.io.*;
import java.util.ArrayList;

public class Corpus {

	//词语的字典
	public Dictionary dictionary = new Dictionary();
	//文本集
	public ArrayList<Document> doc_list = new ArrayList<Document>();
	//类别集
	public ArrayList<String> cat_list = new ArrayList<String>();
	
	public Corpus(File doc_file, File dic_file, File cat_file){
		loadDocDic(doc_file, dic_file, cat_file);
	}
	
	public int getDicLength() {
		return dictionary.length;
	}
	
	public int getDocLength() {
		return doc_list.size();
	}
	
	public void loadDocDic(File doc_file, File dic_file, File cat_file){
		 BufferedReader doc_reader = null;
		 BufferedReader dic_reader = null;
		 BufferedReader cat_reader = null;
	     try {
	    	 //读doc
	    	 doc_reader = new BufferedReader(new FileReader(doc_file));
	    	 String docString = null;
	    	 while ((docString = doc_reader.readLine()) != null) {
	    		 Document doc = new Document();
	    		 String[] w_list = docString.split(" ");
	    		 doc.length = new Integer(w_list.length);
	    		 for(String str : w_list){
	    			 if(!str.isEmpty()){
	    				 Integer id = new Integer(str);
	    				 doc.words.add(new Word(id.intValue()));
	    			 }
	    		 }
	    		 doc_list.add(doc);
	    	 }
	    	 doc_reader.close();
	    	 System.out.println(doc_list.size());
	    	 //读cat
	    	 cat_reader = new BufferedReader(new FileReader(cat_file));
	    	 String catString = null;
	    	 int i = 0;
	    	 while ((catString = cat_reader.readLine()) != null && i < doc_list.size()) {
	    		 Document d = doc_list.get(i);
	    		 int cat_id;
	    		 if(cat_list.isEmpty()) {
	    			 cat_list.add(catString);
	    		 }else {
	    			 if(!cat_list.contains(catString)) {
	    				 cat_list.add(catString); 
	    			 }
	    		 }
	    		 cat_id = cat_list.indexOf(catString);
	    		 d.cat_list.add(Integer.toString(cat_id));
	    		 i++;
	    	 }
	    	 System.out.println(i);
	    	 System.out.println(doc_list.size());
	            
	    	 //读dic
	    	 dic_reader = new BufferedReader(new FileReader(dic_file));
	    	 String dicString = null;
	    	 dictionary.length = new Integer(dic_reader.readLine());
	    	 dictionary.dic = new String[dictionary.length];
	    	 int index = 0;
	    	 while ((dicString = dic_reader.readLine()) != null) {
	    		 if(index < dictionary.length){
	    			 dictionary.dic[index] = dicString;
	    			 index++;
	    		 }
	    	 }
	    	 dic_reader.close();
	     } catch (IOException e) {
	    	 e.printStackTrace();
	     } finally {
	    	 if (doc_reader != null) {
	    		 try {
	    			 doc_reader.close();
	    		 } catch (IOException e1) {
	    		 }
	    	 }
	    	 if (cat_reader != null) {
	    		 try {
	    			 cat_reader.close();
	    		 } catch (IOException e1) {
	    		 }
	    	 }
	    	 else if (dic_reader != null) {
	    		 try {
	    			 dic_reader.close();
	    		 } catch (IOException e2) {
	    		 }
	    	 }
	     }        
	}
	
	public static void main(String[] args){
		Corpus c = new Corpus(new File("data/news_doc.data"), new File("data/news_dic.data"), new File("data/news_cat.data"));
		for(String id : c.cat_list) {
			System.out.println(id);
		}
		for(Document d : c.doc_list) {
			System.out.println(d.cat_list.get(0));
		}
		/*
		for(IDdoc doc : c.doc_list){
			for(Integer id : doc.words){
				//System.out.println(id);
			}
		}
		for(String str : c.dictionary.dic){
			System.out.println(str);
		}
		*/
	}
	
}
