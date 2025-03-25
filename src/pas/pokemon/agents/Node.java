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
  boolean isChance = false;
  int utility = 0;
  List<Node> children = null;
  Node parent = null;
  edu.bu.pas.pokemon.core.Battle.BattleView battle = null;

  void addChild(Node child);
  int getUtility();
  void setUtility(int util);
  Node getParent();
  void setParent(Node parent);
  List<Node> getChildren();
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
      this.children = null;
      this.parent = null;
  }

  public MOC(Node parent) {
    this.utility = 0;
    this.children = null;
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
      this.children = null;
      this.parent = null;
  }
  public PTC(Node parent, Battle.BattleView battle) {
    this.utility = 0;
    this.children = null;
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

  public double getFirstChance(BattleView battlev, TeamView teamv) {
    // gets the chance we go first given current PokeMon
    PokemonView ourActive = teamv.getActivePokemonView();
    PokemonView oppActive = battlev.getTeam2View().getActivePokemonView();

    MoveView[] ourMoves = ourActive.getMoveViews();
    MoveView[] oppMoves = oppActive.getMoveViews();

    Double usFirst = 0.0;
    Double oppFirst = 0.0;

    if (teamv.size() != 1) {
      usFirst = usFirst + teamv.size() - 1;
    }
    if (battlev.getTeam2View().size() != 1) {
      oppFirst = oppFirst + teamv.size() - 1;
    }

    for (int i = 0; i < ourMoves.length; i++) {
      for (int j = 0; j < ourMoves.length; j++) {
        MoveView ourMove = ourMoves[i];
        MoveView oppMove = oppMoves[j];
        if (ourMove.getPriority() > oppMove.getPriority()) {
          usFirst++;
        }
        else if (ourMove.getPriority() < oppMove.getPriority()) {
          oppFirst++;
        }
        else {
          if (ourActive.getCurrentStat(Stat.SPD) > oppActive.getCurrentStat(Stat.SPD)) {
            usFirst++;
          }
          else if (ourActive.getCurrentStat(Stat.SPD) < oppActive.getCurrentStat(Stat.SPD)) {
            oppFirst++;
          }
          else {
            usFirst++;
            oppFirst++;
          }
        }
      }
    }
    return usFirst / (usFirst + oppFirst);
  }

  public Pair<List<Pair<MoveView, MoveView>>, List<Pair<MoveView, MoveView>>> getAllMoveCombos(BattleView battlev) {
    // our results to be included in a pair and returned
    List<Pair<MoveView, MoveView>> usFirst = new ArrayList<>();
    List<Pair<MoveView, MoveView>> oppFirst = new ArrayList<>();
    
    TeamView ourTeam = battlev.getTeam1View();
    TeamView oppTeam = battlev.getTeam2View(); 

    PokemonView ourPoke = ourTeam.getActivePokemonView();
    PokemonView oppPoke = oppTeam.getActivePokemonView();

    MoveView[] ourMoves = ourPoke.getMoveViews();
    MoveView[] oppMoves = oppPoke.getMoveViews();

   for (int i = 0; i < ourMoves.length; i++) {
    // get our current moves
    MoveView ourMove = ourMoves[i];
    MoveView oppMove = oppMoves[i];

    Pair<MoveView, MoveView> movePair = new Pair(ourMove, oppMove);
    if (ourMove.getPriority() > oppMove.getPriority()) {
      usFirst.add(movePair);
    }
    else if (ourMove.getPriority() < oppMove.getPriority()) {
      oppFirst.add(movePair);
    }
    else {
      if (ourPoke.getCurrentStat(Stat.SPD) > oppPoke.getCurrentStat(Stat.SPD)) {
        usFirst.add(movePair);
      }
      else if (ourPoke.getCurrentStat(Stat.SPD) < oppPoke.getCurrentStat(Stat.SPD)) {
        oppFirst.add(movePair);
      }
      else {
        usFirst.add(movePair);
        oppFirst.add(movePair);
      }
    }
   }
   return new Pair<List<Pair<MoveView,MoveView>>,List<Pair<MoveView,MoveView>>>(usFirst, oppFirst);
  }

  // GO FROM MOC TO DETERMINISTIC NODE
  public List<MinMaxNode> MOCtoDeterminstic(MOC prevNode, BattleView battle) {
    TeamView ourTeam = battle.getTeam1View();
    TeamView oppTeam = battle.getTeam2View();

    // extracts all movePairs. leftNodeMovePairs contains a list of all pairings where we go first
    Pair<List<Pair<MoveView,MoveView>>,List<Pair<MoveView,MoveView>>> allMoves = getAllMoveCombos(battle);
    List<Pair<MoveView, MoveView>> leftNodeMovePairs = allMoves.getFirst();
    List<Pair<MoveView, MoveView>> rightNodeMovePairs = allMoves.getSecond();


    List<MoveView> leftMoves = new ArrayList<>();
    List<MoveView> rightMoves = new ArrayList<>();

    // extracts the list of moves we make in the right and left subtrees
    for (int i = 0; i < leftNodeMovePairs.size(); i++) {
      if (!leftMoves.contains(leftNodeMovePairs.get(i).getFirst())) {
        leftMoves.add(leftNodeMovePairs.get(i).getFirst());
      }
    }
    for (int i = 0; i < rightNodeMovePairs.size(); i++) {
      if (!rightMoves.contains(rightNodeMovePairs.get(i).getSecond())) {
        rightMoves.add(rightNodeMovePairs.get(i).getSecond());
      }
    }

    // leftMoves now contains all moves we can make in the left subtree where we are first
    
    MinMaxNode leftNode = new MinMaxNode("max", battle, leftMoves, prevNode);
    MinMaxNode rightNode = new MinMaxNode("min", battle, rightMoves, prevNode);
    List<MinMaxNode> res = new ArrayList<>();
    res.add(rightNode);
    res.add(leftNode);
    return res;
  }

  public List<MRC> generateMRC(MinMaxNode prevNode) {
    List<MRC> results = new ArrayList<>();

    List<MoveView> moves = prevNode.getMoves();
    for (int i = 0; i < moves.size(); i++) {
      MRC newMRC = new MRC(moves.get(i));
      newMRC.setParent(prevNode);
      newMRC.setBattle(prevNode.getBattle());

      prevNode.addChild(newMRC);
      results.add(newMRC);
    }

    return results;
    // all MRCs lead to either determinstic nodes, or PTCs
  }

  public List<MoveView> oppSecondMoves(BattleView battlev) {
   // our results to be included in a pair and returned
   List<Pair<MoveView, MoveView>> usFirst = new ArrayList<>();
   List<Pair<MoveView, MoveView>> oppFirst = new ArrayList<>();
   
   TeamView ourTeam = battlev.getTeam1View();
   TeamView oppTeam = battlev.getTeam2View(); 

   PokemonView ourPoke = ourTeam.getActivePokemonView();
   PokemonView oppPoke = oppTeam.getActivePokemonView();

   MoveView[] ourMoves = ourPoke.getMoveViews();
   MoveView[] oppMoves = oppPoke.getMoveViews();

  for (int i = 0; i < ourMoves.length; i++) {
   // get our current moves
   MoveView ourMove = ourMoves[i];
   MoveView oppMove = oppMoves[i];

   Pair<MoveView, MoveView> movePair = new Pair(ourMove, oppMove);
   if (ourMove.getPriority() > oppMove.getPriority()) {
     usFirst.add(movePair);
   }
   else if (ourMove.getPriority() < oppMove.getPriority()) {
     oppFirst.add(movePair);
   }
   else {
     if (ourPoke.getCurrentStat(Stat.SPD) > oppPoke.getCurrentStat(Stat.SPD)) {
       usFirst.add(movePair);
     }
     else if (ourPoke.getCurrentStat(Stat.SPD) < oppPoke.getCurrentStat(Stat.SPD)) {
       oppFirst.add(movePair);
     }
     else {
       usFirst.add(movePair);
       oppFirst.add(movePair);
     }
   }
  }
  List<MoveView> result = new ArrayList<>();
  for (Pair<MoveView, MoveView> pair : usFirst) {
    if (!result.contains(pair.getSecond())){ 
      result.add(pair.getSecond());
    }
  }
  return result;
 }

  public List<MoveView> usSecondMoves(BattleView battlev) {
    // our results to be included in a pair and returned
    List<Pair<MoveView, MoveView>> usFirst = new ArrayList<>();
    List<Pair<MoveView, MoveView>> oppFirst = new ArrayList<>();
    
    TeamView ourTeam = battlev.getTeam1View();
    TeamView oppTeam = battlev.getTeam2View(); 

    PokemonView ourPoke = ourTeam.getActivePokemonView();
    PokemonView oppPoke = oppTeam.getActivePokemonView();

    MoveView[] ourMoves = ourPoke.getMoveViews();
    MoveView[] oppMoves = oppPoke.getMoveViews();

  for (int i = 0; i < ourMoves.length; i++) {
    // get our current moves
    MoveView ourMove = ourMoves[i];
    MoveView oppMove = oppMoves[i];

    Pair<MoveView, MoveView> movePair = new Pair(ourMove, oppMove);
    if (ourMove.getPriority() > oppMove.getPriority()) {
      usFirst.add(movePair);
    }
    else if (ourMove.getPriority() < oppMove.getPriority()) {
      oppFirst.add(movePair);
    }
    else {
      if (ourPoke.getCurrentStat(Stat.SPD) > oppPoke.getCurrentStat(Stat.SPD)) {
        usFirst.add(movePair);
      }
      else if (ourPoke.getCurrentStat(Stat.SPD) < oppPoke.getCurrentStat(Stat.SPD)) {
        oppFirst.add(movePair);
      }
      else {
        usFirst.add(movePair);
        oppFirst.add(movePair);
      }
    }
  }
  List<MoveView> result = new ArrayList<>();
  for (Pair<MoveView, MoveView> pair : oppFirst) {
    if (!result.contains(pair.getFirst())){ 
      result.add(pair.getFirst());
    }
  }
  return result;
  }


  public List<Node> checkOrGenMRC(MRC prevNode, int currTeam) {
    BattleView battle = prevNode.getBattle();
    List<Pair<Double, BattleView>> potentialFirstOutcomes;

    // List to store all PTC and deterministic nodes
    List<Node> results = new ArrayList<>();

    if (currTeam == 0) {
      potentialFirstOutcomes = prevNode.getMove().getPotentialEffects(battle, 0, 1);
    }
    else {
      potentialFirstOutcomes = prevNode.getMove().getPotentialEffects(battle, 1, 0);
    }

    for (Pair<Double, BattleView> pair : potentialFirstOutcomes) {
      BattleView outcome = pair.getSecond();
      // we have gone first
      if (currTeam == 0) {
        if(outcome.getTeam2View().getActivePokemonView().hasFainted()) {
          PTC nextNode = new PTC(prevNode, outcome);
          results.add(nextNode);
        }
        else {
          // get a list of moves our opponent is now making according to previous MOC, append as moves
          List<MoveView> oppMoves = oppSecondMoves(battle);
          MinMaxNode nextNode = new MinMaxNode("min", outcome, oppMoves, prevNode);
          results.add(nextNode);
        }
      }

      // opponent has gone first
      else { 
        if(outcome.getTeam1View().getActivePokemonView().hasFainted()) {
          PTC nextNode = new PTC(prevNode, outcome);
          results.add(nextNode);
        }
        else {
          // get all the list of moves which resulted in us going second
          List<MoveView> usMoves = usSecondMoves(battle);
          MinMaxNode nextNode = new MinMaxNode("max", outcome, usMoves, prevNode);
          results.add(nextNode);
        }
      }
    }
    return results;
  }
  public List<Node> PTCtoNextTurnEnd(PTC prevNode, BattleView battle, int currentTeamNumber) {
    List<Node> results = new ArrayList<>();
    if (prevNode.getTerminal() == false) {
      List<Battle.BattleView> outcomes = battle.applyPostTurnConditions();
      for (Battle.BattleView outcome : outcomes) {
  
        // Verify post turn effects didn't kill the last pokemon on a team
        Team temp_team_me = new Team(battle.getTeam1View());
        Team temp_team_opp = new Team(battle.getTeam2View());
        if (temp_team_me.getNumAlivePokemon() == 0 || temp_team_opp.getNumAlivePokemon() == 0) {
          prevNode.setTerminal(true);
          return null;
        } else {
          TeamView ourTeam = outcome.getTeam1View();
          double chanceForFirst = getFirstChance(outcome, ourTeam);
          MOC nextTurn = new MOC(prevNode);
          nextTurn.setProbs(chanceForFirst);
          results.add(nextTurn);
        }
      }
      return results;
    //Terminal state. Return nothing
    } else {
      return null;
    }
  }

  public Node generateChildren(BattleView battlev) {
    TeamView ourTeam = battlev.getTeam1View();
    TeamView oppTeam = battlev.getTeam2View();

    // create our initial MOC node
    double chanceForFirst = getFirstChance(battlev, ourTeam);
    MOC root = new MOC();
    root.setProbs(chanceForFirst);

    List<MinMaxNode> secondLayer = MOCtoDeterminstic(root, battlev);
    List<List<MRC>> thirdLayer = new ArrayList<>();
    List<Node> fourthLayer = new ArrayList<>();
    for (MinMaxNode node : secondLayer) {
      List<MRC> results = generateMRC(node);
      thirdLayer.add(results);
    }
    for (int i = 0; i < thirdLayer.size(); i++) {
      for (int j = 0; j < thirdLayer.get(0).size(); j++) {
        MRC currMRC = thirdLayer.get(i).get(j);
        BattleView battle = currMRC.getBattle();
        MinMaxNode currParent = (MinMaxNode) currMRC.getParent();
        if (currParent.getMinMax().equals("max")) {
          List<Node> outcomes = checkOrGenMRC(currMRC, 0);
          for (Node outcome : outcomes) {
            fourthLayer.add(outcome);
          }
        }
        else {
          List<Node> outcomes = checkOrGenMRC(currMRC, 1);
          for (Node outcome : outcomes) {
            fourthLayer.add(outcome);
          }
        }
      }
    }
    for (int i = 0; i < thirdLayer.size(); i++) {
      for ()
    }

    return root;
  }
}