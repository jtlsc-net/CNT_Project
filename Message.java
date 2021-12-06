import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Message
{
    int msgLength;
    byte msgType;
    byte[] msgPayload;
    int msgPieceIndex;
    byte[] msgPieceContent;
    int[] msgBitfield;

    // empty constructor for using just the functions of the Message class
    public Message()
    {

    }

    // constructor for reading only Actual Messages not Handshakes (Refer to checkHandshake() further down)
    public Message(byte[] message)
    {
        msgLength = message.length - 4;
        msgType = message[4];
        if (msgType == 4 || msgType == 6)
        {
            msgPayload = new byte[msgLength - 1];
            System.arraycopy(message, 5, msgPayload, 0, msgLength - 1);
            msgPieceIndex = ByteBuffer.wrap(msgPayload).getInt();
        }
        else if (msgType == 7)
        {
            msgPayload = new byte[msgLength - 1];
            System.arraycopy(message, 5, msgPayload, 0, msgLength - 1);
            msgPieceIndex = ByteBuffer.wrap(Arrays.copyOfRange(msgPayload, 0, 4)).getInt();
            msgPieceContent = Arrays.copyOfRange(msgPayload, 4, msgPayload.length);
        }
        else if (msgType == 5)
        {
            msgPayload = new byte[msgLength - 1];
            System.arraycopy(message, 5, msgPayload, 0, msgLength - 1);
            int payloadLength = msgLength - 1;
            msgBitfield = new int[payloadLength / 4];
            int index = 0;
            for (int i = 0; i < msgBitfield.length; i++)
            {
                msgBitfield[i] = ByteBuffer.wrap(Arrays.copyOfRange(msgPayload, index, index + 4)).getInt();
                index += 4;
            }
        }
    }

    public void parseMsgPayload(int msgType, byte[] _msgPayload)
    {
        if (msgType == 4 || msgType == 6)
        {
            msgPieceIndex = ByteBuffer.wrap(_msgPayload).getInt();
        }
        else if (msgType == 7)
        {
            msgPieceIndex = ByteBuffer.wrap(Arrays.copyOfRange(_msgPayload, 0, 4)).getInt();
            msgPieceContent = Arrays.copyOfRange(_msgPayload, 4, _msgPayload.length);
        }
        else if (msgType == 5)
        {
            msgBitfield = new int[_msgPayload.length / 4];
            int index = 0;
            for (int i = 0; i < msgBitfield.length; i++)
            {
                msgBitfield[i] = ByteBuffer.wrap(Arrays.copyOfRange(_msgPayload, index, index + 4)).getInt();
                index += 4;
            }
        }
    }

    public int getMsgLength()
    {
        return msgLength;
    }

    public byte getMsgType()
    {
        return msgType;
    }

    public byte[] getMsgPayload()
    {
        return msgPayload;
    }

    public int getMsgPieceIndex()
    {
        return msgPieceIndex;
    }

    public byte[] getMsgPieceContent()
    {
        return msgPieceContent;
    }

    public int[] getMsgBitfield() { return msgBitfield; }

    public byte[] createHandshakeMessage(int _peerID)
    {
        byte[] message = new byte[32];
        byte[] header = "P2PFILESHARINGPROJ".getBytes();
        System.arraycopy(header, 0, message, 0, 18);
        for (int i = 18; i < 28; i++)
        {
            message[i] = (byte) 0;
        }
        byte[] peerID = convertIntToBytes(_peerID);
        System.arraycopy(peerID, 0, message, 28, 4)  ;
        return message;
    }

    public byte[] createBitFieldMessage(int[] bitfield)
    {
        // ADD BITFIELD
        byte[] message = new byte[5 + (bitfield.length * 4)];
        System.arraycopy(convertIntToBytes(1 + (bitfield.length * 4)), 0, message, 0, 4);
        message[4] = (byte) 5;
        int index = 5;
        for (int i = 0; i < bitfield.length; i++)
        {
            System.arraycopy(convertIntToBytes(bitfield[i]), 0, message, index, 4);
            index += 4;
        }
        return message;
    }

    public byte[] createCUINMessage(int _msgType)
    {
        byte[] message = new byte[5];
        System.arraycopy(convertIntToBytes(1), 0, message, 0, 4);
        message[4] = (byte) _msgType;
        return message;
    }

    // creates the have(4)/request(6) message
    public byte[] createHOrRMessage(int _msgType, int pieceIndex)
    {
        // 9 bytes = msgLength (4 byte pieceIndex + 1 byte message type) + msgType (1 byte) + msgPayload (4 bytes)
        byte[] message = new byte[9];
        System.arraycopy(convertIntToBytes(5), 0, message, 0, 4);
        message[4] = (byte) _msgType;
        System.arraycopy(convertIntToBytes(pieceIndex), 0, message, 5, 4);
        return message;
    }

    public byte[] createPieceMessage(int pieceIndex, byte[] content)
    {
        byte[] message = new byte[9 + content.length];
        System.arraycopy(convertIntToBytes(9 + content.length), 0, message, 0, 4);
        message[4] = (byte) 7;
        System.arraycopy(convertIntToBytes(pieceIndex), 0, message, 5, 4);
        System.arraycopy(content, 0, message, 9, content.length);
        return message;
    }

    public boolean checkInitialHandshake(byte[] handshake)
    {
        String header = new String(Arrays.copyOfRange(handshake, 0, 18), StandardCharsets.UTF_8);
        return header.equals("P2PFILESHARINGPROJ");
    }

    public boolean checkHandshake(byte[] handshake, int expectedPeerID)
    {
        String header = new String(Arrays.copyOfRange(handshake, 0, 18), StandardCharsets.UTF_8);
        int peerID = ByteBuffer.wrap(Arrays.copyOfRange(handshake, 28, 32)).getInt();
        return header.equals("P2PFILESHARINGPROJ") && peerID == expectedPeerID;
    }


    //helper function to convert integer to byte[] array.
    private byte[] convertIntToBytes(int value)
    {
        return new byte[]
                {
                        (byte)(value >> 24),
                        (byte)(value >> 16),
                        (byte)(value >> 8),
                        (byte)value
                };
    }
}




