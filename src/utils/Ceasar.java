package utils;

public class Ceasar {
	public static String xuliEncrypt(String message, int key){
	    StringBuilder result = new StringBuilder();
	    for (char c : message.toCharArray()) {
	        	char newCharacter;
	        	newCharacter = (char) ((((Character.toUpperCase(c) - 'A') + key) % 26 + 26) % 26 + 'A');
	            if(c >= 'A' && c <= 'Z') {
	            	//do nothing
	            }else if(c >= 'a' && c <= 'z') {
	            	 newCharacter = Character.toLowerCase(newCharacter);
	            }else {
	            	 newCharacter = c;
	            }
	            result.append(newCharacter);
	    }
	    return result.toString();
	}
	public static String xuliDecrypt(String message, int key){
	    return xuliEncrypt(message,-key);
	}

}
