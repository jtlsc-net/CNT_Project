import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

public class Server {

	private static final int sPort = 8000;   //The server will be listening on this port number

	public static void main(String[] args) throws Exception {
		System.out.println("The server is running."); 
        	ServerSocket listener = new ServerSocket(sPort);
		int clientNum = 1;
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
		private String MESSAGE;    //uppercase message send to the client
		private Socket connection;
        	//private ObjectInputStream in;	//stream read from the socket
        	//private ObjectOutputStream out;    //stream write to the socket
			private DataOutputStream out;
			private DataInputStream in;
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
			byte[] get_msg = new byte[32];
			//Check Handshake
			for(int i = 0; i < 32; i++){
				get_msg[i] = in.readByte();
			}
			message = new String(get_msg, StandardCharsets.UTF_8);
			System.out.println("Receive message: " + message + " from client " + no);
			int msg_no = get_msg[31];
			System.out.println("Client is: " + msg_no);
			MESSAGE = message.toUpperCase();
			sendMessage(MESSAGE);
			// try{
				while(true)
				{
					//receive the message sent from the client
					get_msg[0] = in.readByte();
					message = new String(get_msg, StandardCharsets.UTF_8);
					//show the message to the user
					System.out.println("Receive message: " + message + " from client " + no);
					//Capitalize all letters in the message
					MESSAGE = message.toUpperCase();
					//send MESSAGE back to the client
					sendMessage(MESSAGE);
				}
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
			//out.writeByte('\n');
			out.flush();
			System.out.println("Send message: " + msg + " to Client " + no);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}

    }

}