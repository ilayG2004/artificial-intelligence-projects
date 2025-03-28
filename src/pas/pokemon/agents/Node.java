package src.pas.pokemon.agents;


// SYSTEM IMPORTS....feel free to add your own imports here! You may need/want to import more from the .jar!
import edu.bu.pas.pokemon.core.Agent;
import edu.bu.pas.pokemon.core.Battle;
import edu.bu.pas.pokemon.core.Battle.BattleView;
import edu.bu.pas.pokemon.core.Team;
import edu.bu.pas.pokemon.core.Team.TeamView;
import edu.bu.pas.pokemon.core.Move;
import edu.bu.pas.pokemon.core.Move.MoveView;
import edu.bu.pas.pokemon.core.Pokemon.PokemonView;
import edu.bu.pas.pokemon.utils.Pair;

import edu.bu.pas.pokemon.core.enums.Stat;


import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
public class Node {
  private BattleView state;
  private int depth;
  private String type;
  private MoveView move;
  private List<Node> children;
  private double utility;
  public Node(BattleView state, int depth, String type, MoveView move) {
    this.state = state;
    this.depth = depth;
    this.type = type;
    this.move = move;
    this.children = new ArrayList<>();
    this.utility = 0;
  }
  public String toString() {
    if (move != null) {
      return depth + ":  " + type + "   " + move.getName() ;
    } 
    return type;
  }

  public List<Node> getChildren() {
    return this.children;
  }
  public String getType() {
    return this.type;
  }
  public BattleView getBattle() {
    return this.state;
  }
  public MoveView getMove() {
    return this.move;
  }
  public void setUtility(double u) {
    this.utility = u;
  }
  public double getUtility() {
    return this.utility;
  }
  public int getDepth() {
    return this.depth;
  }

  public boolean isTerminalState(BattleView battle) {
    Team temp_team_me = new Team(battle.getTeam1View());
    Team temp_team_opp = new Team(battle.getTeam2View());
    //Terminal state?
    if (temp_team_me.getNumAlivePokemon() == 0) {
        //Enemy wins
        //((MinMaxNode)node).setUtility(-6);
        System.out.println("Enemy wins");
        return true;
    } else if (temp_team_opp.getNumAlivePokemon() == 0) {
        //We win
        //((MinMaxNode)node).setUtility(6);
        System.out.println("We wins");
        return true;
    } else {
        return false;
    }
  }

  public BattleView mostProbableOutcome(MoveView move, int myTeamIdx, int oppTeamIdx) {
    System.out.println("fetching potential effects for " + move.getName());
    List<Pair<Double, BattleView>> outcomes = move.getPotentialEffects(state, myTeamIdx, oppTeamIdx);
    double bestProb = Double.NEGATIVE_INFINITY;
    BattleView bestBattle = null;
    for (Pair<Double,BattleView> outcome : outcomes) {
      if (outcome.getFirst() > bestProb) {
        bestProb = outcome.getFirst();
        bestBattle = outcome.getSecond();
      }
    }
    return bestBattle;
  }

  public void generateChildren(int myTeamIdx) {
    if (this.depth == 3) {System.out.println("DEPTH: " + this.getDepth() + "  " +this.toString());}
    if (this.depth > 3) { return; }
    int oppTeamIdx = (myTeamIdx == 0) ? 1 : 0;
    TeamView myTeam = state.getTeamView(myTeamIdx);
    PokemonView myPoke = myTeam.getActivePokemonView();
  
    if (this.isTerminalState(state)) return;
  
    // Product of an attack is a chance node
    if (this.type.equals("max") || this.type.equals("min")) {
      List<MoveView> moves = myPoke.getAvailableMoves();

      for (MoveView move : moves) {
        if (move == null || move.getPP() == null || move.getPP() <= 0) continue;
        //System.out.println(move.getName());
        //if (move.getName().equals("Light Screen") || move.getName().equals("Double Slap") || move.getName().equals("Pin Missile")) { continue;}
        
        BattleView mostLikelyOutcome = mostProbableOutcome(move, myTeamIdx, oppTeamIdx);
        Node chanceNode = new Node(mostLikelyOutcome, this.depth + 1, "chance", move);
        this.children.add(chanceNode);
      }
    //Product of a chance ndoe is an attack
    } else if (this.type.equals("chance")) {
      if (myTeamIdx == 0) {
        Node newNode = new Node(this.state, this.depth + 1, "min", null);
        this.children.add(newNode); // Opponent acts next
      } else {
        Node newNode = new Node(this.state, this.depth + 1, "max", null);
        this.children.add(newNode); // Our agent acts next
      }
    }
  }
  
}