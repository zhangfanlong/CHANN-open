package ExtractFeatures;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import CloneRepresentation.CRD;
import Global.Path;
import Global.VariationInformation;
import PreProcess.CreateCRDInfo;
import PreProcess.ExtractCRDVisitor;
import PreProcess.GetJavaFiles;


/**
 * @author YueYuan
 *
 */

public class FeatureOperations {
	
	//private static CompilationUnit cu;
	//private static List<String> copiedCodes;
	private static CRD copiedCrd,pastedCrd;
	private static String path;
	private static List<String> currentVersionJavaFiles;
	
	public static void init(String sysPath){
		currentVersionJavaFiles = new ArrayList<String>();
		File sysFolder = new File(sysPath);
		if(sysFolder.isDirectory()){
			File[] childSysFolder = sysFolder.listFiles();
			for(File f : childSysFolder){
				if (f.isDirectory()){
					GetJavaFiles getJavaFile = new GetJavaFiles();
					getJavaFile.GetJavaFilePath(f.getAbsolutePath());
					currentVersionJavaFiles = getJavaFile.getAllJavaFiles();
				}
			}
		}
	}
	
	public static List<String> GetCodesFromFile(String path,int startLine,int endLine){
		List<String> sourceCode = CreateCRDInfo.GetFileContent(path);
		List<String> copiedCodes = CreateCRDInfo.GetCFSourceFromCRDInfo(sourceCode, startLine, endLine);
		return copiedCodes;
	}
	
	public static CompilationUnit CreateAST(String path){
		FeatureOperations.path = path;
		byte[] input =null;
        try {
        	BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(path));
			input = new byte[bufferedInputStream.available()];  
	        bufferedInputStream.read(input);  
	        bufferedInputStream.close(); 
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ASTParser astParser = ASTParser.newParser(AST.JLS8);  
		astParser.setSource(new String(input).toCharArray());
		astParser.setKind(ASTParser.K_COMPILATION_UNIT);
		//cu = (CompilationUnit)(astParser.createAST(null));
		return (CompilationUnit)(astParser.createAST(null));
	}
	
	public static void CreateCRDForFrag(CompilationUnit cu,String path,int startLine,int endLine,String flag){
		//CompilationUnit cu = FeatureOperations.CreateAST(path);
	    int startPos = cu.getPosition(startLine, 0);
		int endPos = cu.getPosition(endLine + 1, 0); 
		ExtractCRDVisitor extraCrdVisitor = new ExtractCRDVisitor(startPos,endPos);
		cu.accept(extraCrdVisitor);	
		
		CRD crd = new CRD();
		crd.setFileName(path);
		crd.setClassName(extraCrdVisitor.className);
		crd.setMethodInfo(extraCrdVisitor.methodInfo);
		crd.setBlockInfos(extraCrdVisitor.blockInfos);
		crd.setStartLine(String.valueOf(startLine));
		crd.setEndLine(String.valueOf(endLine));
		if(extraCrdVisitor.relStartLine != null){
			crd.setRelStartLine(extraCrdVisitor.relStartLine);
			crd.setRelEndLine(String.valueOf(endLine - startLine + Integer.parseInt(crd.getRelStartLine())));
		}
		else if(extraCrdVisitor.pOffset != -1){
			crd.setRelStartLine(String.valueOf(startLine - cu.getLineNumber(extraCrdVisitor.pOffset) + 1));
			crd.setRelEndLine(String.valueOf(endLine - startLine + Integer.parseInt(crd.getRelStartLine())));
		}
		else 
			crd.setRelStartLine(null);

		if(flag.equals("copy")){
			copiedCrd = new CRD();
			copiedCrd = crd;
		} else if(flag.equals("paste")){
			pastedCrd = new CRD();
			pastedCrd = crd;
		}
		
	}
	
	public static MethodInvocCountVisitor GetMethodInvocInfo(CompilationUnit cu,/*String path,*/int startLine,int endLine){
		//CompilationUnit cu = FeatureOperations.CreateAST(path);
		String className = copiedCrd.getClassName();
		int startPos = cu.getPosition(startLine, 0);
  		int endPos = cu.getPosition(endLine + 1,0 ); 

  		MethodInvocCountVisitor invocFeatureVisitor = new MethodInvocCountVisitor(startPos,endPos,className,FeatureOperations.currentVersionJavaFiles);
  		cu.accept(invocFeatureVisitor);
  		return invocFeatureVisitor;
	}

	public static HalsteadMetric GetHalsteadInfo(List<String> copiedCodes){
		copiedCodes = PreProcess.clearComment(copiedCodes);
		copiedCodes = PreProcess.clearString(copiedCodes);
		copiedCodes = PreProcess.clearImport(copiedCodes);
		return new HalsteadMetric(copiedCodes);
	}
	
	public static StructuralFeatureVisitor GetStructuralInfo(CompilationUnit cu,int start,int end,boolean isLine){
		int startPos,endPos;
		if(isLine){
			startPos = cu.getPosition(start, 0);
	  		endPos = cu.getPosition(end + 1,0 ); 
		} else {
			startPos = start;
			endPos = end; 
		}

  		StructuralFeatureVisitor strucFeatureVisitor = new StructuralFeatureVisitor(startPos,endPos);
        cu.accept(strucFeatureVisitor);
        return strucFeatureVisitor;
	}
	
}
