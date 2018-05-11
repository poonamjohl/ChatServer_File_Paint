import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.List;
import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

public class Client extends Thread implements ActionListener, ItemListener {
	static int ConnectPort;
	ChatMessage2 myObject;
	private String STORED_FILE;
	ObjectInputStream in;
	private String FILE_TO_RECEIVED;
	boolean sendingdone = false, receivingdone = false;
	Scanner scan;
	Socket socketToServer;
	ObjectOutputStream myOutputStream;
	ObjectInputStream myInputStream;
	static String IDENTIFY_NUMBER;
	Frame f;
	TextField tf, tfuname;
	String username;
	TextArea ta;
	Panel topPanel;
	Button SendFile;
	Button ConnectButton, DisconnectButton;
	List tlist;
	SimplePaint sp;
	Panel centerPanel;

	public Client() {

		f = new Frame();
		f.setSize(300, 400);
		f.setTitle("Chat Client");
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});

		tfuname = new TextField();
		topPanel = new Panel();
		ConnectButton = new Button("Connect");
		DisconnectButton = new Button("Disconnect");
		SendFile = new Button("Send File");
		DisconnectButton.setEnabled(false);
		SendFile.setEnabled(false);
		topPanel.add(tfuname);
		topPanel.add(ConnectButton);
		topPanel.add(DisconnectButton);
		topPanel.add(SendFile);
		topPanel.setLayout(new GridLayout());
		tfuname.addActionListener(this);
		ConnectButton.addActionListener(this);
		DisconnectButton.addActionListener(this);
		SendFile.addActionListener(this);
		tlist = new List();
		tlist.add("All");
		tlist.addActionListener(new MyListActionListener());
		tlist.addItemListener(this);

		tf = new TextField();
		tf.setEnabled(false);
		tf.addActionListener(this);
		f.add(tf, BorderLayout.SOUTH);
		ta = new TextArea();
		sp = new SimplePaint(this);
		centerPanel = new Panel();
		centerPanel.add(ta);
		centerPanel.add(sp);
		centerPanel.setLayout(new GridLayout(2, 1));

		f.add(topPanel, BorderLayout.NORTH);
		// f.add(ta, BorderLayout.CENTER);
		f.add(centerPanel, BorderLayout.CENTER);
		f.add(tlist, BorderLayout.EAST);
		f.pack();
		f.setVisible(true);
	}

	public void actionPerformed(ActionEvent ae) {
		Object src = ae.getSource();
		if (src == ConnectButton) {
			try {
				socketToServer = new Socket("localhost", ConnectPort);
				myOutputStream = new ObjectOutputStream(socketToServer.getOutputStream());
				myInputStream = new ObjectInputStream(socketToServer.getInputStream());
				start();
				tf.setEnabled(true);
				DisconnectButton.setEnabled(true);
				SendFile.setEnabled(true);
				ConnectButton.setEnabled(false);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		} else if (src == tf) {
			myObject = new ChatMessage2();
			myObject.setMessage(tf.getText());
			tf.setText("");
			try {
				// myOutputStream.reset();
				myOutputStream.writeObject(myObject);
				myOutputStream.writeObject(myObject);
			} catch (IOException ioe) {
				System.out.println(ioe.getMessage());
			}
		} else if (src == DisconnectButton) {
			try {

				myObject = new ChatMessage2();
				SendFile.setEnabled(false);
				// ConnectButton.setEnabled(true);
				myObject.setMessage("bye");
				myOutputStream.writeObject(myObject);
			} catch (IOException ioe) {
				System.out.println(ioe.getMessage());
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		} else if (src == SendFile) {
			JFileChooser chooser = new JFileChooser();
			int returnVal = chooser.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				// File myFile = new File( chooser.getSelectedFile().getAbsolutePath() );
				File myFile = chooser.getSelectedFile();

				try {
					BufferedReader in = new BufferedReader(new FileReader(myFile));
					String line = null;
					myObject = new ChatMessage2();
					myOutputStream.writeObject("file");
					myOutputStream.writeObject(myFile);
					
					myOutputStream.flush();


				} catch (IOException ex) {
					System.err.println("Open plaintext error: " + ex);
				}

				catch (Exception e) {
					System.out.println(e.getMessage());
				}

			}
			return;
		}
	}

	class MyListActionListener implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			List list = (List) (ae.getSource());
			if (list == tlist) {
				// DO Something...
			}
		}
	}

	public void itemStateChanged(ItemEvent e) {
		List list = (List) (e.getItemSelectable());
		// DO something...
	}

	public void run() {
		System.out.println("Listening for messages from server . . . ");
		try {
			myObject = new ChatMessage2();
			myObject.setName(tfuname.getText());
			username = new String(tfuname.getText());
			myOutputStream.writeObject(myObject);

			while (!receivingdone) {

				Object obj = myInputStream.readObject();
				myObject = (ChatMessage2) obj;

				if (myObject != null && myObject.getFile() != null) {
					ta.append(myObject.getName() + "> " + myObject.getMessage() + "\n");
					System.out.println("inside file equals flow");
					// STORED_FILE = (String)myInputStream.readObject();
					STORED_FILE = myObject.getFile().getName();
					// create file to received path;
					FILE_TO_RECEIVED = IDENTIFY_NUMBER + "/" + STORED_FILE;

					FileOutputStream f = new FileOutputStream(new File(FILE_TO_RECEIVED));
					ObjectOutputStream o = new ObjectOutputStream(f);
					//long fileLength = (Long) myInputStream.readObject();
					// int bytesRead = 0;
					// int[] total=new int[1];
					String line;
					BufferedReader br = new BufferedReader(new FileReader(myObject.getFile()));
					try {
						while ((line = br.readLine()) != null) {
							o.write(line.getBytes());
						}
						o.close();
						f.close();
					} catch (IOException is) {
					}

				} else {
					// System.out.println("printing error content" + obj);
					myObject = (ChatMessage2) obj;
					if (!myObject.isDrawing()) {
						if (myObject.getMessage() == null && !(username.equals(myObject.getName()))) {
							continue;
						} else if (myObject.getMessage().equals("newuser") || myObject.getMessage().equals("olduser")) {
							tlist.add(myObject.getName());
						} else if (myObject.getMessage().equals("bye") && !(username.equals(myObject.getName()))) {
							System.out.println(username + " removing : " + myObject.getName());
							tlist.remove(myObject.getName());
						} else if (myObject.getMessage().equals("bye") && username.equals(myObject.getName())) {
							System.out.println("Client : " + username + " has left the chatroom");
							receivingdone = true;
						} 
						  else if(myObject.getMessage().equals("Sending file") )
						  {
							  ta.append(myObject.getName() + "> " + myObject.getMessage() + "\n");

						  }
						else {
							ta.append(myObject.getName() + "> " + myObject.getMessage() + "\n");
						}
					} else {
						if (sp.graphicsForDrawing == null) {
							sp.setUpDrawingGraphics();
						}
						sp.graphicsForDrawing.drawLine(myObject.getPrevX(), myObject.getPrevY(), myObject.getX(),
								myObject.getY()); // Draw the line.
					}
				}
			}
			// myInputStream.close();
		} catch (IOException ioe) {
			System.out.println("IOE: " + ioe.getMessage());
		} catch (ClassNotFoundException cnf) {
			System.out.println(cnf.getMessage());
		} finally {
		}
	}

	public static void main(String[] arg) {
		IDENTIFY_NUMBER = arg[0];
		ConnectPort = Integer.valueOf(arg[1]);
		Client c = new Client();
		mkDir(IDENTIFY_NUMBER);

	}

	private static boolean mkDir(String name) {
		File dir = new File(name);
		if (dir.exists()) {
			return false;
		}
		if (dir.mkdir()) {
			return true;
		} else {
			return false;
		}
	}
}
