package Corpus;

import java.util.ArrayList;

public class PriorLD {

	public ArrayList<Document> doc_list;
	public ArrayList<LongDoc> subld_list;
	public int n_s;
	
	public PriorLD() {
		doc_list = new ArrayList<Document>();
		subld_list = new ArrayList<LongDoc>();
	}
	
	public void addDoc(Document d) {
		doc_list.add(d);
		n_s++;
	}
	
	public void removeDoc(Document d) {
		doc_list.remove(d);
		n_s--;
	}
	
	public boolean isEmpty() {
		return doc_list.isEmpty();
	}
	
}
