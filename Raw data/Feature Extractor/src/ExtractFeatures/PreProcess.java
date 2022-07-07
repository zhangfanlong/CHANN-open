package ExtractFeatures;

import java.util.ArrayList;
import java.util.List;

public class PreProcess {

	private static int calculate(String str, String substr) {
		String temp = str;
		int count = 0;
		int index = temp.indexOf(substr);
		while (index != -1) {
			temp = temp.substring(index + 1);
			index = temp.indexOf(substr);
			count++;
		}
		return count;
	}

	public static List<String> clearComment(List<String> codeFragment) {
		List<String> newCodeFragment = new ArrayList<String>(codeFragment);
		boolean flag = false;
		int index;
		for (int i = 0; i < codeFragment.size(); i++) {
			String tarLine = codeFragment.get(i);
			if (tarLine.indexOf("//") != -1) {
				index = tarLine.indexOf("//");
				if (index > 0) {
					String prevStr = tarLine.substring(0, index);
					int count = calculate(prevStr, "\"");
					if (count % 2 == 0) {
						newCodeFragment.set(i,tarLine.replaceAll("//.*", ""));
						continue;
					}
				} else {
					newCodeFragment.set(i,tarLine.replaceAll("//.*", ""));
					continue;
				}
			}
			if (tarLine.indexOf("/*") != -1) {
				index = tarLine.indexOf("/*");
				if (index > 0) {
					String prevStr = tarLine.substring(0, index);
					int count = calculate(prevStr, "\"");
					if (count % 2 == 0){
						if (tarLine.indexOf("*/") != -1 && tarLine.indexOf("*/") > index){
							newCodeFragment.set(i, tarLine.replaceAll("/\\*.*\\*/", ""));
						} else {
							flag = true;
							newCodeFragment.set(i,tarLine.replaceAll("/\\*.*", ""));
							continue;
						}
					}
				} else {
					if (tarLine.indexOf("*/") != -1 && tarLine.indexOf("*/") > index) {
						newCodeFragment.set(i,tarLine.replaceAll("/\\*.*\\*/", ""));
					} else {
						flag = true;
						newCodeFragment.set(i,tarLine.replaceAll("/*.*", ""));
						continue;
					}
				}
			}
			if (flag && tarLine.indexOf("*/") != -1){
                   flag = false;
                   newCodeFragment.set(i,tarLine.replaceAll(".*\\*/", ""));
                   continue;
            }
            if (flag){
                   newCodeFragment.set(i, "");
            }
		}
		return newCodeFragment;
	}

	public static List<String> clearString(List<String> codeFragment) {
		List<String> newCodeFragment = new ArrayList<String>(codeFragment);
        for (int i = 0; i < codeFragment.size(); i++){
        	String tarLine = newCodeFragment.get(i);
            while (tarLine.indexOf("\"") != -1){
                int index = tarLine.indexOf("\"");
                String prevStr = tarLine.substring(0, index);
                int count = calculate(prevStr, "\'");
                if (count % 2 == 0){
                    int next_index = tarLine.indexOf("\"", index + 1);
                    if (next_index == -1){
                    	newCodeFragment.set(i, prevStr);
                        break;
                    }
                    int new_next_index = 0;
                    int num_of_backlash = 0;
                    for (int j = next_index - 1; j != index; j--){
                        if (tarLine.charAt(j) != '\\') break;
                        num_of_backlash++;
                    }
                    if (num_of_backlash % 2 == 1){
                        new_next_index = tarLine.indexOf("\"", next_index + 1);
                        next_index = new_next_index;
                    }
                    String leftStr = tarLine.substring(next_index + 1);
                    tarLine = prevStr + leftStr;
                    newCodeFragment.set(i,tarLine);
                    
                }
                else break;
            }
        }
        return newCodeFragment;
	}

	public static List<String> clearImport(List<String> codeFragment) {
		 List<String> newCodeFragment = new ArrayList<String>(codeFragment);
         for (int i = 0; i < codeFragment.size(); i++){
        	 String tarLine = codeFragment.get(i);
        	 if(tarLine.trim().indexOf("import") != -1){
        		 String prevStr = tarLine.substring(0, tarLine.indexOf("import"));
        		 int count = calculate(prevStr, "\"");
                 if (count % 2 == 0){
                	 newCodeFragment.set(i, tarLine.replaceAll("\bimport.*;", ""));
                 }
        	 }
         }
         return newCodeFragment;
	}
}
