package src.pas.tetris.agents;


import java.util.HashMap;
import java.util.Hashtable;
// SYSTEM IMPORTS
import java.util.Iterator;
import java.util.List;
import java.util.Random;


// JAVA PROJECT IMPORTS
import edu.bu.pas.tetris.agents.QAgent;
import edu.bu.pas.tetris.agents.TrainerAgent.GameCounter;
import edu.bu.pas.tetris.game.Board;
import edu.bu.pas.tetris.game.Game.GameView;
import edu.bu.pas.tetris.game.minos.Mino;
import edu.bu.pas.tetris.linalg.Matrix;
import edu.bu.pas.tetris.linalg.Shape;
import edu.bu.pas.tetris.nn.Model;
import edu.bu.pas.tetris.nn.LossFunction;
import edu.bu.pas.tetris.nn.Optimizer;
import edu.bu.pas.tetris.nn.models.Sequential;
import edu.bu.pas.tetris.nn.layers.Dense; // fully connected layer
import edu.bu.pas.tetris.nn.layers.ReLU;  // some activations (below too)
import edu.bu.pas.tetris.nn.layers.Tanh;
import edu.bu.pas.tetris.nn.layers.Sigmoid;
import edu.bu.pas.tetris.training.data.Dataset;
import edu.bu.pas.tetris.utils.Pair;


// Other imports
import edu.bu.pas.tetris.game.minos.Mino.MinoType;;


public class TetrisQAgent
    extends QAgent
{

    //public static final double EXPLORATION_PROB = 0.05;
    private Hashtable<Matrix, Integer> exploredCounts = new Hashtable<>();

    private Random random;
    private Board prevBoard;

    // PrevBoard setters and getters
    public void setPrevBoard(Board b) {
        prevBoard = b;
    }
    public Board getPrevBoard() {
        return this.prevBoard;
    }

    public TetrisQAgent(String name)
    {
        super(name);
        this.random = new Random(12345); // optional to have a seed
    }

    public Random getRandom() { return this.random; }

    @Override
    public Model initQFunction()
    {
        // System.out.println("initQFunction called!");
        // build a single-hidden-layer feedforward network
        // this example will create a 3-layer neural network (1 hidden layer)
        // in this example, the input to the neural network is the
        // image of the board unrolled into a giant vector
        final int inputLayer = 5;
        final int hiddenDim = 25;
        final int outDim = 1;
        /*Sequential: represents a neural network where layers are arranged sequentially (i.e. a line graph) */
        Sequential qFunction = new Sequential();
        /* 
        Dense: Dense connection between two groups of artificial neurons (i.e. fully connected layer). 
        This class represents the equation Z = X*W + b where W and b are Parameter matrices, and X is a batch of input examples.
        */

        qFunction.add(new Dense(inputLayer, hiddenDim));
        qFunction.add(new ReLU());
        qFunction.add(new Dense(hiddenDim, hiddenDim));
        qFunction.add(new ReLU());
        qFunction.add(new Dense(hiddenDim, outDim));

        return qFunction;
    }

    /**
        This function is for you to figure out what your features
        are. This should end up being a single row-vector, and the
        dimensions should be what your qfunction is expecting.
        One thing we can do is get the grayscale image
        where squares in the image are 0.0 if unoccupied, 0.5 if
        there is a "background" square (i.e. that square is occupied
        but it is not the current piece being placed), and 1.0 for
        any squares that the current piece is being considered for.
        
        We can then flatten this image to get a row-vector, but we
        can do more than this! Try to be creative: how can you measure the
        "state" of the game without relying on the pixels? If you were given
        a tetris game midway through play, what properties would you look for?
     */
    @Override
    public Matrix getQFunctionInput(final GameView game,
                                    final Mino potentialAction)
    {
        try
        {
            Matrix board = game.getGrayscaleImage(potentialAction);
            Shape boardSize = board.getShape();
            int c = boardSize.getNumCols();
            int r = boardSize.getNumRows();
            // FEATURE 1: Tallest occupied point on board: How close are we to losing?
            int tallestPoint = -1;
            // FEATURE 2: Topography of board. What is the bumpiness? Ideally we want our tertis board to stay flat for easy row clear
            int bumpiness = 0;
            int[] columnHeights = new int[c];
            //Search from top down. Starting at row zero and making your way down  
            for (int x = 0; x < c; x++) {
                int cHeight = 0;
                for (int y = 0; y < r; y++) {
                    if ((board.get(y,x) == 0.5 || board.get(y,x) == 1)) {
                        //System.out.println(x + "," + (r-y) + "= " + board.get(y,x));
                        //Max height of a column is the first nonzero value we come across when going from top to bottom
                        cHeight = r-y;
                        break;
                    }  
                }
                columnHeights[x] = cHeight;
                if (cHeight > tallestPoint) {
                    tallestPoint = cHeight;
                }
            }

            //Bumpiness = sum of the difference between adjacent columns's max heights
            for (int x = 0; x < c - 1; x++) {
                bumpiness += Math.abs(columnHeights[x + 1] - columnHeights[x]);
            }    

            // FEATURE 3: Does this piece clear any rows on the board? Ideally we want our action to result in full rows
            int clearedRows = 0;
            for (int y = 0; y < r; y++) {
                // Lazy check for cleared rows: there should be no cleared row unless our piece caused it. Otherwise they would have been deleted before this game state
                boolean rowCleared = true;
                for (int x = 0; x < c; x++) {
                    if (board.get(y,x) == 0) {
                        rowCleared = false;
                        break;
                    }
                }
                if (rowCleared) { clearedRows++; }
            }
            
            // FEATURE 4: Any unreachable holes in columns? Did we build a tower that leaves empty spaces impossible to reach?
            int numUnreachables = 0;
            for (int x =0; x < c; x++) {
                for (int y =0; y < r; y++) {
                    if ((r-y) > columnHeights[x]) {
                        continue;
                    } else if (((r-y) < columnHeights[x]) && (board.get(y, x) == 0)){
                        numUnreachables++;
                    }
                }
            }
            // FEATURE 5: Snuggness. How well does a piece fit with the existing piece around it?
            int snugness = 0;
            for (int y = 0; y < r; y++) {
                for (int x = 0; x < c; x++) {
                    if (board.get(y, x) == 1.0) {
                        // Check all 4 neighbors for contact with existing blocks
                        if (x > 0 && board.get(y, x - 1) == 0.5) snugness++;
                        if (x < c - 1 && board.get(y, x + 1) == 0.5) snugness++;
                        if (y > 0 && board.get(y - 1, x) == 0.5) snugness++;
                        if (y < r - 1 && board.get(y + 1, x) == 0.5) snugness++;
                    }
                }
            }

            // Determine the total number of features:
            int totalFeatures = 5;

            // Create a new Matrix to hold the full feature vector.
            Matrix fullFeatureVector = Matrix.zeros(1, totalFeatures);

            // Append the features:
            fullFeatureVector.set(0, 0, (double) tallestPoint);
            fullFeatureVector.set(0, 1, (double) bumpiness);
            fullFeatureVector.set(0, 2, (double) clearedRows);
            fullFeatureVector.set(0, 3, (double) numUnreachables);
            fullFeatureVector.set(0, 4, (double) snugness);




            /*
             * WHEN YOU ALTER FEATURE VECTOR, MUST CHANGE HOW THE METHOD:
             * getStateMoveCount() works, making sure the vector
             * creation is identical to this method
             */
            //System.out.println(tallestPoint);
            //System.out.println(bumpiness);
            //System.out.println(clearedRows);
            //System.out.println(numUnreachables);



            // updates exploredCounts with every feature vector created
            if (exploredCounts.containsKey(fullFeatureVector)) { 
                // if we've seen this feature vector
                Integer currentCount = exploredCounts.get(fullFeatureVector);
                exploredCounts.put(fullFeatureVector, currentCount + 1);
            } else {
                exploredCounts.put(fullFeatureVector, 1);
            }

            return fullFeatureVector;

        } catch(Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }

        return null;
    }

   /**
     * Function used to create quasi feature vectors based off some 
     * possible exploration game state and list of moves. Returns its count.
     * @param game
     * @param move
     */
    public Integer getStateMoveCount(GameView game, Mino move) {
        try
        { 
            Matrix board = game.getGrayscaleImage(move);
            Shape boardSize = board.getShape();
            int c = boardSize.getNumCols();
            int r = boardSize.getNumRows();
            // FEATURE 1: Tallest occupied point on board: How close are we to losing?
            int tallestPoint = -1;
            // FEATURE 2: Topography of board. What is the bumpiness? Ideally we want our tertis board to stay flat for easy row clear
            int bumpiness = 0;
            int[] columnHeights = new int[c];
            //Search from top down. Starting at row zero and making your way down  
            for (int x = 0; x < c; x++) {
                int cHeight = 0;
                for (int y = 0; y < r; y++) {
                    if ((board.get(y,x) == 0.5 || board.get(y,x) == 1)) {
                        //System.out.println(x + "," + (r-y) + "= " + board.get(y,x));
                        //Max height of a column is the first nonzero value we come across when going from top to bottom
                        cHeight = r-y;
                        break;
                    }  
                }
                columnHeights[x] = cHeight;
                if (cHeight > tallestPoint) {
                    tallestPoint = cHeight;
                }
            }

            //Bumpiness = sum of the difference between adjacent columns's max heights
            for (int x = 0; x < c - 1; x++) {
                bumpiness += Math.abs(columnHeights[x + 1] - columnHeights[x]);
            }    

            // FEATURE 3: Does this piece clear any rows on the board? Ideally we want our action to result in full rows
            int clearedRows = 0;
            for (int y = 0; y < r; y++) {
                // Lazy check for cleared rows: there should be no cleared row unless our piece caused it. Otherwise they would have been deleted before this game state
                boolean rowCleared = true;
                for (int x = 0; x < c; x++) {
                    if (board.get(y,x) == 0) {
                        rowCleared = false;
                        break;
                    }
                }
                if (rowCleared) { clearedRows++; }
            }
            
            // FEATURE 4: Any unreachable holes in columns? Did we build a tower that leaves empty spaces impossible to reach?
            int numUnreachables = 0;
            for (int x =0; x < c; x++) {
                for (int y =0; y < r; y++) {
                    if ((r-y) > columnHeights[x]) {
                        continue;
                    } else if (((r-y) < columnHeights[x]) && (board.get(y, x) == 0)){
                        numUnreachables++;
                    }
                }
            }
            int snugness = 0;
            for (int y = 0; y < r; y++) {
                for (int x = 0; x < c; x++) {
                    if (board.get(y, x) == 1.0) {
                        // Check all 4 neighbors for contact with existing blocks
                        if (x > 0 && board.get(y, x - 1) == 0.5) snugness++;
                        if (x < c - 1 && board.get(y, x + 1) == 0.5) snugness++;
                        if (y > 0 && board.get(y - 1, x) == 0.5) snugness++;
                        if (y < r - 1 && board.get(y + 1, x) == 0.5) snugness++;
                    }
                }
            }

            // Determine the total number of features:
            int totalFeatures = 5;

            // Create a new Matrix to hold the full feature vector.
            Matrix fullFeatureVector = Matrix.zeros(1, totalFeatures);

            // Append the features:
            fullFeatureVector.set(0, 0, (double) tallestPoint);
            fullFeatureVector.set(0, 1, (double) bumpiness);
            fullFeatureVector.set(0, 2, (double) clearedRows);
            fullFeatureVector.set(0, 3, (double) numUnreachables);
            fullFeatureVector.set(0, 4, (double) snugness);


            /*
             * AS WE CHANGE OUR FEATURE VECTORS, THIS FUNCTION
             * MUST CHANGE IDENTICALLY. 
             */


            // We now have our full quasi feature vector created by our QInputFunction
            // All that is left is to get its counter from exploredCounts

            if (exploredCounts.containsKey(fullFeatureVector)) {
                return exploredCounts.get(fullFeatureVector);
            } else {
                return 0;
            }

        } catch (Exception e) {
            e.printStackTrace(); 
            return 0;
        }

    }


    /*
     * This method should return a boolean on whether or not we should
     * explore a possible game state. Currently, we are given a GameView without
     * a mino. I have generated all possible Minos, their counts given by the
     * getStateMoveCount
     */
    @Override
    public boolean shouldExplore(final GameView game,
                                 final GameCounter gameCounter)
    {
        // Keep a running totalCount of all our counts for each 
        // GameView, Mino pairing
        List<Mino> possibleMoves = game.getFinalMinoPositions();
        Integer totalCount = 0;

        for (int i = 0; i < possibleMoves.size(); i++) {
            Integer count = getStateMoveCount(game, possibleMoves.get(i));
            totalCount = totalCount + count;
        }

        // Return true if our total count is less than 2 times the total
        // # of moves, i.e. if we have seen these state and move pairs an
        // average of 2 or less times
        return totalCount <= (3 * possibleMoves.size());
    }

    /**
     * This method is a counterpart to the "shouldExplore" method. Whenever we decide
     * that we should ignore our policy, we now have to actually choose an action.
     *
     * You should come up with a way of choosing an action so that the model gets
     * to experience something new. The current implemention just chooses a random
     * option, which in practice doesn't work as well as a more guided strategy.
     * I would recommend devising your own strategy here.
     */

    /*
     * This method returns the Mino in the Mino GameView pair
     * we have seen the least. 
     */
    @Override
    public Mino getExplorationMove(final GameView game)
    {
        // list of all possible moves 
        List<Mino> possibleMoves = game.getFinalMinoPositions();
        Integer lowestCount = Integer.MAX_VALUE;

        // to avoid null reference issues, initialize this as our first possible Mino
        Mino returnedMove = possibleMoves.get(0);

        for (int i = 0; i < possibleMoves.size(); i++){
            Mino move = possibleMoves.get(i);
            Integer count = getStateMoveCount(game, move);
            if (count <= lowestCount) {
                returnedMove = move;
                lowestCount = count;
            }
        }

        
        return returnedMove;
    }


    /**
     * This method is called by the TrainerAgent after we have played enough training games.
     * In between the training section and the evaluation section of a cycle, we need to use
     * the exprience we've collected (from the training games) to improve the q-function.
     *
     * You don't really need to change this method unless you want to. All that happens
     * is that we will use the experiences currently stored in the replay buffer to update
     * our model. Updates (i.e. gradient descent updates) will be applied per minibatch
     * (i.e. a subset of the entire dataset) rather than in a vanilla gradient descent manner
     * (i.e. all at once)...this often works better and is an active area of research.
     *
     * Each pass through the data is called an epoch, and we will perform "numUpdates" amount
     * of epochs in between the training and eval sections of each cycle.
     */
    @Override
    public void trainQFunction(Dataset dataset,
                               LossFunction lossFunction,
                               Optimizer optimizer,
                               long numUpdates)
    {
        for(int epochIdx = 0; epochIdx < numUpdates; ++epochIdx)
        {
            dataset.shuffle();
            Iterator<Pair<Matrix, Matrix> > batchIterator = dataset.iterator();

            while(batchIterator.hasNext())
            {
                Pair<Matrix, Matrix> batch = batchIterator.next();

                try
                {
                    Matrix YHat = this.getQFunction().forward(batch.getFirst());

                    optimizer.reset();
                    this.getQFunction().backwards(batch.getFirst(),
                                                  lossFunction.backwards(YHat, batch.getSecond()));
                    optimizer.step();
                } catch(Exception e)
                {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        }
    }

    /**
     * This method is where you will devise your own reward signal. Remember, the larger
     * the number, the more "pleasurable" it is to the model, and the smaller the number,
     * the more "painful" to the model.
     *
     * This is where you get to tell the model how "good" or "bad" the game is.
     * Since you earn points in this game, the reward should probably be influenced by the
     * points, however this is not all. In fact, just using the points earned this turn
     * is a **terrible** reward function, because earning points is hard!!
     *
     * I would recommend you to consider other ways of measuring "good"ness and "bad"ness
     * of the game. For instance, the higher the stack of minos gets....generally the worse
     * (unless you have a long hole waiting for an I-block). When you design a reward
     * signal that is less sparse, you should see your model optimize this reward over time.
     */

    //Life is pain approach: Everything contributing to height or unflat topography "hurts" our model. Model will try to minimize this pain
    @Override
    public double getReward(final GameView game)
    {
        double score = 0;

        // Repeat of our features: topography & max height, as well as available cleared rows
        Board board = game.getBoard();
        int c = board.NUM_COLS;
        int r = board.NUM_ROWS;
        int tallestPoint = -1;
        int bumpiness = 0;
        int[] columnHeights = new int[c];
        //Search from top down. Starting at row zero and making your way down  
        for (int x = 0; x < c; x++) {
            int cHeight = 0;
            for (int y = 0; y < r; y++) {
                if (board.isCoordinateOccupied(x, y)) {
                    //Max height of a column is the first nonzero value we come across when going from top to bottom
                    cHeight = r-y;
                    break;
                }  
            }
            columnHeights[x] = cHeight;
            if (cHeight > tallestPoint) {
                tallestPoint = cHeight;
            }
        }

        //Bumpiness = sum of the difference between adjacent columns's max heights
        for (int x = 0; x < c - 1; x++) {
            bumpiness += Math.abs(columnHeights[x + 1] - columnHeights[x]);
        }    

        // FEATURE 3: Does this piece clear any rows on the board?
            // According to professor wood we can never really detect a cleared row -- it will be deleted from the game state before this function is called
            // The work-around is just checking the score earned this turn
        int scoreThisTurn = game.getScoreThisTurn();

        // FEATURE 4: How many empty spaces are under each columns highest piece (we don't want a board with lots of unreachable holes)
        int[] colEmptySpace = new int[c];
        for (int x = 0; x < c; x++) {
            for (int y = 0; y < r; y++) {
                if ((r-y) > columnHeights[x]) {
                    continue;
                } else if (((r-y) < columnHeights[x]) && (!board.isCoordinateOccupied(x,y))){
                    colEmptySpace[x] += 1;
                }
            }
        }

        // FEATURE 5: How well did our next piece 'fit' into this board. Compare current board to the previous board. Much simpler than keeping track of individual Mino coordinates
        // Ranges from value [-4,4] - there are 4 blocks (coordinates) per mino. Ideally, all 4 blocks to a mino fit nicely with their neighbors
        int snugness = 0;
        int[][] newMinoCoords = new int[4][2];
        if (scoreThisTurn > 0) {
            snugness = 4;
        } else if (prevBoard == null) {
            // First turn. There is no previous board to compare snugness to. Prioritize putting pieces in corners.
            for (int x = 0; x < c-1; x++) {
                for (int y =0; y < r; y++) {
                    if (board.isCoordinateOccupied(9, y)) {
                        snugness++;
                    }
                    if (board.isCoordinateOccupied(x, 21)) {
                        snugness++;
                    }
                }
            }
        } else {
            Board prevBoard = this.getPrevBoard();
            // Find the piece that wasn't there last time. Inferred by seeing what spaces were filled out in the new board
            int piece_iterator = 0;
            for (int x = 0; x < c-1; x++) {
                for (int y = 0; y < r-1; y++) {
                    if (prevBoard.isCoordinateOccupied(x,y) && board.isCoordinateOccupied(x,y)) {
                        continue;
                    } else if (!prevBoard.isCoordinateOccupied(x,y) && board.isCoordinateOccupied(x,y)){
                        newMinoCoords[piece_iterator][0] = x;
                        newMinoCoords[piece_iterator][1] = y;
                        piece_iterator++;
                    }
                }
            }

            for (int piece = 0; piece < 4; piece++) {
                int nx = newMinoCoords[piece][0];
                int ny = newMinoCoords[piece][1];
                int verticalHeight = (r-ny);
                if (nx > 0 && board.isCoordinateOccupied(nx - 1, ny)) snugness+= verticalHeight;
                if (nx < c - 1 && board.isCoordinateOccupied(nx + 1, ny)) snugness+= verticalHeight;
                if (ny > 0 && board.isCoordinateOccupied(nx, ny - 1)) snugness+= verticalHeight;
                if (ny < r - 1 && board.isCoordinateOccupied(nx, ny + 1)) snugness+= verticalHeight;
            }
        }
        //System.out.println(snugness);
        // prioritizing a flat board, with no unreachable holes, and a board with cleared rows
        score = (bumpiness * -4);

        //Exponential tallestPoint penalty tries to prevent stacking
        score -= Math.pow(tallestPoint*2, 2.5);

        for (int i = 0; i < colEmptySpace.length; i++) {
            score -= (colEmptySpace[i]*3);
        }
        score += (scoreThisTurn*100); // score earned from placing previous mino
        score += (Math.min(snugness, 4) * 0.5);

        //if (columnHeights[0] < tallestPoint - 5) score -= 30;
        //if (columnHeights[c-1] < tallestPoint - 5) score -= 30;

        this.setPrevBoard(board);
        return score;
    }

}
