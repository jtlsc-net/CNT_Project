import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;

public class Message
{
    int msgLength; // 4 byte, message length field containing length of in bytes how long payload and message type are.
    int msgType; // type of message to be sent 0-7
    String msgPayload; // payload of the message
    byte[] msgInBytes; // byte array of full message with all fields
    int msgPayloadLength; // length of the messge payload

    //constructor for Message of types 0-3
    public Message(int _msgType)
    {
        msgType = _msgType;
        msgPayload = "";
        msgLength = 1;
        msgPayloadLength = 0;
    }

    //constructor for messages of types 4-6
    public Message(int _msgType, String _msgPayload, int _msgPayloadLength)
    {
        msgType = _msgType;
        msgPayload = _msgPayload;
        msgLength = 1 + _msgPayload.getBytes(StandardCharsets.UTF_8).length;
        msgPayloadLength = _msgPayloadLength;
        byte[] msgPayloadBytes = msgPayload.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(msgPayloadBytes, 0, msgInBytes, 5, msgLength - 1);
    }

    public Message(int _msgType, byte[] _msgPayload)
    {
        msgType = _msgType;
        int nullByteCount = 0; // the number of null bytes at the end of the msgPayload
        for(int i = 5; i < msgLength;i++){
            if(msgInBytes[i] == 0){
                nullByteCount+=1;
            }
        }
        msgLength = 1 + _msgPayload.length;
        // Total message length - the total # of null bits in the payload - the 5 bits for msgType and msgLength 
        int msgPayloadEndIndex = msgLength - nullByteCount - 5;
        if (msgPayloadEndIndex < 5){
            msgPayload = "";
            msgPayloadEndIndex = 0;
           }else {
            byte[] msgPayloadInBytes = new byte[msgPayloadEndIndex];
            System.arraycopy(_msgPayload, 0, msgPayloadInBytes, 0, msgPayloadEndIndex);
            msgPayload = new String(msgPayloadInBytes,StandardCharsets.UTF_8);
            msgPayloadLength = msgPayloadEndIndex;
           }
        //msgPayload = new String(_msgPayload, StandardCharsets.UTF_8);
        System.out.println("Message payload on encode: " + msgPayload);
    }

    public Message(byte[] mesgInBytes)
    {
        msgInBytes = mesgInBytes;
        parseMessageInBytes();
    }

    private void parseMessageInBytes()
    {
        byte[] msgLengthInBytes = new byte[4];
        System.arraycopy(msgInBytes, 0, msgLengthInBytes, 0, 4);
        msgLength = ByteBuffer.wrap(msgLengthInBytes).getInt();
        msgType = msgInBytes[4];
        int nullByteCount = 0; // the number of null bytes at the end of the msgPayload
        for(int i = 5; i < msgLength;i++){
            if(msgInBytes[i] == 0){
                nullByteCount+=1;
            }
        }
        // Total message length - the total # of null bits in the payload - the 5 bits for msgType and msgLength 
        int msgPayloadEndIndex = msgLength - nullByteCount - 5;
        //System.out.println("The msgPayload Index is: " + msgPayloadEndIndex);
        //TODO: This breaks if we have a message of 0.
       // System.out.println("The end index of the message is: " + (msgLength-1));
       if (msgPayloadEndIndex < 5){
        msgPayload = "";
       }else {
        byte[] msgPayloadInBytes = new byte[msgPayloadEndIndex];
        System.arraycopy(msgInBytes, 5, msgPayloadInBytes, 0, msgPayloadEndIndex);
        msgPayload = new String(msgPayloadInBytes,StandardCharsets.UTF_8);
       }
        System.out.println("Message payload on decode: " + msgPayload);
    }

    public int getMsgLength()
    {
        return msgLength;
    }

    public int getMsgType()
    {
        return msgType;
    }

    public String getMsgPayload()
    {
        return msgPayload;
    }


    //returns byte array to be sent through socket
    public byte[] getMessageInBytes()
    {
        // msgLength is only the msgType + msgPayload, so missing 4 bytes for msgLength field
        int actualLength = msgLength + 4;
        // This is the converted byte array passed to the socket
        msgInBytes = new byte[actualLength];
        // msgLength is an integer which is 4 bytes in size, but has to be converted to byte array to get the 4 bytes
        byte[] msgLengthBytes = convertIntToBytes(msgLength);

        //inserting first 4 bytes of message: msgLength field
        System.arraycopy(msgLengthBytes, 0, msgInBytes, 0, 4);
        //inserting message type (1 byte)
        msgInBytes[msgLengthBytes.length] = (byte) msgType;
        byte[] msgPayloadInBytes = msgPayload.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(msgPayloadInBytes, 0, msgInBytes, 5, msgPayload.length());
        //inserting message payload, if necessary since msgType 0-3 do not need/send payloads
        // if (!msgPayload.equals(""))
        // {
        //     byte[] msgPayloadBytes = msgPayload.getBytes(StandardCharsets.UTF_8);
        //     System.arraycopy(msgPayloadBytes, 0, msgInBytes, 5, msgLength - 1);
        // }
        return msgInBytes;
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

