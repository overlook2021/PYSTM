package Corpus;

import java.math.BigDecimal;
import java.util.*;

public class LongDoc {

	public int n_s; //���ı�l�����Ķ��ı�����
	public int n_w; //���ı�l�еĵ�����
	public int[] n_z; //���ı�l����������z�ĵ�����
	public int z; //���ı������⣬����SDMM�㷨
	
	public BigDecimal[] w_dic; //���ı����ֵ�
	
	public ArrayList<Document> doc_list = new ArrayList<Document>();
	public ArrayList<Integer> dic = new ArrayList<Integer>();
	
	public void getDic() {
		for(Document d : doc_list) {
			for(Word w : d.words) {
				if(!dic.contains(w.id)) {
					dic.add(w.id);
				}
			}
		}
	}
	
	public LongDoc(int n_topic, int dic_len) {
		n_z = new int[n_topic];
		w_dic = new BigDecimal[dic_len];
		for(int i = 0; i < dic_len; i++) {
			w_dic[i] = BigDecimal.ZERO;
		}
	}
	
	public void addShortText(Document d) {
		n_s++;
		n_w += d.words.size();
		for(Word w : d.words) {
			n_z[w.z]++;
			w_dic[w.id] = w_dic[w.id].add(BigDecimal.ONE);
		}
	}
	
	public void removeShortText(Document d) {
		n_s--;
		n_w -= d.length;
		for(Word w : d.words) {
			n_z[w.z]--;
			w_dic[w.id] = w_dic[w.id].subtract(BigDecimal.ONE);
		}
	}
	
	public void removeOneTopic(int topic) {
		n_z[topic]--;
	}
	
	public void addOneTopic(int topic) {
		n_z[topic]++;
	}
	
	public int getTopicNum(int topic) {
		return n_z[topic];
	}
	
	public boolean isEmpty() {
		if(n_s == 0) {
			return true;
		}else {
			return false;
		}
	}
}
