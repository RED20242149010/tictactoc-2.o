// ============================================================
//  TicTacToe.java  —  Complete Beginner-Friendly Version
//  How to run:
//    1. Save this file as TicTacToe.java
//    2. Open a terminal in the same folder
//    3. Type:  javac TicTacToe.java
//    4. Type:  java TicTacToe
// ============================================================

// These "import" lines bring in Java tools we need
import java.awt.*;           // For Color, Font, etc.
import java.awt.event.*;     // For mouse clicks and events
import javax.swing.*;        // For the window, buttons, labels

public class TicTacToe {

    // --------------------------------------------------------
    //  WINDOW & LAYOUT PIECES
    // --------------------------------------------------------

    // The main window of our game
    JFrame frame = new JFrame("Tic-Tac-Toe");

    // A text label shown at the top (e.g. "X Turn | X:0 O:0")
    JLabel statusLabel = new JLabel();

    // The 3x3 grid panel where the 9 buttons live
    JPanel boardPanel = new JPanel();

    // The top panel that holds the status text and control buttons
    JPanel topPanel = new JPanel();

    // --------------------------------------------------------
    //  THE 3x3 GRID OF BUTTONS
    //  board[row][column]  — both row and column go from 0 to 2
    // --------------------------------------------------------
    JButton[][] board = new JButton[3][3];

    // --------------------------------------------------------
    //  CONTROL BUTTONS (Restart / Mode / Theme)
    // --------------------------------------------------------
    JButton restartBtn = new JButton("Restart");
    JButton modeBtn    = new JButton("Mode: AI");
    JButton themeBtn   = new JButton("Theme");

    // --------------------------------------------------------
    //  SCORE TRACKING
    // --------------------------------------------------------
    int playerXScore = 0;   // how many times X has won
    int playerOScore = 0;   // how many times O has won

    // --------------------------------------------------------
    //  GAME STATE FLAGS
    //  A "boolean" is either true or false — nothing else
    // --------------------------------------------------------
    boolean isXTurn  = true;   // true = X's turn,  false = O's turn
    boolean gameOver = false;  // true = no more moves allowed
    boolean vsAI     = true;   // true = play against computer, false = 2 players
    boolean darkMode = true;   // true = dark background, false = light background

    // --------------------------------------------------------
    //  PLAYER SYMBOLS
    // --------------------------------------------------------
    String playerX = "X";
    String playerO = "O";

    // --------------------------------------------------------
    //  WIN CELL TRACKER
    //  When someone wins we store which 3 cells made the line
    //  so we can highlight ONLY those 3 cells (not all X's/O's)
    //  Example:  winCells = { {0,0}, {1,1}, {2,2} }  = diagonal
    // --------------------------------------------------------
    int[][] winCells = null;   // null means "no winner yet"

    // --------------------------------------------------------
    //  DRAW TIMER
    //  After a draw we wait 1.5 seconds then auto-restart.
    //  We store the timer here so we can STOP it if the player
    //  clicks Restart before the 1.5 seconds is up.
    //  BUG FIX: Without storing it, old timers kept firing and
    //  locked the board in round 3+.
    // --------------------------------------------------------
    Timer drawTimer = null;

    // ========================================================
    //  CONSTRUCTOR — runs once when the game starts
    //  A constructor sets everything up.
    // ========================================================
    TicTacToe() {

        // --- Window setup ---
        frame.setSize(600, 700);                          // width=600, height=700 pixels
        frame.setLayout(new BorderLayout());              // splits window into NORTH/CENTER/SOUTH areas
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // close app when window is closed

        // --- Status label (shows whose turn it is) ---
        statusLabel.setHorizontalAlignment(JLabel.CENTER);      // center the text
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 32)); // big bold font
        statusLabel.setText("X Turn");

        // --- Button row (Restart / Mode / Theme) ---
        JPanel buttonPanel = new JPanel();   // a small panel just for the 3 buttons
        buttonPanel.add(restartBtn);
        buttonPanel.add(modeBtn);
        buttonPanel.add(themeBtn);

        // --- Top panel holds both the status label and button row ---
        topPanel.setLayout(new GridLayout(2, 1)); // 2 rows, 1 column
        topPanel.add(statusLabel);
        topPanel.add(buttonPanel);

        // Add top panel to the NORTH (top) of the window
        frame.add(topPanel, BorderLayout.NORTH);

        // --- Board panel (3x3 grid with 5px gaps between cells) ---
        boardPanel.setLayout(new GridLayout(3, 3, 5, 5));
        frame.add(boardPanel, BorderLayout.CENTER); // CENTER fills the rest of the window

        // --------------------------------------------------------
        //  CREATE THE 9 BOARD BUTTONS
        //  We use a nested loop: outer loop = rows (i), inner = columns (j)
        // --------------------------------------------------------
        for (int i = 0; i < 3; i++) {             // i goes 0, 1, 2  (rows)
            for (int j = 0; j < 3; j++) {         // j goes 0, 1, 2  (columns)

                JButton btn = new JButton();       // create one empty button
                btn.setFont(new Font("Arial", Font.BOLD, 100)); // huge font for X and O
                btn.setFocusable(false);           // removes the dotted border when clicked

                board[i][j] = btn;                // store button in our 2D array
                boardPanel.add(btn);              // add it to the grid panel

                // When this button is clicked, call handleMove()
                // "e -> handleMove(btn)" is a lambda — a short way to write a listener
                btn.addActionListener(e -> handleMove(btn));

                // --------------------------------------------------------
                //  HOVER EFFECT
                //  When the mouse enters an empty cell → glow colour
                //  When the mouse leaves → back to normal colour
                //
                //  BUG FIX: This was previously inside applyTheme() which
                //  is called on every reset, causing listeners to STACK UP.
                //  After 3 resets, one hover triggered 3+ colour changes.
                //  Now we add the listener ONCE here and never again.
                // --------------------------------------------------------
                btn.addMouseListener(new MouseAdapter() {
                    // mouseEntered fires when the cursor moves onto the button
                    public void mouseEntered(MouseEvent e) {
                        // Only glow if the cell is empty AND the game is still going
                        if (btn.getText().equals("") && !gameOver) {
                            btn.setBackground(new Color(80, 80, 130)); // lighter purple
                        }
                    }
                    // mouseExited fires when the cursor leaves the button
                    public void mouseExited(MouseEvent e) {
                        // Only reset colour if the cell is still empty
                        if (btn.getText().equals("")) {
                            btn.setBackground(new Color(50, 50, 80)); // darker purple
                        }
                    }
                });
            }
        }

        // --------------------------------------------------------
        //  CONTROL BUTTON LISTENERS
        // --------------------------------------------------------
        restartBtn.addActionListener(e -> resetGame());   // restart when clicked
        modeBtn.addActionListener(e -> toggleMode());     // switch AI / 2-Player
        themeBtn.addActionListener(e -> toggleTheme());   // switch dark / light

        // Apply the starting colour theme
        applyTheme();

        // Make the window visible (do this LAST after adding everything)
        frame.setVisible(true);
    }

    // ========================================================
    //  handleMove  — called every time a player clicks a cell
    // ========================================================
    void handleMove(JButton btn) {

        // Ignore the click if the game is over OR the cell already has a symbol
        if (gameOver || !btn.getText().equals("")) return;

        // --------------------------------------------------------
        //  BUG FIX (turn-flip order):
        //  Old code set the text, called checkWinner(), THEN flipped isXTurn.
        //  That meant checkWinner() and updateStatus() used the WRONG turn.
        //  Correct order:
        //    1. Figure out whose symbol to place
        //    2. Place it
        //    3. Check for a winner (game might end here)
        //    4. Flip the turn
        //    5. Update the status label
        //    6. Let the AI move if needed
        // --------------------------------------------------------

        // Step 1 — decide which symbol and colour to use RIGHT NOW
        String symbol   = isXTurn ? playerX : playerO;
        Color  symColor = isXTurn
                ? new Color(100, 200, 255)   // blue for X
                : new Color(255, 160, 80);   // orange for O

        // Step 2 — place the symbol on the button
        btn.setText(symbol);
        btn.setForeground(symColor);
        playSound(); // beep!

        // Step 3 — check if this move ended the game
        if (checkWinner()) return; // if game over, stop here

        // Step 4 — flip whose turn it is
        isXTurn = !isXTurn;  // true becomes false, false becomes true

        // Step 5 — update the "X Turn / O Turn" label
        updateStatus();

        // Step 6 — if we're in AI mode and it's now O's turn, let AI move
        if (vsAI && !isXTurn) {
            aiMove();
        }
    }

    // ========================================================
    //  aiMove  — the computer picks the best empty cell
    //  Uses the Minimax algorithm to never lose
    // ========================================================
    void aiMove() {

        int bestScore   = Integer.MIN_VALUE; // start with the worst possible score
        JButton bestMove = null;             // we'll fill this with the best cell found

        // Try every empty cell
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j].getText().equals("")) {  // cell is empty?

                    board[i][j].setText(playerO);   // pretend AI plays here
                    int score = minimax(false);      // score this hypothetical move
                    board[i][j].setText("");         // undo the pretend move

                    if (score > bestScore) {         // found a better move?
                        bestScore = score;
                        bestMove  = board[i][j];     // remember this cell
                    }
                }
            }
        }

        // --------------------------------------------------------
        //  BUG FIX: Guard against null
        //  In theory bestMove should always be found (we only call
        //  aiMove when cells are empty), but a null check prevents
        //  a crash if something unexpected happens.
        // --------------------------------------------------------
        if (bestMove == null) return;

        // Place O in the best cell found
        bestMove.setText(playerO);
        bestMove.setForeground(new Color(255, 160, 80)); // orange
        playSound();

        // Check if the AI just won (or caused a draw)
        if (!checkWinner()) {
            isXTurn = true;      // give the turn back to X
            updateStatus();
        }
    }

    // ========================================================
    //  minimax  — recursive algorithm that scores every possible
    //             future game state
    //
    //  isMax = true  → it's the AI's turn  (wants to MAXIMISE score)
    //  isMax = false → it's the human's turn (wants to MINIMISE score)
    //
    //  Returns:  +1 if AI wins,  -1 if human wins,  0 for draw
    // ========================================================
    int minimax(boolean isMax) {

        // Base case — check if the game is already decided
        String result = getWinner();
        if (result != null) {
            if (result.equals(playerO)) return  1;  // AI wins   → good
            if (result.equals(playerX)) return -1;  // Human wins → bad for AI
            return 0;                                // Draw       → neutral
        }

        // Start with the worst score for the current player
        int best = isMax ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        // Try every empty cell recursively
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j].getText().equals("")) {

                    // Pretend this player goes here
                    board[i][j].setText(isMax ? playerO : playerX);

                    // Recursively score the resulting board
                    int score = minimax(!isMax);

                    // Undo the pretend move
                    board[i][j].setText("");

                    // Keep the best score found
                    best = isMax ? Math.max(score, best)
                                 : Math.min(score, best);
                }
            }
        }

        return best;
    }

    // ========================================================
    //  checkWinner  — looks at the board and reacts if someone won
    //  Returns true if the game ended (win or draw), false otherwise
    // ========================================================
    boolean checkWinner() {

        String winner = getWinner(); // ask getWinner() what happened

        if (winner == null) return false; // nobody won yet, game continues

        // Someone won or it's a draw — game is over
        gameOver = true;

        // ---- DRAW ----
        if (winner.equals("Draw")) {
            statusLabel.setText("Draw! Restarting...");

            // --------------------------------------------------------
            //  BUG FIX (the main bug you reported):
            //
            //  Old code:  new Timer(1500, e -> resetGame()).start();
            //  Problem:   Swing's Timer repeats by default — it fires
            //             resetGame() every 1500 ms FOREVER.
            //             After 2 draws you had 2 immortal timers both
            //             calling resetGame() at slightly different times,
            //             randomly flipping gameOver and locking the board
            //             in round 3.
            //
            //  Fix:
            //    1. Stop any existing timer before creating a new one
            //    2. setRepeats(false) — fires exactly ONCE then stops
            // --------------------------------------------------------
            if (drawTimer != null && drawTimer.isRunning()) {
                drawTimer.stop(); // kill any previous timer still ticking
            }
            drawTimer = new Timer(1500, e -> resetGame());
            drawTimer.setRepeats(false); // fire once, then stop automatically
            drawTimer.start();
            return true;
        }

        // ---- SOMEONE WON ----
        highlightWin(); // colour the 3 winning cells green

        if (winner.equals(playerX)) playerXScore++;
        else                        playerOScore++;

        statusLabel.setText(winner + " Wins!  X:" + playerXScore + "  O:" + playerOScore);
        return true;
    }

    // ========================================================
    //  getWinner  — checks all winning lines and returns:
    //    "X"    — if X has a line of 3
    //    "O"    — if O has a line of 3
    //    "Draw" — if all 9 cells are filled and no winner
    //    null   — game is still in progress
    //
    //  BUG FIX: Now also stores the winning coordinates in
    //  winCells so highlightWin() can target exactly those 3 cells.
    // ========================================================
    String getWinner() {

        // --- Check all 3 rows ---
        for (int i = 0; i < 3; i++) {
            if (!board[i][0].getText().equals("")                  // cell is not empty
                && board[i][0].getText().equals(board[i][1].getText()) // col0 == col1
                && board[i][1].getText().equals(board[i][2].getText())) // col1 == col2
            {
                winCells = new int[][]{{i,0}, {i,1}, {i,2}}; // remember winning row
                return board[i][0].getText();                 // return "X" or "O"
            }
        }

        // --- Check all 3 columns ---
        for (int j = 0; j < 3; j++) {
            if (!board[0][j].getText().equals("")
                && board[0][j].getText().equals(board[1][j].getText())
                && board[1][j].getText().equals(board[2][j].getText()))
            {
                winCells = new int[][]{{0,j}, {1,j}, {2,j}}; // remember winning column
                return board[0][j].getText();
            }
        }

        // --- Check diagonal: top-left → bottom-right ---
        if (!board[0][0].getText().equals("")
            && board[0][0].getText().equals(board[1][1].getText())
            && board[1][1].getText().equals(board[2][2].getText()))
        {
            winCells = new int[][]{{0,0}, {1,1}, {2,2}};
            return board[0][0].getText();
        }

        // --- Check diagonal: top-right → bottom-left ---
        if (!board[0][2].getText().equals("")
            && board[0][2].getText().equals(board[1][1].getText())
            && board[1][1].getText().equals(board[2][0].getText()))
        {
            winCells = new int[][]{{0,2}, {1,1}, {2,0}};
            return board[0][2].getText();
        }

        // --- Check for draw: are ALL cells filled? ---
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j].getText().equals("")) {
                    return null; // found an empty cell → game still going
                }
            }
        }

        // All cells filled, no winner → Draw
        winCells = null;
        return "Draw";
    }

    // ========================================================
    //  highlightWin  — colours the 3 winning cells bright green
    //
    //  BUG FIX: Old code looped all 9 cells and highlighted
    //  every cell that matched the winner's symbol (e.g. ALL X's).
    //  Now we use winCells which stores exactly the 3 winning coords.
    // ========================================================
    void highlightWin() {
        if (winCells == null) return; // safety check

        Color winColor = new Color(0, 255, 150); // bright green

        for (int[] cell : winCells) {            // loop over the 3 winning cells
            int row = cell[0];
            int col = cell[1];
            board[row][col].setBackground(winColor);
            board[row][col].setForeground(Color.BLACK); // dark text on bright green
        }
    }

    // ========================================================
    //  resetGame  — clears the board and starts a new round
    // ========================================================
    void resetGame() {

        // --------------------------------------------------------
        //  BUG FIX: Stop any pending draw timer FIRST.
        //  If the player clicks Restart during the 1.5-second draw
        //  countdown, the old timer would still fire and call
        //  resetGame() a second time, causing a double-reset glitch.
        // --------------------------------------------------------
        if (drawTimer != null && drawTimer.isRunning()) {
            drawTimer.stop();
        }

        // Reset all game-state variables
        gameOver = false;
        isXTurn  = true;
        winCells = null;

        // Clear every cell's text
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j].setText("");
            }
        }

        updateStatus(); // show "X Turn | X:0 O:0"
        applyTheme();   // restore button colours (safe — no listeners added here)
    }

    // ========================================================
    //  toggleMode  — switch between AI opponent and 2-Player mode
    // ========================================================
    void toggleMode() {
        vsAI = !vsAI;  // flip: true→false or false→true
        modeBtn.setText(vsAI ? "Mode: AI" : "Mode: 2P");
        resetGame();   // start fresh in the new mode
    }

    // ========================================================
    //  toggleTheme  — switch between dark and light themes
    // ========================================================
    void toggleTheme() {
        darkMode = !darkMode;
        applyTheme();
    }

    // ========================================================
    //  applyTheme  — sets all colours based on darkMode flag
    //
    //  BUG FIX: Mouse listeners were added here in the original
    //  code. Because applyTheme() is called on every reset, the
    //  listeners kept stacking. They are now in the constructor.
    //
    //  BUG FIX: We no longer overwrite the foreground of cells
    //  that already have a symbol — only empty cells get reset.
    // ========================================================
    void applyTheme() {

        // Choose colours based on which theme is active
        Color bgColor    = darkMode ? new Color(20, 20, 30)   : new Color(240, 240, 255);
        Color panelColor = darkMode ? new Color(30, 30, 50)   : new Color(220, 220, 250);
        Color textColor  = darkMode ? Color.WHITE              : Color.BLACK;

        // Apply colours to window and panels
        frame.getContentPane().setBackground(bgColor);
        boardPanel.setBackground(panelColor);
        topPanel.setBackground(panelColor);
        statusLabel.setForeground(textColor);

        // Apply colours to each board cell
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                JButton b = board[i][j];
                b.setBackground(new Color(50, 50, 80));         // dark purple cell
                b.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));

                // Only reset foreground colour on EMPTY cells.
                // Cells that already show X or O keep their colour.
                if (b.getText().equals("")) {
                    b.setForeground(Color.WHITE);
                }
            }
        }

        // Colour the control buttons
        restartBtn.setBackground(new Color(0, 150, 255));   // blue
        restartBtn.setForeground(Color.WHITE);

        modeBtn.setBackground(new Color(255, 140, 0));      // orange
        modeBtn.setForeground(Color.WHITE);

        themeBtn.setBackground(new Color(150, 0, 255));     // purple
        themeBtn.setForeground(Color.WHITE);
    }

    // ========================================================
    //  updateStatus  — refreshes the top label with current info
    // ========================================================
    void updateStatus() {
        String turn = isXTurn ? "X" : "O";
        statusLabel.setText(turn + " Turn  |  X:" + playerXScore + "  O:" + playerOScore);
    }

    // ========================================================
    //  playSound  — makes a beep when a move is played
    // ========================================================
    void playSound() {
        Toolkit.getDefaultToolkit().beep();
    }

    // ========================================================
    //  main  — Java starts here. Creates one TicTacToe object
    //          which triggers the constructor and opens the window.
    // ========================================================
    public static void main(String[] args) {
        new TicTacToe();
    }
}