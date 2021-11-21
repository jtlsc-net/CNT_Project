import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

public class Server {

	

	private static final int sPort = 8000;   //The server will be listening on this port number
	public static byte[] header = "P2PFILESHARINGPROJ".getBytes();
	public static void main(String[] args) throws Exception {
		// File Stream for config files/sending file
		FileInputStream cfg_in;
		FileInputStream actual_file_in;
		//Variables related to file information
		String actual_file_name;
		int actual_file_size;
		int actual_file_piece_size;

		System.out.println("The server is running."); 
        ServerSocket listener = new ServerSocket(sPort);
		int clientNum = 1;
		
			// TODO: Make this not like 15 nested try loops.
			// Read config info from Common.cfg.
			// This includes name, size, and piece size.
			try{
				BufferedReader br = new BufferedReader(new FileReader("Common.cfg"));
				String[] line = new String[3];
				//TODO: expand for proper cfg file.
				for(int i = 0; i < 3; i++){
					line[i] = br.readLine();
				}
				
				//Put data from config into vars.
				actual_file_name = line[0].substring(9);
				actual_file_size = Integer.parseInt(line[1].substring(9));
				actual_file_piece_size = Integer.parseInt(line[2].substring(10));
				System.out.println("Name: " + actual_file_name + ", size: " + actual_file_size + ", piece: " + actual_file_piece_size);
				br.close();
			}
			catch(IOException ioReadException){
				System.out.println("Error while reading config file.");
				ioReadException.printStackTrace();
			}
        	try {
            		while(true) {
                		new Handler(listener.accept(),clientNum).start();
				System.out.println("Client "  + clientNum + " is connected!");
				clientNum++;
            			}
        	} finally {
            		listener.close();
        	} 
 
    	}

	/**
     	* A handler thread class.  Handlers are spawned from the listening
     	* loop and are responsible for dealing with a single client's requests.
     	*/
    	private static class Handler extends Thread {
        	private String message;    //message received from the client
			private byte[] inMSG;
			private Message rMSG;
		private String MESSAGE;    //uppercase message send to the client
		private Socket connection;
		private Boolean isValidHandshake = true;
        	//private ObjectInputStream in;	//stream read from the socket
        	//private ObjectOutputStream out;    //stream write to the socket
			private DataOutputStream out;
			private DataInputStream in;
			
			// FileOutputStream actual_file_out;
			BufferedWriter buffered_out;
		private int no;		//The index number of the client

			

        	public Handler(Socket connection, int no) {
            		this.connection = connection;
	    		this.no = no;
        	}

        public void run() {
 		try{
			//initialize Input and Output streams
			//out = new ObjectOutputStream(connection.getOutputStream());
			//out.flush();
			out = new DataOutputStream(connection.getOutputStream());
			//in = new ObjectInputStream(connection.getInputStream());
			in = new DataInputStream(connection.getInputStream());
			// actual_file_out = new FileOutputStream("output.dat");
			buffered_out = new BufferedWriter(new FileWriter("output.dat", true));
			byte[] get_msg = new byte[32];
			//Check Handshake
			for(int i = 0; i < 32; i++){
				get_msg[i] = in.readByte();
			}
			for(int z = 0; z < 18; z++){
				if (header[z] != get_msg[z]){
					isValidHandshake = false;
					//System.out.println("HANDSHAKE invalid");
				}
			}
			
			message = new String(get_msg, StandardCharsets.UTF_8);
			System.out.println("Receive message: " + message + " from client " + no);
			int msg_no = get_msg[31];
			System.out.println("Client is: " + msg_no);
			MESSAGE = message.toUpperCase();
			sendMessage(MESSAGE);
			// try{
				if(isValidHandshake) {
					while(true)
				{
					//receive the message sent from the client
					inMSG = new byte[100];
					int readLen = in.read(inMSG);
					rMSG = new Message(inMSG);
					//show the message to the user
					System.out.println("RLength: " + rMSG.getMsgLength());  //R
					System.out.println("RType: " + rMSG.getMsgType());
					System.out.println("Receive message: " + rMSG.getMsgPayload() + " from client " + no);
					buffered_out.write(rMSG.getMsgPayload());
					//Capitalize all letters in the message
//					MESSAGE = message.toUpperCase();
//					//send MESSAGE back to the client
//					sendMessage(MESSAGE);
					sendMessage(rMSG.getMessageInBytes());
				}
				} else System.out.println("Invalid Handshake prevented connection");
				
			// }
			
			// catch(ClassNotFoundException classnot){
			// 		System.err.println("Data received in unknown format");
			// 	}
		}
		catch(IOException ioException){
			System.out.println("Disconnect with Client " + no);
		}
		finally{
			//Close connections
			try{
				in.close();
				out.close();
				buffered_out.close();
				connection.close();
			}
			catch(IOException ioException){
				System.out.println("Disconnect with Client " + no);
			}
		}
	}

	//send a message to the output stream
	public void sendMessage(String msg)
	{
		try{
			out.writeBytes(msg);
			//out.writeUTF(msg);
			//out.writeByte('\n');
			out.flush();
			System.out.println("Send message: " + msg + " to Client " + no);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	void sendMessage(byte[] msg)
	{
		try{
			//stream write the message
			out.write(msg);
			out.flush();
//			System.out.println("After flush");
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}

    }

}