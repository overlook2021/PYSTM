package Corpus;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class Word {

	public int id;	//词语的id号
	public int z;	//一个单词属于哪个主题
	public int[] z_list; //记录x轮迭代，采样的topic总数量
	
	public int pre_z; //上一轮topic
	
	public Word(int id) {
		this.id = id;		
	}
	
	//初始化，K_z是主题数量
	public void init(int K_z) {
		z_list = new int[K_z];
	}

	public int getZ() {
		return z;
	}

	public void setZ(int z) {
		this.z = z;
	}
	
	public void record(int topic_id) {
		z_list[topic_id]++;
	}
	
	public static void main(String[] args){
		double i = 1;
		int j = 2;
		int g = 3;
		double number = g / (1 + j);
		
		System.out.println(number);							
		
		System.out.println(Double.toString(number));		
 
		DecimalFormat decimalFormat = new DecimalFormat("###################.###########");
		System.out.println(decimalFormat.format(number));	

	}
}
