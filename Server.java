
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Date;
import java.text.SimpleDateFormat;

public class Server {
	public static void main(String[] args) {
		int PORT_NUMBER = Integer.valueOf(args[0]);
		ArrayList<ChatHandler> AllHandlers = new ArrayList<ChatHandler>();

		try {
			ServerSocket s = new ServerSocket(PORT_NUMBER);
			mkDir("Server");
			for (;;) {
				Socket incoming = s.accept();
				new ChatHandler(incoming, AllHandlers).start();
			}
		} catch (Exception e) {
			System.out.println(e);
		}
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

class ChatHandler extends Thread {
	ArrayList<ChatHandler> handlers;

	public ChatHandler(Socket i, ArrayList<ChatHandler> h) {
		incoming = i;
		handlers = h;
		handlers.add(this);
		try {
			in = new ObjectInputStream(incoming.getInputStream());
			out = new ObjectOutputStream(incoming.getOutputStream());
			os = incoming.getOutputStream();
		} catch (IOException ioe) {
			System.out.println("Could not create streams.");
		}
	}

	public synchronized void sendCoordinates() {

		ChatHandler left = null;
		for (ChatHandler handler : handlers) {

			if (handler == this)
				continue;

			ChatMessage2 cm = new ChatMessage2();
			cm.SetCoordinates(myObject.getX(), myObject.getY(), myObject.getPrevX(), myObject.getPrevY());
			cm.SetDrawing(myObject.isDrawing());
			try {
				handler.out.writeObject(cm);
			} catch (IOException ioe) {
				// one of the other handlers hung up
				left = handler; // remove that handler from the arraylist
			}
		}
		handlers.remove(left);
	}

	public synchronized void broadcast() {

		ChatHandler left = null;
		for (ChatHandler handler : handlers) {
			ChatMessage2 cm = new ChatMessage2();
			cm.setName(uname);
			cm.setMessage(myObject.getMessage());
			try {
				handler.out.writeObject(cm);
				if (cm.getMessage().equals("bye") != true) {
					if (handler.appending) {
						handler.aout.writeObject(cm);
					} else {
						handler.fout.writeObject(cm);
					}
				}
				System.out.println("Writing to handler outputstream: " + cm.getMessage());
			} catch (IOException ioe) {
				// one of the other handlers hung up
				left = handler; // remove that handler from the arraylist
			}
		}
		handlers.remove(left);

		if (myObject.getMessage().equals("bye")) { // my client wants to leave
			done = true;
			handlers.remove(this);
			System.out.println("Removed handler. Number of handlers: " + handlers.size());
		}
		System.out.println("Number of handlers: " + handlers.size());
	}

	public synchronized void broadcastfile(File work) {

		ChatHandler left = null;
		for (ChatHandler handler : handlers) {
			ChatMessage2 cm = new ChatMessage2();
			if( handler.equals(this))
			{
				cm.setMessage("Sending file...... ");
				cm.setName(uname);
				try{
				handler.out.writeObject(cm);
				}catch (IOException ioe) {
			}
			}
			else{
			
			cm.setName(uname);
			cm.setFile(work);
			cm.setMessage("You have received a file");
			try {
				handler.out.writeObject(cm);
				if (cm.getMessage().equals("bye") != true) {
					if (handler.appending) {
						handler.aout.writeObject(cm);
					} else {
						handler.fout.writeObject(cm);
					}
				}

				System.out.println("Writing to handler outputstream: " + cm.getMessage());
			} catch (IOException ioe) {
				// one of the other handlers hung up
				left = handler; // remove that handler from the arraylist
			}
		}}
		handlers.remove(left);

	}

	public synchronized void createfile() {
		try {
			File f = (File) in.readObject();

			BufferedReader br = new BufferedReader(new FileReader(f));

			String path = f.getAbsolutePath();
			String p = f.getName();
			long mybytearray = f.length();
			System.out.println("Size of file is " + mybytearray);
			File_Receive = "Server" + "/" + p;
			System.out.println(File_Receive);
			File work = new File(File_Receive);
			FileOutputStream fi = new FileOutputStream(work);
			ObjectOutputStream o = new ObjectOutputStream(fi);

			Object line;
			long current = 0;

			String lineStr;
			while ((lineStr = br.readLine()) != null) {
				
				o.write(lineStr.getBytes());
				o.flush();

			}

			broadcastfile(work);
			o.close();
			fi.close();

		} catch (IOException ioe) {
			// one of the other handlers hung up
			// left = handler; // remove that handler from the arraylist
		} catch (Throwable ioe) {
			System.out.println(ioe);
			// left = handler; // remove that handler from the arraylist
		}
	}

	
	public void send(String str) {
		try {
			out.writeObject(str);
			out.flush();
		} catch (IOException e) {
			System.out.println("I/O exception");
		}
	}

	public void adduser() throws IOException, ClassNotFoundException, EOFException, StreamCorruptedException {
		try {
			myObject = (ChatMessage2) in.readObject();
			uname = new String(myObject.getName());
			System.out.println("Newuser: " + uname);

			File f = new File(uname + ".txt");
			if (f.exists() && !f.isDirectory()) {
				aout = new AppendingObjectOutputStream(new FileOutputStream(uname + ".txt", true));
				appending = true;
				System.out.println("Created AppendingObjectOutputStream");
			} else {
				fout = new ObjectOutputStream(new FileOutputStream(uname + ".txt"));
				System.out.println("Created ObjectOutputStream");
			}
			SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			Date today = Calendar.getInstance().getTime();
			String reportDate = df.format(today);

			ChatMessage2 cm = new ChatMessage2();
			cm.setMessage(reportDate);
			cm.setName(uname);

			if (this.appending) {
				aout.writeObject(cm);
			} else {
				fout.writeObject(cm);
			}

			fin = new ObjectInputStream(new FileInputStream(uname + ".txt"));

			ChatMessage2 mc;

			while (true) {
				mc = (ChatMessage2) fin.readObject();
				this.out.writeObject(mc);
				// this.out.writeObject(mc);
				System.out.println("writing history message back to client\n");
			}
		} catch (EOFException eofe) {
			System.out.println("EOF Exception occurred...");
			fin.close();
		} catch (StreamCorruptedException sce) {
			System.out.println("Stream corrupted exception");
		}
	}

	public synchronized void announce() {

		ChatHandler left = null;
		for (ChatHandler handler : handlers) {
			if (handler == this)
				continue;
			ChatMessage2 cm = new ChatMessage2();
			cm.setName(uname);
			cm.setMessage("newuser");
			try {
				handler.out.writeObject(cm);
				System.out.println("Writing to handler outputstream: " + cm.getMessage());
			} catch (IOException ioe) {
				// one of the other handlers hung up
				left = handler; // remove that handler from the arraylist
			}
		}
		handlers.remove(left);
	}

	public synchronized void senduserlist() {

		ChatHandler left = null;
		for (ChatHandler handler : handlers) {
			if (handler == this)
				continue;
			ChatMessage2 cm = new ChatMessage2();
			cm.setName(handler.uname);
			cm.setMessage("olduser");
			try {
				this.out.writeObject(cm);
				System.out.println("Writing to handler outputstream: " + cm.getMessage());
			} catch (IOException ioe) {
				// one of the other handlers hung up
				left = handler; // remove that handler from the arraylist
			}
		}
		handlers.remove(left);
	}

	public void run() {
		try {

			adduser();
			announce();
			senduserlist();

			while (!done) {
				if (in.readObject().equals("file")) {
					System.out.println("hi");
					createfile();
					System.out.println("hi");
					//break;
				} else {
					myObject = (ChatMessage2) in.readObject();
					if (myObject.isDrawing() == false) {
						System.out.println("Message read: " + myObject.getMessage());
						broadcast();
					} else {
						sendCoordinates();
					}
				}
			}
		} catch (IOException e) {
			if (e.getMessage().equals("Connection reset")) {
				System.out.println("A client terminated its connection.");
			} else {
				System.out.println("Problem receiving: " + e.getMessage());
			}
		} catch (ClassNotFoundException cnfe) {
			System.out.println(cnfe.getMessage());
		} finally {
			handlers.remove(this);
		}
	}

	ChatMessage2 myObject = null;
	private Socket incoming;

	boolean done = false;

	ObjectOutputStream out;
	ObjectInputStream in;
	OutputStream os;
	String uname;
	private String File_Receive;
	ObjectInputStream fin;
	ObjectOutputStream fout;
	AppendingObjectOutputStream aout;
	boolean appending = false;

	Integer count;
}

class AppendingObjectOutputStream extends ObjectOutputStream {

	public AppendingObjectOutputStream(OutputStream out) throws IOException {
		super(out);
	}

	@Override
	protected void writeStreamHeader() throws IOException {
		// do not write a header, but reset:
		reset();
	}

}
