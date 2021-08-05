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
	
	//先验参数
	public double gamma; //pitman-yor的先验
	public double p_law; //pitman-yor的power-law先验，在0-1之间
	public double alpha;	
	public double beta;
	//文档基本参数
	public int n_topic; //主题数目
	public int dic_length; //字典长度
	public int n_doc; //文档集有多少文档
	//采样计数，短文本相关
	public int[] wordN_s; //短文本s中的单词数
	public int[][] wordN_s_z; //短文本s中属于主题z的单词数
	public int[][] wordN_z_v; //属于主题z，词为v的单词数
	public int[] wordN_z; //属于主题z的单词数
	
	public int[][] sum_s_z; //记录短文本s属于主题z的所有采样数据
	public int turn; //记录采样了多少次
	//采样计数，长文本相关
	public ArrayList<LongDoc> ld_list = new ArrayList<LongDoc>();
	//用于最后统计topic数量
	public ArrayList<SumTopic> topic_sum_list = new ArrayList<SumTopic>(); 
	
	//K_z,要生成多少个主题，alpha文档-主题的先验，beta主题-词语的先验，gamma中餐馆的先验
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
		
		//初始化长文本
		for(int index = 0; index < this.n_doc; index++) {
			Document d = c.doc_list.get(index);
			if(ld_list.size() == 0) {
				LongDoc ld = new LongDoc(n_topic, dic_length);
				ld.addShortText(d);
				d.setLd(ld);
				ld_list.add(ld);
			}else {
				int l_n = ld_list.size() + 1; //记录长文本数量,包含了可能的新长文本
				double[] p = new double[l_n]; //这里注意把新的长文本加进来
				for(int i = 0; i < (l_n - 1); i++) {
					p[i] = (ld_list.get(i).n_s - p_law);
				}
				p[l_n-1] = (gamma + (l_n - 1) * p_law); //选择新的长文本的概率
				//从概率数组中采样一个概率
				for(int i = 1; i < l_n; i++) {
					p[i] = p[i] + p[i-1];
				}
				double r_l = Math.random() * p[l_n-1];	//随机产生的长文本
				int l_new;
				for(l_new = 0; l_new < l_n; l_new++) {		
					if(r_l < p[l_new]) {
						break;
					}
				}
				if(l_new < (l_n - 1)) { //长文本是已知的
					LongDoc l = ld_list.get(l_new);
					l.addShortText(d);
					d.setLd(l);
				}else {	//长文本是新的
					LongDoc l = new LongDoc(this.n_topic, dic_length);
					l.addShortText(d);
					d.setLd(l);
					ld_list.add(l);
				}
			}
			
			wordN_s[index] = d.words.size(); //记录短文本单词数
			LongDoc l = d.getLd(); //得到该短文本所在长文本
			//初始化主题
			for(Word w : d.words) {
				w.init(n_topic);
				int topic_z = r.nextInt(n_topic);
				w.setZ(topic_z); //给单词分配一个主题
				wordN_s_z[index][topic_z]++;
				wordN_z_v[topic_z][w.id]++;
				wordN_z[topic_z]++;
				l.addOneTopic(topic_z); //长文本该主题的计数增加
			}
		}
		
		System.out.println("init:" + ld_list.size());
	}
	
	//完整的gibbs sampling
	public void gibbsSampling(int burn, int round, int blank, int k) {
		//初始化各种计数
		init();
		//burn in,前burn轮不考虑
		for(int i = 0; i < burn; i++) {
			this.samplingAndSkip();
			System.out.println("burn in : " + i);
			System.out.println(this.ld_list.size());
		}
		//每过blank记录一次采样信息
		int interval = blank;
		for(int i = 0; i < round; i++) {
			if(interval >= blank) {
				//记录有多少次采样，用来计算文档-主题概率
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
		
		topicSum();		//统计topic的总结果
		this.recordTopK(k, "result/" + data_name + "_pys_top" + Integer.toString(k) + " " + gamma + " " + p_law + " " + n_topic + ".data");
		this.recordDocTopic("result/" + data_name + "_pys_doc_topic " + gamma + " " + p_law + " " + n_topic + ".data");
		//this.recordClassification("result/"  + data_name + "_pys_classification " + gamma + " " + p_law + " " + n_topic + ".data");
	}
	
	//记录文档-主题分布
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
	
	//记录文档-主题分布
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
	
	//一次采样，但是不记录结果
	public void samplingAndSkip() {
		for(int index = 0; index < c.doc_list.size(); index++) {
			Document d = c.doc_list.get(index);
			sampleLongDoc(index, d);
			for(Word w : d.words) {
				sampleTopic(d, index, w);
			}			
		}
	}
	
	//一次采样，但是记录结果
	public void samplingAndRecord() {
		for(int index = 0; index < c.doc_list.size(); index++) {
			Document d = c.doc_list.get(index);
			sampleLongDoc(index, d);
			//d.l_doc_list[d.l]++;
			for(Word w : d.words) {
				sampleTopic(d, index, w);
				w.z_list[w.z]++;
				sum_s_z[index][w.z]++; //记录短文本index，对应的主题
			}			
		}
	}
	
	//doc_id,短文本在corpus的id
	public void sampleLongDoc(int doc_id, Document d){
		
		LongDoc ld_old = d.getLd(); //得到短文本对应的旧的长文本
		//排除短文本d的上一次结果
		ld_old.removeShortText(d);
		if(ld_old.isEmpty()) { //重新分配长文本后，原来的长文本如果为空，就从链表中删除
			ld_list.remove(ld_old);
		}
		
		int l_n = ld_list.size()+1; //记录长文本数量,包含了可能的新长文本
		double[] p = new double[l_n]; //这里注意把新的长文本加进来
		
		for(int i = 0; i < (l_n - 1); i++) {
			LongDoc l = ld_list.get(i);
			//p[i] = (l.n_s - p_law) / (gamma + n_doc - 1);
			p[i] = (l.n_s - p_law) ;
			double num2 = 1;
			for(int j = 1; j <= wordN_s[doc_id]; j++) {
				num2 = num2 * (l.n_w + n_topic * alpha + j - 1);
			}
		
			double num1 = 1 / num2;
			for(int k = 0; k < n_topic; k++) {	//k表示主题的id
				if(wordN_s_z[doc_id][k] != 0) {	//如果短文本中有主题k					
					for(int j = 1; j <= wordN_s_z[doc_id][k]; j++) {
						num1 = num1 * (l.n_z[k] + alpha + j - 1);
					}
				}		
			}
			p[i] = p[i] * num1;
		}
		//选择新的长文本的概率
		//p[l_n-1] = (gamma + (l_n - 1) * p_law) / (gamma + n_doc - 1); 
		p[l_n-1] = (gamma + (l_n - 1) * p_law); 
		double num2 = 1;
		for(int j = 1; j <= wordN_s[doc_id]; j++) {
			num2 = num2 * (n_topic * alpha + j - 1);
		}
		double num1 = 1 / num2;
		for(int k = 0; k < n_topic; k++) {	//k表示主题的id
			if(wordN_s_z[doc_id][k] != 0) {	//如果短文本中有主题k					
				for(int j = 1; j <= wordN_s_z[doc_id][k]; j++) {
					num1 = num1 * (alpha + j - 1);
				}
			}		
		}
		p[l_n-1] = p[l_n-1] * num1;
		
		//System.out.println(p[l_n-1]);
		
		//从概率数组中采样一个概率
		for(int i = 1; i < l_n; i++) {
			p[i] = p[i] + p[i-1];
		}
		double r_l = Math.random() * p[l_n-1];	//随机产生的长文本
		
		int l_new;
		for(l_new = 0; l_new < l_n; l_new++) {		
			if(r_l < p[l_new]) {
				break;
			}
		}

		
		if(l_new < (l_n - 1)) { //长文本是已知的
			LongDoc l = ld_list.get(l_new);
			l.addShortText(d);
			d.setLd(l);
		}else {	//长文本是新的
			LongDoc l = new LongDoc(this.n_topic, dic_length);
			l.addShortText(d);
			d.setLd(l);
			ld_list.add(l);
		}
	}
	
	public void sampleTopic(Document d, int d_id, Word w) {
		//排除词w的上一次结果
		LongDoc ld = d.getLd();
		ld.removeOneTopic(w.z);
		this.wordN_z_v[w.z][w.id]--;
		this.wordN_z[w.z]--;
		this.wordN_s_z[d_id][w.z]--;
		
		int pNum = n_topic;
		double[] p = new double[pNum];	
		int p_index = 0;
		for(int i = 0; i < n_topic; i++) {	//i表示主题id
			p[p_index] = (ld.getTopicNum(i) + alpha) * 
					(this.wordN_z_v[i][w.id] + beta) / 
					(this.wordN_z[i] + this.dic_length * beta);
			p_index++;
		}
		
		//从概率数组中采样一个概率
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
		
		//更新计数
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
	
	//计算topK并写入文件，k是保留多少个词
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
		System.out.println("time： "+(endTime-startTime)+"ms");
	}
}
