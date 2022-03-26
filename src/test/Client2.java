package test;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Client2 {

	public static void main(String[] args) throws Exception {
		// Client
		while(true) {
			DatagramSocket socket = new DatagramSocket();
			
			String dataStr = "Client3 send";
			InetAddress serverIP = InetAddress.getLocalHost();
			DatagramPacket dataPacket = new DatagramPacket(dataStr.getBytes(),dataStr.length(),serverIP,8777);
			socket.send(dataPacket);
			socket.close();
			
		}
	}

}
