package com.company;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

/**
 * A program for solving mazes using recursion
 *
 * @author Jakub Adrian Niemiec
 * @version 1.0
 * @since 2019-06-20
 */

public class Main {
    private static int startX;  // Starting co-ordinate X
    private static int startY;  // Starting co-ordinate Y
    private static int endX;    // Exit co-ordinate X
    private static int endY;    // Exit co-ordinate Y
    private static int height;  // Maze height
    private static int width;   // Maze width
    private static String[][] mazeStructure;    // 2D Array containing maze structure
    private static List<String> mazeFile;   // String input of the raw maze file read by user

    public static void main(String[] args) throws IOException {
        System.out.println("GENTRACK MAZE TECHNICAL TEST V - JAKUB ADRIAN NIEMIEC");

        // Ask user for maze file and verify its structure
        while (true) {
            if (askUserForFile() && extractMazeProperties(mazeFile)) {
                break;
            }
        }

        // Initialise the maze with S and E positions
        initialiseMaze(mazeStructure);

        // If maze can be solved
        if (solveMaze(startY, startX)) {
            // Show solved maze
            showSolvedMaze(mazeStructure);
        } else {
            // Tell user that maze cannot be solved
            System.out.println("[INFO]  No Solution Found");
        }

        // Keep console alive to show solution until user chooses to exit
        System.out.println("[INFO]  Press any key to exit.");
        System.in.read();   // Wait for any key to terminate program
    }

    /**
     * Method asking user for file to process
     *
     * @return boolean showing if file is valid
     */
    private static Boolean askUserForFile() {
        // Request file from terminal input
        Scanner input = new Scanner(System.in);
        System.out.println("Please specify path to maze file: \n(i.e. /Users/jakub/input.txt)");
        String filePath = input.nextLine();

        try {
            // Attempt to load the maze file
            mazeFile = loadMazeFile(filePath);
            return true;

        } catch (IOException noFile) {
            System.out.println("\n[ERROR]  The file specified cannot be found. " +
                    "\n[INFO]  Please check that the path you entered is correct and that the file exists.");
            System.out.println();
            return false;
        }

    }

    /**
     * Extracts features of the maze (shape, start/end positions, structure)
     *
     * @param inputFile raw string file from user input
     * @return true if file is loaded properly
     */
    private static boolean extractMazeProperties(List<String> inputFile) {
        try {
            // Extract maze height
            height = getMazeHeight(inputFile);
            // Extract maze width
            width = getMazeWidth(inputFile);
            // Extract start position X
            startX = getMazeStartX(inputFile);
            // Extract start position Y
            startY = getMazeStartY(inputFile);
            // Extract exit position X
            endX = getMazeEndX(inputFile);
            // Extract exit position Y
            endY = getMazeEndY(inputFile);
            // Extract maze
            mazeStructure = getMazeStructure(inputFile, height, width);
            return true;
        } catch (NumberFormatException e) {
            System.out.println("\n[ERROR]  Specified file ir not a maze or does not follow predefined format.\n");
            return false;
        }
    }

    /**
     * Prints solved maze in the specified format
     *
     * @param mazeStructure 2D array containing the maze
     */
    private static void showSolvedMaze(String[][] mazeStructure) {
        // Set maze entrance
        mazeStructure[startY][startX] = "S";
        // Set maze exit
        mazeStructure[endY][endX] = "E";
        formatMazeOutput();
        System.out.println("Maze Solution:");
        printMatrix(mazeStructure);
    }

    /**
     * Initialises maze with start and end positions
     *
     * @param mazeStructure maze with start and end positions
     */
    private static void initialiseMaze(String[][] mazeStructure) {
        // Set maze entrance
        mazeStructure[startY][startX] = "S";
        // Set maze exit
        mazeStructure[endY][endX] = "E";
    }

    /**
     * Formats maze output to specified format by replacing symbols
     */
    private static void formatMazeOutput() {
        // Replace all 1s with #
        for (int i = 0; i < mazeStructure.length; i++) {
            for (int j = 0; j < mazeStructure[i].length; j++) {
                if (mazeStructure[i][j].equals("1")) {
                    mazeStructure[i][j] = "#";
                }

                if (mazeStructure[i][j].equals("0")) {
                    mazeStructure[i][j] = " ";
                }
            }
        }
    }

    /**
     * Recursive function for solving the maze
     *
     * @param y current Y co-ordinate
     * @param x current X co-ordinate
     * @return boolean of whether the maze has been solved
     */
    private static Boolean solveMaze(int y, int x) {
        // Check if we reached a wall
        if (!isPositionClear(y, x)) {
            return false;
        }

        // Check if we found the exit
        if (isExit(y, x)) {
            return true;
        }

        // Mark current position as part of the solution
        markCurrentPosition(y, x);

        // Check if you can move South
        try {
            if (solveMaze(y + 1, x)) {
                return true;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // Check if loop
            if (solveMaze(0, x)) {
                return true;
            }
        }

        // Check if you can move East
        try {
            if (solveMaze(y, x + 1)) {
                return true;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // Check if loop
            if (solveMaze(y, 0)) {
                return true;
            }
        }

        // Check if you can move West
        try {
            if (solveMaze(y, x - 1)) {
                return true;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            if (solveMaze(y, width - 1)) {
                return true;
            }
        }

        // Check if you can move North
        try {
            if (solveMaze(y - 1, x)) {
                return true;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            if (solveMaze(height - 1, x)) {
                return true;
            }
        }

        // Unmark current position as solution path
        unmarkCurrentPosition(y, x);

        return false;
    }

    /**
     * Replaces current maze position with an 'X' to indicate traversal path
     *
     * @param y current Y co-ordinate
     * @param x current X co-ordinate
     */
    private static void markCurrentPosition(int y, int x) {
        mazeStructure[y][x] = "X";
    }

    /**
     * Restores original mark "0" for the given maze position
     *
     * @param y current Y co-ordinate
     * @param x current X co-ordinate
     */
    private static void unmarkCurrentPosition(int y, int x) {
        mazeStructure[y][x] = "0";
    }

    /**
     * Checks if the current co-ordinate is an exit
     *
     * @param y current Y co-ordinate
     * @param x current X co-ordinate
     * @return boolean of whether current position is an exit
     */
    private static Boolean isExit(int y, int x) {
        // Check if current point is an exit (meaning maze is solved)
        if (mazeStructure[y][x].equals("E")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks whether current position is clear for traversal (checks against walls etc)
     *
     * @param y current Y co-ordinate
     * @param x current X co-ordinate
     * @return boolean of whether current position is a path
     */
    private static Boolean isPositionClear(int y, int x) {
        if (mazeStructure[y][x].equals("1")) {
            return false;
        } else if (mazeStructure[y][x].equals("X")) {
            return false;
        } else if (mazeStructure[y][x].equals("#")) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Loads the structure of the maze from raw file for use by main program
     *
     * @param rawFile original maze file
     * @param height  maze height
     * @param width   maze width
     * @return 2D array representing the maze structure
     */
    private static String[][] getMazeStructure(List<String> rawFile, int height, int width) {
        // Read maze structure starting from line 3 of input file
        List<String> rawMazeStructure = rawFile.subList(3, rawFile.size());
        // Create a maze array
        String[][] maze = new String[height][width];
        // Convert input maze into an array (remove blank spaces)
        for (int i = 0; i < rawMazeStructure.size(); i++) {
            // Get current string
            String currentRow = rawMazeStructure.get(i);
            String[] separated = currentRow.split(" ");
            for (int j = 0; j < separated.length; j++) {
                maze[i][j] = separated[j];
            }
        }
        return maze;
    }

    /**
     * Get maze end co-ordinate Y
     *
     * @param rawFile input file from user
     * @return integer Y co-ordinate for maze exit
     */
    private static int getMazeEndY(List<String> rawFile) {
        String mazeSize = rawFile.get(2);
        String[] separated = mazeSize.split(" ");
        return Integer.parseInt(separated[1]);
    }

    /**
     * Get maze end co-ordinate X
     *
     * @param rawFile input file from user
     * @return integer X co-ordinate for maze exit
     */
    private static int getMazeEndX(List<String> rawFile) {
        String mazeSize = rawFile.get(2);
        String[] separated = mazeSize.split(" ");
        return Integer.parseInt(separated[0]);
    }

    /**
     * Get maze start co-ordinate Y
     *
     * @param rawFile input file from user
     * @return integer Y co-ordinate for maze start
     */
    private static int getMazeStartY(List<String> rawFile) {
        String mazeSize = rawFile.get(1);
        String[] separated = mazeSize.split(" ");
        return Integer.parseInt(separated[1]);
    }

    /**
     * Get maze start co-ordinate X
     *
     * @param rawFile input file from user
     * @return integer X co-ordinate for maze start
     */
    private static int getMazeStartX(List<String> rawFile) {
        String mazeSize = rawFile.get(1);
        String[] separated = mazeSize.split(" ");
        return Integer.parseInt(separated[0]);
    }

    /**
     * Get maze width
     *
     * @param rawFile input file from user
     * @return integer of width of the maze
     */
    private static int getMazeWidth(List<String> rawFile) {
        String mazeSize = rawFile.get(0);
        String[] separated = mazeSize.split(" ");
        return Integer.parseInt(separated[0]);
    }

    /**
     * Get maze height
     *
     * @param rawFile input file from user
     * @return integer of height of the maze
     */
    private static int getMazeHeight(List<String> rawFile) {
        String mazeSize = rawFile.get(0);
        String[] separated = mazeSize.split(" ");
        return Integer.parseInt(separated[1]);
    }

    /**
     * Loads the specified maze file
     *
     * @param path path to the file to be loaded
     * @return raw list of strings with file contents
     * @throws IOException throws exception if file is not found
     */
    private static List<String> loadMazeFile(String path) throws IOException {
        // Define new file reader and buffer
        Path filepath = Paths.get(path);
        Files.readAllBytes(filepath);

        return Files.readAllLines(filepath, StandardCharsets.UTF_8);
    }

    /**
     * Prints given 2D array as a matrix
     *
     * @param matrix 2D array to be printed as a matrix
     */
    private static void printMatrix(String[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }
}

