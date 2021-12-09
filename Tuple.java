public class Tuple {
    int peerToSend;
    byte[] message;

    public Tuple(int peerID, byte[] _message)
    {
        peerToSend = peerID;
        message = _message;
    }

    public int getPeerToSend()
    {
        return peerToSend;
    }

    public byte[] getMessage() {
        return message;
    }
}