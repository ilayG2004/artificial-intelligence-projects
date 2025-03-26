package src.pas.pokemon.agents;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.SwingPropertyChangeSupport;

import edu.bu.pas.pokemon.core.Battle;
import edu.bu.pas.pokemon.core.Move;
import edu.bu.pas.pokemon.core.Battle.BattleView;
import edu.bu.pas.pokemon.core.Move.MoveView;
import edu.bu.pas.pokemon.core.Pokemon.PokemonView;
import edu.bu.pas.pokemon.core.Team;
import edu.bu.pas.pokemon.core.Team.TeamView;
import edu.bu.pas.pokemon.core.enums.Stat;
import edu.bu.pas.pokemon.utils.Pair;
import java.util.HashMap;

public class TreeBuilder {
  
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
      for (int j = 0; j < oppMoves.length; j++) {
          if (ourMoves[i] == null || oppMoves[j] == null) {
              //Skip invalid move combo
              continue;
          }
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
    if (oppFirst == 0.0) {
      return 1.0;
    }
    if (usFirst == 0.0) {
      return 0.0;
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
      for (int j = 0; j < oppMoves.length; j++) {
          if (ourMoves[i] == null || oppMoves[j] == null) {
              //Skip invalid move combo
              continue;
          }
        MoveView ourMove = ourMoves[i];
        MoveView oppMove = oppMoves[j];
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

    prevNode.addChild(rightNode);
    prevNode.addChild(leftNode);
    return res;
  }

  /* 
  public List<MRC> generateMRC(MinMaxNode prevNode) {
    List<MRC> results = new ArrayList<>();

    List<MoveView> moves = prevNode.getMoves();
    for (int i = 0; i < moves.size(); i++) {
      MRC newMRC = new MRC(moves.get(i));
      newMRC.setParent(prevNode);
      newMRC.setBattle(prevNode.getBattle());

      prevNode.addMRC(newMRC);
      results.add(newMRC);
    }

    return results;
    // all MRCs lead to either determinstic nodes, or PTCs
  }
  */

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
    for (int j = 0; j < oppMoves.length; j++) {
        if (ourMoves[i] == null || oppMoves[j] == null) {
            //Skip invalid move combo
            continue;
        }
      MoveView ourMove = ourMoves[i];
      MoveView oppMove = oppMoves[j];
   // get our current moves


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
    List<Pair<MoveView, MoveView>> oppFirst = new ArrayList<>();
    
    TeamView ourTeam = battlev.getTeam1View();
    TeamView oppTeam = battlev.getTeam2View(); 

    PokemonView ourPoke = ourTeam.getActivePokemonView();
    PokemonView oppPoke = oppTeam.getActivePokemonView();

    MoveView[] ourMoves = ourPoke.getMoveViews();
    MoveView[] oppMoves = oppPoke.getMoveViews();

  //No scenario where we go second. Just return their moves
  if (ourMoves.length == 0 && oppMoves.length != 0) {
    List<MoveView> result = new ArrayList<>();
    for (MoveView oppmove : oppMoves) {
      result.add(oppmove);
    }
    return result;
  }
  //No scenario opponent goes second. Just return our moves
  if (ourMoves.length != 0 && oppMoves.length == 0) {
    List<MoveView> result = new ArrayList<>();
    for (MoveView ourMove : ourMoves) {
      result.add(ourMove);
    }
    return result;
  }
  for (int i = 0; i < ourMoves.length; i++) {
    for (int j = 0; j < oppMoves.length; j++) {
      if (ourMoves[i] == null || oppMoves[j] == null) {
        //Skip invalid move combo
        continue;
      }
      // get our current moves
      MoveView ourMove = ourMoves[i];
      MoveView oppMove = oppMoves[j];

      Pair<MoveView, MoveView> movePair = new Pair(ourMove, oppMove);
      if (ourMove.getPriority() < oppMove.getPriority()) {
        oppFirst.add(movePair);
      }
      else {
        if (ourPoke.getCurrentStat(Stat.SPD) <= oppPoke.getCurrentStat(Stat.SPD)) {
          oppFirst.add(movePair);
        }
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
    System.out.println("GENNING MRCS: " + prevNode.toString());
    System.out.println("# of potential outcomes: " + potentialFirstOutcomes.size());

    for (Pair<Double, BattleView> pair : potentialFirstOutcomes) {
      BattleView outcome = pair.getSecond();
      // we have gone first
      if (currTeam == 0) {
        Node nodeParent = prevNode.getParent();
        MinMaxNode currParent = ((MinMaxNode)nodeParent);
        if(outcome.getTeam2View().getActivePokemonView().hasFainted() || currParent.getSecond()) {
          PTC nextNode = new PTC(prevNode, outcome);
          results.add(nextNode);
          prevNode.addChild(nextNode);
        }
        else {
          // get a list of moves our opponent is now making according to previous MOC, append as moves
          List<MoveView> oppMoves = oppSecondMoves(battle);
          MinMaxNode nextNode = new MinMaxNode("min", outcome, oppMoves, prevNode);
          nextNode.setSecond();
          results.add(nextNode);
          prevNode.addChild(nextNode);
        }
      }

      // opponent has gone first
      else { 
        Node nodeParent = prevNode.getParent();
        MinMaxNode currParent = ((MinMaxNode)nodeParent);
        if(outcome.getTeam1View().getActivePokemonView().hasFainted() || currParent.getSecond()) {
          PTC nextNode = new PTC(prevNode, outcome);
          results.add(nextNode);
          prevNode.addChild(nextNode);
        }
        else {
          // get all the list of moves which resulted in us going second
          List<MoveView> usMoves = usSecondMoves(battle);
          MinMaxNode nextNode = new MinMaxNode("max", outcome, usMoves, prevNode);
          nextNode.setSecond();
          results.add(nextNode);
          prevNode.addChild(nextNode);
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
          nextTurn.setBattle(outcome);
          results.add(nextTurn);
          prevNode.addChild(nextTurn);
        }
      }
      return results;
    //Terminal state. Return nothing
    } else {
      return null;
    }
  }

  public void completeMinMaxNode(MinMaxNode prevNode, int currTeam) {
    BattleView battle = prevNode.getBattle();
    List<Node> results = new ArrayList<>();
    HashMap<MoveView, List<Pair<Double, BattleView>>> dict = new HashMap<>();
    HashMap<MoveView, List<Pair<Double, BattleView>>> otherDict = new HashMap<>();
    TeamView ourTeam = battle.getTeam1View();
    TeamView oppTeam = battle.getTeam2View();
    
    Team temp_team_me = new Team(battle.getTeam1View());
    Team temp_team_opp = new Team(battle.getTeam2View());

    //Terminal state?
    if (temp_team_me.getNumAlivePokemon() == 0) {
      System.out.println("Terminal node. ENEMY WINS");
    } else if (temp_team_opp.getNumAlivePokemon() == 0) {
      System.out.println("Terminal node. WE WIN");
    
    //Not terminal state. Our team goes first
    } else if (currTeam == 0) {
      List<MoveView> moves = ourTeam.getActivePokemonView().getAvailableMoves();
      for (MoveView move : moves) {
        if (move != null) {
          List<Pair<Double, BattleView>> outcomes = move.getPotentialEffects(battle, 0, 1);
          dict.put(move, outcomes);
          List<MoveView> otherPlrMoves = oppSecondMoves(battle);
          
          for (Pair<Double, BattleView> p1PairOutcome : outcomes) {
            BattleView p1Outcome = p1PairOutcome.getSecond();
            for (MoveView otherPlrMove : otherPlrMoves) {
              List<Pair<Double, BattleView>> otherPlrOutcomes = otherPlrMove.getPotentialEffects(p1Outcome, 1, 0);
              otherDict.put(move, otherPlrOutcomes);
              //Create min node with outcome from previous node's attack. Whose parent is the max node
              MinMaxNode otherPlayerMove = new MinMaxNode("min", p1Outcome, otherPlrMoves, prevNode);
              otherPlayerMove.setOutcomes(otherDict);
              prevNode.addKid(otherPlayerMove);
            }
            
          }
        } else {
          continue;
        }
      }
      prevNode.setOutcomes(dict);

    //Not terminal state. Enemy team goes first.
    } else {
      List<MoveView> moves = oppTeam.getActivePokemonView().getAvailableMoves();
      for (MoveView move : moves) {
        if (move != null) {
          List<Pair<Double, BattleView>> outcomes = move.getPotentialEffects(battle, 1, 0);
          dict.put(move, outcomes);

          List<MoveView> otherPlrMoves = usSecondMoves(battle);
          for (Pair<Double, BattleView> p1PairOutcome : outcomes) {
            BattleView p1Outcome = p1PairOutcome.getSecond();
            for (MoveView otherPlrMove : otherPlrMoves) {
              List<Pair<Double, BattleView>> otherPlrOutcomes = otherPlrMove.getPotentialEffects(p1Outcome, 0, 1);
              otherDict.put(move, otherPlrOutcomes);

              //Create max node with outcome from previous node's attack. Whose parent is the max node
              MinMaxNode otherPlayerMove = new MinMaxNode("max", p1Outcome, otherPlrMoves, prevNode);
              otherPlayerMove.setOutcomes(otherDict);
              prevNode.addKid(otherPlayerMove);
            }
          }
        } else {
          continue;
        }
        prevNode.setOutcomes(dict);
      }
    }
  }

  public Node generateChildrenWrapper(BattleView battlev) {
    // initialize our first MOC, call generateChildren on it and return,
    // generateChildren handles recursive calls
    MOC root = new MOC();
    root.setBattle(battlev);
    generateChildren(root);
    return root;
  }


  public void generateChildren(Node node) {
    System.out.println(node.toString());
    BattleView battlev = node.getBattle();
    if (battlev == null) {
      System.out.println("null");
    }
    TeamView ourTeam = battlev.getTeam1View();

    // INPUT MOC - OUTPUT MINMAXNODES
    if (node instanceof MOC) {
      double chanceForFirst = getFirstChance(battlev, ourTeam);
      ((MOC)node).setProbs(chanceForFirst);

      // populates the MOC's children field with two MinMaxNodes
      MOCtoDeterminstic((MOC)node, battlev);
      if (((MOC)node).getChildren().size() != 0) {
        for (Node nextNode : ((MOC)node).getChildren()) {
          generateChildren((MinMaxNode)(nextNode));
        }
      }
    }

    // INPUT MINMAXNODE - OUTPUT MINMAXNODES MOCS
    if (node instanceof MinMaxNode) {
      if (((MinMaxNode)node).getMinMax().equals("max")) {
        completeMinMaxNode(((MinMaxNode)node), 0);
        if (!((MinMaxNode)node).getKids().isEmpty()) {
          for (Node nextNode : ((MinMaxNode)node).getKids()) {
            generateChildren(nextNode);
          }
        }
      }
    }


    
    // INPUT MINMAXNODE - OUTPUT MRCS
    /*
    else if (node instanceof MinMaxNode){
      generateMRC((MinMaxNode)node);
      if (((MinMaxNode)node).getMRCS() != null) {
        for (Node nextNode : ((MinMaxNode)node).getMRCS()) {
          generateChildren((MRC)nextNode);
        }
      }
    }

    // INPUT MRCS - OUTPUT MINMAXNODES & PTCS
    else if (node instanceof MRC) {
      MinMaxNode currParent = ((MinMaxNode) node.getParent());
      if (currParent.getMinMax().equals("max")) {
        checkOrGenMRC(((MRC)node), 0);
        for (Node nextNode : ((MRC)node).getChildren()) {
          generateChildren(nextNode);
        }
      }
      else {
        checkOrGenMRC(((MRC)node), 1);
        for (Node nextNode : ((MRC)node).getChildren()) {
          generateChildren(nextNode);
        }
      }
    }

    // INPUT PTCS - OUTPUT MOCS
    else if (node instanceof PTC) {
      PTCtoNextTurnEnd(((PTC)node), battlev, 0);
      for (Node nextNode : ((PTC)node).getChildren()) {
        generateChildren(nextNode);
      }
    }
    */
    return;
  }
}