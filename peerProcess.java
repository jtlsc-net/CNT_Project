import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class peerProcess {
    private int peerId;
    private String hostName = "";
    private int listeningPort = 0;
    private int hasFile = 0; // value is 1 if the peer has the complete file
    private String fileName = "";
    private int fileSize = 0;
    private byte[][] filePieces;
    private int pieceSize = 0;
    private int[] bitfield;
    private File output;

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
                File newFile = new File(output.getAbsolutePath() + "/RECONSTRUCTED" + fileName);

                Files.write(newFile.toPath(), completeFile);

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

    public static void main(String[] args) {
        int peerID;
        String st;
        if (args.length > 0) {
            try {
                peerID = Integer.parseInt(args[0]);
                peerProcess peer = new peerProcess(peerID);
                try {
                    BufferedReader in = new BufferedReader(new FileReader("PeerInfo.cfg"));
                    while ((st = in.readLine()) != null) {

                        String[] tokens = st.split("\\s+");
                        // System.out.println("tokens begin ----");
                        // for (int x=0; x<tokens.length; x++) {
                        // System.out.println(tokens[x]);
                        // }
                        // System.out.println("tokens end ----");
                        if (tokens[0].equals(String.valueOf(peerID))) {
                            peer.setHostName(tokens[1]);
                            peer.setListeningPort(Integer.parseInt(tokens[2]));
                            peer.setHasFile(Integer.parseInt(tokens[3]));
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
                } catch (Exception ex) {
                    System.out.println(ex.toString());
                }

            } catch (NumberFormatException e) {
                System.err.println("Argument" + args[0] + " must be an integer.");
                System.exit(1);
            }
        }
    }
}
