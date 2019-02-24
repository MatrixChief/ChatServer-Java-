public class TicTacToeGame {

    public String playerX;
    public String playerY;
    private char[][] gameBoard;
    private final int size=3;
    /*
    Sample TicTacToe Board
      0 | 1 | 2
     -----------
      3 | 4 | 5
     -----------
      6 | 7 | 8
     */

    public TicTacToeGame(String playerX, String playerY){

    }

    public void initialize(){

        this.gameBoard=new char[3][3];
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                gameBoard[i][j]=' ';
            }
        }
    }

    public void printBoard(){

    }

    /*public char printBoard(){
        for(int i=0;i<size;i++){
            System.out.println();
        }
    }

    public int takeTurn(int index){

    }

    public char getWinner(){

    }

    public boolean isTied(){

    }

    public char getSpace(int index){

    }

    public String toString(){

    }*/
}