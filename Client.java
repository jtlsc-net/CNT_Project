import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.nio.charset.StandardCharsets; // for UTF-8

public class Client {
	Socket requestSocket;           //socket connect to the server
	//ObjectOutputStream out;         //stream write to the socket
	DataOutputStream out;
 	//ObjectInputStream in;          //stream read from the socket
	DataInputStream in;
	String message;                //message send to the server
	String MESSAGE;                //capitalized message read from the server
    String type;                   //type of msg
    int byte_length;               //number of bytes to store string
    String byte_length_string;     //string for no. of bytes
	byte[] handshake_message = new byte[32];	   //array for handshake
	byte[] speed;
	byte[] ran;

    public void Client() {}
    
    void run()
    {
        try{
			//create a socket to connect to the server
			requestSocket = new Socket("localhost", 8000);
			System.out.println("Connected to localhost in port 8000");
			//initialize inputStream and outputStream
			//out = new ObjectOutputStream(requestSocket.getOutputStream());
			//out.flush();
			out = new DataOutputStream(requestSocket.getOutputStream());
			//in = new ObjectInputStream(requestSocket.getInputStream());
			in = new DataInputStream(requestSocket.getInputStream());
			
			//get Input from standard input
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			//handshake_message = "P2PFILESHARINGPROJ".getBytes() + {0,0,0,0,0,0,0,0,0,0} + {1,1,1,1};
			speed = "P2PFILESHARINGPROJ".getBytes();
			int P2Pbytelength = "P2PFILESHARINGPROJ".getBytes().length;
			System.out.println(P2Pbytelength + " <- this is the length");
			for(int z = 0; z < 18; z++)
			{
				handshake_message[z] = speed[z];
			}
			for(int j = P2Pbytelength; j < 28; j++)
			{
				handshake_message[j] = 0;
			}
			for(int k = 28; k < 32; k++)
			{
				handshake_message[k] = 1;
			}
			String q = new String(handshake_message, StandardCharsets.UTF_8);
			System.out.println("Before sendMessage.");
			sendMessage(q);
			//MESSAGE = (String)in.readUTF();
			byte[] rec_msg = new byte[32];
			for(int i = 0; i < 32; i++){
				rec_msg[i] = in.readByte();
			}
			MESSAGE = new String(rec_msg, StandardCharsets.UTF_8);
			System.out.println("After MESSAGE");
			//show the message to the user
			System.out.println("Receive message: " + MESSAGE);
			
			while(true)
			{
				System.out.print("Hello, please input a sentence: ");
				//read a sentence from the standard input
				message = bufferedReader.readLine();
                //get msg type
                System.out.print("Please enter type: ");
                type = bufferedReader.readLine();
                //insert type at start of string
                message = type + message;
                //get bytelength of string  NOTE: this currently includes type field.  Unsure if this is correct or not.
                byte_length = message.getBytes().length;
                //insert byte no.
                byte_length_string = String.valueOf(byte_length);
                message = byte_length_string + message;
				//Send the sentence to the server
				sendMessage(message);
				//Receive the upperCase sentence from the server
				MESSAGE = (String)in.readUTF();
				//show the message to the user
				System.out.println("Receive message: " + MESSAGE);
			}
		}
		catch (ConnectException e) {
    			System.err.println("Connection refused. You need to initiate a server first.");
		} 
		// catch ( ClassNotFoundException e ) {
        //     		System.err.println("Class not found");
        // 	} 
		catch(UnknownHostException unknownHost){
			System.err.println("You are trying to connect to an unknown host!");
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
		finally{
			//Close connections
			try{
				in.close();
				out.close();
				requestSocket.close();
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
	}
	//send a message to the output stream
	void sendMessage(String msg)
	{
		try{
			//stream write the message
			out.writeBytes(msg);
			//out.writeByte('\n');
			out.flush();
			System.out.println("After flush");
			//out.flush();
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	//main method
	public static void main(String args[])
	{
		Client client = new Client();
		client.run();
	}
}