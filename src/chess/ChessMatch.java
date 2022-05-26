package chess;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess_piece.Bishop;
import chess_piece.King;
import chess_piece.Knight;
import chess_piece.Pawn;
import chess_piece.Queen;
import chess_piece.Rook;


public class ChessMatch {
    private int turn;
    private Color currentPlayer;
    private Board board;
    private boolean check;
    private boolean checkMate;
    private ChessPiece enPassantVulnerable;
    private ChessPiece promoted;

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

    public ChessPiece getEnPassantVulnerable() {
        return enPassantVulnerable;
    }

    public ChessPiece getPromoted() {
        return promoted;
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

        ChessPiece movedPiece = (ChessPiece)board.piece(target);

        //Special move promotion
        promoted = null;
        if(movedPiece instanceof Pawn) {
            if(movedPiece.getColor() == Color.WHITE && target.getRow() == 0 || movedPiece.getColor() == Color.BLACK && target.getRow() == 7) {
                promoted = (ChessPiece)board.piece(target);
                promoted = replacePromotedPiece("Q");
            }
        }

        //Verificando se o oponente está em check
        check = (testCheck(opponent(currentPlayer))) ? true : false;
        if(testCheckMate(opponent(currentPlayer))) {
            checkMate = true;
        }
        else { 
            
            nextTurn();
        }

        //Special move en passant
        if(movedPiece instanceof Pawn && target.getRow() == source.getRow() - 2 || target.getRow() == source.getRow() + 2 ) {
            enPassantVulnerable = movedPiece;
        } else {
            enPassantVulnerable = null;
        }

        return (ChessPiece) capturedPiece;
    }

    public ChessPiece replacePromotedPiece(String type) {
        if(promoted == null) {
            throw new IllegalStateException("There is no piece to be promoted");
        }

        if(!type.equals("B") && !type.equals("N") && !type.equals("R") && !type.equals("Q")) {
            throw new InvalidParameterException("Invalid type for promotion");
        }

        Position pos = promoted.getChessPosition().toPosition();
        Piece p = board.removePiece(pos);
        piecesOnTheBoard.remove(p);

        ChessPiece newPiece = newPiece(type, promoted.getColor());
        board.placePiece(newPiece, pos);
        piecesOnTheBoard.add(newPiece);

        return newPiece;


    }

    private ChessPiece newPiece(String type, Color color) {
        if(type.equals("B")) return new Bishop(board, color);
        if(type.equals("N")) return new Knight(board, color);
        if(type.equals("Q")) return new Queen(board, color);
        return new Rook(board, color);
    }
    
    private Piece makeMove(Position sourcePosition, Position targetPosition) {
        ChessPiece p = (ChessPiece)board.removePiece(sourcePosition);
        p.increaseMoveCount();
        Piece capturedPiece = board.removePiece(targetPosition);
        board.placePiece(p, targetPosition);

        if (capturedPiece != null) {
            piecesOnTheBoard.remove(capturedPiece);
            capturedPieces.add(capturedPiece);
        }

        //# Special move Castling kingside rook
        if (p instanceof King && targetPosition.getColumn() == sourcePosition.getColumn() + 2 ) {
            Position sourceTorre = new Position(sourcePosition.getRow(), sourcePosition.getColumn() + 3);
            Position targetTorre = new Position(sourcePosition.getRow(), sourcePosition.getColumn() + 1);
            ChessPiece rook = (ChessPiece)board.removePiece(sourceTorre);
            board.placePiece(rook, targetTorre);
            rook.increaseMoveCount();

        }

        //# Special move Castling queenside rook
        if (p instanceof King && targetPosition.getColumn() == sourcePosition.getColumn() - 2 ) {
            Position sourceTorre = new Position(sourcePosition.getRow(), sourcePosition.getColumn() - 4);
            Position targetTorre = new Position(sourcePosition.getRow(), sourcePosition.getColumn() - 1);
            ChessPiece rook = (ChessPiece)board.removePiece(sourceTorre);
            board.placePiece(rook, targetTorre);
            rook.increaseMoveCount();

        }

        //#special move enPassant
        if (p instanceof Pawn) {
           if(sourcePosition.getColumn() != targetPosition.getColumn() && capturedPiece == null) {
               Position pawnPosition;
               if(p.getColor() == Color.WHITE) {
                   pawnPosition = new Position(targetPosition.getRow() + 1, targetPosition.getColumn());
               } else {
                pawnPosition = new Position(targetPosition.getRow() - 1, targetPosition.getColumn());
               }

               capturedPiece = board.removePiece(pawnPosition);
               capturedPieces.add(capturedPiece);
               piecesOnTheBoard.remove(capturedPiece);
           } 
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

        //# Special move Castling kingside rook
        if (p instanceof King && targetPosition.getColumn() == sourcePosition.getColumn() + 2 ) {
            Position sourceTorre = new Position(sourcePosition.getRow(), sourcePosition.getColumn() + 3);
            Position targetTorre = new Position(sourcePosition.getRow(), sourcePosition.getColumn() + 1);
            ChessPiece rook = (ChessPiece)board.removePiece(targetTorre);
            board.placePiece(rook, sourceTorre);
            rook.decreaseMoveCount();

        }

        //# Special move Castling queenside rook
        if (p instanceof King && targetPosition.getColumn() == sourcePosition.getColumn() - 2 ) {
            Position sourceTorre = new Position(sourcePosition.getRow(), sourcePosition.getColumn() - 4);
            Position targetTorre = new Position(sourcePosition.getRow(), sourcePosition.getColumn() - 1);
            ChessPiece rook = (ChessPiece)board.removePiece(targetTorre);
            board.placePiece(rook, sourceTorre);
            rook.decreaseMoveCount();

        }

        //#special move enPassant
        if (p instanceof Pawn) {
            if(sourcePosition.getColumn() != targetPosition.getColumn() && capturedPiece == enPassantVulnerable) {
                ChessPiece pawn = (ChessPiece)board.removePiece(targetPosition);               
                Position pawnPosition;
                if(p.getColor() == Color.WHITE) {
                    pawnPosition = new Position(3, targetPosition.getColumn());
                } else {
                 pawnPosition = new Position(4, targetPosition.getColumn());
                }

                board.placePiece(pawn, pawnPosition);
            } 
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
		placeNewPiece('a', 1, new Rook(board, Color.WHITE));
        placeNewPiece('c', 1, new Bishop(board, Color.WHITE));
        placeNewPiece('b', 1, new Knight(board, Color.WHITE));
        placeNewPiece('d', 1, new Queen(board, Color.WHITE));
        placeNewPiece('e', 1, new King(board, Color.WHITE, this));
        placeNewPiece('f', 1, new Bishop(board, Color.WHITE));
        placeNewPiece('g', 1, new Knight(board, Color.WHITE));
        placeNewPiece('h', 1, new Rook(board, Color.WHITE));
        placeNewPiece('a', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('b', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('c', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('d', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('e', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('f', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('g', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('h', 2, new Pawn(board, Color.WHITE, this));

        placeNewPiece('a', 8, new Rook(board, Color.BLACK));
        placeNewPiece('c', 8, new Bishop(board, Color.BLACK));
        placeNewPiece('b', 8, new Knight(board, Color.BLACK));
        placeNewPiece('d', 8, new Queen(board, Color.BLACK));
        placeNewPiece('e', 8, new King(board, Color.BLACK, this));
        placeNewPiece('f', 8, new Bishop(board, Color.BLACK));
        placeNewPiece('g', 8, new Knight(board, Color.BLACK));
        placeNewPiece('h', 8, new Rook(board, Color.BLACK));
        placeNewPiece('a', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('b', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('c', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('d', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('e', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('f', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('g', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('h', 7, new Pawn(board, Color.BLACK, this));
        
	} 
}
