package chess;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess_piece.King;
import chess_piece.Rook;


public class ChessMatch {
    private int turn;
    private Color currentPlayer;
    private Board board;
    private boolean check;
    private boolean checkMate;

    private List<Piece> piecesOnTheBoard = new ArrayList<>();
    private List<Piece> capturedPieces = new ArrayList<>();

    public ChessMatch() {
        board = new Board(8, 8);
        turn = 1;
        currentPlayer = Color.WHITE; 
        initialSetup();
    }

    public int getTurn() {
        return turn;
    }

    public Color getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean getCheck() {
        return check;
    }

    public boolean getCheckMate() {
        return checkMate;
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


        if(testCheck(currentPlayer)) {
            undoMove(source, target, capturedPiece);
            throw new ChessException("You can't put yourself in check");
        }

        //Verificando se o oponente está em check
        check = (testCheck(opponent(currentPlayer))) ? true : false;
        if(testCheckMate(opponent(currentPlayer))) {
            checkMate = true;
        }
        else { 
            
            nextTurn();
        }
        return (ChessPiece) capturedPiece;
    }
    
    private Piece makeMove(Position sourcePosition, Position targePosition) {
        ChessPiece p = (ChessPiece)board.removePiece(sourcePosition);
        p.increaseMoveCount();
        Piece capturedPiece = board.removePiece(targePosition);
        board.placePiece(p, targePosition);

        if (capturedPiece != null) {
            piecesOnTheBoard.remove(capturedPiece);
            capturedPieces.add(capturedPiece);
        }
        return capturedPiece;
    }

    private void undoMove(Position sourcePosition, Position targetPosition, Piece capturedPiece) {
        ChessPiece p = (ChessPiece)board.removePiece(targetPosition);
        p.decreaseMoveCount();
        board.placePiece(p, sourcePosition);

        if (capturedPiece != null) {
            board.placePiece(capturedPiece, targetPosition);
            capturedPieces.remove(capturedPiece);
            piecesOnTheBoard.add(capturedPiece);
        }
    }

    private void validateSourcePosition(Position sourcePosition) {
        if (!board.thereIsAPiece(sourcePosition)) {
            throw new ChessException("There is no piece on source position");
        }
        if(currentPlayer != ((ChessPiece)board.piece(sourcePosition)).getColor()) {
            throw new ChessException("The chosen piece is not yours");   
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

    private void nextTurn() {
        turn++;

        //Operação ternária
        currentPlayer = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }

    private Color opponent(Color color) {
        return (color == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }


    //Faz a varredura da partida para buscar se há reis na partida baseado em uma cor.
    private ChessPiece king(Color color) {
        List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == color).collect(Collectors.toList());
        for (Piece p : list) {
            if(p instanceof King) {
                return (ChessPiece) p;
            }
        }
        throw new IllegalStateException("There is no " + color + " king on the board");
    }


    private boolean testCheckMate(Color color) {
        if (!testCheck(color)) {
            return false;
        }
        List <Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece) x).getColor() == color).collect(Collectors.toList());

        for (Piece p : list) {
            boolean[][] mat = p.possibleMoves();
            for (int i = 0; i < board.getRows(); i++) {
                for (int j = 0; j < board.getColumns(); j++) {
                    if(mat[i][j]) {
                       Position source = ((ChessPiece)p).getChessPosition().toPosition();
                       Position target = new Position(i, j);
                       Piece capturedPiece = makeMove(source, target); 
                       boolean testCheck = testCheck(color);
                       undoMove(source, target, capturedPiece);
                       if(!testCheck) {
                           return false;
                       }
                    }
                }
            }
        }

        return true;
    }


    private boolean testCheck (Color color) {
        Position kingPosition = king(color).getChessPosition().toPosition();
        List<Piece> opponentPieces = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == opponent(color)).collect(Collectors.toList());
        for(Piece p : opponentPieces) {
            boolean[][] mat = p.possibleMoves();
            if(mat[kingPosition.getRow()][kingPosition.getColumn()] == true) {
                return true;
            }
        }

        return false;
    }


    private void placeNewPiece(char column, int row, ChessPiece piece) {
        board.placePiece(piece, new ChessPosition(column, row).toPosition());
        piecesOnTheBoard.add(piece);
    }


    //Inicia a partida de xadrez
    private void initialSetup() {
		placeNewPiece('h', 7, new Rook(board, Color.WHITE));
        placeNewPiece('d', 1, new Rook(board, Color.WHITE));
        placeNewPiece('e', 1, new King(board, Color.WHITE));

        placeNewPiece('b', 8, new Rook(board, Color.BLACK));
        placeNewPiece('a', 8, new King(board, Color.BLACK));
	} 
}
