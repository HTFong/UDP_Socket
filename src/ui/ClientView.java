package ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import utils.Ceasar;


public class ClientView extends JFrame {
	private static final long serialVersionUID = 1L;
	private JFrame frame;
	private JTextField clientTypingBoard;
	private JList clientActiveUsersList;
	private JTextArea clientMessageBoard;
	private JButton clientSendMsgBtn, clientKillProcessBtn;

	DataInputStream inputStream;
	DataOutputStream outStream;
	DefaultListModel<String> dm;
	String id, secretKey, clientIds = "";
	

	public ClientView(String id, Socket s, String secretKey) { // constructor call, it will initialize required variables
		initialize(); // initialize UI components
		addEvents();
		this.id = id;
		this.secretKey = secretKey;
		try {
			frame.setTitle("[" + id + "][" + secretKey + "]--VIEW"); // set title of UI
			dm = new DefaultListModel<String>(); // where you can see data of clientIds Active
			clientActiveUsersList.setModel(dm); // show that list on JList
			inputStream = new DataInputStream(s.getInputStream()); // initialize input and output stream
			outStream = new DataOutputStream(s.getOutputStream());
			new ServerMsg().start(); // create a thread for reading msg from server
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void addEvents() {
		clientSendMsgBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String textAreaMessage = clientTypingBoard.getText();
				if (textAreaMessage == null || textAreaMessage.isEmpty()) {
					JOptionPane.showMessageDialog(frame, "Please type something");
					return;
				} // return do nothing if empty msg
				
				List<String> clientList = clientActiveUsersList.getSelectedValuesList(); // get the name of all id-selected on JList
				if (clientList.size() == 0) {
					JOptionPane.showMessageDialog(frame, "No user selected");
					return;
				}// return do nothing if dont choose user to sent msg
				
				try {
					String messageToBeSentToServer = "";
					for (String selectedUsr : clientList) { // append all the id-selected in JList
						clientIds += (clientIds.isEmpty()) ?  selectedUsr :  "," + selectedUsr; // condition
					}
					messageToBeSentToServer = secretKey + ":" + clientIds + ":" + Ceasar.xuliEncrypt(textAreaMessage, Integer.parseInt(secretKey)); // prepare message to be sent to server
					outStream.writeUTF(messageToBeSentToServer);// send to server: [secretKey][clientIds][msg]
					//
					//display UI
					//
					clientMessageBoard.append("<You sent msg to " + clientIds + ">" + textAreaMessage + "\n"); //show on messageBoard
					clientTypingBoard.setText(""); // clear typingBox
					clientIds = ""; // clear the all the client ids
					
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(frame, "User does not exist anymore."); // if user doesn't exist then show message
				}
			}
		});
		
		clientKillProcessBtn.addActionListener(new ActionListener() { // kill process event, but can connect again
			public void actionPerformed(ActionEvent e) {
				try {
					outStream.writeUTF("exit"); // send to server
					clientMessageBoard.append("You are disconnected now.\n");
					frame.dispose(); // close the frame 
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
	}

	class ServerMsg extends Thread {
		@Override
		public void run() {
			while (true) {
				try {
					String m = inputStream.readUTF();  // read message from server, this will contain :;.,/=<comma separated clientsIds>
					System.out.println("raw string inside thread : " + m); // print message for testing purpose
					String[] msgList = m.split(":",3); //filter
					for (String string : msgList) {
						System.out.println(string);
					}
					if (msgList[2].contains(";.,/=")) { // case update JList
						msgList[2] = msgList[2].substring(5); // cut prefix and get String of clientIds
						dm.clear(); // clear the list before inserting new elements
						StringTokenizer st = new StringTokenizer(msgList[2], ","); // split all id in the clientIds and add to dm below
						while (st.hasMoreTokens()) {
							String u = st.nextToken();
							if (!id.equals(u)) // no need to add current client id
								dm.addElement(u); // add all the active user ids to the defaultList to display on active user pane on ClientView UI
						}
					} else { // case print on the messageBoard
						clientMessageBoard.append("<"+msgList[1]+">"+Ceasar.xuliDecrypt(msgList[2], Integer.parseInt(msgList[0])) + "\n");
					}
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
			}
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() { // initialize all the components of UI
		frame = new JFrame();
		frame.setBounds(100, 100, 525, 339);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setTitle("Client View");

		clientMessageBoard = new JTextArea();
		clientMessageBoard.setEditable(false);
		clientMessageBoard.setBounds(12, 25, 303, 179);
		frame.getContentPane().add(clientMessageBoard);

		clientTypingBoard = new JTextField();
		clientTypingBoard.setHorizontalAlignment(SwingConstants.LEFT);
		clientTypingBoard.setBounds(12, 220, 303, 71);
		frame.getContentPane().add(clientTypingBoard);
		clientTypingBoard.setColumns(10);

		clientSendMsgBtn = new JButton("Send");
		
		clientSendMsgBtn.setBounds(353, 220, 112, 30);
		frame.getContentPane().add(clientSendMsgBtn);

		clientActiveUsersList = new JList();
		clientActiveUsersList.setToolTipText("Active Users");
		clientActiveUsersList.setBounds(353, 38, 112, 166);
		frame.getContentPane().add(clientActiveUsersList);

		clientKillProcessBtn = new JButton("Kill Process");
		
		clientKillProcessBtn.setBounds(353, 261, 112, 30);
		frame.getContentPane().add(clientKillProcessBtn);

		JLabel lblNewLabel = new JLabel("Active Users");
		lblNewLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblNewLabel.setBounds(353, 11, 95, 16);
		frame.getContentPane().add(lblNewLabel);

		ButtonGroup btngrp = new ButtonGroup();

		frame.setVisible(true);
	}
}

