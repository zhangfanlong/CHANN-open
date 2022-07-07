package PreProcess;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GetJavaFiles {
	
	private List<String> allJavaFiles;
	
	public GetJavaFiles(){
		this.allJavaFiles = new ArrayList<String>();
	}
	
	public  void GetJavaFilePath(String path){
		File file=new File(path); 
        File[] childFiles=file.listFiles();
        for(int i=0;childFiles!=null && i<childFiles.length;i++){ 
        	if(childFiles[i].isDirectory()){
        		GetJavaFilePath(childFiles[i].getPath()); 
            } 
        	if(childFiles[i].isFile() && childFiles[i].getName().endsWith(".java"))
            	this.allJavaFiles.add(childFiles[i].getAbsolutePath());   
        } 
	}

	public List<String> getAllJavaFiles() {
		return allJavaFiles;
	}
	
}
