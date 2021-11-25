import java.io.*;
import java.util.*;

public class peerProcess {
    private int peerId;
    private String hostName = "";
    private int listeningPort = 0; 
    private int hasFile = 0; //value is 1 if the peer has the complete file
    private String fileName = "";
    private int fileSize = 0;
    private int pieceSize = 0;

    peerProcess(int id) {
        peerId = id;
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
    public String toString() {
        return String.format("PeerId: %d \n HostName: %s \n listeningPort: %d \n hasFile:%d \n fileName:%s \n fileSize:%d \n pieceSize:%d",peerId,hostName,listeningPort,hasFile, fileName,fileSize,pieceSize);
    }
    public static void main (String[] args) {
        int peerID;
        String st;
        if (args.length > 0) {  
            try { 
                peerID = Integer.parseInt(args[0]);
                peerProcess peer = new peerProcess(peerID);
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
                }
                catch (Exception ex) {
                    System.out.println(ex.toString());
                }
    
            } catch (NumberFormatException e) {
                System.err.println("Argument" + args[0] + " must be an integer.");
                System.exit(1);
            }
        }
    }
}

