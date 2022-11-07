import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

/**
 * A small tic-tac-toe board. Initially with a human and a primitive but valid computer player.
 */
public class TicTacToeMain {

    static final int BOARD_SIZE = 9;

    public static void main(String[] args) {
        // TODO: replace the GreedyPlayer with a PerfectPlayer (which uses brute-force)
        play(new HumanPlayer(), new GreedyPlayer());
    }

    /**
     * @param b     TicTacToe-board to check for a winner
     * @param color either X or O
     * @return true if the colorToPlay wins the current board (has three in a line)
     */
    static boolean isWin(Stone[] b, Stone color) {
        // @formatter:off
        return b[0] == color && b[0] == b[1] && b[1] == b[2] ||
                b[3] == color && b[3] == b[4] && b[4] == b[5] ||
                b[6] == color && b[6] == b[7] && b[7] == b[8] ||
                b[0] == color && b[0] == b[3] && b[3] == b[6] ||
                b[1] == color && b[1] == b[4] && b[4] == b[7] ||
                b[2] == color && b[2] == b[5] && b[5] == b[8] ||
                b[0] == color && b[0] == b[4] && b[4] == b[8] ||
                b[2] == color && b[2] == b[4] && b[4] == b[6];
        // @formatter:on
    }

    static String toString(Stone[] board) {
        var sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                var index = i * 3 + j;
                String color = board[index] == Stone.CROSS ? "X" : "O";
                color = "\033[1m" + color + "\033[0m";
                color = board[index] == null ? "\033[37m" + index + "\033[0m" : color;
                sb.append(color)
                        .append("  ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * The main game loop to play a game of tic-tac-toe with two opponents.
     *
     * @param xPlayer the player to start
     * @param oPlayer the 2nd player, must not be the same as xPlayer
     */
    static void play(TicTacToePlayer xPlayer, TicTacToePlayer oPlayer) {
        // the board is organized as a 1-dimensional matrix of size 3x3=9
        // to get the index for position at row r and colum c, calculate index=r*3+c
        //     0 | 1 | 2
        //    ---+---+---
        //     3 | 4 | 5
        //    ---+---+---
        //     6 | 7 | 8
        var board = new Stone[BOARD_SIZE]; // all null -> empty
        var currentPlayer = xPlayer;
        for (var round = 0; round < BOARD_SIZE; round++) { // it lasts for 9 rounds
            var currentColor = currentPlayer == xPlayer ? Stone.CROSS : Stone.CIRCLE;
            // we copy to avoid sideffects by the player (not needed if they behave)
            var clone = Arrays.copyOf(board, board.length);
            var playTo = currentPlayer.play(clone, currentColor);
            if (playTo < 0 || playTo >= 9 || board[playTo] != null) {
                System.out.println(toString(board));
                throw new IllegalStateException("cannot play to position " + playTo);
            }
            board[playTo] = currentColor; // do the move
            if (isWin(board, currentColor)) { // game over and current player wins
                System.out.println(toString(board) + "...and the winner is: " + currentColor);
                return;
            }
            currentPlayer = currentPlayer == xPlayer ? oPlayer : xPlayer;
        }
        System.out.println("it's a draw!");
    }

    /**
     * Represents the state of a single piece/field on the TicTacToe-board.
     */
    enum Stone {
        CROSS, CIRCLE;

        public Stone opponent() {
            return this == Stone.CROSS ? Stone.CIRCLE : Stone.CROSS;
        }
    }

    interface TicTacToePlayer {

        /**
         * Decide on the next move on a partially played TicTacToe-board.
         *
         * @param board       the current board to play on
         * @param colorToPlay my own color
         * @return the position to play to (0-8), where 0 is top-left
         */
        int play(Stone[] board, Stone colorToPlay);
    }

    /**
     * Simple human-player taking input from stdin.
     */
    static class HumanPlayer implements TicTacToePlayer {

        @Override
        public int play(Stone[] board, Stone colorToPlay) {
            System.out.println(TicTacToeMain.toString(board) + "where to to put the next " + colorToPlay + "? (0-8): ");
            var scanner = new Scanner(System.in, StandardCharsets.UTF_8);
            return Integer.parseInt(scanner.nextLine());
        }
    }

    /**
     * Valid (but stupid) player, who always plays as much top-left as possible.
     */
    static class GreedyPlayer implements TicTacToePlayer {
        Stone[] board;
        int bestMove = Integer.MIN_VALUE;

        @Override
        public int play(Stone[] board, Stone mycolor) {
            this.board = board;
            bestMove = -1;
            var depth = (int) Arrays.stream(board).filter(Objects::isNull).count();
            getScore(mycolor, depth, true);

            return bestMove;
        }

        private int getScore(Stone myColor, int depth, boolean isFirst) {
            if (isWin(board, myColor.opponent())) {
                return -1;
            }
            if (depth == 0) {
                return 0;
            }
            var bestValue = Integer.MIN_VALUE;
            for (int i = 0; i < BOARD_SIZE; i++) {
                if (board[i] == null) {
                    board[i] = myColor;
                    int currentValue = -getScore(myColor.opponent(), depth - 1, false);
                    board[i] = null;
                    if (currentValue > bestValue) {
                        bestValue = currentValue;
                        if (isFirst) {
                            bestMove = i;
                        }
                    }
                }
            }
            return bestValue;
        }
    }
}
