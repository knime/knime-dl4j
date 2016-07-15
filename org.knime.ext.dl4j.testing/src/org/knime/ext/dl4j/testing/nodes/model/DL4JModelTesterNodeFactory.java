package org.knime.ext.dl4j.testing.nodes.model;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "DL4JModelTester" Node.
 *
 *
 * @author KNIME
 */
public class DL4JModelTesterNodeFactory extends NodeFactory<DL4JModelTesterNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public DL4JModelTesterNodeModel createNodeModel() {
        return new DL4JModelTesterNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<DL4JModelTesterNodeModel> createNodeView(final int viewIndex,
        final DL4JModelTesterNodeModel nodeModel) {
        return null;
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
        return new DL4JModelTesterNodeDialog();
    }

}
