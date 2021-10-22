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

	// File Stream for config files/sending file
	FileInputStream actual_file_in;
	//Variables related to file information
	String actual_file_name;
	int actual_file_size;
	int actual_file_piece_size;

	String message;                //message send to the server
	String MESSAGE;                //capitalized message read from the server
    String type;                   //type of msg
    int byte_length;               //number of bytes to store string
    String byte_length_string;     //string for no. of bytes
	int[] peer; 				   // stores a list of neighboring peers
	byte[] header = "P2PFILESHARINGPROJ".getBytes(); //constant for Handshake header
	byte[] handshake_message = new byte[32];	   //array for handshake
	byte[] speed;
	byte[] ran;
	byte[] incomingMessage = new byte[100];
	Message msg;
	Message inMSG;
	Boolean isValidHandshake = true;

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
			for(int z = 0; z < 18; z++){
				if (header[z] != rec_msg[z]){
					isValidHandshake = false;
				}
			}
			MESSAGE = new String(rec_msg, StandardCharsets.UTF_8);
			//show the message to the user
			System.out.println("Receive message: " + MESSAGE);
			
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
			actual_file_in = new FileInputStream(actual_file_name);
			//Actual message sending loop.
			int user_piece_index = 0;
			byte[] byte_piece = new byte[actual_file_piece_size];
			byte baby_byte;
			if (isValidHandshake) {
				while(true)
			{
				//New Implementation w/ Message Class
				//clear out byte array for reading.
				for(int i = 0; i < actual_file_piece_size; i++){
					byte_piece[i] = 0;
				}
				System.out.print("Hello, please input message type (integer): ");
				int msgType = Integer.parseInt(bufferedReader.readLine());
				if (msgType >= 0 && msgType <= 3)
				{
					msg = new Message(msgType);
				}
				else if(msgType == 4)
				{
					break;
				}
				else if(msgType == 5)
				{
					break;
				}
				else if(msgType == 6)
				{
					break;
				}
				else if(msgType == 7){
					System.out.print("Hello, please input piece index field number (integer): ");
					user_piece_index = Integer.parseInt(bufferedReader.readLine()); 
					actual_file_in.skip((user_piece_index * 12)); //skips to the offset value
					for(int read_index = 0; read_index < 12; read_index++){ // read in 12 bytes
						if((baby_byte = (byte) actual_file_in.read()) != (byte) -1){ // if the read byte isn't null, save the byte into the piece
							byte_piece[read_index] = baby_byte;
							//System.out.println("The read index is: " + read_index + " " +Arrays.toString(byte_piece));
							//System.out.println("The read index is: " + read_index + " " + new String(byte_piece,StandardCharsets.UTF_8));
						}
					}
					msg = new Message(msgType, byte_piece);
				}
				else if(msgType == 8){
					break;
				}
				else
				{
					System.out.print("Please input message: ");
					message = bufferedReader.readLine();
					msg = new Message(msgType, message,12);
				}
				sendMessage(msg.getMessageInBytes());
				int readLen = in.read(incomingMessage);
				inMSG = new Message(incomingMessage);
				System.out.println("Received Message: " + inMSG.getMsgPayload());
			}
			} else System.out.println("Invalid Handshake prevented connection");
			
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
			//out.writeUTF(msg);
			//out.writeByte('\n');
			out.flush();
			System.out.println("After flush");
			//out.flush();
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
			//System.out.println("After flush");
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