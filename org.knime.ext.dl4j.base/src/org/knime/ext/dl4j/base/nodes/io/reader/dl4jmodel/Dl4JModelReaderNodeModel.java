/*******************************************************************************
 * Copyright by KNIME GmbH, Konstanz, Germany
 * Website: http://www.knime.com; Email: contact@knime.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, Version 3, as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7:
 *
 * KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 * Hence, KNIME and ECLIPSE are both independent programs and are not
 * derived from each other. Should, however, the interpretation of the
 * GNU GPL Version 3 ("License") under any applicable laws result in
 * KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 * you the additional permission to use and propagate KNIME together with
 * ECLIPSE with only the license terms in place for ECLIPSE applying to
 * ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 * license terms of ECLIPSE themselves allow for the respective use and
 * propagation of ECLIPSE together with KNIME.
 *
 * Additional permission relating to nodes for KNIME that extend the Node
 * Extension (and in particular that are based on subclasses of NodeModel,
 * NodeDialog, and NodeView) and that only interoperate with KNIME through
 * standard APIs ("Nodes"):
 * Nodes are deemed to be separate and independent programs and to not be
 * covered works.  Notwithstanding anything to the contrary in the
 * License, the License does not apply to Nodes, you are not required to
 * license Nodes under the License, and you are granted a license to
 * prepare and propagate Nodes, in each case even if such Nodes are
 * propagated with or for interoperation with KNIME.  The owner of a Node
 * may freely choose the license terms applicable to such Node, including
 * when such Node is propagated with or for interoperation with KNIME.
 *******************************************************************************/
package org.knime.ext.dl4j.base.nodes.io.reader.dl4jmodel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.util.CheckUtils;
import org.knime.core.util.FileUtil;
import org.knime.ext.dl4j.base.AbstractDLNodeModel;
import org.knime.ext.dl4j.base.DLModelPortObject;
import org.knime.ext.dl4j.base.DLModelPortObjectSpec;
import org.knime.ext.dl4j.base.util.DLModelPortObjectUtils;

/**
 * Node to read a previously saved Deep Learning Model.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class Dl4JModelReaderNodeModel extends AbstractDLNodeModel {

    private SettingsModelString m_infile;

    private DLModelPortObjectSpec m_outSpec;

    /**
     * Constructor for the node model.
     */
    protected Dl4JModelReaderNodeModel() {
        super(new PortType[]{}, new PortType[]{DLModelPortObject.TYPE});
    }

    @Override
    protected DLModelPortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {

        try {
            //read spec only for configure
            m_outSpec = loadSpec();
        } catch (final Exception e) {
            throw new InvalidSettingsException(e.getMessage(), e);
        }
        return new DLModelPortObjectSpec[]{m_outSpec};
    }

    @Override
    protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
        CheckUtils.checkSourceFile(m_infile.getStringValue());

        final URL url = FileUtil.toURL(m_infile.getStringValue());
        final File file = FileUtil.getFileFromURL(url);

        final FileInputStream fileIn = new FileInputStream(file);
        final ZipInputStream zipIn = new ZipInputStream(fileIn);
        DLModelPortObject portObjectOnly;

        try {
            portObjectOnly = DLModelPortObjectUtils.loadPortFromZip(zipIn);
        } finally {
            zipIn.close();
            fileIn.close();
        }

        return new PortObject[]{
            new DLModelPortObject(portObjectOnly.getLayers(), portObjectOnly.getMultilayerLayerNetwork(), m_outSpec)};
    }

    @Override
    protected List<SettingsModel> initSettingsModels() {
        m_infile = Dl4JModelReaderNodeDialog.createFileModel();

        final List<SettingsModel> settings = new ArrayList<>();
        settings.add(m_infile);

        return settings;
    }

    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        final String fileName = settings.getString("dl4j_model_reader_file");

        if ((fileName == null) || (fileName.length() == 0)) {
            throw new InvalidSettingsException("No input file specified");
        }
        super.validateSettings(settings);
    }

    /**
     * Helper method to load only spec.
     *
     * @return the loaded spec
     * @throws Exception
     */
    private DLModelPortObjectSpec loadSpec() throws Exception {
        final String warning = CheckUtils.checkSourceFile(m_infile.getStringValue());
        if (warning != null) {
            throw new IOException("Unable to load Spec from file. Reason: " + warning);
        }

        final URL url = FileUtil.toURL(m_infile.getStringValue());
        final File file = FileUtil.getFileFromURL(url);

        final FileInputStream fileIn = new FileInputStream(file);

        final ZipInputStream zipIn = new ZipInputStream(fileIn);

        final DLModelPortObjectSpec spec = DLModelPortObjectUtils.loadSpecFromZip(zipIn);

        zipIn.close();
        fileIn.close();

        return spec;
    }

}
