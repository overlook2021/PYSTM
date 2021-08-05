package MODEL;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import Corpus.Corpus;
import Corpus.Document;
import Corpus.LongDoc;
import Corpus.SumTopic;
import Corpus.SumWord;
import Corpus.Word;

public class PYSTM {
	public Corpus c;
	public String data_name;
	public void setData_name(String data_name) {
		this.data_name = data_name;
	}
	
	//�������
	public double gamma; //pitman-yor������
	public double p_law; //pitman-yor��power-law���飬��0-1֮��
	public double alpha;	
	public double beta;
	//�ĵ���������
	public int n_topic; //������Ŀ
	public int dic_length; //�ֵ䳤��
	public int n_doc; //�ĵ����ж����ĵ�
	//�������������ı����
	public int[] wordN_s; //���ı�s�еĵ�����
	public int[][] wordN_s_z; //���ı�s����������z�ĵ�����
	public int[][] wordN_z_v; //��������z����Ϊv�ĵ�����
	public int[] wordN_z; //��������z�ĵ�����
	
	public int[][] sum_s_z; //��¼���ı�s��������z�����в�������
	public int turn; //��¼�����˶��ٴ�
	//�������������ı����
	public ArrayList<LongDoc> ld_list = new ArrayList<LongDoc>();
	//�������ͳ��topic����
	public ArrayList<SumTopic> topic_sum_list = new ArrayList<SumTopic>(); 
	
	//K_z,Ҫ���ɶ��ٸ����⣬alpha�ĵ�-��������飬beta����-��������飬gamma�в͹ݵ�����
	public PYSTM(String doc_file, String dic_file, String cat_file, 
			int K_z, double alpha, double beta, double gamma, double p_law){
		c = new Corpus(new File(doc_file), new File(dic_file), new File(cat_file));
		
		this.alpha = alpha;
		this.beta = beta;
		this.gamma = gamma;
		this.p_law = p_law;
		
		n_topic = K_z;
		dic_length = c.getDicLength();
		n_doc = c.getDocLength();
		
		this.wordN_s = new int[n_doc];
		this.wordN_s_z = new int[n_doc][n_topic];
		this.wordN_z_v = new int[n_topic][dic_length];
		this.wordN_z = new int[n_topic];
		
		sum_s_z = new int[n_doc][n_topic];
	}
	
	public void init() {		
		Random r = new Random();
		
		//��ʼ�����ı�
		for(int index = 0; index < this.n_doc; index++) {
			Document d = c.doc_list.get(index);
			if(ld_list.size() == 0) {
				LongDoc ld = new LongDoc(n_topic, dic_length);
				ld.addShortText(d);
				d.setLd(ld);
				ld_list.add(ld);
			}else {
				int l_n = ld_list.size() + 1; //��¼���ı�����,�����˿��ܵ��³��ı�
				double[] p = new double[l_n]; //����ע����µĳ��ı��ӽ���
				for(int i = 0; i < (l_n - 1); i++) {
					p[i] = (ld_list.get(i).n_s - p_law);
				}
				p[l_n-1] = (gamma + (l_n - 1) * p_law); //ѡ���µĳ��ı��ĸ���
				//�Ӹ��������в���һ������
				for(int i = 1; i < l_n; i++) {
					p[i] = p[i] + p[i-1];
				}
				double r_l = Math.random() * p[l_n-1];	//��������ĳ��ı�
				int l_new;
				for(l_new = 0; l_new < l_n; l_new++) {		
					if(r_l < p[l_new]) {
						break;
					}
				}
				if(l_new < (l_n - 1)) { //���ı�����֪��
					LongDoc l = ld_list.get(l_new);
					l.addShortText(d);
					d.setLd(l);
				}else {	//���ı����µ�
					LongDoc l = new LongDoc(this.n_topic, dic_length);
					l.addShortText(d);
					d.setLd(l);
					ld_list.add(l);
				}
			}
			
			wordN_s[index] = d.words.size(); //��¼���ı�������
			LongDoc l = d.getLd(); //�õ��ö��ı����ڳ��ı�
			//��ʼ������
			for(Word w : d.words) {
				w.init(n_topic);
				int topic_z = r.nextInt(n_topic);
				w.setZ(topic_z); //�����ʷ���һ������
				wordN_s_z[index][topic_z]++;
				wordN_z_v[topic_z][w.id]++;
				wordN_z[topic_z]++;
				l.addOneTopic(topic_z); //���ı�������ļ�������
			}
		}
		
		System.out.println("init:" + ld_list.size());
	}
	
	//������gibbs sampling
	public void gibbsSampling(int burn, int round, int blank, int k) {
		//��ʼ�����ּ���
		init();
		//burn in,ǰburn�ֲ�����
		for(int i = 0; i < burn; i++) {
			this.samplingAndSkip();
			System.out.println("burn in : " + i);
			System.out.println(this.ld_list.size());
		}
		//ÿ��blank��¼һ�β�����Ϣ
		int interval = blank;
		for(int i = 0; i < round; i++) {
			if(interval >= blank) {
				//��¼�ж��ٴβ��������������ĵ�-�������
				turn++; 
				this.samplingAndRecord();
				interval = 0;
				System.out.println("Record : " + i);
				System.out.println(this.ld_list.size());
			}else {
				this.samplingAndSkip();
				interval++;
				System.out.println("skip : " + i);
			}
		}
		
		topicSum();		//ͳ��topic���ܽ��
		this.recordTopK(k, "result/" + data_name + "_pys_top" + Integer.toString(k) + " " + gamma + " " + p_law + " " + n_topic + ".data");
		this.recordDocTopic("result/" + data_name + "_pys_doc_topic " + gamma + " " + p_law + " " + n_topic + ".data");
		//this.recordClassification("result/"  + data_name + "_pys_classification " + gamma + " " + p_law + " " + n_topic + ".data");
	}
	
	//��¼�ĵ�-����ֲ�
	public void recordDocTopic(String file_name) {
		File dt_file = new File(file_name);
		FileWriter dt_fw = null;
		BufferedWriter dt_writer = null;
		try {
			if(!dt_file.exists()){
				dt_file.createNewFile();
			}
			dt_fw = new FileWriter(dt_file);
			dt_writer = new BufferedWriter(dt_fw);
			for(int index = 0; index < n_doc; index++) {
				Document d = c.doc_list.get(index);
				String line = d.cat_list.get(0) + " ";
				for(int topic_id = 0; topic_id < n_topic; topic_id++) {
					double p = (double) sum_s_z[index][topic_id] / (turn * d.length);
					line += Integer.toString(topic_id) + ":" + Double.toString(p) + " ";
				}
				dt_writer.write(line);
				dt_writer.newLine();
			}
		}catch (Exception e){
			e.printStackTrace();
		}finally{
			try{
				dt_writer.close();
				dt_fw.close();
			}catch (IOException e) {
                e.printStackTrace();
            }
		}
	}
	
	//��¼�ĵ�-����ֲ�
	public void recordClassification(String file_name) {
		File dt_file = new File(file_name);
		FileWriter dt_fw = null;
		BufferedWriter dt_writer = null;
		try {
			if(!dt_file.exists()){
				dt_file.createNewFile();
			}
			dt_fw = new FileWriter(dt_file);
			dt_writer = new BufferedWriter(dt_fw);
			for(int index = 0; index < n_doc; index++) {
				Document d = c.doc_list.get(index);
				String line = "";
				for(int topic_id = 0; topic_id < n_topic; topic_id++) {
					double p = (double) sum_s_z[index][topic_id] / (turn * d.length);
					line +=  Double.toString(p) + " ";
				}
				dt_writer.write(line);
				dt_writer.newLine();
			}
		}catch (Exception e){
			e.printStackTrace();
		}finally{
			try{
				dt_writer.close();
				dt_fw.close();
			}catch (IOException e) {
                e.printStackTrace();
            }
		}
	}
	
	//һ�β��������ǲ���¼���
	public void samplingAndSkip() {
		for(int index = 0; index < c.doc_list.size(); index++) {
			Document d = c.doc_list.get(index);
			sampleLongDoc(index, d);
			for(Word w : d.words) {
				sampleTopic(d, index, w);
			}			
		}
	}
	
	//һ�β��������Ǽ�¼���
	public void samplingAndRecord() {
		for(int index = 0; index < c.doc_list.size(); index++) {
			Document d = c.doc_list.get(index);
			sampleLongDoc(index, d);
			//d.l_doc_list[d.l]++;
			for(Word w : d.words) {
				sampleTopic(d, index, w);
				w.z_list[w.z]++;
				sum_s_z[index][w.z]++; //��¼���ı�index����Ӧ������
			}			
		}
	}
	
	//doc_id,���ı���corpus��id
	public void sampleLongDoc(int doc_id, Document d){
		
		LongDoc ld_old = d.getLd(); //�õ����ı���Ӧ�ľɵĳ��ı�
		//�ų����ı�d����һ�ν��
		ld_old.removeShortText(d);
		if(ld_old.isEmpty()) { //���·��䳤�ı���ԭ���ĳ��ı����Ϊ�գ��ʹ�������ɾ��
			ld_list.remove(ld_old);
		}
		
		int l_n = ld_list.size()+1; //��¼���ı�����,�����˿��ܵ��³��ı�
		double[] p = new double[l_n]; //����ע����µĳ��ı��ӽ���
		
		for(int i = 0; i < (l_n - 1); i++) {
			LongDoc l = ld_list.get(i);
			//p[i] = (l.n_s - p_law) / (gamma + n_doc - 1);
			p[i] = (l.n_s - p_law) ;
			double num2 = 1;
			for(int j = 1; j <= wordN_s[doc_id]; j++) {
				num2 = num2 * (l.n_w + n_topic * alpha + j - 1);
			}
		
			double num1 = 1 / num2;
			for(int k = 0; k < n_topic; k++) {	//k��ʾ�����id
				if(wordN_s_z[doc_id][k] != 0) {	//������ı���������k					
					for(int j = 1; j <= wordN_s_z[doc_id][k]; j++) {
						num1 = num1 * (l.n_z[k] + alpha + j - 1);
					}
				}		
			}
			p[i] = p[i] * num1;
		}
		//ѡ���µĳ��ı��ĸ���
		//p[l_n-1] = (gamma + (l_n - 1) * p_law) / (gamma + n_doc - 1); 
		p[l_n-1] = (gamma + (l_n - 1) * p_law); 
		double num2 = 1;
		for(int j = 1; j <= wordN_s[doc_id]; j++) {
			num2 = num2 * (n_topic * alpha + j - 1);
		}
		double num1 = 1 / num2;
		for(int k = 0; k < n_topic; k++) {	//k��ʾ�����id
			if(wordN_s_z[doc_id][k] != 0) {	//������ı���������k					
				for(int j = 1; j <= wordN_s_z[doc_id][k]; j++) {
					num1 = num1 * (alpha + j - 1);
				}
			}		
		}
		p[l_n-1] = p[l_n-1] * num1;
		
		//System.out.println(p[l_n-1]);
		
		//�Ӹ��������в���һ������
		for(int i = 1; i < l_n; i++) {
			p[i] = p[i] + p[i-1];
		}
		double r_l = Math.random() * p[l_n-1];	//��������ĳ��ı�
		
		int l_new;
		for(l_new = 0; l_new < l_n; l_new++) {		
			if(r_l < p[l_new]) {
				break;
			}
		}

		
		if(l_new < (l_n - 1)) { //���ı�����֪��
			LongDoc l = ld_list.get(l_new);
			l.addShortText(d);
			d.setLd(l);
		}else {	//���ı����µ�
			LongDoc l = new LongDoc(this.n_topic, dic_length);
			l.addShortText(d);
			d.setLd(l);
			ld_list.add(l);
		}
	}
	
	public void sampleTopic(Document d, int d_id, Word w) {
		//�ų���w����һ�ν��
		LongDoc ld = d.getLd();
		ld.removeOneTopic(w.z);
		this.wordN_z_v[w.z][w.id]--;
		this.wordN_z[w.z]--;
		this.wordN_s_z[d_id][w.z]--;
		
		int pNum = n_topic;
		double[] p = new double[pNum];	
		int p_index = 0;
		for(int i = 0; i < n_topic; i++) {	//i��ʾ����id
			p[p_index] = (ld.getTopicNum(i) + alpha) * 
					(this.wordN_z_v[i][w.id] + beta) / 
					(this.wordN_z[i] + this.dic_length * beta);
			p_index++;
		}
		
		//�Ӹ��������в���һ������
		for(int i = 1; i < pNum; i++) {
			p[i] = p[i] + p[i-1];
		}
		double r = Math.random() * p[pNum-1];
		int topic_New;
		for(topic_New = 0; topic_New < pNum; topic_New++) {		
			if(r < p[topic_New]) {
				break;
			}
		}
		
		//���¼���
		ld.addOneTopic(topic_New);
		this.wordN_z_v[topic_New][w.id]++;
		this.wordN_z[topic_New]++;
		this.wordN_s_z[d_id][topic_New]++;
		
		w.setZ(topic_New);
	}
	
	public void topicSum() {
		int[][] temp_z_v = new int[n_topic][c.dictionary.length];
		
		for(Document d : c.doc_list) {
			for(Word w : d.words) {
				for(int i = 0; i < n_topic; i++) {		
					temp_z_v[i][w.id] += w.z_list[i];
				}
			}
		}
		
		for(int i = 0; i < n_topic; i++) {
			SumTopic sum_z = new SumTopic(i);
			for(int j = 0; j < c.dictionary.length; j++) {
				SumWord sum_w = new SumWord(j, temp_z_v[i][j]);
				sum_z.word_list.add(sum_w);
			}
			this.topic_sum_list.add(sum_z);
		}
	}
	
	//����topK��д���ļ���k�Ǳ������ٸ���
	public void recordTopK(int k, String file_name) {
		File topk_file = new File(file_name);
		FileWriter topk_w = null;
		BufferedWriter topk_bf = null;
		try{
			if(!topk_file.exists()){
				topk_file.createNewFile();
			}
			topk_w = new FileWriter(topk_file);
			topk_bf = new BufferedWriter(topk_w);
			
			for(SumTopic sum_z : this.topic_sum_list) {
				sum_z.sortWords();
				String tempS = "";
				for(int i = 0; i < k; i++) {
					SumWord sum_w = sum_z.word_list.get(i);
					tempS += c.dictionary.dic[sum_w.wordID] + " ";
				}
				topk_bf.write(tempS);
				topk_bf.newLine();
			}
			
		}catch (Exception e){
			e.printStackTrace();
		}finally{
			try{
				topk_bf.close();
				topk_w.close();
			}catch (IOException e) {
                e.printStackTrace();
            }
		}
	}
	
	public static void main(String[] args) {
		//PYSTM py = new PYSTM("data/StackOverflow_doc.txt", "data/StackOverflow_dic.txt", "data/StackOverflow_LABEL.txt", 100, 0.1, 0.1, 5000, 0.0);
		//PYSTM py = new PYSTM("data/Biomedical_doc.txt", "data/Biomedical_dic.txt", "data/Biomedical_LABEL.txt", 
		//		50, 0.1, 0.1, 800, 0.0);
		
		//py.setData_name("StackOverflow");
		
		PYSTM py = new PYSTM("data/10000news_doc.txt", "data/10000news_dic.txt", "data/10000news_LABEL.txt", 
				15, 0.1, 0.01, 1000, 0.8);
		
		py.setData_name("10000news");
		long startTime=System.currentTimeMillis();
		//ptm.gibbsSampling(100, 1000, 10, 10);ptm
		
		py.gibbsSampling(500, 2000, 10, 10);
		long endTime=System.currentTimeMillis(); 
		System.out.println("time�� "+(endTime-startTime)+"ms");
	}
}
