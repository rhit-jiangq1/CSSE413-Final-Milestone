import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.Scanner;

import edu.stanford.nlp.io.EncodingPrintWriter.out;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

/**
 * Represents an intelligent agent moving through a particular room. The robot
 * only has one sensor - the ability to get the status of any tile in the
 * environment through the command env.getTileStatus(row, col).
 * 
 * @author Adam Gaweda, Michael Wollowski
 */

public class Robot {
	private Environment env;
	private int posRow;
	private int posCol;
	private boolean toCleanOrNotToClean;
	private boolean pathFound;
	private Block b;

	private Properties props;
	private StanfordCoreNLP pipeline;
	SemanticGraph graph;
	private LinkedList<Action> actions;

	/**
	 * Initializes a Robot on a specific tile in the environment.
	 */

	public Robot(Environment env, int posRow, int posCol) {
		this.env = env;
		this.posRow = posRow;
		this.posCol = posCol;
		this.toCleanOrNotToClean = false;
		this.b = null;
		this.actions = new LinkedList<>();

		props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, sentiment");
		pipeline = new StanfordCoreNLP(props);
	}

	public void init() {

	}

	public Block getBlock() {
		return this.b;
	}

	public boolean setBlock(Block b) {
		if (this.b == null) {
			this.b = b;
			return true;
		} else if (b == null) {
			this.b = null;
			return true;
		} else {
			return false;
		}
	}

	public boolean getPathFound() {
		return pathFound;
	}

	public int getPathLength() {
		// TODO: modify this procedure to return the actual path length.
		// You will likely have to track it in some counter.
		return 0;
	}

	public int getPosRow() {
		return posRow;
	}

	public int getPosCol() {
		return posCol;
	}

	public void incPosRow() {
		posRow++;
	}

	public void decPosRow() {
		posRow--;
	}

	public void incPosCol() {
		posCol++;
	}

	public void decPosCol() {
		posCol--;
	}

	/**
	 * Returns the next action to be taken by the robot. A support function that
	 * processes the path LinkedList that has been populates by the search
	 * functions.
	 */
	public Action getAction() {
		if (this.actions.isEmpty()) {
			this.getCommand();
		}
		for (Action a : this.actions) {
			return this.actions.remove(0);
		}
		return Action.DO_NOTHING;
	}

	public void getCommand() {
		System.out.print("> ");
		Scanner sc = new Scanner(System.in);
		String name = sc.nextLine();

		Annotation annotation;
		annotation = new Annotation(name);
		pipeline.annotate(annotation);
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		if (sentences != null && !sentences.isEmpty()) {
			CoreMap sentence = sentences.get(0);
			graph = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
			graph.prettyPrint();
		}

		Action output = Action.DO_NOTHING;

		// keyword
		Action temp = this.keyword(name);
		if (!temp.equals(Action.DO_NOTHING)) {// have single action
			if (!this.checkSingleAction(temp, new Position(this.posRow, this.posCol))) {
				return;
			} else {
				this.actions.add(temp);
				return;
			}
		}

		// check sentence
		ProcessReturn pReturn = null;
		List<IndexedWord> li = graph.getAllNodesByPartOfSpeechPattern("VB");
		for (IndexedWord word : li) {
			if (word.word().equals("pick")) {
				pReturn = processPickUp(word);
			} else if (word.word().equals("put")) {
				output = processPutDown(word);
			} else if (word.word().equals("stack")) {
				output = processStack(word);
			} else if (word.word().equals("unstack")) {
				output = processUnstack(word);
			}
		}

		// single action
		if (pReturn.blockID == -1 && pReturn.pos == null) {
			if (this.checkSingleAction(pReturn.action, new Position(this.posRow, this.posCol))) {
				this.actions.add(pReturn.action);
				return;
			} else {
				return;
			}
		}

		// only block id
		if (pReturn.blockID != -1 && pReturn.pos == null) {
			int id = pReturn.blockID;
			Block targetBlock = null;

			// find the block
			for (Block b : this.env.getBlocks()) {
				if (b.getID() == id) {
					targetBlock = b;
				}
			}
			if (targetBlock == null) {
				System.out.println("Block " + id + " does not exist");
				return;
			}

			// Check valid
			Position blockPos = targetBlock.getPosition();
			if (!checkSingleAction(pReturn.action, blockPos)) {
				return;
			} else {
				LinkedList<Position> tlist = new LinkedList<Position>();
				tlist.add(blockPos);
				this.bfs(tlist);
				this.actions.add(pReturn.action);
				return;
			}
		}

		// only position
		if (pReturn.blockID == -1 && pReturn.pos != null) {
			Block targetBlock = env.getBlock(pReturn.pos.getRow(), pReturn.pos.getCol());
			if (targetBlock == null) {
				return;
			}

			// Check valid
			Position blockPos = targetBlock.getPosition();
			if (!checkSingleAction(pReturn.action, blockPos)) {
				return;
			} else {
				LinkedList<Position> tlist = new LinkedList<Position>();
				tlist.add(blockPos);
				this.bfs(tlist);
				this.actions.add(pReturn.action);
				return;
			}
		}

	}

	/**
	 * This method implements breadth-first search. It populates the path LinkedList
	 * and sets pathFound to true, if a path has been found. IMPORTANT: This method
	 * increases the openCount field every time your code adds a node to the open
	 * data structure, i.e. the queue or priorityQueue
	 * 
	 */
	public void bfs(LinkedList<Position> targets) {
		// start up
		Queue<State> open = new LinkedList<State>();

		State start = new State(this.posRow, this.posCol, 0, this.actions, targets);
		open.add(start);
		ArrayList<State> closed = new ArrayList<>();

		// child build helper list
		int[][] step = { { -1, 0 }, { 1, 0 }, { 0, 1 }, { 0, -1 } };
		Action[] act = { Action.MOVE_UP, Action.MOVE_DOWN, Action.MOVE_RIGHT, Action.MOVE_LEFT };
		// pop
		while (!open.isEmpty()) {
			State current = open.poll();

			// check current
			if (this.env.getTileStatus(current.row, current.col) == TileStatus.TARGET) {
//				current.path.add(Action.CLEAN);
				for (Position p : current.targets) {
					if (p.getRow() == current.row && p.getCol() == current.col) {

						current.targets.remove(p);
						break;
					}
				}
				if (current.targets.isEmpty()) {
					// TODO
					this.actions = current.path;
//					this.path.add(Action.CLEAN);
//					this.pathLength = current.cost;
					this.pathFound = true;
					return;
				}
			}
			// add child
			for (int i = 0; i < 4; i++) {

				if (!this.env.getTileStatus(current.row + step[i][0], current.col + step[i][1])
						.equals(TileStatus.IMPASSABLE)
						&& !this.env.getTileStatus(current.row - 1, current.col).equals(TileStatus.DIRTY)) {// valid pos
					LinkedList newPath = (LinkedList) current.path.clone();
					newPath.add(act[i]);
					State newState = new State(current.row + step[i][0], current.col + +step[i][1], current.cost + 1,
							newPath, current.targets);
					// check open and closed
					if (!open.contains(newState) && !closed.contains(newState)) {
//						this.openCount++;
						open.add(newState);
					}
				}
			}
			closed.add(current);
		}

		this.pathFound = false;

	}

	public class State implements Comparable {
		public int row;
		public int col;
		public int cost;
		public LinkedList<Action> path;
		public LinkedList<Position> targets;
		public int estimate;

		public State(int row, int col, int cost, LinkedList<Action> path, LinkedList<Position> targets) {
			this.row = row;
			this.col = col;
			this.cost = cost;
			this.path = (LinkedList) path.clone();
			this.targets = (LinkedList<Position>) targets.clone();
		}

		public State(int row, int col, int cost, LinkedList path, LinkedList<Position> targets, int estimate) {
			this.row = row;
			this.col = col;
			this.cost = cost;
			this.path = (LinkedList) path.clone();
			this.targets = (LinkedList<Position>) targets.clone();
			this.estimate = estimate;
		}

		@Override
		public boolean equals(Object obj) {
			// ll eq
			if (this.targets.size() != ((State) obj).targets.size()) {
				return false;
			}
			int same = 0;

			for (int i = 0; i < this.targets.size(); i++) {
				for (int j = 0; j < ((State) obj).targets.size(); j++) {
					if (this.targets.get(i).equals(((State) obj).targets.get(j))) {
						same++;
					}
				}
			}

			return this.row == ((State) obj).row && this.col == ((State) obj).col && same == this.targets.size();
		}

		@Override
		public int compareTo(Object o) {
			int thisF = this.cost + this.estimate;
			int oF = ((State) o).cost + ((State) o).estimate;
			if (thisF == oF) {
				return 0;
			} else if (thisF > oF) {
				return 1;
			} else {
				return -1;
			}
		}

	}

	/**
	 * Check whether the given action is valid at the given position
	 * 
	 * @param action
	 * @return boolean
	 */
	public boolean checkSingleAction(Action action, Position pos) {
		// check valid action
		switch (action) {
		case UNSTACK:
			if (!canUnstack(pos)) {
				System.out.println("I can't unstack");
				return false;
			}
			break;
		case STACK:
			if (!canStack(pos)) {
				System.out.println("I can't stack");
				return false;
			}
			break;
		case PUT_DOWN:
			if (!canPutDown(pos)) {
				System.out.println("I can't put down");
				return false;
			}
			break;
		case PICK_UP:
			if (!canPickUp(pos)) {
				System.out.println("I can't pick up");
				return false;
			}
			break;
		default:
			break;
		}
		return true;
	}

	public Action keyword(String name) {
		Action output = Action.DO_NOTHING;
		name = name.toLowerCase();
		if (name.equals("u")) {
			output = Action.MOVE_UP;
		}
		if (name.equals("d")) {
			output = Action.MOVE_DOWN;
		}
		if (name.equals("l")) {
			output = Action.MOVE_LEFT;
		}
		if (name.equals("r")) {
			output = Action.MOVE_RIGHT;
		}
		if (name.equals("us") || name.equals("unstack")) {
			output = Action.UNSTACK;
		}
		if (name.equals("s") || name.equals("stack")) {
			output = Action.STACK;
		}
		if (name.equals("pd") || name.equals("putdown")) {
			output = Action.PUT_DOWN;
		}
		if (name.equals("pu") || name.equals("pickup")) {
			output = Action.PICK_UP;
		}
		return output;
	}

	public boolean canPickUp(Position Pos) {
		int row = Pos.getRow();
		int col = Pos.getCol();
		if (this.b != null) {// block in hand
			return false;
		} else if (!this.env.isTarget(row, col)) {// no block on current position
			return false;
		} else if (this.env.isTower(row, col)) {// stacked
			return false;
		}
		return true;
	}

	public boolean canPutDown(Position Pos) {
		int row = Pos.getRow();
		int col = Pos.getCol();
		if (this.b == null) {// block not in hand
			return false;
		} else if (this.env.isTarget(row, col)) {// block on current position
			return false;
		}
		return true;
	}

	public boolean canStack(Position Pos) {
		int row = Pos.getRow();
		int col = Pos.getCol();
		if (this.b == null) {// block not in hand
			return false;
		} else if (!this.env.isTower(row, col)) {// not stacked
			return false;
		}
		return true;
	}

	public boolean canUnstack(Position Pos) {
		int row = Pos.getRow();
		int col = Pos.getCol();
		if (this.b != null) {// block in hand
			return false;
		} else if (!this.env.isTower(row, col)) {// not stacked
			return false;
		}
		return true;
	}

	private class ProcessReturn {
		Action action;
		int blockID;
		Position pos;

		public ProcessReturn(Action action, String blockIDString, String posString) {
			this.action = action;
			// process block id
			if (blockIDString.isBlank()) {
				this.blockID = -1;
			} else {
				this.blockID = Integer.parseInt(blockIDString);
			}
			// process posString
			if (posString.isBlank()) {
				this.pos = null;
			} else {
				this.pos = stringToPos(posString);
			}

		}

		public Position stringToPos(String s) {
			String[] arrOfStr = s.split(",", 2);
			Position p = new Position(Integer.parseInt(arrOfStr[0]), Integer.parseInt(arrOfStr[1]));
			return p;
		}

	}

	public boolean isCoordinate(String s) {
		if (s.contains(",")) {
			return true;
		} else {
			return false;
		}
	}

	public ProcessReturn processPickUp(IndexedWord word) {
		String blockID = "";
		String index = "";
		Action outputAct = Action.DO_NOTHING;
		List<IndexedWord> lst = graph.getChildList(word);
//		System.out.println("child list: " + lst);
		for (IndexedWord w : lst) {
			if (w.tag().equals("RP") && w.word().equals("up") && outputAct.equals(Action.DO_NOTHING)) {
				outputAct = Action.PICK_UP;
//				System.out.println("Action changed to pickup");
			}

			if (w.tag().equals("NN")) {
				if (w.word().equals("block")) {
					List<IndexedWord> blockChildLst = graph.getChildList(w);
//					System.out.println("block child list: " + blockChildLst);
					for (IndexedWord w1 : blockChildLst) {
						if (w1.tag().equals("CD")) {// && blockID == ""
							if (this.isCoordinate(w1.word())) {
								index = w1.word();
							} else {
								blockID = w1.word();
							}
//							System.out.println("blockID: " + blockID);
						}
					}
				}
			} else if (w.tag().equals("CD")) {
				if (this.isCoordinate(w.word())) {
					index = w.word();
				} else {
					blockID = w.word();
				}
//				System.out.println("index: " + index);
			}
		}

		System.out.println("Action: " + outputAct.toString());
		System.out.println("Block ID: " + blockID);
		System.out.println("Position: " + index);

		return new ProcessReturn(outputAct, blockID, index);
	}

	public Action processPutDown(IndexedWord word) {
		String blockID = "";
		String index = "";
		Action outputAct = Action.DO_NOTHING;
		List<IndexedWord> lst = graph.getChildList(word);
//		System.out.println("child list: " + lst);
		for (IndexedWord w : lst) {
			if (w.tag().equals("RP") && w.word().equals("down") && outputAct.equals(Action.DO_NOTHING)) {
				outputAct = Action.PUT_DOWN;
//				System.out.println("Action changed to putdown");
			}

			if (w.tag().equals("NN")) {
				if (w.word().equals("block")) {
					List<IndexedWord> blockChildLst = graph.getChildList(w);
//					System.out.println("block child list: " + blockChildLst);
					for (IndexedWord w1 : blockChildLst) {
						if (w1.tag().equals("CD") && blockID == "") {
							blockID = w1.word();
//							System.out.println("blockID: " + blockID);
						}
					}
				} else if (w.word().equals("position")) {

				}
			} else if (w.tag().equals("CD")) {
				index = w.word();
//				System.out.println("index: " + index);
			}
		}

		System.out.println("Action: " + outputAct.toString());
		System.out.println("Block ID: " + blockID);
		System.out.println("Position: " + index);

		return outputAct;
	}

	public Action processStack(IndexedWord word) {
		String blockID = "";
		String index = "";
		Action outputAct = Action.STACK;
		List<IndexedWord> lst = graph.getChildList(word);
//		System.out.println("child list: " + lst);
		for (IndexedWord w : lst) {
			if (w.tag().equals("NN")) {
				if (w.word().equals("block")) {
					List<IndexedWord> blockChildLst = graph.getChildList(w);
//					System.out.println("block child list: " + blockChildLst);
					for (IndexedWord w1 : blockChildLst) {
						if (w1.tag().equals("CD") && blockID == "") {
							blockID = w1.word();
//							System.out.println("blockID: " + blockID);
						}
					}
				} else if (w.word().equals("position")) {

				}
			} else if (w.tag().equals("CD")) {
				index = w.word();
//				System.out.println("index: " + index);
			}
		}

		System.out.println("Action: " + outputAct.toString());
		System.out.println("Block ID: " + blockID);
		System.out.println("Position: " + index);
		return outputAct;
	}

	public Action processUnstack(IndexedWord word) {
		String blockID = "";
		String index = "";
		Action outputAct = Action.UNSTACK;
		List<IndexedWord> lst = graph.getChildList(word);
//		System.out.println("child list: " + lst);
		for (IndexedWord w : lst) {
			if (w.tag().equals("NN")) {
				if (w.word().equals("block")) {
					List<IndexedWord> blockChildLst = graph.getChildList(w);
//					System.out.println("block child list: " + blockChildLst);
					for (IndexedWord w1 : blockChildLst) {
						if (w1.tag().equals("CD") && blockID == "") {
							blockID = w1.word();
//							System.out.println("blockID: " + blockID);
						}
					}
				} else if (w.word().equals("position")) {

				}
			} else if (w.tag().equals("CD")) {
				index = w.word();
//				System.out.println("index: " + index);
			}
		}

		System.out.println("Action: " + outputAct.toString());
		System.out.println("Block ID: " + blockID);
		System.out.println("Position: " + index);
		return outputAct;
	}

}
