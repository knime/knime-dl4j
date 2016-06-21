package org.knime.ext.dl4j.testing.conf;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.Assert;



public class ValidateConfiguration {
	
	/* Deep Belief Configuration Test
	 * deepbelief1_ref: config saved from deeplearning4j-example > DBNMnistFullExample (18.02.2016)
	 * deepbelief1_test: config rebuild and saved with dl4j integration (corresponds to res\deepbelief1.dl4j)
	 */
	@Test
	public void compareConfigsDeepBelief(){
		
		File refFile = new File("res\\deepbelief1_ref");
		File testFile = new File("res\\deepbelief1_test");
		
		try {
			compareConfigurations(refFile, testFile);
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

	}
	
	/* LeNet Configuration Test
	 * lenet1_ref: config saved from deeplearning4j-example > LenetMnistExample (18.02.2016)
	 * lenet1_test: config rebuild and saved with dl4j integration (corresponds to res\lenet.dl4j)
	 * 
	 */
	@Test
	public void compareConfigsLeNet(){
		
		File refFile = new File("res\\lenet1_ref");
		File testFile = new File("res\\lenet1_test");
		
		try {
			compareConfigurations(refFile, testFile);
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

	}
	
	private void compareConfigurations(File conf1, File conf2) throws IOException{
		List<String> linesConf1 = FileUtils.readLines(conf1);
		List<String> linesConf2 = FileUtils.readLines(conf2);
		
		Assert.assertEquals("Configurations have different number of lines.",linesConf1.size(),linesConf2.size());
		
		int i = 0;
		for(String line : linesConf1){
			Assert.assertEquals("Line number: " + i + "not equal. \n" + line + "\n" + linesConf2.get(i),line, linesConf2.get(i));
			i++;
		}
	}
}
