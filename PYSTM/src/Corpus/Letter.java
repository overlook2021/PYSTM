package Corpus;

public class Letter {
	public static String[] lettersOnlyString(String line){
		char[] c_String = line.toCharArray();
		char[] c_Array = new char[c_String.length];
		int index = 0;
		for(char c_temp : c_String){
			if((c_temp <= 'Z' && c_temp >= 'A') || (c_temp <= 'z' && c_temp >= 'a')){
				c_Array[index++] = c_temp;
			}else {
				c_Array[index++] = ' ';
			}
		}
		String s = new String(c_Array, 0, index);
		String[] s_list = s.split(" ");
		if(s_list.length > 0 ) {
			return s_list;
		}else {
			return null;
		}

	}
}
