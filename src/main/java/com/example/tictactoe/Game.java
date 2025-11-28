package com.example.tictactoe;

import java.util.Arrays;

/**
 * Game: thread-safe Tic-Tac-Toe model.
 * Board positions are 0..8 internally, exposed as 1..9 to clients.
 */
public class Game {
    private char[] board;   // '1'..'9' initially, then 'X' or 'O'
    private char turn;      // 'X' or 'O'
    private String winner;  // "X", "O", "draw", or null

    public Game() {
        reset();
    }

    public synchronized void reset() {
        board = new char[9];
        for (int i = 0; i < 9; i++) board[i] = (char) ('1' + i);
        turn = 'X';
        winner = null;
    }

    /**
     * Attempts to place the current player's mark at 1..9 position.
     * Returns a result object describing success, message and updated state.
     */
    public synchronized MoveResult makeMove(int posOneBased) {
        if (winner != null) {
            return MoveResult.failure("Game already finished: " + winner);
        }
        if (posOneBased < 1 || posOneBased > 9) {
            return MoveResult.failure("Position out of range (1-9).");
        }
        int idx = posOneBased - 1;
        char cur = board[idx];
        if (cur == 'X' || cur == 'O') {
            return MoveResult.failure("Slot already taken.");
        }
        board[idx] = turn;
        evaluateWinner();
        if (winner == null) toggleTurn();
        return MoveResult.success("Move accepted.");
    }

    private void toggleTurn() {
        turn = (turn == 'X') ? 'O' : 'X';
    }

    private void evaluateWinner() {
        String[] lines = new String[] {
                "" + board[0] + board[1] + board[2],
                "" + board[3] + board[4] + board[5],
                "" + board[6] + board[7] + board[8],
                "" + board[0] + board[3] + board[6],
                "" + board[1] + board[4] + board[7],
                "" + board[2] + board[5] + board[8],
                "" + board[0] + board[4] + board[8],
                "" + board[2] + board[4] + board[6]
        };

        for (String line : lines) {
            if ("XXX".equals(line)) {
                winner = "X";
                return;
            } else if ("OOO".equals(line)) {
                winner = "O";
                return;
            }
        }

        // draw if no cells contain digits 1..9
        boolean anyEmptyNumber = false;
        for (char c : board) {
            if (c >= '1' && c <= '9') {
                anyEmptyNumber = true;
                break;
            }
        }
        if (!anyEmptyNumber) {
            winner = "draw";
        }
    }

    // Read-only snapshots for API responses
    public synchronized char[] getBoardSnapshot() {
        return Arrays.copyOf(board, board.length);
    }

    public synchronized char getTurn() {
        return turn;
    }

    public synchronized String getWinner() {
        return winner;
    }

    // Result wrapper for move API
    public static class MoveResult {
        public final boolean ok;
        public final String message;

        private MoveResult(boolean ok, String message) {
            this.ok = ok;
            this.message = message;
        }

        public static MoveResult success(String msg) {
            return new MoveResult(true, msg);
        }

        public static MoveResult failure(String msg) {
            return new MoveResult(false, msg);
        }
    }
}
