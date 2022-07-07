package ExtractFeatures;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HalsteadMetric {
	public static List<String> Keywords;  
    private static void InitKeywordCollection(){
    	Keywords = new ArrayList<String>();
    	Keywords.add("break");
    	Keywords.add("case");
    	Keywords.add("catch");
    	Keywords.add("continue");
    	Keywords.add("do");
    	Keywords.add("else");
    	Keywords.add("finally");
    	Keywords.add("for");
    	Keywords.add("goto");
    	Keywords.add("if");
    	Keywords.add("return");
    	Keywords.add("sizeof");
    	Keywords.add("switch");
    	Keywords.add("try");
    	Keywords.add("while");
    	Keywords.add("new");
    }
   
    public static List<String> Operators;
    private static void InitOperatorCollection(){
    	Operators = new ArrayList<String>();
    	Operators.add("=");
    	Operators.add("==");
    	Operators.add("+=");
        Operators.add("-=");
        Operators.add("*=");
        Operators.add("/=");
        Operators.add("%=");
        Operators.add("&=");
        Operators.add("|=");
        Operators.add("^=");
        Operators.add("!=");
        Operators.add("<<=");
        Operators.add("<=");
        Operators.add(">>=");
        Operators.add(">=");
        Operators.add("<");
        Operators.add("<<");
        Operators.add(">");
        Operators.add(">>");
        Operators.add("+");
        Operators.add("++");
        Operators.add("-");
        Operators.add("--");
        Operators.add("&");
        Operators.add("&&");
        Operators.add("|");
        Operators.add("||");
        Operators.add("*");
        Operators.add("/");
        Operators.add("%");
        Operators.add("!");
        Operators.add("^");
        Operators.add("~");
        Operators.add(".");
        Operators.add("?:");
        Operators.add("()");
        Operators.add("[]");
        Operators.add("instanceof");
    }
 
    public static void InitHalsteadParam(){
        InitKeywordCollection();
        InitOperatorCollection();
    }
   
	private int uniOPERATORCount;
    private int uniOperandCount;
    private int totalOPERATORCount;
    private int totalOperandCount;
    
    public HalsteadMetric(List<String> codeFragment){
        uniOperandCount = 0;
        uniOPERATORCount = 0;
        totalOperandCount = 0;
        totalOPERATORCount = 0;
        InitHalsteadParam();
        this.GetOPERATORCountFromCode(codeFragment);
        this.GetOperandCountFromCode(codeFragment);
    }
    
    private void GetOPERATORCountFromCode(List<String> codeFragment){
        int[] KeyCounter = new int[Keywords.size()];
        int[] OptorCounter = new int[Operators.size()];

        for(String line : codeFragment){
            int keyIndexHead, keyIndexTail;
            String curStr=""; 
            String leftStr;
            for (String cKey : Keywords){
                leftStr = line;

                while (leftStr.indexOf(cKey) != -1){
                    keyIndexHead = leftStr.indexOf(cKey);
                    keyIndexTail = keyIndexHead + cKey.length() - 1;
                    curStr = leftStr.substring(0, keyIndexTail + 1);
                    if ((keyIndexHead == 0 || !String.valueOf(curStr.charAt(keyIndexHead - 1)).matches("[A-Za-z0-9_]")) &&
                            (keyIndexTail == curStr.length() - 1 || !String.valueOf(curStr.charAt(keyIndexTail + 1)).matches("[A-Za-z0-9_]"))){
                            KeyCounter[Keywords.indexOf(cKey)]++;
                    }
                    if(keyIndexTail < leftStr.length()-1){
                        leftStr = leftStr.substring(keyIndexTail + 1);
                    }else break;
                }
            }
            
            int opIndex;
            if (line.indexOf("=") != -1){
                leftStr = line;
                boolean flag;
                while (leftStr.indexOf("=") != -1){
                    flag = false;
                    opIndex = leftStr.indexOf("=");
                    if(opIndex+1 == leftStr.length()) break;
                    if(opIndex < leftStr.length()-3){
                    	curStr = leftStr.substring(0, opIndex + 3);    
                    }else {
                    	curStr = leftStr.substring(0, opIndex + 2);      
                    }
                    for (int i = 1; i <= 14; i++){
                        if (curStr.indexOf(Operators.get(i)) != -1){
                            OptorCounter[i]++;
                            flag = true;
                            break;
                        }
                    }
                    if (!flag){
                        OptorCounter[Operators.indexOf("=")]++;
                    }
                    if(opIndex < leftStr.length()-3){
                    	leftStr = leftStr.substring(opIndex + 3);  
                    }else if(opIndex < leftStr.length()-2){ 
                    	leftStr = leftStr.substring(opIndex + 2);
                    }else break;   
                }
            }
            
            if (line.indexOf("<") != -1){
                leftStr = line;
                while (leftStr.indexOf("<") != -1){
                    opIndex = leftStr.indexOf("<");
                    if(opIndex+1 == leftStr.length()) break;
                    if(opIndex < leftStr.length()-3){ 
                    	 curStr = leftStr.substring(0, opIndex + 3);     
                    }else {
                    	curStr = leftStr.substring(0, opIndex + 2);
                    }
                    if (curStr.indexOf("<<") != -1 && curStr.indexOf("<<=") == -1)
                    { OptorCounter[Operators.indexOf("<<")]++; }
                    else{
                        if (curStr.indexOf("<=") == -1 && curStr.indexOf("<<=") == -1)
                        { OptorCounter[Operators.indexOf("<")]++; }
                    }
                    
                    if(opIndex < leftStr.length()-3){
                    	leftStr = leftStr.substring(opIndex + 3);  
                    }else if(opIndex < leftStr.length()-2){ 
                    	leftStr = leftStr.substring(opIndex + 2);
                    }else break; 
                }
            }   
            if (line.indexOf(">") != -1){
                leftStr = line;
                while (leftStr.indexOf(">") != -1){
                    opIndex = leftStr.indexOf(">");
                    if(opIndex+1 == leftStr.length()) break;
                    if(opIndex < leftStr.length()-3){
                   	 	curStr = leftStr.substring(0, opIndex + 3);
                    }else {
                    	curStr = leftStr.substring(0, opIndex + 2);
                    }
 
                    if (curStr.indexOf(">>") != -1 && curStr.indexOf(">>=") == -1)
                    { OptorCounter[Operators.indexOf(">>")]++; }
                    else{
                        if (curStr.indexOf(">=") == -1 && curStr.indexOf(">>=") == -1)
                        { OptorCounter[Operators.indexOf(">")]++; }
                    }
                    if(opIndex < leftStr.length()-3){
                    	leftStr = leftStr.substring(opIndex + 3);
                    }else if(opIndex < leftStr.length()-2){
                    	leftStr = leftStr.substring(opIndex + 2);
                    }else break;
                    
                }
            }
                    
            if (line.indexOf("+") != -1){
                leftStr = line;
                while (leftStr.indexOf("+") != -1){
                    opIndex = leftStr.indexOf("+");
                    if (opIndex == leftStr.length() - 1){
                        OptorCounter[Operators.indexOf("+")]++;
                        break;
                    }
                    curStr = leftStr.substring(0, opIndex + 2);         
                    if (curStr.indexOf("++") != -1)
                    { OptorCounter[Operators.indexOf("++")]++; }
                    else{
                        if (curStr.indexOf("+=") == -1)
                        { OptorCounter[Operators.indexOf("+")]++; }
                    }
                    if (opIndex >= leftStr.length() - 3) break;
                    leftStr = leftStr.substring(opIndex + 2);
                }
            }
            if (line.indexOf("-") != -1){
                leftStr = line;
                while (leftStr.indexOf("-") != -1){
                    opIndex = leftStr.indexOf("-");
                    if (leftStr.length() == opIndex + 1){
                        OptorCounter[Operators.indexOf("-")]++;
                        break;
                    }
                    curStr = leftStr.substring(0, opIndex + 2);
                    if (curStr.indexOf("--") != -1)
                    { OptorCounter[Operators.indexOf("--")]++; }
                    else{
                        if (curStr.indexOf("-=") == -1)
                        { OptorCounter[Operators.indexOf("-")]++; }
                    }
                    if (opIndex >= leftStr.length() - 3) break;
                    leftStr = leftStr.substring(opIndex + 2);
                }
            }
            if (line.indexOf("&") != -1){
                leftStr = line;
                while (leftStr.indexOf("&") != -1){
                    opIndex = leftStr.indexOf("&");
                    if(opIndex+1 == leftStr.length()) break;
                    curStr = leftStr.substring(0, opIndex + 2);
                    if (curStr.indexOf("&&") != -1)
                    { OptorCounter[Operators.indexOf("&&")]++; }
                    else{
                        if (curStr.indexOf("&=") == -1)
                        { OptorCounter[Operators.indexOf("&")]++; }
                    }
                    if (opIndex >= leftStr.length() - 3) break;
                    leftStr = leftStr.substring(opIndex + 2);
                }
            }
            if (line.indexOf("|") != -1){
                leftStr = line;
                while (leftStr.indexOf("|") != -1){
                    opIndex = leftStr.indexOf("|");
                    if(opIndex+1 == leftStr.length()) break;
                    curStr = leftStr.substring(0, opIndex + 2);
                    if (curStr.indexOf("||") != -1)
                    { OptorCounter[Operators.indexOf("||")]++; }
                    else{
                        if (curStr.indexOf("|=") == -1)
                        { OptorCounter[Operators.indexOf("|")]++; }
                    }
                    if (opIndex >= leftStr.length() - 3) break;
                    leftStr = leftStr.substring(opIndex + 2);
                }
            }
            
            if (line.indexOf("*") != -1){
                leftStr = line;
                while (leftStr.indexOf("*") != -1){
                    opIndex = leftStr.indexOf("*");
                    if(opIndex+1 == leftStr.length()) break;
                    curStr = leftStr.substring(0, opIndex + 2);
                    leftStr = leftStr.substring(opIndex + 2);
                    if (curStr.indexOf("*=") == -1)
                    { OptorCounter[Operators.indexOf("*")]++; }
                    
                    if (opIndex >= leftStr.length() - 3) break;
                    leftStr = leftStr.substring(opIndex + 2);
                }
            }
            if (line.indexOf("/") != -1){
                leftStr = line;
                while (leftStr.indexOf("/") != -1){
                    opIndex = leftStr.indexOf("/");
                    if(opIndex+1 == leftStr.length()) break;
                    curStr = leftStr.substring(0, opIndex + 2);
                    if (curStr.indexOf("/=") == -1)
                    { OptorCounter[Operators.indexOf("/")]++; }
                   
                    if (opIndex >= leftStr.length() - 3) break;
                    leftStr = leftStr.substring(opIndex + 2);
                }
            }
            if (line.indexOf("%") != -1) {
                leftStr = line;
                while (leftStr.indexOf("%") != -1){
                    opIndex = leftStr.indexOf("%");
                    if(opIndex+1 == leftStr.length()) break;
                    curStr = leftStr.substring(0, opIndex + 2);
                    if (curStr.indexOf("%=") == -1)
                    { OptorCounter[Operators.indexOf("%")]++; }
                    
                    if (opIndex >= leftStr.length() - 3) break;
                    leftStr = leftStr.substring(opIndex + 2);
                }
            }
            if (line.indexOf("!") != -1){
                leftStr = line;
                while (leftStr.indexOf("!") != -1){
                    opIndex = leftStr.indexOf("!");
                    if(opIndex+1 == leftStr.length()) break;
                    curStr = leftStr.substring(0, opIndex + 2);
                    if (curStr.indexOf("!=") == -1)
                    { OptorCounter[Operators.indexOf("!")]++; }
                    
                    if (opIndex >= leftStr.length() - 3) break;
                    leftStr = leftStr.substring(opIndex + 2);
                }
            }
            if (line.indexOf("^") != -1){
                leftStr = line;
                while (leftStr.indexOf("^") != -1){
                    opIndex = leftStr.indexOf("^");
                    if(opIndex+1 == leftStr.length()) break;
                    curStr = leftStr.substring(0, opIndex + 2);
                    if (curStr.indexOf("^=") == -1)
                    { OptorCounter[Operators.indexOf("^")]++; }

                    if (opIndex >= leftStr.length() - 3) break;
                    leftStr = leftStr.substring(opIndex + 2);
                }
            }
            if (line.indexOf("~") != -1){
                leftStr = line;
                while (leftStr.indexOf("~") != -1){
                    opIndex = leftStr.indexOf("~");
                    if(opIndex+1 == leftStr.length()) break;
                    curStr = leftStr.substring(0, opIndex + 2);
                    if (curStr.indexOf("~=") == -1)
                    { OptorCounter[Operators.indexOf("~")]++; }

                    if (opIndex >= leftStr.length() - 3) break;
                    leftStr = leftStr.substring(opIndex + 2);
                }
            }   
      
            if (line.indexOf(".") != -1){
                leftStr = line;
                while (leftStr.indexOf(".") != -1){
                    opIndex = leftStr.indexOf(".");
                    if (leftStr.length() == opIndex + 1){
                    	OptorCounter[Operators.indexOf(".")]++;
                        break;
                    }
                    curStr = leftStr.substring(0, opIndex + 2);
                    leftStr = leftStr.substring(opIndex + 1);
                    try{
                        if (!curStr.substring(opIndex - 1, opIndex + 2).matches("\\d\\.\\d"))
                            { OptorCounter[Operators.indexOf(".")]++; }
                    }
                    catch(StringIndexOutOfBoundsException e){
                        System.out.println(codeFragment);
                    }
                }
            }
            
            if (line.indexOf("?") != -1 && line.indexOf(":") != -1){/
                leftStr = line;
                int index1, index2;
                while (leftStr.indexOf("?") != -1 && leftStr.indexOf(":") != -1){
                    index1 = leftStr.indexOf("?");
                    index2 = leftStr.indexOf(":");
                    if (leftStr.length() == index2 + 1){
                        OptorCounter[Operators.indexOf("?:")]++;
                        break;
                    }
                    else if (index1 > index2){
                        OptorCounter[Operators.indexOf("?:")]++;
                        break;
                    }
                    curStr = leftStr.substring(0, index2 + 2);
                    leftStr = leftStr.substring(index2 + 2);
                    String str = curStr.substring(index1, index2);
                    if (str.indexOf(";") == -1)
                    { OptorCounter[Operators.indexOf("?:")]++; }   // "?;"=>"?;"
                }
            }
            
            if (line.indexOf("(") != -1 && line.indexOf(")") != -1 && line.indexOf("(") <  line.indexOf(")")){
                int indexFirstL, indexLastR;
                indexFirstL = line.indexOf("(");
                indexLastR = line.lastIndexOf(")");
                /*System.out.println("==========================================");
                System.out.println("L:  " + indexFirstL + "  R:  " + indexLastR);
                System.out.println("==========================================");
                */
                String str = line.substring(indexFirstL, indexLastR+1);
                int lCount = 0,coupleCount = 0;
                for (int i = 0; i < str.length(); i++){
                    if (str.charAt(i) == '(')
                    { lCount++; }
                    else if (str.charAt(i) == ')')
                    { lCount--; coupleCount++; }
                    else
                    { continue; }
                }
                OptorCounter[Operators.indexOf("()")] += coupleCount;
            }

            if (line.indexOf("[") != -1 && line.indexOf("]") != -1 && line.indexOf("[") <line.indexOf("]")){
                int indexFirstL, indexLastR;
                indexFirstL = line.indexOf("[");
                indexLastR = line.lastIndexOf("]");
                String str = line.substring(indexFirstL+1, indexLastR);
                int lCount, coupleCount;
                lCount = 0;
                coupleCount = 0;
                for (int i = 0; i < str.length(); i++){
                    if (str.charAt(i) == '[')
                    { lCount++; }
                    else if (str.charAt(i) == ']')
                    { lCount--; coupleCount++; }
                    else
                    { continue; }
                }
                OptorCounter[Operators.indexOf("[]")] += coupleCount;
            }
            
            if (line.indexOf(" instanceof ") != -1){
                leftStr = line;
                while (leftStr.indexOf(" instanceof ") != -1)
                {
                    opIndex = leftStr.indexOf(" instanceof ");
                    curStr = leftStr.substring(0, opIndex + 12);
                    leftStr = leftStr.substring(opIndex + 12);
                    OptorCounter[Operators.indexOf("instanceof")]++;
                }
            }
        }//For
        
        for (int keyCount : KeyCounter){
            if (keyCount > 0){
                this.uniOPERATORCount++;
                this.totalOPERATORCount += keyCount;
            }
        }

        for (int optorCount : OptorCounter){
            if (optorCount > 0){
                this.uniOPERATORCount++;
                this.totalOPERATORCount += optorCount;
            }
        }
    }

    private void GetOperandCountFromCode(List<String> codeFragment){
    	List<String> operandList = new ArrayList<String>();
    	List<Integer> operandCounter = new ArrayList<Integer>();
    	List<String> operatorList = new ArrayList<String>();
    	List<Integer> operatorCounter = new ArrayList<Integer>();
        boolean flagOperand,flagOperator;
        for(int k=0;k<codeFragment.size();k++){
        	String tar = codeFragment.get(k);
        	
        	if(tar.indexOf(".") != -1){
        		String[] maybeFunc = tar.split("\\.");
        		for(int c=1;c<maybeFunc.length;c++){
        			if(maybeFunc[c].indexOf("(") != -1){
        				int opIndex = maybeFunc[c].indexOf("(");
        				flagOperator = false;
        				for(int j=0;j<operatorList.size();j++){
        					if(maybeFunc[c].substring(0,opIndex).equals(operatorList.get(j))){
        						flagOperator = true;
        						operatorCounter.set(j, operatorCounter.get(j)+1);
        						break;
        					}
        				}
        				if (!flagOperator){
                            operatorList.add(maybeFunc[c].substring(0,opIndex));
                            operatorCounter.add(1);
                        }
        			}
        		}
        	}
        	
        	List<String> tokens = new ArrayList<String>();
       		Pattern pattern=Pattern.compile("\\b[a-zA-Z_0-9]+[a-zA-Z_0-9]*\\b");
       		Matcher matcher=pattern.matcher(tar);
       		while(matcher.find()){
       			 tokens.add(matcher.group(0));
       		}
       		if(tokens.size() != 0){
        		for (int i = 0; i < tokens.size(); i++){
        			String tarToken = tokens.get(i);
        			KeyWordsCollection keyCollection = new KeyWordsCollection();
                    if (keyCollection.IsKeyWord(tarToken))	continue;
                    else{      
                    	int opStartIndex = tar.indexOf(tarToken);
                    	int opEndIndex = opStartIndex + tarToken.length() - 1;
                    	if(opEndIndex <= tar.length()-3){
                    		if((opStartIndex == 0 && tar.charAt(opEndIndex+1) == '(') || 
                        			(opStartIndex > 0 && tar.charAt(opStartIndex-1) != '.' && tar.charAt(opEndIndex+1) == '(') ){
                        		
                        		flagOperator = false;
                				for(int j=0;j<operatorList.size();j++){
                					if(tarToken.equals(operatorList.get(j))){
                						flagOperator = true;
                						operatorCounter.set(j, operatorCounter.get(j)+1);
                						break;
                					}
                				}
                				if (!flagOperator){
                                    operatorList.add(tarToken);
                                    operatorCounter.add(1);
                                }
                        	}
                    	}
                    	
                		flagOperand = false;
                		for(int j=0;j<operandList.size();j++){
                			if(tokens.get(i).equals(operandList.get(j))){
                				flagOperand = true;
                				operandCounter.set(j, operandCounter.get(j)+1);
                				break;
                			}
                		}
                		if (!flagOperand){
                            operandList.add(tokens.get(i));
                            operandCounter.add(1);
                        }
                    }   
        		}
       		} 	       	
        }
        
        
        this.uniOperandCount = operandList.size() - operatorList.size();
        for (int count : operandCounter) {
            this.totalOperandCount += count;
        }
        
        if(operatorList.size()!=0){         
            this.uniOPERATORCount += operatorList.size();
            for (int count : operatorCounter) {  
            	this.totalOperandCount -= count;
                this.totalOPERATORCount += count;
            }
        }    
    }

    public int getUniOPERATORCount() {
		return uniOPERATORCount;
	}

	public int getUniOperandCount() {
		return uniOperandCount;
	}

	public int getTotalOPERATORCount() {
		return totalOPERATORCount;
	}

	public int getTotalOperandCount() {
		return totalOperandCount;
	}
}
