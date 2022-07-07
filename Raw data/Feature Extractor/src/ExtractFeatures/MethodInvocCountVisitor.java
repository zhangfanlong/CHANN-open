package ExtractFeatures;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
//import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class MethodInvocCountVisitor extends ASTVisitor{
	private static boolean getCloneInfo;
	private int startPos;
	private int endPos;
	String packageName;
	private String className;
	
	private HashMap<String,String> fieldVar;
	private HashMap<String,String> allLocalVar;
	private HashMap<String,String> classAndSuper;
	
	private int totalMethodInvocCount;
	private int libraryMethodInvocCount;
	private int localMethodInvocCount;
	private int otherMethodInvocCount;
	//private int fieldAccessCount;
	private int totalParameterCount;
	private List<String> allJavaFiles;
	
	public MethodInvocCountVisitor(int start,int end,String className,List<String> allJavaFiles){		
		startPos=start;
		endPos=end;
		getCloneInfo = false;
		
		this.className = className;
		this.allJavaFiles = new ArrayList<String>();
		this.allJavaFiles = allJavaFiles;
		_initCodeFeature(); 
		
		fieldVar = new HashMap();
		allLocalVar = new HashMap();
		classAndSuper = new HashMap();
	}
	
	private boolean isInProject(String str){
		//System.out.println(str);
		//for(String p : this.allJavaFiles){
		//	if(p.endsWith(str + ".java"))
		//		return true;
		//}
		//return false;
		try{
			for(String p : this.allJavaFiles){
				if(p.endsWith(str + ".java"))
					return true;
			}
		}
		catch(NullPointerException e){
			System.out.println("Error.");
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

	public boolean visit(TypeDeclaration node) {
		String superClass=null;
		if(node.getSuperclassType()!= null)
			superClass=node.getSuperclassType().toString();
		this.classAndSuper.put(node.getName().toString(),superClass);
		return super.visit(node);
	}
	
/*	public boolean visit(PackageDeclaration node) {
		this.packageName = node.getName().toString();
		return super.visit(node);
	}*/

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
			
			if(node.getExpression() == null){
				++ this.localMethodInvocCount;
			}else{
				String exp = node.getExpression().toString();
				if(!exp.contains(".")){
					if(exp.equals("this") || this.fieldVar.get(exp)!= null || this.classAndSuper.get(exp)!=null){
						++ this.localMethodInvocCount;
					}else if(this.allLocalVar.get(exp)!=null){
						String type =  this.allLocalVar.get(exp);
						if(this.isInProject(type)){
							++ this.otherMethodInvocCount;
						}else{
							++ this.libraryMethodInvocCount;
						}
					}else if(this.isInProject(exp)){
						++ this.otherMethodInvocCount;
					}else if(!this.isInProject(exp)){
						++ this.libraryMethodInvocCount;
					}else{
						System.out.println(" " + exp +"   "  + node.getName());
						System.out.println("shuchishuchu "+exp +" --- > "+this.allLocalVar.get(exp));
					}
				}else{
					System.out.println(":   " + exp + "    " + node.toString());
					++ this.libraryMethodInvocCount;
				}
			}
			/*ITypeBinding declarClass = node.resolveMethodBinding().getDeclaringClass();
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
			}	*/
		}
		return super.visit(node);
	}

	public boolean visit(SuperMethodInvocation node) {
		if(getCloneInfo){
			++totalMethodInvocCount;		
			this.totalParameterCount += node.arguments().size();
			
			String superClass = this.classAndSuper.get(this.className); 
			if(this.classAndSuper.get(superClass)!=null){
				++this.localMethodInvocCount;
			}else if(this.isInProject(superClass)){
				++ this.otherMethodInvocCount;
			}else{
				++ this.libraryMethodInvocCount;
			}
			/*ITypeBinding declarClass = node.resolveMethodBinding().getDeclaringClass();
			if(declarClass.getQualifiedName().substring(0, declarClass.getQualifiedName().indexOf(".")).equals(packageName)){
				++ this.otherMethodInvocCount;
			}
			else if(this.isInProject(declarClass.getQualifiedName())){
				++ this.otherMethodInvocCount;
			}
			else{
				++ this.libraryMethodInvocCount;
			}	*/	
			
			/*
			 * ASTNode tempNode;
			tempNode = (SuperMethodInvocation)node;
			while(tempNode.getParent().getNodeType() != ASTNode.TYPE_DECLARATION){
				switch(tempNode.getParent().getNodeType()){
					case ASTNode.BLOCK: tempNode = (Block)tempNode.getParent();break;
					case ASTNode.METHOD_DECLARATION: tempNode = (MethodDeclaration)tempNode.getParent();break;
					
					case ASTNode.RETURN_STATEMENT: tempNode = (ReturnStatement)tempNode.getParent();break;
					case ASTNode.IF_STATEMENT: tempNode = (IfStatement)tempNode.getParent();break;
					case ASTNode.WHILE_STATEMENT: tempNode = (WhileStatement)tempNode.getParent();break;
					case ASTNode.FOR_STATEMENT: tempNode = (ForStatement)tempNode.getParent();break;
					case ASTNode.DO_STATEMENT: tempNode = (DoStatement)tempNode.getParent();break;
					case ASTNode.ASSIGNMENT: tempNode = (Assignment)tempNode.getParent();break;
					case ASTNode.EXPRESSION_STATEMENT: tempNode = (ExpressionStatement)tempNode.getParent();break;
					case ASTNode.VARIABLE_DECLARATION_STATEMENT: tempNode = (VariableDeclarationStatement)tempNode.getParent();break;
					case ASTNode.VARIABLE_DECLARATION_FRAGMENT: tempNode = (VariableDeclarationFragment)tempNode.getParent();break;
					default: System.out.println("super" + tempNode.getParent().getNodeType());
				}
			}
			tempNode = (TypeDeclaration)tempNode.getParent();*/
		}
		return super.visit(node);
	}
	
	public boolean visit(VariableDeclarationFragment node) {
		
		if(node.getParent().getNodeType() == ASTNode.FIELD_DECLARATION){
			FieldDeclaration parent = (FieldDeclaration)node.getParent();
			this.fieldVar.put(node.getName().toString(), parent.getType().toString());
		}
		else if(node.getParent().getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT){
			VariableDeclarationStatement parent = (VariableDeclarationStatement)node.getParent();
			this.allLocalVar.put(node.getName().toString(), parent.getType().toString());
		}else if(node.getParent().getNodeType() == ASTNode.VARIABLE_DECLARATION_EXPRESSION){
			VariableDeclarationExpression parent = (VariableDeclarationExpression)node.getParent();
			this.allLocalVar.put(node.getName().toString(), parent.getType().toString());
		}
		else{
			System.out.println("------------------------------------------");
			System.out.println("GetInvocCountVisitor " + node.getParent().getNodeType());
			System.out.println("------------------------------------------");
		}
		return super.visit(node);
	}
	
	public boolean visit(SingleVariableDeclaration node) {
		if(getCloneInfo){
			this.allLocalVar.put(node.getName().toString(), node.getType().toString());
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
