/**
 * 
 */
package edu.unl.act.rma.firm.test;

import java.io.File;
import java.util.logging.Logger;

import junit.framework.Assert;

/**
 * @author Jon Dokulil
 *
 */
public class SupportMethods {
	
	public static void assertToStringFormat(String s) {
		assertToStringFormat(s, 0);
	}
	
	public static void assertToStringFormat(String s, int expectedNumberOfPairs) {
		assertToStringFormat(s, expectedNumberOfPairs, new String[] {});
	}
	
	public static void assertToStringFormat(String s, int expectedNumberOfPairs, String[] expectedValues) {
		String[][] expectedNamesAndValues = null;
		if (null != expectedValues) {
			expectedNamesAndValues = new String[expectedValues.length][2];
			for (int i = 0; i < expectedValues.length; i++) {
				expectedNamesAndValues[i][0] = null;
				expectedNamesAndValues[i][1] = expectedValues[i];
			}
		}
		assertToStringFormat(s, expectedNumberOfPairs, expectedNamesAndValues);
	}
	
	public static void assertToStringFormat(String s, int expectedNumberOfPairs, String[][] expectedNamesAndValues) {
		if (s == null || s.equals("")) {
			Assert.assertTrue("empty string", 1 > expectedNumberOfPairs);
			Assert.assertTrue("empty string", 1 > expectedNamesAndValues.length);
			return;
		}
		logInfo(s);
		String[] pairs = s.split(",");
		if (0 < expectedNumberOfPairs) {
			Assert.assertEquals("number of name/value pairs", expectedNumberOfPairs, pairs.length);
		}
		if (null != expectedNamesAndValues &&
			0 < expectedNamesAndValues.length) {
			Assert.assertEquals("1st dimension of expected name/value pairs", expectedNamesAndValues.length, pairs.length);
		}
		for (int i = 0; i < pairs.length; i++) {
			Assert.assertTrue("name/value pair contains '=' char", 0 < pairs[i].indexOf('='));
			String[] pair = pairs[i].split("=");
			Assert.assertTrue("name/value pair contains at most 1 '=' char", 3 > pair.length);
			Assert.assertNotSame("name is not empty", "", pair[0]);
			if (null != expectedNamesAndValues &&
				i < expectedNamesAndValues.length &&
				null != expectedNamesAndValues[i]) {
				if (null != expectedNamesAndValues[i][0]) {
					Assert.assertEquals("name", expectedNamesAndValues[i][0], pair[0]);
				}
				if (null != expectedNamesAndValues[i][1]) {
					Assert.assertEquals("value", expectedNamesAndValues[i][1], (2 == pair.length ? pair[1] : ""));
				}
			}
		}
	}
	
	public static Logger getTestLog() {
		return Logger.getLogger("edu.unl.firm.test");
	}
	
	public static void logInfo(String message) {
		SupportMethods.getTestLog().info(message);
	}
	
	public static String getTemporaryTestDirectory() {
		if (null == temporaryTestDirectory) {
			File defaultDirectory = new File(System.getProperty("java.io.tmpdir"));
			temporaryTestDirectory = defaultDirectory.getAbsolutePath()+ "\\FIRM_TEST_TEMP";
			if (!new File(temporaryTestDirectory).exists()) {
				if (!new File(temporaryTestDirectory).mkdir()) {
					temporaryTestDirectory = null;
				}
			}
		}
		return temporaryTestDirectory;
	}
	private static String temporaryTestDirectory = null;
	
	public static void deleteTemporaryTestDirectory() {
		if (new File(getTemporaryTestDirectory()).exists()) {
			SupportMethods.deletePath(getTemporaryTestDirectory());
		}
	}
	
	public static boolean deletePath(String path) {
		System.out.println(path);
		File directory = new File(path);
		if (directory.exists() && directory.isDirectory()) {
		    String[] contents = directory.list();
		    for (int i=0; i < contents.length; i++) {
		        if (!deletePath(path + "\\" + contents[i])) {
		            return false;
		        }
		    }
		}
		return directory.delete();
	}
}