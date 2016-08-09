package org.knime.ext.dl4j.testing.nodes.conversion.pretraining;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "DL4JModelTester" Node.
 *
 *
 * @author KNIME
 */
public class PretrainingInputToDL4JVectorNodeFactory extends NodeFactory<PretrainingInputToDL4JVectorNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public PretrainingInputToDL4JVectorNodeModel createNodeModel() {
        return new PretrainingInputToDL4JVectorNodeModel();
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
    public NodeView<PretrainingInputToDL4JVectorNodeModel> createNodeView(final int viewIndex,
        final PretrainingInputToDL4JVectorNodeModel nodeModel) {
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
        return new PretrainingInputToDL4JVectorNodeDialog();
    }

}
