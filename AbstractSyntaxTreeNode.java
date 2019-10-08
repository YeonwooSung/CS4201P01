import java.util.Iterator;
import java.util.List;

public class AbstractSyntaxTreeNode {
	final SymbolToken token;
    final List<AbstractSyntaxTreeNode> children;

    public AbstractSyntaxTreeNode(SymbolToken token, List<AbstractSyntaxTreeNode> children) {
        this.token = token;
        this.children = children;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder(50);
        generateTreeString(buffer, "", "");
        return buffer.toString();
    }

    private void generateTreeString(StringBuilder buffer, String prefix, String childrenPrefix) {
        buffer.append(prefix);
        buffer.append(token.getTokenString());
        buffer.append('\n');

        Iterator<AbstractSyntaxTreeNode> it;

        // use for loop to iterate the Iterator.
        for (it = children.iterator(); it.hasNext();) {
        	AbstractSyntaxTreeNode next = it.next();

            if (it.hasNext()) {
                next.generateTreeString(buffer, childrenPrefix + "戍式式 ", childrenPrefix + "弛   ");
            } else {
                next.generateTreeString(buffer, childrenPrefix + "戌式式 ", childrenPrefix + "    ");
            }
        }
    }
}
