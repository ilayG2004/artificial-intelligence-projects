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

    public static final double EXPLORATION_PROB = 0.05;

    private Random random;

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
        final int inputLayer = 4;
        final int hiddenDim = 16;
        final int outDim = 1;
        /*Sequential: represents a neural network where layers are arranged sequentially (i.e. a line graph) */
        Sequential qFunction = new Sequential();
        /* 
        Dense: Dense connection between two groups of artificial neurons (i.e. fully connected layer). 
        This class represents the equation Z = X*W + b where W and b are Parameter matrices, and X is a batch of input examples.
        */

        qFunction.add(new Dense(inputLayer, hiddenDim));
        qFunction.add(new Tanh());
        qFunction.add(new Dense(hiddenDim, hiddenDim));
        qFunction.add(new Tanh());
        qFunction.add(new Dense(hiddenDim, hiddenDim));
        qFunction.add(new Tanh());
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
                    if ((board.get(x,y) == 0.5 || board.get(x,y) == 1)) {
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
                for (int x = 0; x < c; c++) {
                    if (board.get(x,y) == 0) {
                        rowCleared = false;
                        break;
                    }
                }
                if (rowCleared) { clearedRows++; }
            }

            

            // Determine the total number of features:
            // - flattenedImage is a row vector with some number of elements
            // - plus 3 additional features (tallest point, bumpiness, rows cleared by this action)
            int totalFeatures = 3;

            // Create a new Matrix to hold the full feature vector.
            Matrix fullFeatureVector = Matrix.zeros(1, totalFeatures);

            // Append the features:
            fullFeatureVector.set(0, 0, (double) tallestPoint);
            fullFeatureVector.set(0, 1, (double) bumpiness);
            fullFeatureVector.set(0, 2, (double) clearedRows);

            return fullFeatureVector;

        } catch(Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }

        return null;
    }

    /**
     * This method is used to decide if we should follow our current policy
     * (i.e. our q-function), or if we should ignore it and take a random action
     * (i.e. explore).
     *
     * Remember, as the q-function learns, it will start to predict the same "good" actions
     * over and over again. This can prevent us from discovering new, potentially even
     * better states, which we want to do! So, sometimes we should ignore our policy
     * and explore to gain novel experiences.
     *
     * The current implementation chooses to ignore the current policy around 5% of the time.
     * While this strategy is easy to implement, it often doesn't perform well and is
     * really sensitive to the EXPLORATION_PROB. I would recommend devising your own
     * strategy here.
     */
    @Override
    public boolean shouldExplore(final GameView game,
                                 final GameCounter gameCounter)
    {
        // System.out.println("cycleIdx=" + gameCounter.getCurrentCycleIdx() + "\tgameIdx=" + gameCounter.getCurrentGameIdx());
        // Less likely to explore as game counter increases
        long maxCycles = gameCounter.getNumCycles();
        long currentCycle = gameCounter.getCurrentCycleIdx();

        // In the beginning, explore 50% of time, half way thru cycles explore 25% of time, all the way thru 5% of the time
        double exploration_prob = 0.5 - (0.2 * (currentCycle*2/maxCycles));
        return this.getRandom().nextDouble() <= exploration_prob;
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
    @Override
    public Mino getExplorationMove(final GameView game)
    {
        int randIdx = this.getRandom().nextInt(game.getFinalMinoPositions().size());
        return game.getFinalMinoPositions().get(randIdx);
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

        // prioritizing a flat board, and a board with cleared rows
        score = (bumpiness * -3);
        score -= (tallestPoint * 2);
        score += (scoreThisTurn/6); // score earned from placing previous mino

        return score;
    }

}
