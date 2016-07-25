package org.knime.ext.dl4j.testing.nodes.conversion.regression;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "DL4JModelTester" Node.
 *
 *
 * @author KNIME
 */
public class RegressionInputToDL4JVectorNodeFactory extends NodeFactory<RegressionInputToDL4JVectorNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public RegressionInputToDL4JVectorNodeModel createNodeModel() {
        return new RegressionInputToDL4JVectorNodeModel();
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
    public NodeView<RegressionInputToDL4JVectorNodeModel> createNodeView(final int viewIndex,
        final RegressionInputToDL4JVectorNodeModel nodeModel) {
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
        return new RegressionInputToDL4JVectorNodeDialog();
    }

}
