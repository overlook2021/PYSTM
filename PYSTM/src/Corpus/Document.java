package Corpus;

import java.util.ArrayList;

public class Document {
	
	public int l; //�ĵ������ĸ����ı�
	
	public int id;
	
	public LongDoc ld; //��CRSTM�㷨�õģ�������֪�����ĸ����ı�
	public PriorLD pld; 
	//���
	public ArrayList<String> cat_list = new ArrayList<String>();


	public LongDoc getLd() {
		return ld;
	}

	//���ó��ı�
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

	//���Ｏ
	public ArrayList<Word> words = new ArrayList<Word>();
	//�ĵ���������
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
