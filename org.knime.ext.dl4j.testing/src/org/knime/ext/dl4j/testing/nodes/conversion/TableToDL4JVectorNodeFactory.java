package org.knime.ext.dl4j.testing.nodes.conversion;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "DL4JModelTester" Node.
 *
 *
 * @author KNIME
 */
public class TableToDL4JVectorNodeFactory extends NodeFactory<TableToDL4JVectorNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public TableToDL4JVectorNodeModel createNodeModel() {
        return new TableToDL4JVectorNodeModel();
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
    public NodeView<TableToDL4JVectorNodeModel> createNodeView(final int viewIndex,
        final TableToDL4JVectorNodeModel nodeModel) {
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
        return new TableToDL4JVectorNodeDialog();
    }

}
