package Corpus;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class Word {

	public int id;	//�����id��
	public int z;	//һ�����������ĸ�����
	public int[] z_list; //��¼x�ֵ�����������topic������
	
	public int pre_z; //��һ��topic
	
	public Word(int id) {
		this.id = id;		
	}
	
	//��ʼ����K_z����������
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
