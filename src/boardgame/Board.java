package boardgame;

public class Board {
    private int rows;
    private int columns;
    private Piece [][] pieces;

    public Board(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        pieces = new Piece[rows][columns];
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    //Método que retorna uma peça com linha e coluna
    public Piece piece (int row, int column) {
        return pieces[row][column];
    }

    //Retorna a posição de uma peça
    public Piece piece (Position position) {
        return pieces[position.getRow()][position.getColumn()];
    }
}
