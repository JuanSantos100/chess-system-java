package chess;

import boardgame.Board;
import boardgame.Piece;

public class ChessPiece extends Piece{
    private Color color;


    public ChessPiece(Board board) {
        super(board);
        this.color = color;
        
    }

    //A cor de uma peça não pode ser alterada
    

    public Color getColor() {
        return color;
    }
    
}
