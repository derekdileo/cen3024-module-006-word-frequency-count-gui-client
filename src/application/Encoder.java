package application;

import java.io.UnsupportedEncodingException;

public class Encoder {

	/** An attempt to encode String array for transfer over client / server connection.
	 * @param strings user-entered URL, start, finish points
	 * @return byte array for transfer from client to server via Socket
	 * @throws UnsupportedEncodingException
	 */
	public byte[] encode(String[] strings) throws UnsupportedEncodingException {
		return String.join(",", strings).getBytes("UTF_8");
	}
	
	/** An attempt to decode byte array after transfer over client / server connection.
	 * @param encoded byte array which was transferred from client to server
	 * @return String array which contains user-entered URL, start, finish points to be used in WebScrape class
	 * @throws UnsupportedEncodingException
	 */
	public String[] decode(byte[] encodedArray) throws UnsupportedEncodingException {
		return new String(encodedArray, "UTF_8").split(",");
	} 
	
}
