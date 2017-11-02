/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   04.10.2016 (David Kolb): created
 */
package org.knime.ext.dl4j.libs.cuda;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.knime.ext.dl4j.libs.DL4JPluginActivator.BackendType;

/**
 * Class to check the installed CUDA Toolkit version.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class CudaVersionChecker {

    private static final Logger LOGGER = Logger.getLogger(CudaVersionChecker.class);

    private final static String[] NVCC_COMMAND = new String[]{"nvcc", "--version"};

    private final static String CUDA_8_0_REGEX = "Cuda\\scompilation\\stools.*release\\s8\\.0.*";

    private final static String CUDA_7_5_REGEX = "Cuda\\scompilation\\stools.*release\\s7\\.5.*";

    private CudaVersionChecker() {
        //util class
    }

    /**
     * Check the installed CUDA Toolkit version and returns the corresponding {@link BackendType}. This method tries to
     * run the command 'nvcc --version' and parses its output. If the command is not found the CUDA Toolkit probably
     * isn't installed on the system.
     *
     * @return the backend type corresponding to the found CUDA Toolkit version
     * @throws CudaNotFoundException if the command 'nvcc --version' could not be found
     * @throws UnsupportedCudaVersionException if the found CUDA Toolkit version does not match the supported CUDA
     *             versions
     */
    public static BackendType getCudaVersion() throws CudaNotFoundException, UnsupportedCudaVersionException {

        ProcessBuilder pb = new ProcessBuilder(NVCC_COMMAND);
        Process nvcc;

        try {
            nvcc = pb.start();
            nvcc.waitFor(10, TimeUnit.SECONDS);

            List<String> nvccOut = getLines(nvcc.getInputStream());
            LOGGER.debug(toString(nvccOut.toArray(new String[nvccOut.size()]), " "));

            return parseNVCCOut(nvccOut);
        } catch (IOException | InterruptedException e) {
            throw new CudaNotFoundException("The command: '" + toString(NVCC_COMMAND, " ") + "' could not be executed!",
                e);
        }

    }

    /**
     * Tries to parse the output of 'nvcc --version'. Checks each line if it matches the static regexes declared in this
     * class.
     *
     * @param lines 'nvcc --version' output
     * @return the found backend type
     * @throws UnsupportedCudaVersionException if no supported CUDA ersion could be found
     */
    private static BackendType parseNVCCOut(final List<String> lines) throws UnsupportedCudaVersionException {
        for (String line : lines) {
            if (line.matches(CUDA_7_5_REGEX)) {
                LOGGER.debug("'" + line + "' matches '" + CUDA_7_5_REGEX + "'");
                return BackendType.GPU_CUDA7_5;
            } else if (line.matches(CUDA_8_0_REGEX)) {
                LOGGER.debug("'" + line + "' matches '" + CUDA_8_0_REGEX + "'");
                return BackendType.GPU_CUDA8_0;
            }
        }
        LOGGER.debug("No cuda regex matches '" + NVCC_COMMAND[0] + " " + NVCC_COMMAND[1] + "' output.");
        throw new UnsupportedCudaVersionException("No compatible Cuda Version was found!");
    }

    private static List<String> getLines(final InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        List<String> out = new ArrayList<String>();

        String line;
        while ((line = reader.readLine()) != null) {
            out.add(line);
        }

        return out;
    }

    private static String toString(final String[] strings, final String delim) {
        String s = "";
        int i = 0;
        for (String string : strings) {
            s += string;
            if (i != strings.length - 1) {
                s += delim;
            }
            i++;
        }
        return s;
    }
}
