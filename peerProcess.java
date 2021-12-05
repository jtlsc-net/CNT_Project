import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class peerProcess extends Thread{
    private int peerId;
    private String hostName = "";
    private int listeningPort = 0; 
    private int sendingPort = 0;
    private int hasFile = 0; //value is 1 if the peer has the complete file
    private String fileName = "";
    private int fileSize = 0;
    private byte[][] filePieces;
    private int pieceSize = 0;
    private int[] bitfield;
    private File output;
    private Thread t;
    private String threadName;
    private int threadType;
    private Socket clientSocket;
    private Socket serverSocket;
    private DataOutputStream out;
    private DataInputStream in;
    private Message msg = new Message(); // Singleton-like object, to create all messages and check handshake
    public static byte[] header = "P2PFILESHARINGPROJ".getBytes();


    peerProcess(int id, String name, int type) {
        peerId = id;
        threadName = name + id;
        threadType = type;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setListeningPort(int listeningPort) {
        this.listeningPort = listeningPort;
    }

    public void setHasFile(int hasFile) {
        this.hasFile = hasFile;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public void setPieceSize(int pieceSize) {
        this.pieceSize = pieceSize;
    }
    public void setClientSocket(Socket client){
        this.clientSocket = client;
    }
    public void setSendingPort(int port){
        this.sendingPort = port;
    }
    public int getPeerId() {
        return peerId;
    }

    public String getHostName() {
        return hostName;
    }

    public int getListeningPort() {
        return listeningPort;
    }

    public int getHasFile() {
        return hasFile;
    }

    public String getFileName() {
        return fileName;
    }

    public int getFileSize() {
        return fileSize;
    }

    public int getPieceSize() {
        return pieceSize;
    }
    public int getThreadType() {
        return threadType;
    }
    public Socket getClientSocket(){
        return clientSocket;
    }
    public int getSendingPort(){
        return sendingPort;
    }
    public String toString() {
        return String.format(
                "PeerId: %d \n HostName: %s \n listeningPort: %d \n hasFile:%d \n fileName:%s \n fileSize:%d \n pieceSize:%d \nbitfield:"
                        + Arrays.toString(bitfield),
                peerId, hostName, listeningPort, hasFile, fileName, fileSize, pieceSize);
    }

    public String bitfieldtoString(int n) {
        String result = "";
        for (int i = 0; i < 32; i++) {
            result += getBit(n, i);
        }
        return result;
    }

    public int getBit(int n, int k) { // gets the kth bit in n
        return (n >> k) & 1;
    }

    public int modifyBit(int n, int p, int b) { // modify the pth bit in num n with binary value b
        int mask = 1 << p;
        return ((n & ~mask) | (b << p));
    }

    public void writePieces() {
        byte[] reconstructedFile = new byte[fileSize];
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        for (byte[] b : filePieces) {
            os.write(b, 0, b.length);
        }
        reconstructedFile = os.toByteArray();
        // System.out.println("There are " + length + "bytes in the reconstructed
        // file");
        File newFile = new File(output.getAbsolutePath() + "/RECONSTRUCTED" + fileName);
        try {
            Files.write(newFile.toPath(), reconstructedFile);
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public void setBitField() {
        int totalPieces = (int) Math.ceil((double) (fileSize / pieceSize));
        int bitfieldInts = (int) Math.ceil((double) (fileSize / pieceSize) / 8);
        bitfield = new int[bitfieldInts];
        if (getHasFile() == 1) {
            Arrays.fill(bitfield, 1);
            int index = 0;
            for (int i = 0; i < bitfieldInts; i++) {
                for (int j = 0; j < 32; j++) {
                    if (index < totalPieces) {
                        bitfield[i] = modifyBit(bitfield[i], j, 1);
                    } else {
                        modifyBit(bitfield[i], j, 0);
                    }
                    index++;
                }
            }
            // break the file down into number of pieces
            int piece = 0;

            filePieces = new byte[totalPieces][];
            output = new File("peer_" + peerId);
            if (output.exists() == false) {
                output.mkdir();
            }

            try {
                File f = new File(output.getAbsolutePath() + "/" + fileName);
                byte[] completeFile = Files.readAllBytes(f.toPath());
                // System.out.println(new String(completeFile));
                int pieceBegin = 0;
                int pieceEnd = 0;
                // File newFile = new File(output.getAbsolutePath() + "/RECONSTRUCTED" + fileName);

                // Files.write(newFile.toPath(), completeFile);

                for (int i = 0; i < totalPieces; i++) {
                    if (i == totalPieces - 1) {
                        filePieces[i] = Arrays.copyOfRange(completeFile, pieceBegin, fileSize);
                        // System.out.println(new String(filePieces[i]));

                    } else if (pieceBegin + pieceSize <= fileSize) {
                        pieceEnd = pieceBegin + pieceSize;
                        filePieces[i] = Arrays.copyOfRange(completeFile, pieceBegin, pieceEnd);
                        // System.out.println(new String(filePieces[i]));
                        pieceBegin = pieceEnd;

                    }

                }
            } catch (Exception e) {
                System.out.println("Error breaking down completed file");
            }

        } else {
            Arrays.fill(bitfield, 0);
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

    public void run(){
        // Each threadtype is a different type of thread
        // the main server thread is 0
        // when it receives a connection it spawns a 1 thread
        // and when a peer attempts to connect to another peer it spawns a 2 thread
        // Type 0 = server listener thread
        if(threadType == 0){
            // Start listener server
            try{
                ServerSocket listener = new ServerSocket(listeningPort);
                Socket connectedSocket;
                peerProcess clientReceived;
                try{
                    while(true){
                        connectedSocket = listener.accept();
                        clientReceived = new peerProcess(peerId, "client_received", 1);
                        clientReceived.setHostName(hostName);
                        clientReceived.setHasFile(hasFile);
                        clientReceived.setFileName(fileName);
                        clientReceived.setFileSize(fileSize);
                        clientReceived.setPieceSize(pieceSize);
                        clientReceived.setClientSocket(connectedSocket);
                        clientReceived.start();
                    }
                }finally{
                    listener.close();
                }
            }
            catch(IOException e){
                System.out.println(e);
            }
        }


        if(threadType == 1){
            // Do server operations
            try{
                byte[] get_msg = new byte[32];
                byte[] remotePeerBytes = new byte[4];
                int remotePeer;
                Boolean badHeader = false;
                out = new DataOutputStream(clientSocket.getOutputStream());
			    in = new DataInputStream(clientSocket.getInputStream());
                Log log = new Log(peerId);

                for(int i = 0; i < 32; i++){
                    get_msg[i] = in.readByte();
                }
                for(int z = 0; z < 18; z++){
                    if (header[z] != get_msg[z]){
                        log.WriteLog(6, peerId, 0);
                        badHeader = true;
                    }
                }
                if(!badHeader){
                    for(int j = 28; j < 32; j++){
                        remotePeerBytes[j-28] = get_msg[j];
                    }
                    remotePeer = ByteBuffer.wrap(remotePeerBytes).getInt();
                    log.WriteLog(1, peerId, remotePeer);
                }
            }
            catch(IOException e){
                //lol
            }

        }


        if(threadType == 2){
            // Do client seek ops.
            try{
//                byte[] handshakeBytesFirstField = "P2PFILESHARINGPROJ".getBytes();
//                byte[] handshakeMessage = new byte[32];
//                byte[] peerIdBytes = ByteBuffer.allocate(4).putInt(peerId).array();
                
                serverSocket = new Socket(hostName, sendingPort);
                out = new DataOutputStream(serverSocket.getOutputStream());
                in = new DataInputStream(serverSocket.getInputStream());

//                for(int i = 0; i < 18; i++){
//                    handshakeMessage[i] = handshakeBytesFirstField[i];
//                }
//                for(int j = 18; j < 28; j++)
//                {
//                    handshakeMessage[j] = 0;
//                }
//                for(int k = 28; k < 32; k++)
//                {
//                    handshakeMessage[k] = peerIdBytes[k-28];
//                }
                
                // String q = new String(handshakeMessage, StandardCharsets.UTF_8);
                
                sendMessage(msg.createHandshakeMessage(peerId));
                // sendMessage(handshakeMessage);

            }
            catch(ConnectException e){
                System.err.println(e);
            }
            catch(IOException f){
                System.err.println(f);
            }
            finally{
                try{
                    serverSocket.close();
                }
                catch(IOException e){
                    System.err.println(e);
                }
            }
        }
    }

    public void start(){
        if(t == null){
            t = new Thread (this, threadName);
            t.start();
        }
    }
    public static void main (String[] args) {
        int peerID;
        String st;
        ArrayList<String[]> stringArray = new ArrayList<String[]>();
        if (args.length > 0) {  
            try {
                peerID = Integer.parseInt(args[0]);
                peerProcess peer = new peerProcess(peerID, "server_listener", 0);
                try {
                    // Read in from PeerInfo
                    BufferedReader in = new BufferedReader(new FileReader("PeerInfo.cfg"));
                    while((st = in.readLine()) != null){
                        // Keep all ones before the current one in the list, and set values for the current one.
                        
                         String[] tokens = st.split("\\s+");
                         //System.out.println("tokens begin ----");
                         //for (int x=0; x<tokens.length; x++) {
                         //    System.out.println(tokens[x]);
                         //}
                         //System.out.println("tokens end ----");
                         if (tokens[0].equals(String.valueOf(peerID))){
                             peer.setHostName(tokens[1]);
                             peer.setListeningPort(Integer.parseInt(tokens[2]));
                             peer.setHasFile(Integer.parseInt(tokens[3]));
                             break;
                         }
                         else{
                            stringArray.add(tokens);
                         }
                    }
                    in.close();
                } catch (Exception ex) {
                    System.out.println(ex.toString());
                }
                try {
                    BufferedReader in = new BufferedReader(new FileReader("Common.cfg"));
                    while ((st = in.readLine()) != null) {
                        String[] tokens = st.split("\\s+");
                        if (tokens[0].equals("FileName")) {
                            peer.setFileName(tokens[1]);
                        } else if (tokens[0].equals("FileSize")) {
                            peer.setFileSize(Integer.parseInt(tokens[1]));
                        } else if (tokens[0].equals("PieceSize")) {
                            peer.setPieceSize(Integer.parseInt(tokens[1]));
                        }
                    }
                    peer.setBitField();
                    // peer.writePieces();
                    in.close();
                    System.out.println(peer.toString());
                    peer.start();
                }
                catch (Exception ex) {
                    System.out.println(ex.toString());
                }
                String[] hosts;
                peerProcess clientPeer;
                for (int i = 0; i < stringArray.size(); i++){
                    hosts = stringArray.get(i);
                    clientPeer = new peerProcess(peerID, "clientPeer" + peerID, 2);
                    clientPeer.setHostName(hosts[1]);
                    clientPeer.setSendingPort(Integer.parseInt(hosts[2]));
                    clientPeer.setHasFile(Integer.parseInt(hosts[3]));
                    clientPeer.start();
                }
            } catch (NumberFormatException e) {
                System.err.println("Argument" + args[0] + " must be an integer.");
                System.exit(1);
            }
        }
    }
}
