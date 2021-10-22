public class BitField {
    
    //each PIECE_INDEX_XtoY indicates that the peer contains a piece in the range from X to Y
    public final static int PIECE_INDEX_1to7 = 1;
    public final static int PIECE_INDEX_8to15 = 2;
    public final static int PIECE_INDEX_16to23 = 4;
    public final static int PIECE_INDEX_24to31 = 8;
    public final static int PIECE_INDEX_32to39 = 16;
    public final static int PIECE_INDEX_40to47 = 32;
    public final static int PIECE_INDEX_47to55 = 64;
    public final static int PIECE_INDEX_56to58 = 128;
    private int piecesContained;

    public BitField(){
        piecesContained = 0;
    }
    //executed when a new piece is downloaded to the peer
    public void addAPiece(final int pieces){
        piecesContained = pieces;
    }

    // return the bitfield of pieces that the respective peer contains
    public int getPieces(){
        return this.piecesContained;
    }

    //checks for the existence of a speciic piece in the bitfield
    boolean containsPiece(int mask){
        return (piecesContained & mask) > 0;
    }

}
