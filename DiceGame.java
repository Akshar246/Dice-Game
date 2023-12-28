package javaapplication5;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class DiceGame {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();

        // command for starting or ending Game 
        System.out.println("Press '1' to start the game or '0' to quit:");
        String gameChoice = scanner.nextLine().trim().toLowerCase();

        // Quiting commands
        if ("0".equals(gameChoice)) {
            System.out.println("Exiting the game, GoodBye!");
            return;
        } else if (!"1".equals(gameChoice)) {
            System.out.println("Invalid input. Please enter '1' to start the game or '0' to quit.");
            return;
        }

        // Initializing arrays to store scores and used categories
        int[][] scores = new int[8][3];
        boolean[][] usedCategories = new boolean[7][3];

        // loop for 7 rounds
        for (int round = 1; round <= 7; round++) {
            System.out.println("|----------------|");
            System.out.println(" Round " + round);
            System.out.println("|----------------|");

            // Loop for each player's turn
            for (int player = 1; player <= 2; player++) {
                System.out.println("Player " + player + "'s turn:");

                // Lists to store dice values
                List<Integer> dice = new ArrayList<>();
                List<Integer> removedDice = new ArrayList<>();
                List<Integer> selectedForSequence = new ArrayList<>();

                int category = 0;
                boolean categorySelected = false;
                boolean deferredChances  = false;

                // Loop for each chance in a player's turn
                for (int chance = 1; chance <= 3; chance++) {

                    System.out.println("Chance " + chance + ": ");
                    System.out.println("Press 't' to throw dice or 'd' to defer: ");

                    // Reading player's choice for dice throw or defer
                    String defferThrowChoice = scanner.nextLine().trim();
                    if ("t".equalsIgnoreCase(defferThrowChoice)) {
                        if (chance == 1 || deferredChances) {
                            rollDice(dice, 5, random);
                        } else if (category == 7) {
                            dice.removeAll(selectedForSequence);
                            rollDice(dice, 5 - selectedForSequence.size(), random);
                        } else {
                            // For categories 1 to 6, roll only remaining dice after removing selected ones
                            dice.removeAll(removedDice);
                            rollDice(dice, dice.size(), random);
                        }

                        printDice(dice);
                        deferredChances = false;

                        // Loop for selecting a category
                        while (!categorySelected) {
                            System.out.print("Press 's' to select a category or 'd' to defer: ");
                            String categoryChoice = scanner.nextLine().trim();

                            if ("s".equalsIgnoreCase(categoryChoice)) {
                                boolean validCategory = false;

                                // Loop for validating the selected category
                                while (!validCategory) {
                                    System.out.print("Select a category One (1) Two (2) Three (3) Four (4) Five (5) Six (6) Sequence (7): ");
                                    if (scanner.hasNextInt()) {
                                        category = scanner.nextInt();
                                        scanner.nextLine();

                                        // Check if the number is within the valid range (1-7)
                                        if (category >= 1 && category <= 7) {
                                            validCategory = true;

                                            // Check if the category has already been used
                                            if (!usedCategories[category - 1][player]) {
                                                categorySelected = true;
                                                usedCategories[category - 1][player] = true;
                                            } else {
                                                System.out.println("Category already selected. Choose a different category.");
                                            }
                                        } else {
                                            System.out.println("Invalid input. Enter a number between 1 and 7.");
                                        }
                                    } else {
                                        System.out.println("Invalid input. Enter a number between 1 and 7.");
                                        scanner.next();
                                    }
                                }
                            } else if ("d".equalsIgnoreCase(categoryChoice)) {
                                System.out.println("Category selection deferred.");
                                deferredChances = true;
                                break;
                            }
                        }

                        // Specific logic for category 7 (sequence formation)
                        if (categorySelected && category == 7) {
                            System.out.print("Try to form a sequence? (y/n): ");
                            String sequenceChoice = scanner.nextLine().trim();

                            if ("y".equalsIgnoreCase(sequenceChoice)) {
                                selectDiceForSequence(dice, selectedForSequence, scanner);
                                if (selectedForSequence.size() == 5) {
                                    if (checkSequence(selectedForSequence)) {
                                        System.out.println("It's a sequence! Game over.");
                                        break;
                                    } else {
                                        System.out.println("No sequence with selected dice.");
                                    }
                                }
                            }
                        } else if (categorySelected) {
                            // Logic for categories 1 to 6: removing selected dice
                            removeSelectedDice(dice, removedDice, category);
                            System.out.println("Selected Dice after Chance " + chance + ": " + removedDice);
                        }
                    } else if ("d".equalsIgnoreCase(defferThrowChoice)) {
                        System.out.println("Chance " + chance + " deferred.");
                        deferredChances = true;
                        continue;
                    }
                }

                // Calculating the score for the turn
                int score = 0;
                if (category == 7) {
                    if (checkSequence(selectedForSequence) && selectedForSequence.size() == 5) {
                    }
                    // No points awarded for an unsuccessful sequence (the score remains 0)
                    updateScores(scores, player, category, removedDice, selectedForSequence);
                } else {
                    // Score calculation for categories 1 to 6: sum of selected dice
                    for (int die : removedDice) {
                        score += die;
                    }

                    // Final check for sequence in category 7
                    finalSequenceCheck(selectedForSequence);
                    updateScores(scores, player, category, removedDice, selectedForSequence);
                }

                ScoreTable(scores);

                // Displaying final selected dice for categories 1 to 6
                if (category >= 1 && category <= 6) {
                    System.out.println("Player " + player + "'s final selected dice for Category " + category + ": " + removedDice);
                }
                System.out.println(); // Adding a newline for separation between players
            }
        }

        // Calculating total scores for both players
        int player1TotalScore = scores[7][1];
        int player2TotalScore = scores[7][2];

        // Displaying final scores and determining the winner
        System.out.println("Final Scores:");
        ScoreTable(scores);

        if (player1TotalScore > player2TotalScore) {
            System.out.println("Player 1 wins!");
        } else if (player2TotalScore > player1TotalScore) {
            System.out.println("Player 2 wins!");
        } else {
            System.out.println("It's a tie!");
        }
    }

    // Method for printing the current dice values
    private static void printDice(List<Integer> dice) {
        System.out.print("Rolled Dice: ");
        for (int die : dice) {
            System.out.print("[" + die + "] ");
        }
        System.out.println();
    }

    // Method for rolling dice
    private static void rollDice(List<Integer> dice, int numberOfDice, Random random) {
        dice.clear();
        for (int i = 0; i < numberOfDice; i++) {
            dice.add(random.nextInt(6) + 1);
        }
    }

    // Method for removing selected dice based on the category
    private static void removeSelectedDice(List<Integer> dice, List<Integer> removedDice, int category) {
        List<Integer> diceToRemove = new ArrayList<>();

        // Find dice that match the category number
        for (int die : dice) {
            if (die == category) {
                diceToRemove.add(die);
            }
        }

        // Remove matching dice from the original list and add them to the removedDice list
        for (int die : diceToRemove) {
            dice.remove(Integer.valueOf(die));
            removedDice.add(die);
        }
    }

    // Method to check if the selected dice form a sequence
    private static boolean checkSequence(List<Integer> dice) {
        Collections.sort(dice);

        // Loop through the sorted dice to check for consecutive numbers
        for (int i = 0; i < dice.size() - 1; i++) {
            if (dice.get(i) + 1 != dice.get(i + 1)) {
                return false;
            }
        }

        return true;
    }

    // Method for selecting dice to form a sequence
    private static void selectDiceForSequence(List<Integer> dice, List<Integer> selectedForSequence, Scanner scanner) {
        System.out.println("Select dice to keep for sequence (enter numbers separated by spaces, e.g., 1 2 3):");
        // Displaying available dice for selection
        for (int i = 0; i < dice.size(); i++) {
            System.out.println((i + 1) + ". [" + dice.get(i) + "]");
        }

        // Reading player's choices for dice to keep for sequence
        String[] choices = scanner.nextLine().split("\\s+");
        for (String choice : choices) {
            try {
                int index = Integer.parseInt(choice.trim()) - 1;
                if (index >= 0 && index < dice.size() && !selectedForSequence.contains(dice.get(index))) {
                    selectedForSequence.add(dice.get(index));
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input: " + choice); // Handle invalid number input
            }
        }

        // Display selected dice for sequence
        System.out.println("Selected Dice for Sequence:");
        for (Integer die : selectedForSequence) {
            System.out.print("[" + die + "] ");
        }
        System.out.println();
    }

    // Method to check for a sequence at the end of the third chance
    private static void finalSequenceCheck(List<Integer> selectedForSequence) {
        // Check if a sequence is formed with 5 dice
        if (selectedForSequence.size() < 5 || !checkSequence(selectedForSequence)) {
            System.out.println("No sequence found by the end of Chance 3.");
        } else {
            System.out.println("A sequence was successfully formed!");
        }
    }

    // Method to display the current score table
    private static void ScoreTable(int[][] scores) {
        System.out.println("Score Table:");
        System.out.println("-----------------------------------");
        System.out.printf("|%-10s| %-10s |%-10s|\n", "Category", "Player 1", "Player 2");
        System.out.println("-----------------------------------");

        for (int i = 0; i < 8; i++) {
            if (i < 7) {
                // Display scores for categories 1 to 7
                System.out.printf("|%-10d |%-10d |%-10d|\n", i + 1, scores[i][1], scores[i][2]);
                System.out.println("-----------------------------------");
            } else {
                // Display total scores
                System.out.println("-----------------------------------");
                System.out.printf("|%-10s |%-10d |%-10d|\n", "Total", scores[7][1], scores[7][2]);
                System.out.println("-----------------------------------");
            }
        }
    }

    private static void updateScores(int[][] scores, int player, int category, List<Integer> removedDice, List<Integer> selectedForSequence) {
        int score = 0;
        if (category == 7) {
            // catagory for sequence
            if (selectedForSequence.size() == 5 && checkSequence(selectedForSequence)) {
                score = 20;
            }
        } else {
            // Score calculation with sum
            for (int die : removedDice) {
                score += die;
            }
        }

        // Update the scores array with the new score
        scores[category - 1][player] += score;
        scores[7][player] += score;
    }
}
