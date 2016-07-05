//package org.knime.ext.dl4j.testing.util;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.nio.charset.Charset;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.zip.ZipInputStream;
//import java.util.zip.ZipOutputStream;
//
//import org.apache.commons.io.FileUtils;
//import org.apache.commons.io.IOUtils;
//import org.deeplearning4j.models.embeddings.WeightLookupTable;
//import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
//import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
//import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
//import org.junit.Assert;
//import org.junit.Test;
//import org.knime.ext.textprocessing.dl4j.util.WordVectorPortObjectUtils;
//
//
//public class WordVectorPortObjectUtilsTest {
//
//	/** path to test file containing word vectors
//	 *  e.g.:
//	 *	been 0.003065010067075491 0.19051805138587952 ...
//	 *	year -0.201705202460289 0.13373790681362152 ...
//	 */
//	private final String WORDVEC_PATH = "res\\wordvec.txt";
//	
//	/**
//	 * Test for {@link WordVectorPortObjectUtils#loadWordVectors(ZipInputStream)}
//	 * method.
//	 * 
//	 * @throws IOException
//	 */
//	@Test
//	public void writeWordVectorsTest() throws IOException{
//		//read reference word vectors
//		//WordVectorSerializer.loadTxtVectors skips first line for any reason
//		File fileRef = new File(WORDVEC_PATH);
//		WordVectors wordVectorsRef = WordVectorSerializer.loadTxtVectors(fileRef);
//		
//		//write word vector reference to zip file
//		File fileSerialized = new File("res\\wordvec_serialized.zip");
//		writeToZip(wordVectorsRef, fileSerialized);
//		
//		//read raw txt from zip file
//		FileInputStream fileIn = new FileInputStream(fileSerialized);
//		ZipInputStream zipIn = new ZipInputStream(fileIn);
//		Assert.assertEquals("word_vectors",zipIn.getNextEntry().getName());		
//		List<String> linesSerialized = IOUtils.readLines(zipIn, Charset.forName("UTF-8"));
//		zipIn.close();
//		
//		//read raw txt from ref file
//		List<String> linesRef = FileUtils.readLines(fileRef, Charset.forName("UTF-8"));
//		
//		//linesRef.size() - 1 because first line is skipped in ref file
//		Assert.assertTrue(linesRef.size() == linesSerialized.size());
//		
//		//check if lines match
//		for(String line : linesSerialized){
//			Assert.assertTrue(line,linesRef.contains(line));
//		}		
//		
//		cleanUp(fileSerialized);
//	}
//	
//	/**
//	 * Test for {@link WordVectorPortObjectUtils#loadWordVectors(ZipInputStream)}
//	 * method.
//	 * 
//	 * @throws IOException
//	 */
//	@Test
//	public void loadWordVectorsTest() throws IOException{
//		//read reference word vectors
//		File fileRef = new File(WORDVEC_PATH);
//		
//		//deeplearning4j-nlp-0.4-rc3.9:
//		//WordVectorSerializer.loadTxtVectors skips first line when vector length is lower
//		//than 4 or contains no white spaces
//		WordVectors wordVectorsRef = WordVectorSerializer.loadTxtVectors(fileRef);
//		
//		//write word vector reference to zip file
//		File fileSerialized = new File("res\\wordvec_serialized.zip");
//		writeToZip(wordVectorsRef, fileSerialized);
//		
//		//read raw txt from zip file
//		FileInputStream fileIn = new FileInputStream(fileSerialized);
//		ZipInputStream zipIn = new ZipInputStream(fileIn);
//		WordVectors wordVectorDeserialized = WordVectorPortObjectUtils.loadWordVectors(zipIn);
//		
//		
//		compareVocabs(wordVectorsRef.vocab(), wordVectorDeserialized.vocab());
//		compareLookupTables(wordVectorsRef.lookupTable(), wordVectorDeserialized.lookupTable(), 
//				new ArrayList<String>(wordVectorsRef.vocab().words()));
//		compareLookupTables(wordVectorsRef.lookupTable(), wordVectorDeserialized.lookupTable(),
//				new ArrayList<String>(wordVectorDeserialized.vocab().words()));
//		zipIn.close();
//		cleanUp(fileSerialized);
//	}
//	
//	private void writeToZip(WordVectors vec, File location) throws IOException{
//		FileOutputStream fileOut = new FileOutputStream(location);
//		ZipOutputStream zipOut = new ZipOutputStream(fileOut);
//		WordVectorPortObjectUtils.writeWordVectors(vec, zipOut);
//		zipOut.close();
//	}
//	
//	private void cleanUp(File location) throws IOException{
//		FileUtils.forceDelete(location);
//	}
//	
//	/**
//	 * Checks main elements of two {@link WeightLookupTable}s if they are equal
//	 * using words from a corresponding vocabulary.
//	 * 
//	 * @param table1 first table
//	 * @param table2 second table, which we want to compare with the first one
//	 * @param vocab words corresponding to the {@link WeightLookupTable}s
//	 */
//	private void compareLookupTables(WeightLookupTable<?> table1, WeightLookupTable<?> table2, List<String> vocab){
//		//compare vector length
//		Assert.assertTrue(table1.layerSize() == table2.layerSize());
//		//compare word vectors
//		for(String word : vocab){
//			table1.vector(word).equals(table2.vector(word));
//		}
//	}
//	
//	/**
//	 * Checks main elements of two {@link VocabCache}s if they are equal.
//	 * 
//	 * @param vocab1 first vocab
//	 * @param vocab2 second vocab, which we want to compare with the first one
//	 */
//	private void compareVocabs(VocabCache<?> vocab1, VocabCache<?> vocab2){
//		List<String> vocWords1 = new ArrayList<String>(vocab1.words());
//		List<String> vocWords2 = new ArrayList<String>(vocab2.words());
//		
//		//compare sizes
//		Assert.assertTrue(vocWords1.size() == vocWords2.size());
//		Assert.assertTrue(vocab1.totalNumberOfDocs() == vocab2.totalNumberOfDocs());
//		Assert.assertTrue(vocab1.numWords() == vocab2.numWords());
//		Assert.assertTrue(vocab1.totalWordOccurrences() == vocab2.totalWordOccurrences());
//		
//		//compare words
//		for(String word : vocWords1){
//			Assert.assertTrue("Vocab2 does not contain word: " + word,vocWords2.contains(word));
//			Assert.assertTrue(vocab1.tokenFor(word).equals(vocab2.tokenFor(word)));
//			Assert.assertTrue(vocab1.wordFrequency(word) == vocab2.wordFrequency(word));			
//		}
//	}
//}
