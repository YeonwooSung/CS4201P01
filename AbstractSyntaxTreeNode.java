import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AbstractSyntaxTreeNode {
	private SymbolToken token;
    private List<AbstractSyntaxTreeNode> children;

    public AbstractSyntaxTreeNode(SymbolToken token) {
        this.token = token;
        this.children = null;
    }

    public AbstractSyntaxTreeNode(SymbolToken token, List<AbstractSyntaxTreeNode> children) {
        this.token = token;
        this.children = children;
    }

    public void mergeChildren(AbstractSyntaxTreeNode node) {
    	if (this.children == null) {
    		this.children = new ArrayList<AbstractSyntaxTreeNode>();
    	}

    	List<AbstractSyntaxTreeNode> list = node.children;
    	for (AbstractSyntaxTreeNode n : list) {
    		this.children.add(n);
    	}
    }

    public void printOutChildren(int level) {
    	if (children == null) return;

    	for (AbstractSyntaxTreeNode n : children) {
    		if (n == null || children == null) continue;

    		System.out.print(level + " ");
    		System.out.println(n.token.getTokenString());
    		n.printOutChildren(level + 1);
    	}
    }

    public void insertChildNode(AbstractSyntaxTreeNode child) {
    	if (child == null) {
    		return;
    	}

    	if (this.children == null) {
    		this.children = new ArrayList<AbstractSyntaxTreeNode>();
    	}

    	this.children.add(child);
    }

}
