package Corpus;

import java.util.ArrayList;

public class Document {
	
	public int l; //文档属于哪个长文本
	
	public int id;
	
	public LongDoc ld; //给CRSTM算法用的，用来得知属于哪个长文本
	public PriorLD pld; 
	//类别
	public ArrayList<String> cat_list = new ArrayList<String>();


	public LongDoc getLd() {
		return ld;
	}

	//设置长文本
	public void setLd(LongDoc ld) {
		this.ld = ld;
	}
	
	public PriorLD getPLD() {
		return pld;
	}
	public void setPLD(PriorLD pld) {
		this.pld = pld;
	}

	public int[] l_doc_list; 

	//词语集
	public ArrayList<Word> words = new ArrayList<Word>();
	//文档词语数量
	public Integer length;
	
	public void init(int K_l) {
		l_doc_list = new int[K_l];
	}
	
	public int getL() {
		return l;
	}

	public void setL(int l) {
		this.l = l;
	}
}
