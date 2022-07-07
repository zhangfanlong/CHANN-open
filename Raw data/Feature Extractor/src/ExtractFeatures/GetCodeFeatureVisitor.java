package ExtractFeatures;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
//import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

public class GetCodeFeatureVisitor extends ASTVisitor{
	private static boolean getCloneInfo;
	private int startPos;
	private int endPos;
	String packageName;
	private String className;
	
	private int totalMethodInvocCount;
	private int libraryMethodInvocCount;
	private int localMethodInvocCount;
	private int otherMethodInvocCount;
	//private int fieldAccessCount;
	private int totalParameterCount;
	private List<String> allJavaFiles;
	
	public GetCodeFeatureVisitor(int start,int end,String className,List<String> allJavaFiles){		
		startPos=start;
		endPos=end;
		getCloneInfo = false;
		
		this.className = className;
		this.allJavaFiles = new ArrayList<String>();
		this.allJavaFiles = allJavaFiles;
		_initCodeFeature(); 
	}
	
	private boolean isInProject(String str){
		//System.out.println(str);
		str = str.replace(".", "\\");
		for(String p : this.allJavaFiles){
			if(p.endsWith(str + ".java"))
				return true;
		}
		return false;
	}
	
	private void _initCodeFeature(){
		totalMethodInvocCount = 0;
		libraryMethodInvocCount = 0;
		localMethodInvocCount = 0;
		otherMethodInvocCount = 0;
		totalParameterCount = 0;
		//fieldAccessCount = 0;
	}
	
	public void preVisit(ASTNode node) {
		int nodeStart = node.getStartPosition();
		int nodeEnd = node.getStartPosition() + node.getLength() - 1;

		if ((nodeStart >= this.startPos) && (nodeEnd <= this.endPos)) {
			getCloneInfo = true;
		} else {
			getCloneInfo = false;
		}
		super.preVisit(node);
	}

	public boolean visit(PackageDeclaration node) {
		this.packageName = node.getName().toString();
		return super.visit(node);
	}

	/*	public boolean visit(FieldAccess node) {
		if(getCloneInfo){
			++ fieldAccessCount;
		}
		return super.visit(node);
	}
	
	public boolean visit(SimpleName node) {
		if(getCloneInfo){
			if(node.resolveBinding().getKind() == 3){
				IVariableBinding varbinding = (IVariableBinding)node.resolveBinding();
				if(varbinding.getDeclaringClass() != null && varbinding.getDeclaringClass().getName().equals(className)){
					++ fieldAccessCount;
				}
					
			}
		}
		return super.visit(node);
	}
*/
	public boolean visit(MethodInvocation node) {
		if(getCloneInfo){
			++totalMethodInvocCount;
			
			this.totalParameterCount += node.arguments().size();
			
			ITypeBinding declarClass = node.resolveMethodBinding().getDeclaringClass();
			if(declarClass.isFromSource() && declarClass.getName().equals(this.className)){
				++ this.localMethodInvocCount;
			}
			else if(declarClass.getQualifiedName().substring(0, declarClass.getQualifiedName().indexOf(".")).equals(packageName)){
				++ this.otherMethodInvocCount;
			}
			else if(this.isInProject(declarClass.getQualifiedName())){
				++ this.otherMethodInvocCount;
				//System.out.println("Other->"+ node.getName());
			}
			else{
				++ this.libraryMethodInvocCount;
				//System.out.println("Lib->"+ node.getName());
			}	
		}
		return super.visit(node);
	}

	public boolean visit(SuperMethodInvocation node) {
		if(getCloneInfo){
			ITypeBinding declarClass = node.resolveMethodBinding().getDeclaringClass();
			if(declarClass.getQualifiedName().substring(0, declarClass.getQualifiedName().indexOf(".")).equals(packageName)){
				++ this.otherMethodInvocCount;
			}
			else if(this.isInProject(declarClass.getQualifiedName())){
				++ this.otherMethodInvocCount;
			}
			else{
				++ this.libraryMethodInvocCount;
			}		
		}
		return super.visit(node);
	}
	
	public int getTotalMethodInvocCount() {
		return totalMethodInvocCount;
	}

	public int getLibraryMethodInvocCount() {
		return libraryMethodInvocCount;
	}

	public int getLocalMethodInvocCount() {
		return localMethodInvocCount;
	}

	public int getOtherMethodInvocCount() {
		return otherMethodInvocCount;
	}

/*	public int getFieldAccessCount() {
		return fieldAccessCount;
	}*/

	public int getTotalParameterCount() {
		return totalParameterCount;
	}
	
}
