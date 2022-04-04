package chess;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess_piece.King;
import chess_piece.Rook;


public class ChessMatch {
    private Board board;

    public ChessMatch() {
        board = new Board(8, 8); 
        initialSetup();
    }


    
    public ChessPiece[][] getPieces() {
        ChessPiece[][] match = new ChessPiece[board.getRows()][board.getColumns()];
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getColumns(); j++) {
                match[i][j] = (ChessPiece) board.piece(i, j);
            }
        }

        return match;
    }

    public boolean[][] possibleMoves(ChessPosition sourcePosition) {
        Position position = sourcePosition.toPosition();
        validateSourcePosition(position);
        return board.piece(position).possibleMoves();
    }

    public ChessPiece performChessMove (ChessPosition sourcePosition, ChessPosition targetPosition) {
        //Convertendo as posições de matriz para posições de xadez
        Position source = sourcePosition.toPosition();
        Position target = targetPosition.toPosition();

        validateSourcePosition(source);
        validateTargetPosition(source, target);
        Piece capturedPiece = makeMove (source, target);
        return (ChessPiece) capturedPiece;
    }
    
    private Piece makeMove(Position sourcePosition, Position targePosition) {
        Piece p = board.removePiece(sourcePosition);
        Piece capturedPiece = board.removePiece(targePosition);
        board.placePiece(p, targePosition);
        return capturedPiece;
    }
    private void validateSourcePosition(Position sourcePosition) {
        if (!board.thereIsAPiece(sourcePosition)) {
            throw new ChessException("There is no piece on source position");
        }
        if(!board.piece(sourcePosition).isThereAnyPossibleMove()) {
            throw new ChessException("There is no possible for the chosen piece");
        }
    }

    private void validateTargetPosition(Position sourcePosition, Position targePosition) {
        if (!board.piece(sourcePosition).possibleMove(targePosition)) {
            throw new ChessException("The chosen piece can't move to the target position");
        }
    }


    private void placeNewPiece(char column, int row, ChessPiece piece) {
        board.placePiece(piece, new ChessPosition(column, row).toPosition());
    }


    //Inicia a partida de xadrez
    private void initialSetup() {
		placeNewPiece('c', 1, new Rook(board, Color.WHITE));
        placeNewPiece('c', 2, new Rook(board, Color.WHITE));
        placeNewPiece('d', 2, new Rook(board, Color.WHITE));
        placeNewPiece('e', 2, new Rook(board, Color.WHITE));
        placeNewPiece('e', 1, new Rook(board, Color.WHITE));
        placeNewPiece('d', 1, new King(board, Color.WHITE));

        placeNewPiece('c', 7, new Rook(board, Color.BLACK));
        placeNewPiece('c', 8, new Rook(board, Color.BLACK));
        placeNewPiece('d', 7, new Rook(board, Color.BLACK));
        placeNewPiece('e', 7, new Rook(board, Color.BLACK));
        placeNewPiece('e', 8, new Rook(board, Color.BLACK));
        placeNewPiece('d', 8, new King(board, Color.BLACK));
	} 
}
