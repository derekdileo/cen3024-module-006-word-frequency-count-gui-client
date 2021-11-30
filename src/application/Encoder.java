package application;

import java.io.UnsupportedEncodingException;

public class Encoder {

	/**
	 * @param strings
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static byte[] encode(String[] strings) throws UnsupportedEncodingException {
		return String.join(",", strings).getBytes("UTF_8");
	}
	
	/**
	 * @param encodedArray
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String[] decode(byte[] encodedArray) throws UnsupportedEncodingException {
		return new String(encodedArray, "UTF_8").split(",");
	} 
	
}
