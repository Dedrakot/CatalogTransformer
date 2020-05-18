package org.dedrakot.saxon;

import net.sf.saxon.s9api.ExtensionFunction;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.OccurrenceIndicator;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SequenceType;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

import java.util.Set;

public class NodeContentBanFunction implements ExtensionFunction {
    private final Set<String> bannedSet;
    private int counter;

    public NodeContentBanFunction(Set<String> bannedSet) {
        this.bannedSet = bannedSet;
    }

    public int getCounter() {
        return counter;
    }

    @Override
    public QName getName() {
        return new QName("remover", "isAllowed");
    }

    @Override
    public SequenceType getResultType() {
        return SequenceType.makeSequenceType(ItemType.BOOLEAN, OccurrenceIndicator.ONE);
    }

    @Override
    public SequenceType[] getArgumentTypes() {
        return new SequenceType[]{SequenceType.makeSequenceType(ItemType.ANY_NODE, OccurrenceIndicator.ONE)};
    }

    @Override
    public XdmValue call(XdmValue[] arguments) {
        XdmNode node = (XdmNode) arguments[0].itemAt(0);
        String nodeText = node.getStringValue();
        boolean banned = bannedSet.contains(nodeText);
        if (banned) {
            counter++;
        }
        return XdmValue.makeValue(!banned);
    }
}
