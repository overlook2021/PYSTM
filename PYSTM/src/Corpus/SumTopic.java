package Corpus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SumTopic {

	public int z_id;
	public ArrayList<SumWord> word_list = new ArrayList<SumWord>();
	
	public SumTopic(int id) {
		this.z_id = id;
	}
	
	public void sortWords() {
		//重写Compare类的compare函数，按照降序排列
		Collections.sort(word_list, new Comparator<Object>() {  
            public int compare(Object o1, Object o2) {  
            	SumWord w1 = (SumWord) o1;
            	SumWord w2 = (SumWord) o2;
            	if (w1.wordN > w2.wordN) {  
                    return -1;  
                }  
            	if (w1.wordN < w2.wordN) {  
                    return 1;  
                } 
            	return 0;
            }
		});
	}
	
}
