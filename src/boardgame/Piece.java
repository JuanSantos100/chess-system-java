package boardgame;

public class Piece {
    protected Position position;
    private Board board;

    public Piece (Board board) {
        this.board = board;
        position = null; // A posição inicial de uma peça é nulo
    }

    //Não crio o método setBoard para não ser possível alterar o meu tabuleiro

    protected Board getBoard() {
        return board;
    }

}
