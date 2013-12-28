package edu.unl.act.rma.firm.test;

import java.util.*;

public class DataGenerator {
	private static Random random;
	
	private static Random getRandom() {
		if (null == random) {
			random = new Random();
		}
		return random;
	}
	
	//boolean
	public static boolean getBoolean() {
		return getRandom().nextBoolean();
	}
	
	//double
	public static double getDouble() {
		return getRandom().nextDouble();
	}
	public static double getDouble(double min, double max) {
		if (max < min) {
			throw new IllegalArgumentException();
		}
		if (min == max) {
			return min;
		}
		return min + (getRandom().nextDouble() * (max - min));
	}
	public static double getDoubleGreaterThan(double min) {
		return getDouble(min, Double.MAX_VALUE);
	}
	public static double getDoubleLessThan(double max) {
		return getDouble(Double.MIN_VALUE, max);
	}
	
	//float
	public static float getFloat() {
		return getRandom().nextFloat();
	}
	public static float getFloat(float min, float max) {
		if (max < min) {
			throw new IllegalArgumentException();
		}
		if (min == max) {
			return min;
		}
		return min + (getRandom().nextFloat() * (max - min));
	}
	public static float getFloatGreaterThan(float min) {
		return getFloat(min, Float.MAX_VALUE);
	}
	public static float getFloatLessThan(float max) {
		return getFloat(Float.MIN_VALUE, max);
	}
	
	//int
	public static int getInt() {
		return getRandom().nextInt();
	}
	public static int getInt(int min, int max) {
		if (max < min) {
			throw new IllegalArgumentException();
		}
		if (min == max) {
			return min;
		}
		return min + getRandom().nextInt(max - min); //TODO: won't ever return max value
	}
	public static int getIntGreaterThan(int min) {
		return getInt(min, Integer.MAX_VALUE);
	}
	public static int getIntLessThan(int max) {
		return getInt(Integer.MIN_VALUE, max);
	}
	
	//long
	public static long getLong() {
		return getRandom().nextLong();
	}
	public static long getLong(long min, long max) {
		if (max < min) {
			throw new IllegalArgumentException();
		}
		if (min == max) {
			return min;
		}
		return min + (long)(getRandom().nextDouble() * (max - min));
	}
	public static long getLongGreaterThan(long min) {
		return getLong(min, Long.MAX_VALUE);
	}
	public static long getLongLessThan(long max) {
		return getLong(Long.MIN_VALUE, max);
	}
	
	//string
	private static final int typicalStringLength = 512;

	public static String getString() {
		return getString(typicalStringLength);
	}
	
	public static String getString(int maximumLength) {
		return getString(0, maximumLength);
	}
	
	public static String getString(int minimumLength, int maximumLength) {
		return getString("", "", minimumLength, maximumLength);
	}
	
	public static String getString(String charactersToExclude) {
		return getString(charactersToExclude.toCharArray());
	}
	
	public static String getString(char[] charactersToExclude) {
		return getString("", charactersToExclude, 0, typicalStringLength);
	}

	public static String getString(String originalString, String charactersToExclude, int minimumLength, int maximumLength) {
		return getString(originalString, charactersToExclude.toCharArray(), minimumLength, maximumLength);
	}
	
	public static String getString(String originalString, char[] charactersToExclude, int minimumLength, int maximumLength) {
		if (0 > minimumLength) {
			throw new IllegalArgumentException("The minimum  cannot be negative.");
		}
		if (maximumLength < minimumLength) {
			throw new IllegalArgumentException("The maximum length cannot be less than the minimum length.");
		}
		StringBuilder stringBuilder;
		if (null != originalString && 0 < originalString.length()) {
			stringBuilder = new StringBuilder(originalString);
		}
		else {
			stringBuilder = new StringBuilder();
		}
		int length = getInt(minimumLength, maximumLength);
		if (stringBuilder.length() < length) {
			for (int i = stringBuilder.length(); i < length; i++) {
				//TODO: need to do Unicode
				stringBuilder.append((char)getInt(32, 255));
				//stringBuilder.append((char)getRandom().nextInt(Integer.MAX_VALUE));
				//This doesn't seem to be producing Unicode as expected
			}
		}
		String returnString = stringBuilder.toString();
		if (null != charactersToExclude && 0 < charactersToExclude.length) {
			for (char characterToExclude : charactersToExclude) {
				 //TODO: implement a method overload that takes a valid chars input
				returnString = returnString.replace(characterToExclude, '_');
			}
		}

		return returnString;
	}
}