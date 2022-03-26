package ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SwingConstants;

public class LoginClient extends JFrame{

	private JFrame frame;
	private JTextField txtUserName;
	private int port = 8777;
	private JButton clientLoginBtn;
	private JTextField txtKey;
	private JLabel lblKey;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) { // main function which will make UI visible
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoginClient window = new LoginClient();
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
	public LoginClient() {
		initialize();
		addEvents();
	}
	
	/**
	 * Add Events
	 */
	private void addEvents() {
		clientLoginBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					String id = txtUserName.getText();
					String key = txtKey.getText().trim();
					if(id.trim().isEmpty()) {// constraint values which empty
						return;
					}
					
					Socket s = new Socket("localhost", port); // create a socket
					DataInputStream inputStream = new DataInputStream(s.getInputStream()); // create input and output stream
					DataOutputStream outStream = new DataOutputStream(s.getOutputStream());
					
					outStream.writeUTF(id); // send username to the output stream
					String msgFromServer = new DataInputStream(s.getInputStream()).readUTF(); // receive message on socket
					
					if(msgFromServer.equals("Username already taken")) {// namesake
						JOptionPane.showMessageDialog(frame,  "Username already taken\n");
					}else {
						new ClientView(id, s, key); // move to client UI
						frame.dispose();// close login UI
					}
				}catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		});
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 405, 186);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setTitle("Client Register");

		txtUserName = new JTextField();
		txtUserName.setBounds(120, 24, 231, 31);
		frame.getContentPane().add(txtUserName);
		txtUserName.setColumns(10);

		clientLoginBtn = new JButton("Connect");
		
		
		clientLoginBtn.setFont(new Font("Tahoma", Font.PLAIN, 17));
		clientLoginBtn.setBounds(230, 83, 121, 33);
		frame.getContentPane().add(clientLoginBtn);

		JLabel lblUserName = new JLabel("Username");
		lblUserName.setFont(new Font("Tahoma", Font.PLAIN, 17));
		lblUserName.setHorizontalAlignment(SwingConstants.RIGHT);
		lblUserName.setBounds(10, 26, 100, 29);
		frame.getContentPane().add(lblUserName);
		
		txtKey = new JTextField();
		txtKey.setColumns(10);
		txtKey.setBounds(120, 85, 58, 31);
		frame.getContentPane().add(txtKey);
		
		lblKey = new JLabel("Key");
		lblKey.setHorizontalAlignment(SwingConstants.RIGHT);
		lblKey.setFont(new Font("Tahoma", Font.PLAIN, 17));
		lblKey.setBounds(10, 87, 100, 29);
		frame.getContentPane().add(lblKey);
	}

	
}
