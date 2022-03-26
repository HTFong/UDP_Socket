package ui;
import java.awt.EventQueue;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import utils.Ceasar;



public class ServerView {
	/*
	 * References: https://www.youtube.com/watch?v=rd272SCl-XE
	 * 			   https://www.youtube.com/watch?v=ZzZeteJGncY
	 * */

	private static final long serialVersionUID = 1L;

	private static Map<String, Socket> allUsersList = new ConcurrentHashMap<>(); // keeps the mapping of all the usernames used and their socket connections
	private static Set<String> activeUserSet = new HashSet<>(); // this set keeps track of all the active users 

	private static int port = 8777;
	private ServerSocket serverSocket;

	private JFrame frame;
	private JTextArea serverMessageBoard; 
	private JList allUserNameList;  
	private JList activeClientList; 
	private DefaultListModel<String> activeDlm = new DefaultListModel<String>(); // keeps list of active users for display on UI
	private DefaultListModel<String> allDlm = new DefaultListModel<String>(); // keeps list of all users for display on UI
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ServerView window = new ServerView();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ServerView() {
		initialize();
		try {
			serverSocket = new ServerSocket(port);
			serverMessageBoard.append("Client2 started on port: " + port + "\n" + "Waiting for the clients...\n");
			new ClientAccept().start(); // this will create a thread for client
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 526, 349);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setTitle("Client2 View");

		serverMessageBoard = new JTextArea();
		serverMessageBoard.setEditable(false);
		serverMessageBoard.setBounds(12, 29, 313, 236);
		frame.getContentPane().add(serverMessageBoard);

		allUserNameList = new JList();
		allUserNameList.setBounds(357, 167, 127, 98);
		frame.getContentPane().add(allUserNameList);

		activeClientList = new JList();
		activeClientList.setBounds(357, 34, 127, 85);
		frame.getContentPane().add(activeClientList);

		JLabel lblNewLabel = new JLabel("All Usernames");
		lblNewLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblNewLabel.setBounds(356, 140, 127, 16);
		frame.getContentPane().add(lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("Active Users");
		lblNewLabel_1.setBounds(357, 12, 98, 23);
		frame.getContentPane().add(lblNewLabel_1);
	}
	
	/**
	 * All thread
	 */
	class ClientAccept extends Thread {
		@Override
		public void run() {
			while (true) {
				try {
					Socket clientSocket = serverSocket.accept();
					String uName = new DataInputStream(clientSocket.getInputStream()).readUTF(); // read username(id) from client
					DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream()); // create an output stream for client
					if (activeUserSet != null && activeUserSet.contains(uName)) { // check the exist of uName in HashSet
						dos.writeUTF("Username already taken");
					} else {
						dos.writeUTF(""); // clear the existing message
						activeUserSet.add(uName); // add to activeUserSet
						allUsersList.remove(uName);// before update socket
						allUsersList.putIfAbsent(uName, clientSocket); // add to allUserList if its not exist
						activeDlm.addElement(uName); // add user to the active-user JList
						if (!allDlm.contains(uName)) allDlm.addElement(uName); // namesake
						activeClientList.setModel(activeDlm); // add list active-user to JList
						allUserNameList.setModel(allDlm); // add list all-user to JList
						serverMessageBoard.append("Client " + uName + " Connected...\n"); // print message on server-messageBoard
						new MsgRead(clientSocket, uName).start(); // create a thread to read messages
						new PrepareCLientList().start(); //create a thread to update all the active clients
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	private String countString(String str) {
		StringBuilder rs = new StringBuilder();
		char[] s = str.toUpperCase().toCharArray();
		int count = 0;
		for (int i = 65; i <= 90; i++) {
			count = 0;
			for (char c : s) {
				if((char)i == c) {
					count++;
				}
			}
			if(count!=0) {
				rs.append("["+(char)i +"]="+count+",");
			}
		}
		return rs.toString();
	}

	class MsgRead extends Thread { // this class reads the messages coming from client and take appropriate actions
		Socket s;
		String Id;
		private MsgRead(Socket s, String uname) { // socket and username will be provided by client
			this.s = s;
			this.Id = uname;
		}

		@Override
		public void run() {
			while (allUserNameList != null && !allUsersList.isEmpty()) {  // if data in allUserList is not empty then proceed further
				try {
					String message = new DataInputStream(s.getInputStream()).readUTF(); // read message from client
					//System.out.println("message read ==> " + message); // testing
					String[] msgList = message.split(":",3);// appended [secretKey][clientIds][msg]
					if (msgList[0].equalsIgnoreCase("exit")) { // if a client's process is killed then notify other clients
						activeUserSet.remove(Id); // remove that client
						new PrepareCLientList().start(); // update the active and all user list on UI

						Iterator<String> itr = activeUserSet.iterator(); // iterate over other active users
						while (itr.hasNext()) {
							String usrName2 = (String) itr.next();
							if (!usrName2.equalsIgnoreCase(Id)) { // we don't need to send this message to ourself
								new DataOutputStream(((Socket) allUsersList.get(usrName2)).getOutputStream())
								.writeUTF(0+ ":" + "Server"+ ":" + Id + " disconnected..."); // send to client: to all
								new PrepareCLientList().start(); // update the active user list for every client after a user is disconnected
							}
						}
						activeDlm.removeElement(Id); // remove client from Jlist for server
						activeClientList.setModel(activeDlm); //update the active user list
						serverMessageBoard.append(Id + " disconnected....\n"); // print message on server message board
					} else {
						String[] sendToList = msgList[1].split(","); // save userList, gonna sent futher
						for (String usr : sendToList) { // for every user send message
							if (activeUserSet.contains(usr)) { // check again if user is active then send the message
								new DataOutputStream(((Socket) allUsersList.get(usr)).getOutputStream())
										.writeUTF(msgList[0]+ ":" + Id + ":" + msgList[2]); // send to client: user's receiver 
							}
						}
						new DataOutputStream(((Socket) allUsersList.get(Id)).getOutputStream())
						.writeUTF(0+ ":Server:" + countString(Ceasar.xuliDecrypt(msgList[2], Integer.parseInt(msgList[0])))); // send to client: user's request
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	class PrepareCLientList extends Thread { // it prepares the list of active user to be displayed on the UI
		@Override
		public void run() {
			try {
				String ids = "";
				Iterator<String> itr = activeUserSet.iterator(); // iterate over all active users
				while (itr.hasNext()) { // prepare string of all the users
					String key = itr.next();
					ids += key + ",";
				}
				if (ids.length() != 0) {
					ids = ids.substring(0, ids.length() - 1);// reformat
				}
				itr = activeUserSet.iterator(); 
				while (itr.hasNext()) {
					String key = (String) itr.next();
					new DataOutputStream(((Socket) allUsersList.get(key)).getOutputStream())
					.writeUTF(0+ ":" + "Server"+ ":" + ";.,/=" + ids); // send to client: send the list of active users with identifier prefix ;.,/=
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
