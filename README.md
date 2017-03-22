# ![Image](https://www.knime.org/files/knime_logo_github_40x40.png) KNIME® - Deeplearning4J Integration

[KNIME Analytics Platform - Deeplearning4J Integration](http://tech.knime.org/deeplearning4j) allows to use deep neural networks in KNIME. The extension consists of a set of new nodes which allow to modularly assemble a deep neural network architecture, train the network on data, and use the trained network for predictions.

![Image](https://abload.de/img/dl4j_readme_imgo2z71.png)

The integration is based on [Deeplearning4J](https://deeplearning4j.org/), which is an open-source Deep Learning library running on the JVM. 

_Please note: The KNIME - Deeplearning4J Integration is contained in [KNIME Labs](http://tech.knime.org/knime-labs)._

### Content
This repository contains the source code of KNIME - Deeplearning4J Integration. The code is organized as follows:

* _org.knime.ext.dl4j.base_: Deeplearning4J Integration nodes
* _org.knime.ext.dl4j.bin.*_: CPU/GPU backend fragments
* _org.knime.ext.dl4j.libs_: DL4J Library, dependencies, and OSGi plugin activator
* _org.knime.ext.dl4j.testing_ : Deeplearning4J Integration testing nodes

### Additional Extensions
The KNIME - Deeplearning4J Integration contains two additional extensions for text- and image processing:

* [KNIME Analytics Platform - Deeplearning4J Text Processing Extension](https://tech.knime.org/deeplearning4j-textprocessing):
Provides nodes to train and use Word Vector Models (Word2Vec & Doc2Vec).
* [KNIME Analytics Platform - Deeplearning4J Image Processing Extension](https://tech.knime.org/deeplearning4j-imageprocessing):
Provides converters which allow to use images from [KNIME Analytics Platform - Image Processing Extension](http://tech.knime.org/community/image-processing) as input for deep networks. See project repository on [GitHub](https://github.com/knime-ip/knip-deeplearning4j).

### Development
Instructions for how to work with our code or develop extensions for KNIME Analytics Platform can be found in the _knime-sdk-setup_ repository on [BitBucket](https://bitbucket.org/KNIME/knime-sdk-setup) or [GitHub](http://github.com/knime/knime-sdk-setup).

### Join the Community!
* [KNIME Forum](https://tech.knime.org/forum)




