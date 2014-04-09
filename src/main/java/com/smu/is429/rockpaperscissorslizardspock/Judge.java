package com.smu.is429.rockpaperscissorslizardspock;

import java.util.EnumMap;

public class Judge {

  private final EnumMap<Move, EnumMap<Move, Integer>> decisionMatrix;

  // Constructor
  public Judge() {

    // We need to create the decison matrix used for determining game outcome
    decisionMatrix = new EnumMap<Move, EnumMap<Move, Integer>>(Move.class);

    // Start off with rock
    EnumMap<Move, Integer> temp = new EnumMap<Move, Integer>(Move.class);
    temp.put(Move.ROCK, 0);
    temp.put(Move.PAPER, -1);
    temp.put(Move.SCISSORS, 1);
    temp.put(Move.LIZARD, 1);
    temp.put(Move.SPOCK, -1);
    decisionMatrix.put(Move.ROCK, temp);

    // Now we look at paper
    temp = new EnumMap<Move, Integer>(Move.class);
    temp.put(Move.ROCK, 1);
    temp.put(Move.PAPER, 0);
    temp.put(Move.SCISSORS, -1);
    temp.put(Move.LIZARD, -1);
    temp.put(Move.SPOCK, 1);
    decisionMatrix.put(Move.PAPER, temp);    

    // Now we look at scissors
    temp = new EnumMap<Move, Integer>(Move.class);
    temp.put(Move.ROCK, 1);
    temp.put(Move.PAPER, 1);
    temp.put(Move.SCISSORS, 0);
    temp.put(Move.LIZARD, 1);
    temp.put(Move.SPOCK, -1);
    decisionMatrix.put(Move.SCISSORS, temp);

    // Now we look at lizard
    temp = new EnumMap<Move, Integer>(Move.class);
    temp.put(Move.ROCK, -1);
    temp.put(Move.PAPER, 1);
    temp.put(Move.SCISSORS, -1);
    temp.put(Move.LIZARD, 0);
    temp.put(Move.SPOCK, 1);
    decisionMatrix.put(Move.LIZARD, temp);

    // Finally we look at spock
    temp = new EnumMap<Move, Integer>(Move.class);
    temp.put(Move.ROCK, 1);
    temp.put(Move.PAPER, -1);
    temp.put(Move.SCISSORS, 1);
    temp.put(Move.LIZARD, -1);
    temp.put(Move.SPOCK, 0);
    decisionMatrix.put(Move.SPOCK, temp);    

  }

  // Perform checks to determine who wins the game
  // If player's bot win, return 1
  // If player's bot lost, return -1
  // else return 0
  public int hasWon(Move playerMove, Move aiMove) {

    return decisionMatrix.get(playerMove).get(aiMove).intValue();

  }

}