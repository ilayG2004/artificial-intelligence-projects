package src.pas.pokemon.agents;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.event.SwingPropertyChangeSupport;

import edu.bu.pas.pokemon.core.Battle;
import edu.bu.pas.pokemon.core.Move;
import edu.bu.pas.pokemon.core.Battle.BattleView;
import edu.bu.pas.pokemon.core.Move.MoveView;
import edu.bu.pas.pokemon.core.Pokemon;
import edu.bu.pas.pokemon.core.Pokemon.PokemonView;
import edu.bu.pas.pokemon.core.Team;
import edu.bu.pas.pokemon.core.Team.TeamView;
import edu.bu.pas.pokemon.core.enums.Stat;
import edu.bu.pas.pokemon.utils.Pair;
import java.util.HashMap;
import java.util.HashSet;
/*
public class TreeBuilder {
  Set<String> visitedStates = new HashSet<>();
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
 for (Pair<MoveView, MoveView> pair : oppFirst) {
   if (!result.contains(pair.getFirst())){ 
     result.add(pair.getFirst());
   }
 }
 return result;
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
  public boolean KOD(BattleView battle, int enemyTeamIDX) {
    System.out.println(" " +battle.getTeamView(enemyTeamIDX).getActivePokemonView().getName() + " has " + battle.getTeamView(enemyTeamIDX).getActivePokemonView().getCurrentStat(Stat.HP));
    if (battle.getTeamView(enemyTeamIDX).getActivePokemonView().getCurrentStat(Stat.HP) <= 0) {
      return true;
    }
    return false;
  }

  public void completeMinMaxNode(MinMaxNode prevNode, int currTeam, int currDepth) {
    BattleView battle = prevNode.getBattle();
    HashMap<MoveView, List<Pair<Double, BattleView>>> dict = new HashMap<>();
    int caster = -1;
    int opp = -1;

    //Consolidate team number
    if (currTeam == 0) {
      caster = 0;
      opp = 1;
    } else {
      caster = 1;
      opp = 0;
    }
    
    //Terminal state?
   if (isTerminalState(battle)) {
    return;
   }
    
    //Not terminal state. Generate info for curr team (team going first)
  List<MoveView> moves = ((MinMaxNode)prevNode).getMoves();
   if (moves.isEmpty()) { System.out.println("Impossible for opp to go at this point"); return;}
    for (MoveView move : moves) {
      if (move != null) {
        //Document the (Move, Battle Outcome) pairs for each applied move by team going first in that game state
        List<Pair<Double, BattleView>> outcomes = move.getPotentialEffects(battle, caster, opp);
        dict.put(move, outcomes);
        prevNode.setOutcomes(dict);
        List<MoveView> otherPlrMoves;
        if (caster == 0) {
          otherPlrMoves = oppSecondMoves(battle);
        } else {
          otherPlrMoves = usSecondMoves(battle);
        }
       
        //Document the (Move, Battle Outcome) pairs for each applied move on the various game states by team going second
        for (Pair<Double, BattleView> p1PairOutcome : outcomes) {
          BattleView p1Outcome = p1PairOutcome.getSecond();
          System.out.println(battle.getTeamView(caster).getActivePokemonView().getName() +" uses " +move.getName() + ", new state: " + getBattleStateKey(p1Outcome));
          //System.out.print(battle.getTeamView(caster).getActivePokemonView().getName() + " uses " +move.getName());
          if (!(KOD(p1Outcome, opp))) {
            for (MoveView otherPlrMove : otherPlrMoves) {
              List<Pair<Double, BattleView>> otherPlrOutcomes = otherPlrMove.getPotentialEffects(p1Outcome, opp, caster);
              HashMap<MoveView, List<Pair<Double, BattleView>>> otherDict = new HashMap<>();
              otherDict.put(otherPlrMove, otherPlrOutcomes);
              //Create opposite node of outcomes from other player move who documents outcomes of applying their moves to the game state caused by the first player
              MinMaxNode otherPlayerMove;
              if (caster == 0) {
                otherPlayerMove = new MinMaxNode("min", p1Outcome, otherPlrMoves, prevNode);
              } else {
                otherPlayerMove = new MinMaxNode("max", p1Outcome, otherPlrMoves, prevNode);
              }
              otherPlayerMove.setOutcomes(otherDict);
              prevNode.addKid(otherPlayerMove);

              //NEW TURN
              for (Pair<Double, BattleView> p2Outcome : otherPlrOutcomes) {
                BattleView resultingBattle = p2Outcome.getSecond();
                MOC newMoc = new MOC(otherPlayerMove);
                newMoc.setBattle(resultingBattle);
                generateChildren(newMoc, currDepth+2);
                otherPlayerMove.addKid(newMoc);
              }
            }
            
          } else {
            System.out.println(battle.getTeamView(caster).getActivePokemonView().getName() + " KOD opponent with " + move.getName());
            MOC newMoc = new MOC(prevNode);
            newMoc.setBattle(p1Outcome);
            generateChildren(newMoc, currDepth+1);
            prevNode.addKid(newMoc);
          }
        }
      } else {
        continue;
      }
    }
  }
  public String getBattleStateKey(BattleView battle) {
    StringBuilder sb = new StringBuilder();
  
    Team temp_team_me = new Team(battle.getTeam1View());
    Team temp_team_opp = new Team(battle.getTeam2View());

    for (Pokemon p : temp_team_me.getPokemon()) {
      sb.append(p.getName()).append(":").append(p.getCurrentStat(Stat.HP)).append("|");
    }
  
    for (Pokemon p : temp_team_opp.getPokemon()) {
      sb.append(p.getName()).append(":").append(p.getCurrentStat(Stat.HP)).append("|");
    }
  
    return sb.toString();
  }

  public Node generateChildrenWrapper(BattleView battlev) {
    // initialize our first MOC, call generateChildren on it and return,
    // generateChildren handles recursive calls
    MOC root = new MOC();
    root.setBattle(battlev);
    generateChildren(root, 0);
    return root;
  }

  int MAX_DEPTH = 2;
  public void generateChildren(Node node, int depth) {
    if (depth > MAX_DEPTH) return;
    System.out.println(node.toString());
    BattleView battlev = node.getBattle();

    
    String stateKey = getBattleStateKey(battlev);
    if (visitedStates.contains(stateKey)) {
      System.out.println("State already exists:  " + stateKey);
      return;
    }
    if (node.getParent() != null) {
      visitedStates.add(stateKey);
    }   
    

    if (battlev == null) {
      System.out.println("Battleview is null");
    }
    TeamView ourTeam = battlev.getTeam1View();

    if (isTerminalState(battlev)) {
      //Battle ended
      return;
    }

      // INPUT MOC - OUTPUT MINMAXNODES
      if (node instanceof MOC) {
        double chanceForFirst = getFirstChance(battlev, ourTeam);
        ((MOC)node).setProbs(chanceForFirst);
        System.out.println(node);
        // populates the MOC's children field with two MinMaxNodes
        MOCtoDeterminstic((MOC)node, battlev);
        System.out.println(node.getChildren());
        if (((MOC)node).getChildren().size() != 0) {
          for (Node nextNode : ((MOC)node).getChildren()) {
            generateChildren((MinMaxNode)(nextNode), depth+1);
          }
        }
      }

      // INPUT MINMAXNODE - OUTPUT MINMAXNODES MOCS
      if (node instanceof MinMaxNode) {
        if (((MinMaxNode)node).getMinMax().equals("max")) {
          System.out.println("Expanding " + ((MinMaxNode)node).getMinMax() + " node for Team 0");
          completeMinMaxNode(((MinMaxNode)node), 0, depth);
          if (!((MinMaxNode)node).getKids().isEmpty()) {
            for (Node nextNode : ((MinMaxNode)node).getKids()) {
              generateChildren(nextNode, depth + 1);
            }
          }
        } else {
          System.out.println("Expanding " + ((MinMaxNode)node).getMinMax() + " node for Team 1");
          completeMinMaxNode(((MinMaxNode)node), 1, depth);
          if (!((MinMaxNode)node).getKids().isEmpty()) {
            for (Node nextNode : ((MinMaxNode)node).getKids()) {
              generateChildren(nextNode, depth + 1);
            }
          }
        }
      }
    }

}
     */