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
/*
public interface Node {
  void addChild(Node child);
  double getUtility();
  void setUtility(double util);
  Node getParent();
  void setParent(Node parent);
  List<Node> getChildren();
  void setBattle(BattleView battle);
  BattleView getBattle();
}

class MOC implements Node {
  private Double left_prob = 0.0;
  private Double right_prob = 0.0;
  private double utility;
  private List<Node> children = new ArrayList<>();
  private Node parent;
  private edu.bu.pas.pokemon.core.Battle.BattleView battle;
  private MoveView optimalMove;

  public MOC() {
      this.utility = 0;
      this.parent = null;
  }

  public MOC(Node parent) {
    this.utility = 0;
    this.parent = parent;
  }
  public void addOptimalMove(MoveView move) {
    this.optimalMove = move;
  }
  public MoveView getOptimalMove() {
    return this.optimalMove;
  }
  @Override
  public void addChild(Node child) {
      this.children.add(child);
  }

  public void setProbs(double prob) {
    this.left_prob = prob;
    this.right_prob = 1 - prob;
  }

  public double getLeftProb() {
    return this.left_prob;
  }

  public double getRightProb() {
    return this.right_prob;
  }

  @Override
  public double getUtility() {
      return this.utility;
  }

  @Override
  public void setUtility(double util) {
      this.utility = util;
  }

  @Override
  public Node getParent() {
      return this.parent;
  }

  @Override
  public void setParent(Node parent) {
      this.parent = parent;
  }

  @Override
  public List<Node> getChildren() {
      return this.children;
  }

  @Override
  public void setBattle(BattleView battle) {
    this.battle = battle;
  }

  @Override
  public BattleView getBattle() {
    return this.battle;
  }

  @Override
  public String toString() {
    if (parent != null) {
      return "MOC Node [Left Prob: " + this.left_prob + ", Right Prob: " + this.right_prob + "]" + ". Parent=" + parent.toString();
    } else {
      return "MOC Node [Left Prob: " + this.left_prob + ", Right Prob: " + this.right_prob + "]" + ". Parent=";
    }
  }
}
class MinMaxNode implements Node {
  private String minmax;
  private boolean isChance = false;
  private boolean isSecond = false;
  private double utility;
  private List<Node> children = new ArrayList<>();
  private Node parent;
  private edu.bu.pas.pokemon.core.Battle.BattleView battle;
  private List<edu.bu.pas.pokemon.core.Move.MoveView> possibleMoves;
  private HashMap<MoveView, List<Pair<Double, BattleView>>> outcomes;
  private List<Double> moveUtils = new ArrayList<>();


  public MinMaxNode(String minmax, BattleView battle, List<MoveView> moves, Node root) {
      this.minmax = minmax;
      this.possibleMoves = moves;
      this.battle = battle;
      this.parent = root;
  }

  // DUMMY FUNCTION
  @Override
  public void addChild(Node child) {
    this.children.add(child);
  }

  // DUMMY FUNCTION
  @Override
  public List<Node> getChildren() {
    List<Node> lst = new ArrayList<>();
    return lst;
  }

  public void setOutcomes(HashMap<MoveView, List<Pair<Double, BattleView>>> lst) {
    this.outcomes = lst;
  }

  public HashMap<MoveView, List<Pair<Double, BattleView>>> getOutcomes() {
    return this.outcomes;
  }

  // REAL ADDCHILD FUNCTION
  public void addKid(Node child) {
    this.children.add(child);
  }

  // REAL GETCHILDREN FUNCTION
  public List<Node> getKids() {
    return this.children;
  }

  public void setSecond() {
    this.isSecond = true;
  }

  public boolean getSecond() {
    return this.isSecond;
  }

  @Override
  public double getUtility() {
      return this.utility;
  }

  @Override
  public void setUtility(double util) {
      this.utility = util;
  }

  @Override
  public Node getParent() {
      return this.parent;
  }

  @Override
  public void setParent(Node parent) {
      this.parent = parent;
  }

  public String getMinMax() {
      return this.minmax;
  }

  public List<MoveView> getMoves() {
    return this.possibleMoves;
  }

  public void setMoves(List<MoveView> moves) {
    this.possibleMoves = moves;
  }

  public void setBattle(BattleView battle) {
    this.battle = battle;
  }

  public BattleView getBattle() {
    return this.battle;
  }
  public String moveToString(List<edu.bu.pas.pokemon.core.Move.MoveView> possibleMoves) {
    String res = "";
    if (!possibleMoves.isEmpty()) {
      for (MoveView move : possibleMoves) {
        res = res + ", " + move.getName();
      }
    } else {
      return "Empty move list";
    }
    return res;
  }

  @Override
  public String toString() {
      return "MinMaxNode [" + minmax + "] with moves: " + moveToString(possibleMoves) + ". Parent=" + parent.toString();
  }

  public void addMoveExpectedUtil(double i) {
    this.moveUtils.add(i);
  }
}
  */
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

  //This only fetches one child. Recursively generate children during tree traversal
  public void generateChildren(int myTeamIdx) {
    int oppTeamIdx = (myTeamIdx == 0) ? 1 : 0;
    TeamView myTeam = state.getTeamView(myTeamIdx);
    PokemonView myPoke = myTeam.getActivePokemonView();
    TeamView oppTeam = state.getTeamView(oppTeamIdx);
    PokemonView oppPoke = oppTeam.getActivePokemonView();
  
    System.out.println(this.toString());
  
    if (this.isTerminalState(state)) return;
  
    // Product of an attack is a chance node
    if (this.type.equals("max") || this.type.equals("min")) {
      List<MoveView> moves = (this.type.equals("max")) ? myPoke.getAvailableMoves() : oppPoke.getAvailableMoves();
  
      for (MoveView move : moves) {
        if (move == null || move.getPP() == null || move.getPP() <= 0) continue;
  
        List<Pair<Double, BattleView>> outcomes = move.getPotentialEffects(state, myTeamIdx, oppTeamIdx);
  
        for (Pair<Double, BattleView> outcome : outcomes) {
          BattleView newState = outcome.getSecond();  // Result of the move
          Node chanceNode = new Node(newState, this.depth + 1, "chance", move);
          this.children.add(chanceNode);
        }
      }
    //Product of a chance ndoe is an attack
    } else if (this.type.equals("chance")) {
      PokemonView p0 = state.getTeamView(0).getActivePokemonView();
      PokemonView p1 = state.getTeamView(1).getActivePokemonView();
  
      int spd0 = p0.getCurrentStat(Stat.SPD);
      int spd1 = p1.getCurrentStat(Stat.SPD);
  
      if (spd0 >= spd1) {
        this.children.add(new Node(this.state, this.depth + 1, "min", null)); // Opponent acts next
      } else {
        this.children.add(new Node(this.state, this.depth + 1, "max", null)); // Our agent acts next
      }
    }
  }
  
}