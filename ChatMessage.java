import java.io.*;

public class ChatMessage implements Serializable {
	public String name;
	public String message;
	public int type;
	private byte[] file;
	static final int SENDING_FILE = 3;
	public File myfile;

	public ChatMessage() {
	}

	public ChatMessage(String name, String message) {
		setName(name);
		setMessage(message);
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public ChatMessage(byte[] buffer) {
		file = buffer;
		type = SENDING_FILE;
	}

	public void setFile(File myfile) {
		this.myfile = myfile;
	}

	public File getFile() {
		return myfile;
	}

}