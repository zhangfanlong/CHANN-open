package ExtractFeatures;

public class RelatedNodes { 
	public static enum relevantNode{
		this_or_super,
		assignment, 
		//identifier,
		literal,
		
		if_then_statement,
		if_then_else_statement,
		switch_statement,
		
		while_statement,
		do_statement,
		for_statement,
	}
}
