import java.io.*;
import java.util.*;
import java.net.*;

public class peerProcess extends Thread{
    private int peerId;
    private String hostName = "";
    private int listeningPort = 0; 
    private int sendingPort = 0;
    private int hasFile = 0; //value is 1 if the peer has the complete file
    private String fileName = "";
    private int fileSize = 0;
    private int pieceSize = 0;
    private Thread t;
    private String threadName;
    private int threadType;
    private Socket clientSocket;
    private Socket serverSocket;

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
        return String.format("PeerId: %d \n HostName: %s \n listeningPort: %d \n hasFile:%d \n fileName:%s \n fileSize:%d \n pieceSize:%d",peerId,hostName,listeningPort,hasFile, fileName,fileSize,pieceSize);
    }

    public void run(){
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
                Log log = new Log(peerId);
                log.WriteLog(1, peerId, 1);
            }
            catch(IOException e){
                //lol
            }

        }
        if(threadType == 2){
            // Do client seek ops.
            try{
                serverSocket = new Socket(hostName, sendingPort);
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
                    BufferedReader in = new BufferedReader(new FileReader("PeerInfo.cfg"));
                    while((st = in.readLine()) != null) {
                        
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
                }
                catch (Exception ex) {
                    System.out.println(ex.toString());
                }
                try {
                    BufferedReader in = new BufferedReader(new FileReader("Common.cfg"));
                    while((st = in.readLine()) != null) {
                         String[] tokens = st.split("\\s+");
                         if (tokens[0].equals("FileName")) {
                             peer.setFileName(tokens[1]);
                         }else if (tokens[0].equals("FileSize")){
                             peer.setFileSize(Integer.parseInt(tokens[1]));
                         }else if (tokens[0].equals("PieceSize")){
                            peer.setPieceSize(Integer.parseInt(tokens[1]));
                        }
                    }
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
                    clientPeer = new peerProcess(Integer.parseInt(hosts[0]), "clientPeer" + hosts[0], 2);
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

