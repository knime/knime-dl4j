package org.knime.ext.dl4j.testing.nodes.conversion;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "VectorConversionTester" Node.
 * 
 *
 * @author KNIME
 */
public class VectorConversionTesterNodeFactory 
        extends NodeFactory<VectorConversionTesterNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public VectorConversionTesterNodeModel createNodeModel() {
        return new VectorConversionTesterNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<VectorConversionTesterNodeModel> createNodeView(final int viewIndex,
            final VectorConversionTesterNodeModel nodeModel) {
        return new VectorConversionTesterNodeView(nodeModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new VectorConversionTesterNodeDialog();
    }

}

