package boardgame;

public abstract class Piece {
    protected Position position;
    private Board board;

    public Piece (Board board) {
        this.board = board;
        position = null; // A posição inicial de uma peça é nulo
    }

    //Não crio o método setBoard para não ser possível alterar o meu tabuleiro

    public abstract boolean[][] possibleMoves();

    public boolean possibleMove(Position position) {
        return possibleMoves()[position.getRow()][position.getColumn()];
    }


    /*
        Faz uma varredura em toda a matriz de posição
        verificando se há alguma posição na matrix que 
        retorna TRUE (indicando que há um movimento possível)
    */
    public boolean isThereAnyPossibleMove() {
        boolean[][] mat = possibleMoves();
        for (int i = 0; i < mat.length; i++) {
            for (int j = 0; j < mat.length; j++) {
                if (mat[i][j]) {
                    return true;
                }
            }
        }

        return false;  
    }

    protected Board getBoard() {
        return board;
    }

}
