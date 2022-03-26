package test;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Client {

	public static void main(String[] args) throws Exception {
		// Server
		while(true) {
			DatagramSocket socket = new DatagramSocket(8777);
			
			byte[] byteData = new byte[1024];
			
			DatagramPacket dataPacket = new DatagramPacket(byteData, byteData.length);
			socket.receive(dataPacket);
			System.out.println(new String(dataPacket.getData(),0,byteData.length));
			socket.close();
			Thread.sleep(1000);
		}
	}

}
