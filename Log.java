import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;
import java.io.*;
import java.time.format.DateTimeFormatter;  
import java.time.LocalDateTime;
import java.util.*;

public class Log {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    BufferedWriter bw = null;
    //TODO make peerid global in class
    
    //Constructor
    public Log(int peerID) throws IOException{
        try{
            MakeLogFile(peerID);
        }
        catch(IOException e){
            throw e;
        }
    }

    //Makes file for logging with peerID
    private void MakeLogFile(int peerID) throws IOException{
        try{
            String new_file_name = "log_peer_" + peerID + ".log";
            File file = new File(new_file_name);
            file.createNewFile();
            FileWriter fw = new FileWriter(file);
	        bw = new BufferedWriter(fw);
        }
        catch(IOException e){
            System.out.println("IO exception");
            throw e;
        }
    }

    //==================================================================================
    // Log writing functions

    /**
     * Download complete message
     * @param selfID
     * @throws IOException
     */
    public void WriteLog(int selfID) throws IOException{
        //[Time]: Peer [peer_ID] has downloaded the complete file. 
        LocalDateTime now = LocalDateTime.now(); 
        String log_string = dtf.format(now) + ": Peer " + selfID + " has downloaded the complete file.";
        try{
            bw.write(log_string);
            bw.newLine();
            bw.flush();
        }
        catch(IOException e){
            throw e;
        }
    }

    // TCP connect/connected, choke/unchoke, interested/not interested, change of optimal neighbor
    //public void WriteLog()
    /**
    * @param type 0 for TCP connect, 1 for TCP connected to, 2 for choke, 3 for unchoke, 4 for interested
    * 5 for not interested, 6 for change of optimistically unchoked neighbor
    */
    public void WriteLog(int type, int selfID, int otherID) throws IOException {
        LocalDateTime now = LocalDateTime.now(); 
        String log_string;
        switch (type) {
            case 0: // TCP connect
            log_string = dtf.format(now) + ": Peer " + selfID + " makes a connection to Peer " + otherID + ".";
            break;
            case 1: // TCP connected to
            log_string = dtf.format(now) + ": Peer " + selfID + " is connected from Peer " + otherID + ".";
            break;
            case 2: // choke
            log_string = dtf.format(now) + ": Peer " + selfID + " is choked by Peer " + otherID + ".";
            break;
            case 3: // unchoke
            log_string = dtf.format(now) + ": Peer " + selfID + " is unchoked by Peer " + otherID + ".";
            break;
            case 4: // interested
            log_string = dtf.format(now) + ": Peer " + selfID + " received the ‘interested’ message from " + otherID + ".";
            break;
            case 5: // not interested
            log_string = dtf.format(now) + ": Peer " + selfID + " received the ‘not interested’ message from " + otherID + ".";
            break;
            case 6: // change of optimistically unchoked neighbor
            log_string = dtf.format(now) + ": Peer " + selfID + " has the optimistically unchoked neighbor " + otherID + ".";
            break;
            case 7: // 
            default: log_string = dtf.format(now) + ": Invalid Message type, switchy boi";
            break;
        }
        try{
            bw.write(log_string);
            bw.newLine();
            bw.flush();
        }
        catch(IOException e){
            throw e;
        }
    }

    /**
     * Change of preferred neighbors message
     * @param selfID
     * @param preferredList New list of preferred neighbors.
     * @throws IOException
     */
    public void WriteLog(int selfID, List<Integer> preferredList) throws IOException{
        LocalDateTime now = LocalDateTime.now();
        String log_string;
        log_string = dtf.format(now) + ": Peer " + selfID + " has the preferred neighbors ";
        //Get n-1 elements of list
        for(int i = 0; i < preferredList.size() - 1; i++){
            log_string = log_string + preferredList.get(i) + ", ";
        }
        //Get last element n for formatting reasons
        log_string = log_string + preferredList.get(preferredList.size() - 1) + ".";
        try{
            bw.write(log_string);
            bw.newLine();
            bw.flush();
        }
        catch(IOException e){
            throw e;
        }
    }

    /**
     * Have message
     * @param haveString make "have" to select this message
     * @param selfID
     * @param otherID
     * @param pieceIndex
     * @throws IOException
     */
    public void WriteLog(String haveString, int selfID, int otherID, int pieceIndex) throws IOException{
        LocalDateTime now = LocalDateTime.now();
        String log_string;
        log_string = dtf.format(now) + ": Peer " + selfID + " received the 'have' message from " + otherID + " for the piece " + pieceIndex + ".";
        try{
            bw.write(log_string);
            bw.newLine();
            bw.flush();
        }
        catch(IOException e){
            throw e;
        }
    }

    /**
     * Piece Downloaded message
     * @param selfID
     * @param otherID
     * @param pieceIndex
     * @param numberPieces Total number of pieces downloaded.
     * @throws IOException
     */
    public void WriteLog(int selfID, int otherID, int pieceIndex, int numberPieces) throws IOException{
        LocalDateTime now = LocalDateTime.now();
        String log_string;
        log_string = dtf.format(now) + ": Peer " + selfID + " has downloaded the piece " + pieceIndex + " from " + otherID
        + ". Now the number of pieces it has is " + numberPieces + ".";
        try{
            bw.write(log_string);
            bw.newLine();
            bw.flush();
        }
        catch(IOException e){
            throw e;
        }
    }

    public void WriteLog(int selfID, String msg) throws IOException 
    {
        LocalDateTime now = LocalDateTime.now();
        String log_string;
        log_string = dtf.format(now) + ": Peer " + selfID + msg;
        try{
            bw.write(log_string);
            bw.newLine();
            bw.flush();
        }
        catch(IOException e){
            throw e;
        }
    }
    // Uncomment below code for testing.

    // public static void main(String args[]){
    //     List<Integer> list = new ArrayList<Integer>();
    //     list.add(15);
    //     list.add(20);
    //     list.add(39);
    //     list.add(42);

    //     try{
    //         Log mylog = new Log(1);
    //         mylog.WriteLog(1);
    //         mylog.WriteLog(0, 1, 2);
    //         mylog.WriteLog(1,1,2);
    //         mylog.WriteLog(2,1,2);
    //         mylog.WriteLog(3,1,2);
    //         mylog.WriteLog(4,1,2);
    //         mylog.WriteLog(5,1,2);
    //         mylog.WriteLog(6,1,2);
    //         mylog.WriteLog(7,1,2);
    //         mylog.WriteLog(1, list);
    //         mylog.WriteLog("have", 1, 2, 14);
    //         mylog.WriteLog(1, 2, 14, 5);
    //     }
    //     catch(IOException e){
    //         System.out.println("oof");
    //         System.out.println(e);
    //     }
    //     System.out.println("Done");
    // }
}