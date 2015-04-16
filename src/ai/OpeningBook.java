package ai;

import java.io.BufferedReader;
import java.lang.Math;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;

import game.Game;
import gtp.Vertex;

/** Opening book.
The file format contains a single line per entry, containing a board size,
a sequence that leads to the position and, seperated with a pipe symbol,
a list of moves to play in this position. Example:
@verbatim
19 | Q3 Q4
19 P3 D16 | Q16
19 Q3 | C3 C17 D3 D4 D16 Q16 R16 R17
9 E5 D3 G4 | E7
@endverbatim
Equivalent positions are derived automatically by rotating and/or
mirroring. If there are duplicates, because of sequences with move
transpositions or rotating/mirroring, reading will throw an exception
containing an error message with line number information of the
duplicates. 
This comment and opening book file from fuego.*/

public class OpeningBook {
	
	public String sideSize;
	public Game game;
	public List<int[]> beforeMoves = new ArrayList<int[]>();
	public List<int[]> movesToTake = new ArrayList<int[]>();
	public int moreMovesToPlay = -1;
	public int movesLeft = 0;
	
	public OpeningBook(int sideSize, Game game) {
		this.sideSize = Integer.toString(sideSize);
		this.game = game;
		initializeOpeningBook();

	}
	public int move;
	public int movesTaken;
	public boolean playOpeningBookMove(int[] firstMoves) {
		move = 0;
		boolean broken = false;
		/* if no moves have been played */
		if(firstMoves[0] == 0) {
		//	System.out.println("Just playing first move yo");
			/* find a move without beforemoves */
			for(int i = 0; i < beforeMoves.size(); i++) {
				if(beforeMoves.get(i)[0] == -1) {
					/* and play it */
					move = movesToTake.get(i)[0];
					System.out.println(move);
					return true;
				}
			}
		}
		// System.out.println(movesLeft);
		/* if there were moves already recognized and they weren't all equal */
		if(movesLeft > 0) {
			
			/* get the next move and decrement */
			printArray(movesToTake.get(moreMovesToPlay));
			int tempMove = movesToTake.get(moreMovesToPlay)[movesToTake.get(moreMovesToPlay).length - movesLeft];
			if(!isContainedInArray(firstMoves, tempMove)) {
				move = tempMove;
				movesLeft = movesLeft - 1;
			//	System.out.println(" " + movesLeft);
				return true;
			}
		} 
		List<int[]> beforeMoves;
		List<int[]> movesToTake;
		/* for every type of modification to the moves */
		for(int j=0;j<6;j++) {
			/* modify the beforeMoves and movesToTake */
			beforeMoves = modifyList(this.beforeMoves, j);
			movesToTake = modifyList(this.movesToTake, j);
			/* for every line */
		//	System.out.println("!!MODIFIED!!");
			for(int i = 0; i < beforeMoves.size(); i++) {
				/* if there are more moves than the moves that are being checked then skip this line */
				if(beforeMoves.get(i).length == movesTaken) {
		//			printArray(beforeMoves.get(i));
		//			System.out.println("There have been " +movesTaken+ " moves taken.");
					broken = false;
					/* if any of the opening moves last moves match the last move */
		//			System.out.println(" " + beforeMoves.get(i)[beforeMoves.get(i).length - 1] + ":" + game.getMove(0) + " ");
					if(beforeMoves.get(i)[beforeMoves.get(i).length - 1] == game.getMove(0)) {
						
						/* check if the whole sequence matches, if the length is larger than one */
						printArray(beforeMoves.get(i));
						printArray(firstMoves);
						for(int n=0;n<beforeMoves.get(i).length;n++) {
							/* if the element in the beforemoves 0-n doesn't match the move -beforeMoves.length ago,
							 * with -1 to accomodate for the way that getmove functions
							 */
							System.out.println("LENGTH: " + beforeMoves.get(i).length + " ");
							if(beforeMoves.get(i)[n] != firstMoves[n]) {
								/* there's no need to look at any other moves, so just move onto the next line */
								System.out.println(beforeMoves.get(i)[n] + " failed to match ");
								broken = true;
								break;
								
							}
						
							if(broken) {
								continue;
							}
						}
		//				System.out.println("FOUND A MOVE! ");
						/* if a move set matched, the move is equal to the matching move to take */
						int tempMove = movesToTake.get(i)[0];
						if(!isContainedInArray(firstMoves, tempMove)) {
							move = tempMove;
							/* if there's more moves to play for this set, then set the value equal to the iteration */
							if(movesToTake.get(i).length > 1) {
								moreMovesToPlay = i;
								/* and set the amount of moves left to go through equal to the length -1 */
								movesLeft = movesToTake.get(i).length -1;
							}
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	public List<int[]> modifyList(List<int[]> list, int type) {
		List<int[]> modifiedList = new ArrayList<int[]>();
		for(int[] array : list) {
			modifiedList.add(modifyArray(array, type));
		}
		return modifiedList;
	}
	
	public int[] modifyArray(int[] array, int type) {
	//	printGrid(convertArrayToGrid(array));
		int[][] rotate = Game.rotate(convertArrayToGrid(array));
	//	System.out.println("ROTATE");
	//	printGrid(rotate);
		switch(type) {
		case 0: return array;
		case 1: return convertGridToArray(rotate, array.length);
		case 2: return convertGridToArray(Game.rotate(rotate), array.length);
		case 3: return convertGridToArray(Game.rotate(Game.rotate(rotate)), array.length);
		case 4: return convertGridToArray(Game.verticalFlip(convertArrayToGrid(array)), array.length);
		case 5: return convertGridToArray(Game.horizontalFlip(convertArrayToGrid(array)), array.length);
		}
		return null;
	}
	
	public int[][] convertArrayToGrid(int[] array) {
		int[][] grid = new int[Integer.parseInt(sideSize)][Integer.parseInt(sideSize)];
		for(int i=0;i<array.length;i++) {
			int counter = 0;
			for(int k=0; k<grid.length;k++) {
				
				for(int j=0; j<grid.length;j++) {
					counter++;
					
					if(counter == array[i]) {
	//					System.out.println("Success at move: "+counter+" converting to: " + (k+1) + ", " + (j+1));
						grid[k][j] = i+1;
					}
				}
			}
		}
	//	printGrid(grid);
		
		return grid;
	}
	
	public int[] convertGridToArray(int[][] grid, int arraySize) {
		int[] array = new int[arraySize];

			for(int k=0; k<grid.length;k++) {
				for(int j=0; j<grid.length;j++) {
					if(grid[k][j] > 0) {
	//					System.out.println("found #"+grid[k][j]+" at point "+(k+1) + " " + (j+1));
						array[grid[k][j]-1] = new Vertex(j+1, k+1).toPosition(game.getGrid()) + 1;
					}
				}
			}

	//	printArray(array);
		return array;
	}
	
	public void printArray(int[] array) {
		System.out.print("[");
		for(int i = 0; i<array.length;i++) {
			System.out.print(array[i] + ", ");
		}
		System.out.print("]");
		System.out.println();
	}
	
	public boolean isContainedInArray(int[] array, int value) {
		for(int i = 0; i<array.length;i++) {
			if(array[i] == value)
				return true;
		}
		return false;
	}
	
	public void printGrid(int[][] grid) {
		
		for(int i = 0; i<grid.length;i++) {
			System.out.print("[");
			for(int j = 0; j<grid.length;j++) {
				System.out.print(grid[i][j] + ", ");
			}
			System.out.print("]");
			System.out.println();
		}
		
	}
	
	public void initializeOpeningBook() {
		try {
			for(String line: FileUtils.readLines(new File("D:/#Work/Learning/Third Year Project/Project/book.dat"))) {
				if(line.startsWith(sideSize)) {
					String[] pipeSplit = line.split("\\|");
					String[] tempBeforeMoves = pipeSplit[0].substring(2).split("\\s");
					if(tempBeforeMoves == null || tempBeforeMoves.length == 0) {
						tempBeforeMoves = new String[1];
						tempBeforeMoves[0] = "";
					}
					String[] tempMovesToTake = pipeSplit[1].split("\\s");
					int[] beforeMoves = new int[tempBeforeMoves.length];
					int[] movesToTake = new int[tempMovesToTake.length];
					if(tempBeforeMoves[0].isEmpty()) {
						beforeMoves[0] = -1;
					} else {
						for(int i=0;i<tempBeforeMoves.length;i++) {
							String[] splitBeforeMoves = tempBeforeMoves[i].split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
							int move = new Vertex(convertLetterToInt(splitBeforeMoves[0]), Integer.parseInt(splitBeforeMoves[1])).toPosition(game.getGrid());
	
							beforeMoves[i] = move;
						}
					}
					if(tempMovesToTake != null && !tempMovesToTake[0].isEmpty()  ) {
						for(int i=0;i<tempMovesToTake.length;i++) {
							String[] splitMovesToTake = tempMovesToTake[i].split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
							int move = new Vertex(convertLetterToInt(splitMovesToTake[0]), Integer.parseInt(splitMovesToTake[1])).toPosition(game.getGrid());
							movesToTake[i] = move;
						}
					} else {
						String removeWhiteSpace = pipeSplit[1].replaceAll("\\s+","");
						String[] splitMovesToTake = removeWhiteSpace.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
						int move = new Vertex(convertLetterToInt(splitMovesToTake[0]), Integer.parseInt(splitMovesToTake[1])).toPosition(game.getGrid());
						movesToTake[0] = move;
					}
					this.beforeMoves.add(beforeMoves);
					this.movesToTake.add(movesToTake);
				}
			    
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public int convertLetterToInt(String letter) {
		switch(letter) {
			case "A": return 1; 
			case "B": return 2; 
			case "C": return 3; 
			case "D": return 4; 
			case "E": return 5; 
			case "F": return 6; 
			case "G": return 7; 
			case "H": return 8; 
			case "J": return 9; 
			case "K": return 10; 
			case "L": return 11; 
			case "M": return 12; 
			case "N": return 13; 
			case "O": return 14; 
			case "P": return 15; 
			case "Q": return 16; 
			case "R": return 17; 
			case "S": return 18; 
			case "T": return 19; 
			case "U": return 20; 
			case "V": return 21; 
			case "W": return 22; 
			case "X": return 23; 
			case "Y": return 24; 
			case "Z": return 25; 
		}
		return 1;
	}
}
