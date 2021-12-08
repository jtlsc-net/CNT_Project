/*
 *                     CEN5501C Project2
 * This is the program starting remote processes.
 * This program was only tested on CISE SunOS environment.
 * If you use another environment, for example, linux environment in CISE 
 * or other environments not in CISE, it is not guaranteed to work properly.
 * It is your responsibility to adapt this program to your running environment.
 */

import java.util.*;

public class RemotePeerInfo {
	public String peerId;
	public String peerAddress;
	public String peerPort;
	public int peerInt;
	public boolean unchoked = false;
	public boolean choked = false; 
	public boolean optimisticallyUnchoked = false;
	public int piecesDownloaded = 0;

	
	public RemotePeerInfo(String pId, String pAddress, String pPort) {
		peerId = pId;
		try{
			peerInt = Integer.valueOf(peerId);
		} catch (Exception e){
			peerInt = -1;
		}
		peerAddress = pAddress;
		peerPort = pPort;
		Random rand = new Random();
		piecesDownloaded = rand.nextInt(4);
	}

	public String getPeerId(){
		return peerId;
	}
	
    public int getPiecesDownloaded() {
		return piecesDownloaded;
    }
	
}