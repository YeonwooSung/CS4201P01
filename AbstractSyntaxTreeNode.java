import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AbstractSyntaxTreeNode {
	private SymbolToken token;
    private List<AbstractSyntaxTreeNode> children;

    public AbstractSyntaxTreeNode(SymbolToken token) {
        this.token = token;
        this.children = new ArrayList<AbstractSyntaxTreeNode>();
    }

    public AbstractSyntaxTreeNode(SymbolToken token, List<AbstractSyntaxTreeNode> children) {
        this.token = token;
        this.children = children;
    }

    public void mergeChildren(AbstractSyntaxTreeNode node) {
    	List<AbstractSyntaxTreeNode> list = node.children;
    	for (AbstractSyntaxTreeNode n : list) {
    		this.children.add(n);
    	}
    }

    /**
     * Print out the AST.
     */
    public void printOutChildren() {
    	StringBuilder buffer = new StringBuilder();
    	this.generateTreeString(buffer, "", "");
    	System.out.println(buffer.toString());
    }

    /**
     * Insert the child node to the array list.
     * @param child - child node
     */
    public void insertChildNode(AbstractSyntaxTreeNode child) {
    	if (child == null) {
    		return;
    	}

    	this.children.add(child);
    }

    /**
     * Generate the tree string.
     * @param buffer - buffer to store the generated tree string
     * @param prefix - prefix
     * @param childrenPrefix - prefix for children
     */
    private void generateTreeString(StringBuilder buffer, String prefix, String childrenPrefix) {
        buffer.append(prefix);
        buffer.append(this.token.getTokenString());
        buffer.append('\n');

        for (Iterator<AbstractSyntaxTreeNode> it = children.iterator(); it.hasNext();) {
        	AbstractSyntaxTreeNode next = it.next();
            if (it.hasNext()) {
                next.generateTreeString(buffer, childrenPrefix + "戍式式 ", childrenPrefix + "弛   ");
            } else {
                next.generateTreeString(buffer, childrenPrefix + "戌式式 ", childrenPrefix + "    ");
            }
        }
    }
}
