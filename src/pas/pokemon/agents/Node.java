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

public interface Node {
  void addChild(Node child);
  int getUtility();
  void setUtility(int util);
  Node getParent();
  void setParent(Node parent);
  List<Node> getChildren();
  void setBattle(BattleView battle);
  BattleView getBattle();
}

class MOC implements Node {
  private Double left_prob;
  private Double right_prob;
  private boolean isChance = true;
  private int utility;
  private List<Node> children;
  private Node parent;
  private edu.bu.pas.pokemon.core.Battle.BattleView battle;

  public MOC() {
      this.utility = 0;
      this.children = new ArrayList<>();
      this.parent = null;
  }

  public MOC(Node parent) {
    this.utility = 0;
    this.children = new ArrayList<>();
    this.parent = parent;
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
  public int getUtility() {
      return this.utility;
  }

  @Override
  public void setUtility(int util) {
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
      return "MOC Node [Left Prob: " + left_prob + ", Right Prob: " + right_prob + "]";
  }
}

class MRC implements Node {
  private edu.bu.pas.pokemon.core.Move.MoveView move;
  private boolean isChance = true;
  private int utility;
  private List<Node> children;
  private Node parent;
  private edu.bu.pas.pokemon.core.Battle.BattleView battle;

  public MRC(edu.bu.pas.pokemon.core.Move.MoveView move) {
      this.move = move;
  }

  @Override
  public void addChild(Node child) {
      this.children.add(child);
  }

  @Override
  public int getUtility() {
      return this.utility;
  }

  @Override
  public void setUtility(int util) {
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

  public edu.bu.pas.pokemon.core.Move.MoveView getMove() {
      return this.move;
  }

  public void setBattle(BattleView battle) {
    this.battle = battle;
  }

  public BattleView getBattle() {
    return this.battle;
  }
  @Override
  public String toString() {
      return "MRC Node [Move: " + move.getName() + "]";
  }
}

class MinMaxNode implements Node {
  private String minmax;
  private boolean isChance = false;
  private int utility;
  private List<Node> children;
  private Node parent;
  private edu.bu.pas.pokemon.core.Battle.BattleView battle;
  private List<edu.bu.pas.pokemon.core.Move.MoveView> possibleMoves;

  public MinMaxNode(String minmax, BattleView battle, List<MoveView> moves, Node root) {
      this.minmax = minmax;
      this.possibleMoves = moves;
      this.battle = battle;
  }


  @Override
  public void addChild(Node child) {
      this.children.add(child);
  }

  @Override
  public int getUtility() {
      return this.utility;
  }

  @Override
  public void setUtility(int util) {
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
  @Override
  public String toString() {
      return "MinMaxNode [" + minmax + "] with moves: " + possibleMoves;
  }
}

class PTC implements Node {
  private boolean isChance = true;
  private int utility;
  private List<Node> children;
  private Node parent;
  private edu.bu.pas.pokemon.core.Battle.BattleView battle;
  private boolean isTerminal;

  public PTC() {
      this.utility = 0;
      this.children = new ArrayList<>();
      this.parent = null;
  }
  public PTC(Node parent, Battle.BattleView battle) {
    this.utility = 0;
    this.children = new ArrayList<>();
    this.parent = parent;
    this.battle = battle;
    Team temp_team_me = new Team(battle.getTeam1View());
    Team temp_team_opp = new Team(battle.getTeam2View());
    if (temp_team_me.getNumAlivePokemon() == 0 || temp_team_opp.getNumAlivePokemon() == 0) {
      this.isTerminal = true;
    } else {
      this.isTerminal = false;
    }
  }

  public boolean getTerminal() {
    return this.isTerminal;
  }
  public void setTerminal(boolean b) {
    this.isTerminal = b;
  }

  @Override
  public void addChild(Node child) {
      this.children.add(child);
  }

  @Override
  public int getUtility() {
      return this.utility;
  }

  @Override
  public void setUtility(int util) {
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
    return "PTC Node [Terminal: " + isTerminal + "]";
  }
}