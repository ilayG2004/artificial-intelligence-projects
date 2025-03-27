package src.pas.pokemon.agents;


// SYSTEM IMPORTS....feel free to add your own imports here! You may need/want to import more from the .jar!
import edu.bu.pas.pokemon.core.Agent;
import edu.bu.pas.pokemon.core.Battle;
import edu.bu.pas.pokemon.core.Battle.BattleView;
import edu.bu.pas.pokemon.core.Team;
import edu.bu.pas.pokemon.core.Team.TeamView;
import edu.bu.pas.pokemon.core.Move;
import edu.bu.pas.pokemon.core.Move.MoveView;
import edu.bu.pas.pokemon.utils.Pair;
//Imported stat and typing enums
import edu.bu.pas.pokemon.core.enums.Stat;
import edu.bu.pas.pokemon.core.enums.Type;
import edu.bu.pas.pokemon.core.DamageEquation;
import edu.bu.pas.pokemon.core.Pokemon;
import edu.bu.pas.pokemon.core.Pokemon.PokemonView;
import edu.bu.pas.pokemon.core.SwitchMove;



import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


// JAVA PROJECT IMPORTS


public class TreeTraversalAgent extends Agent  {
    private Node rootNode;
	private class StochasticTreeSearcher extends Object implements Callable<Pair<MoveView, Long> >  // so this object can be run in a background thread
	{

        // TODO: feel free to add any fields here! If you do, you should probably modify the constructor
        // of this class and add some getters for them. If the fields you add aren't final you should add setters too!
		private final BattleView rootView;
        private final int maxDepth;
        private final int myTeamIdx;
        

        // If you change the parameters of the constructor, you will also have to change
        // the getMove(...) method of TreeTraversalAgent!
		public StochasticTreeSearcher(BattleView rootView, int maxDepth, int myTeamIdx)
        {
            this.rootView = rootView;
            this.maxDepth = maxDepth;
            this.myTeamIdx = myTeamIdx;
        }


        // Getter methods. Since the default fields are declared final, we don't need setters
        // but if you make any fields that aren't final you should give them setters!
		public BattleView getRootView() { return this.rootView; }
        public int getMaxDepth() { return this.maxDepth; }
        public int getMyTeamIdx() { return this.myTeamIdx; }

		/**
		 * TODO: implement me!
		 * This method should perform your tree-search from the root of the entire tree.
         * You are welcome to add any extra parameters that you want! If you do, you will also have to change
         * The call method in this class!
		 * @param node the node to perform the search on (i.e. the root of the entire tree)
		 * @return The MoveView that your agent should execute
		 */
        public boolean isTerminalState(BattleView battle) {
            Team temp_team_me = new Team(battle.getTeam1View());
            Team temp_team_opp = new Team(battle.getTeam2View());
            //Terminal state?
            if (temp_team_me.getNumAlivePokemon() == 0) {
                //Enemy wins
                //((MinMaxNode)node).setUtility(-6);
                return true;
            } else if (temp_team_opp.getNumAlivePokemon() == 0) {
                //We win
                //((MinMaxNode)node).setUtility(6);
                return true;
            } else {
                return false;
            }
        }
        public MoveView stochasticTreeSearch( BattleView rootView) //, int depth)
        {
            /*
            Node bestChild = null;
            
            //BASE CASE: TERMINAL NODE
            if (isTerminalState(rootView)) {
                return ((MinMaxNode)root).getParent().get(Move);
            }
        
            if (root instanceof MinMaxNode) {
                //Max node
                if (((MinMaxNode)root).getMinMax().equals("max")) {
                    double alpha = Double.POSITIVE_INFINITY;
                    List<Node> children = ((MinMaxNode)root).getKids();
                    for (Node child : children) {
                        alpha = Math.min(alpha, stochastic)
                    }
                //Min node
                } else {

                }
            }
                 */
            return null;
        }

        @Override
        public Pair<MoveView, Long> call() throws Exception
        {
            double startTime = System.nanoTime();

            MoveView move = this.stochasticTreeSearch(this.getRootView());
            double endTime = System.nanoTime();

            return new Pair<MoveView, Long>(move, (long)((endTime-startTime)/1000000));
        }
		
	}

	private final int maxDepth;
    private long maxThinkingTimePerMoveInMS;

	public TreeTraversalAgent()
    {
        super();
        this.maxThinkingTimePerMoveInMS = 180000 * 2; // 6 min/move
        this.maxDepth = 1000; // set this however you want
    }

    /**
     * Some constants
     */
    public int getMaxDepth() { return this.maxDepth; }
    public long getMaxThinkingTimePerMoveInMS() { return this.maxThinkingTimePerMoveInMS; }

    @Override
    public Integer chooseNextPokemon(BattleView view)
    {
        // TODO: replace me! This code calculates the first-available pokemon.
        // It is likely a good idea to expand a bunch of trees with different choices as the active pokemon on your
        // team, and see which pokemon is your best choice by comparing the values of the root nodes.


        for(int idx = 0; idx < this.getMyTeamView(view).size(); ++idx)
        {
            if(!this.getMyTeamView(view).getPokemonView(idx).hasFainted())
            {
                //Is this living pokemon resistant to the enemy?
                if (this.getMyTeamView(view).getPokemonView(idx).getCurrentType1().isSuperEffective(this.getOpponentTeamView(view).getActivePokemonView().getCurrentType1(), this.getOpponentTeamView(view).getActivePokemonView().getCurrentType2()) || 
                this.getMyTeamView(view).getPokemonView(idx).getCurrentType2().isSuperEffective(this.getOpponentTeamView(view).getActivePokemonView().getCurrentType1(), this.getOpponentTeamView(view).getActivePokemonView().getCurrentType2())) {
                    return idx;
                }
            }
        }
        //Otherwise just pick the first not dead pokemon
        for(int idx = 0; idx < this.getMyTeamView(view).size(); ++idx)
        {
            if(!this.getMyTeamView(view).getPokemonView(idx).hasFainted())
            {
                //Is this living pokemon resistant to the enemy?
                return idx;
            }
        }
        return null;
    }

    /**
     * This method is responsible for getting a move selected via the minimax algorithm.
     * There is some setup for this to work, namely making sure the agent doesn't run out of time.
     * Please do not modify.
     */
    @Override
    public MoveView getMove(BattleView battleView)
    {

        // will run the minimax algorithm in a background thread with a timeout
        ExecutorService backgroundThreadManager = Executors.newSingleThreadExecutor();

        // preallocate so we don't spend precious time doing it when we are recording duration
        MoveView move = null;
        long durationInMs = 0;

        // this obj will run in the background
        StochasticTreeSearcher searcherObject = new StochasticTreeSearcher(
            battleView,
            this.getMaxDepth(),
            this.getMyTeamIdx()
        );

        // submit the job
        Future<Pair<MoveView, Long> > future = backgroundThreadManager.submit(searcherObject);

        try
        {
            // set the timeout
            Pair<MoveView, Long> moveAndDuration = future.get(
                this.getMaxThinkingTimePerMoveInMS(),
                TimeUnit.MILLISECONDS
            );

            // if we get here the move was chosen quick enough! :)
            move = moveAndDuration.getFirst();
            durationInMs = moveAndDuration.getSecond();

            // convert the move into a text form (algebraic notation) and stream it somewhere
            // Streamer.getStreamer(this.getFilePath()).streamMove(move, Planner.getPlanner().getGame());
        } catch(TimeoutException e)
        {
            // timeout = out of time...you lose!
            System.err.println("Timeout!");
            System.err.println("Team [" + (this.getMyTeamIdx()+1) + " loses!");
            System.exit(-1);
        } catch(InterruptedException e)
        {
            e.printStackTrace();
            System.exit(-1);
        } catch(ExecutionException e)
        {
            e.printStackTrace();
            System.exit(-1);
        }

        return move;
    }
    

    public int utilFunction(MoveView move, BattleView battle, BattleView projecBattleView, PokemonView casterView, PokemonView enemyView) {
        int lowerbound = -6;
        int upperbound = 6;
        int currentUtil = 0;
        Move.Category moveCategory = move.getCategory();
        boolean isSpecialDefender = false;
        if (this.getOpponentTeamView(battle).getActivePokemonView().getCurrentStat(Stat.SPDEF) > this.getOpponentTeamView(battle).getActivePokemonView().getCurrentStat(Stat.DEF) ) {
            isSpecialDefender = true;
        }
        if (!(move instanceof SwitchMove.SwitchMoveView)) {
            if (moveCategory == Move.Category.SPECIAL || moveCategory == Move.Category.PHYSICAL) {
            Type moveType = move.getType();
            Type[] myPokemonTypes = new Type[2];
            myPokemonTypes[0] = casterView.getCurrentType1();
            myPokemonTypes[1] = casterView.getCurrentType2();

            //Get enemy typings
            Type[] enemyTypes = new Type[2];
            enemyTypes[0] = enemyView.getCurrentType1();
            enemyTypes[1] = enemyView.getCurrentType2();

            //OKO - JUST RETURN 6
            if (getOpponentTeamView(projecBattleView).getActivePokemonView().getCurrentStat(Stat.HP) <= 0) {
                return 6;
            }

            //1.5x damage - STAB BONUS
            if (moveType.equals(myPokemonTypes[0]) || moveType.equals(myPokemonTypes[1])) {
                currentUtil += 2;
            }

            //4x damage
            if (Type.isSuperEffective(moveType, enemyTypes[0]) && Type.isSuperEffective(moveType, enemyTypes[1])) {
                currentUtil +=3;
            //2x damage
            } else if (Type.isSuperEffective(moveType, enemyTypes[0]) || Type.isSuperEffective(moveType, enemyTypes[1])){
                currentUtil +=2;
            //4x resistance
            } else if (Type.isSuperEffective(enemyTypes[0], moveType) && Type.isSuperEffective(enemyTypes[1], moveType)) {
                currentUtil -= 3;
            //2x resistance
            } else if (Type.isSuperEffective(enemyTypes[0], moveType) || Type.isSuperEffective(enemyTypes[1], moveType)) {
                currentUtil -= 2;
            }
            //EVALUTE OVERALL DAMAGE
            //Did we miss?
            if (getOpponentTeamView(projecBattleView).getActivePokemonView().getCurrentStat(Stat.HP) != getOpponentTeamView(battle).getActivePokemonView().getCurrentStat(Stat.HP)) {
                //<0.2 of enemy's current health
                if (getOpponentTeamView(projecBattleView).getActivePokemonView().getCurrentStat(Stat.HP) >= 0.8 * getOpponentTeamView(battle).getActivePokemonView().getCurrentStat(Stat.HP)) {
                    currentUtil -= 3;
                //>1/3 of enemy's current health
                } else if (getOpponentTeamView(projecBattleView).getActivePokemonView().getCurrentStat(Stat.HP) <= 0.77 * getOpponentTeamView(battle).getActivePokemonView().getCurrentStat(Stat.HP) && getOpponentTeamView(projecBattleView).getActivePokemonView().getCurrentStat(Stat.HP) >= 0.5 * getOpponentTeamView(battle).getActivePokemonView().getCurrentStat(Stat.HP)) {
                    currentUtil += 1;
                    //>1/2 of enemy's current health
                } else if (getOpponentTeamView(projecBattleView).getActivePokemonView().getCurrentStat(Stat.HP) <= 0.5 * getOpponentTeamView(battle).getActivePokemonView().getCurrentStat(Stat.HP)) {
                        currentUtil += 2;
                }
            } else {
                //Miss. Health is exactly the same
                currentUtil = -5;
            }
                //Are we attacking with the opponent's weaker defense stat?
                //Possibly unneccessary but really useful when attacking Psychic types
            if (moveCategory == Move.Category.PHYSICAL && isSpecialDefender) {
                currentUtil += 1;
            } else {
                currentUtil -= 1;
            }
            if (moveCategory == Move.Category.SPECIAL && !(isSpecialDefender)) {
                currentUtil += 1;
            } else {
                currentUtil -=1 ;
            }
        //STATUS EFFECT
        } else {
            //ILAY TODO: SET SPECIFIC UTILITY VALUES BASED ON WHAT STATUS AND WHO IT IS EFFECTING

            //NOTE: This function DOES NOT ACCOUNT FOR PROBABILITY OF OUR STATUS ATTACK MOVE FAILING
                //GEN 1 POKEMON -- ACCURACY IS THE BEST THING TO LOWER AGAINST OPPONENTS
            if (getOpponentTeamView(projecBattleView).getActivePokemonView().getCurrentStat(Stat.ACC) < getOpponentTeamView(battle).getActivePokemonView().getCurrentStat(Stat.ACC)) {
                currentUtil += 3;
            }
            
            // 2. Useful to decrease attack or special defense
            if (isSpecialDefender) {
                if (getOpponentTeamView(projecBattleView).getActivePokemonView().getCurrentStat(Stat.SPATK) < getOpponentTeamView(battle).getActivePokemonView().getCurrentStat(Stat.SPATK)) {
                    currentUtil += 2;
                } else if (getOpponentTeamView(projecBattleView).getActivePokemonView().getCurrentStat(Stat.ATK) < getOpponentTeamView(battle).getActivePokemonView().getCurrentStat(Stat.ATK)) {
                    currentUtil += 1;
                }
            } else {
                if (getOpponentTeamView(projecBattleView).getActivePokemonView().getCurrentStat(Stat.SPATK) < getOpponentTeamView(battle).getActivePokemonView().getCurrentStat(Stat.SPATK)) {
                    currentUtil += 1;
                } else if (getOpponentTeamView(projecBattleView).getActivePokemonView().getCurrentStat(Stat.ATK) < getOpponentTeamView(battle).getActivePokemonView().getCurrentStat(Stat.ATK)) {
                    currentUtil += 2;
                }
            }
                // 3. Speed also useful to decrease
            if (getOpponentTeamView(projecBattleView).getActivePokemonView().getCurrentStat(Stat.SPD) < getOpponentTeamView(battle).getActivePokemonView().getCurrentStat(Stat.SPD)) {
                currentUtil += 2;
            }
                
            // 4. Defnse is & evasiveness low priority for lowering. Who casres
            if (getOpponentTeamView(projecBattleView).getActivePokemonView().getCurrentStat(Stat.DEF) < getOpponentTeamView(battle).getActivePokemonView().getCurrentStat(Stat.DEF)) {
                currentUtil -= 1;
            } else if (getOpponentTeamView(projecBattleView).getActivePokemonView().getCurrentStat(Stat.DEF) < getOpponentTeamView(battle).getActivePokemonView().getCurrentStat(Stat.DEF)) {
                currentUtil -= 1;
            }

            //INCREASING OUR STATS
            //1. If we are a special user, special attack/defense is really useful
            if (isSpecialDefender) {
                //Special attackers should prioritize increasing their own SPECIAl stat over other stats
                if (getMyTeamView(projecBattleView).getActivePokemonView().getCurrentStat(Stat.SPATK) > getMyTeamView(battle).getActivePokemonView().getCurrentStat(Stat.SPATK)) {
                    //GET READY FOR THE ALAKAZAM SWEEEP
                    currentUtil += 3;
                } else if (getMyTeamView(projecBattleView).getActivePokemonView().getCurrentStat(Stat.ATK) > getMyTeamView(battle).getActivePokemonView().getCurrentStat(Stat.ATK)) {
                    currentUtil += 1;
                }
            } else {
                //Phjsyical attackers should prioritize increasing their own ATK stat over SPECIAL
                if (getMyTeamView(projecBattleView).getActivePokemonView().getCurrentStat(Stat.ATK) > getMyTeamView(battle).getActivePokemonView().getCurrentStat(Stat.ATK)) {
                    currentUtil += 2;
                } else if (getMyTeamView(projecBattleView).getActivePokemonView().getCurrentStat(Stat.SPATK) > getMyTeamView(battle).getActivePokemonView().getCurrentStat(Stat.SPATK)) {
                    currentUtil += 1;
                }
            }
            
            //2.  If we were originally slower than out opponent but this stat move made us faster. Increase utiloity a good bit, otherwise not useful
            if (getMyTeamView(battle).getActivePokemonView().getCurrentStat(Stat.SPD) < getOpponentTeamView(battle).getActivePokemonView().getCurrentStat(Stat.SPD) && getMyTeamView(projecBattleView).getActivePokemonView().getCurrentStat(Stat.SPD) > getOpponentTeamView(projecBattleView).getActivePokemonView().getCurrentStat(Stat.SPD)) {
                currentUtil += 2;
            } else if ((getMyTeamView(battle).getActivePokemonView().getCurrentStat(Stat.SPD) < getOpponentTeamView(battle).getActivePokemonView().getCurrentStat(Stat.SPD) && getMyTeamView(projecBattleView).getActivePokemonView().getCurrentStat(Stat.SPD) <= getOpponentTeamView(projecBattleView).getActivePokemonView().getCurrentStat(Stat.SPD))){
                currentUtil -= 2;
            }

            //3. Defnse is useless. Honestly a waste of a valuable turn to increase defense stat
            if (getMyTeamView(projecBattleView).getActivePokemonView().getCurrentStat(Stat.DEF) > getMyTeamView(battle).getActivePokemonView().getCurrentStat(Stat.DEF)) {
                currentUtil -=5 ;
            }

            //4. Accuracy also useless. If our accuracy dips low enough we should just switch pokemon to reverse the effect
            if (getMyTeamView(projecBattleView).getActivePokemonView().getCurrentStat(Stat.ACC) > getMyTeamView(battle).getActivePokemonView().getCurrentStat(Stat.ACC)) {
                currentUtil -=5 ;
            }
        }
        //Evaluate utility for a switchmove node
        } else {
            //swithcmove
            currentUtil = 0;
        }
        if (currentUtil > upperbound) {
            currentUtil = upperbound;
        } else if (currentUtil < lowerbound) {
            currentUtil = lowerbound;
        }
        return currentUtil;
    }

/*
    public void util(Node node) {
        int lowerbound = -6;
        int upperbound = 6;
        
        int caster = -1;
        int opp = -1;

        BattleView battle = (node).getBattle();

        PokemonView casterView = this.getMyTeamView(battle).getActivePokemonView();
        PokemonView enemyView = this.getOpponentTeamView(battle).getActivePokemonView();

        if (node instanceof MinMaxNode) {
            if (((MinMaxNode)node).getMinMax().equals("max")) {
                casterView = this.getMyTeamView(battle).getActivePokemonView();
                enemyView = this.getOpponentTeamView(battle).getActivePokemonView();
    
                caster = 0;
                opp = 1;
            } else {
                enemyView = this.getMyTeamView(battle).getActivePokemonView();
                casterView = this.getOpponentTeamView(battle).getActivePokemonView();
                
                caster = 1;
                opp = 0;
            }
        } else {
            //WHY ARE WE DOING THIS???
            ((MOC)node).setUtility(opp);
        }
        
        //Terminal node?
        Team temp_team_me = new Team(battle.getTeam1View());
        Team temp_team_opp = new Team(battle.getTeam2View());

        //Terminal state?
        if (temp_team_me.getNumAlivePokemon() == 0) {
            //Enemy wins
            ((MinMaxNode)node).setUtility(-6);
        } else if (temp_team_opp.getNumAlivePokemon() == 0) {
            //We win
            ((MinMaxNode)node).setUtility(6);
        } else {
            List<MoveView> possibleMoves = ((MinMaxNode)node).getMoves();
            if (!possibleMoves.isEmpty()) {
                for (MoveView move : possibleMoves) {
                    List<Pair<Double, Battle.BattleView>> outcomes = move.getPotentialEffects(battle, caster, opp);
                    double expectedUtility = 0.0;
                    for (Pair<Double, Battle.BattleView> outcome : outcomes) {
                        double probability = outcome.getFirst();
                        BattleView newState = outcome.getSecond();

                        int utilityValue = utilFunction(move, battle, newState, casterView, enemyView);
                        expectedUtility += probability * utilityValue;
                    }
                    ((MinMaxNode)node).addMoveExpectedUtil(expectedUtility);
                }
            }   
        }
                
    }
         */
}
