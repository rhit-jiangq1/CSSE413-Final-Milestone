import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;

import edu.stanford.nlp.io.EncodingPrintWriter.out;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
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
	private String sentiment;
	private ArrayList<ProcessReturn> commands;
	private ArrayList<Position> ft;

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
		this.commands = new ArrayList<>();
		this.ft = new ArrayList<>();

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
	
	public ArrayList<String> processBuild() {
		ArrayList<String> idList = new ArrayList<>();
		List<IndexedWord> lit = graph.getAllNodesByPartOfSpeechPattern("VB");
		
		for (IndexedWord word : lit) {
			if (word.word().toLowerCase().equals("build")) {
//				System.out.println("Child of: " + word.word());
				List<IndexedWord> buildC = this.graph.getChildList(word);
//				for (IndexedWord bc : buildC) {
//					System.out.print("   " + bc.word());
//				}
//				System.out.println();
				for (IndexedWord bc : buildC) {
					if (bc.tag().equals("CD")) {
						idList.add(bc.word());
//						System.out.println("Child of: " + bc.word());
						List<IndexedWord> numC = this.graph.getChildList(bc);
//						for (IndexedWord nc : numC) {
//							System.out.print("   " + nc.word());
//						}
//						System.out.println();
						for (IndexedWord nc : numC) {
							if(nc.tag().equals("CD")) {
								idList.add(nc.word());
							}
						}
						break;
					}
				}

			}
		}
		
//		for(int i = 0; i < idList.size(); i++) {
//			System.out.println(idList.get(i));
//		}
		
		return idList;
	}
	
	public ArrayList<Position> processMoveTower(IndexedWord word){
		ArrayList<Position> output = new ArrayList<>();
		
		List<IndexedWord> vcl = this.graph.getChildList(word);
//		System.out.print(word.word() + ": ");
//		for (IndexedWord vc : vcl) {
//			System.out.print(vc.word() + " " + vc.tag() + "  ");
//		}
//		System.out.println();
		for (IndexedWord vc : vcl) {
			List<IndexedWord> tcl = this.graph.getChildList(vc);
//			System.out.print(vc.word() + ": ");
//			for (IndexedWord tc : tcl) {
//				System.out.print(tc.word() + " " + tc.tag() + "  ");
//			}
//			System.out.println();
			for (IndexedWord tc : tcl) {
				if(tc.tag().equals("CD")) {
					output.add(this.stringToPos(tc.word()));
				}
			}
			
		}
		
//		for(Position p : output) {
//			System.out.println(p.getRow() + " " + p.getCol());
//		}
		
		return output;
		
	}
	
	public void getCommand() {
		if(!this.commands.isEmpty()) {
			processReturn(this.commands.remove(0));
			return;
		}

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
//			graph.prettyPrint();
			this.sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
//			System.out.println("This is the sentiment: " + this.sentiment);
		}


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
		ArrayList<String> goalIDs = null;
		ArrayList<Position> fromTo = null;
		boolean move = false;
		
		List<IndexedWord> li = graph.getAllNodesByPartOfSpeechPattern("VB|NNP|NN|RB|JJ");
		for (IndexedWord word : li) {
			String w = word.word().toLowerCase();
			if (w.equals("pick") && word.tag().equals("VB")) {
				pReturn = processPickUp(word);
			} else if (w.equals("put") && word.tag().equals("VB")) {
				pReturn = processPutDown(word);
			} else if (w.equals("stack") && word.tag().equals("NN")) {
				pReturn = processStack(word);
			} else if (w.equals("unstack") && (word.tag().equals("NNP") || word.tag().equals("RB") || word.tag().equals("JJ"))) {
				pReturn = processUnstack(word);
			}else if(w.equals("place") && word.tag().equals("VB")) {
				pReturn = processPlace(word);
			}else if(w.equals("build") && word.tag().equals("VB")) {
				goalIDs = processBuild();
			}else if(w.equals("move") && word.tag().equals("VB")) {
				fromTo = processMoveTower(word);
				move = true;
			}else if(w.equals("reverse") && word.tag().equals("JJ")) {
				fromTo = processReverse(word);
				move = false;
			}
		}

		boolean senti = this.sentiment();
		
		if(pReturn != null) {
			//top level position or id
			li = graph.getAllNodesByPartOfSpeechPattern("CD");
			for (IndexedWord word : li) {
//				System.out.println("Top level CD: " + word.word());
				if(this.isCoordinate(word.word())) {
					pReturn.addPos(word.word());
				}else {
					pReturn.addID(word.word());
				}
			}
			
			if(!pReturn.action.equals(Action.DO_NOTHING)) {
				processReturn(pReturn);
			}else {
//				System.out.println("place");
				if(processReturn(new ProcessReturn(Action.PICK_UP, pReturn.blockID, null))) {
					if(this.env.getTileStatus(pReturn.pos.getRow(), pReturn.pos.getCol()) == TileStatus.TARGET) {
						this.commands.add(new ProcessReturn(Action.STACK, -1, pReturn.pos));
					}else {
						this.commands.add(new ProcessReturn(Action.PUT_DOWN, -1, pReturn.pos));
					}
				}
			}
			return;
		// build tower case
		}else if(goalIDs != null) {
			LinkedList<Predicate> startState = new LinkedList<>();
			LinkedList<Predicate> goals = new LinkedList<>();
			//build start state
			startState = this.buildStartState();
			//build goals
			for(int i = 0; i < goalIDs.size() - 1; i++) {
				goals.add(new On(goalIDs.get(i), goalIDs.get(i+1)));
			}
			//run planner
			Set<String> objects = Planner.Objects(startState);
			LinkedList<Rule> plan = new LinkedList<>();
//			System.out.println("Start State:");
//			for (Predicate ss : startState) {
//				System.out.println(ss);
//			}
//			System.out.println("\nGoals:");
//			for (Predicate g : goals) {
//				System.out.println(g);
//			}
			
			Planner.STRIPS(startState, goals, plan, objects);
			//read plan
			this.processPlan(plan);
				
//			System.out.println("\nResult--------------------");
//			
//			if (plan.isEmpty()) {
//				System.out.println("\nNo plan.");
//			}
//			for(Rule a: plan) {
//				System.out.println(a);
//			}
//			System.out.println();
//			for(ProcessReturn pr : this.commands) {
//				System.out.println(pr.toString());
//			}
//			System.out.println();
//			System.out.println("\nState:");
//			for (Predicate p : startState) {
//				System.out.println(p);
//			}
		//reverse or move
		}else if(fromTo != null && !move) {//only reverse
			this.reverse(fromTo, this.getTowerHeight(fromTo.get(0).getRow(), fromTo.get(0).getCol()));
			
			return;
		}else if(fromTo != null && move) {
			Position midPoint = new Position(fromTo.get(0).getRow(), fromTo.get(1).getCol());
			ArrayList<Position> ft1 = new ArrayList<>();
			ft1.add(fromTo.get(0));
			ft1.add(midPoint);
			ArrayList<Position> ft2 = new ArrayList<>();
			ft2.add(midPoint);
			ft2.add(fromTo.get(1));
			this.reverse(ft1, this.getTowerHeight(fromTo.get(0).getRow(), fromTo.get(0).getCol()));
			this.reverse(ft2, this.getTowerHeight(fromTo.get(0).getRow(), fromTo.get(0).getCol()));
			
		}else if(senti) {
			return;
		}else {
			System.out.println("I can't understand what you said");
		}
		
		
	}
	private int getTowerHeight(int row, int col) {
		int h = 0;
		if (this.env.isTarget(row, col)) {
			h++;
			Position tempP = new Position(row, col);
			for (Block b: this.env.getBlocks()) {
				if (tempP.equals(b.getPosition())) {
					Block currentBlock = b;
					while (currentBlock != null) {
						h++;
						currentBlock = currentBlock.getNextBlock();
					}
					
					return h;
				}
			}
		}
		
		return -1;
	
	}
	
	private void reverse(ArrayList<Position> fromTo, int towerHeight) {
		boolean tower = false;
		System.out.println("Fromto List size:"+fromTo.size());
		for(int i = 0; i < towerHeight - 1; i++) {
			
			if(towerHeight > 1 && i != towerHeight - 2) {
				this.commands.add(new ProcessReturn(Action.UNSTACK, -1, fromTo.get(0)));
			}else {
				this.commands.add(new ProcessReturn(Action.PICK_UP, -1, fromTo.get(0)));
			}
			
			if(this.env.isTarget(fromTo.get(1).getRow(), fromTo.get(1).getCol()) || tower) {
				this.commands.add(new ProcessReturn(Action.STACK, -1, fromTo.get(1)));
			}else {
				this.commands.add(new ProcessReturn(Action.PUT_DOWN, -1, fromTo.get(1)));
				tower = true;
			}

		}
	}
	
	private ArrayList<Position> processReverse(IndexedWord word) {
		List<IndexedWord> li = graph.getAllNodesByPartOfSpeechPattern("CD");
		ArrayList<Position> output = new ArrayList<>();
		for(IndexedWord w : li) {
			output.add(stringToPos(w.word()));
		}
		for(Position p : output) {
			System.out.println(p.getRow() + " " + p.getCol());
		}
		return output;
	}

	private void processPlan(LinkedList<Rule> plan) {
		for(int i = 0; i < plan.size(); i++) {
			Rule p = plan.get(i);
			if(p instanceof PickUp) {
				this.commands.add(new ProcessReturn(Action.PICK_UP, Integer.parseInt(((PickUp) p).block), null));
			}else if(p instanceof PutDown) {
				this.commands.add(new ProcessReturn(Action.PUT_DOWN, Integer.parseInt(((PutDown) p).block), null));
			}else if(p instanceof StackIt) {
				this.commands.add(new ProcessReturn(Action.STACK, Integer.parseInt(((StackIt) p).target), null));
			}else if(p instanceof UnStackIt) {
				this.commands.add(new ProcessReturn(Action.UNSTACK, Integer.parseInt(((UnStackIt) p).block), null));
			}
		}
	}

	public LinkedList<Predicate> buildStartState(){
		LinkedList<Predicate> startState = new LinkedList<>();
		//hand
		if(this.b == null) {
			startState.add(new Handempty());
		}else {
			startState.add(new Holding(""+this.b.getID()));
		}
		
		//blocks
		for(Block b : this.env.getBlocks()) {
			startState.add(new OnTable(""+b.getID()));
			Block current = b;
			while(current.getNextBlock() != null) {
				startState.add(new On("" + current.getNextBlock().getID(), "" + current.getID()));
				current = current.getNextBlock();
			}
			startState.add(new Clear("" + current.getID()));
		}
		
		return startState;
		
	}
	
	public boolean processReturn(ProcessReturn pReturn) {
		// single action
		if (pReturn.blockID == -1 && pReturn.pos == null) {
//			System.out.println("Single action");
			if (this.checkSingleAction(pReturn.action, new Position(this.posRow, this.posCol))) {
				this.actions.add(pReturn.action);
				return true;
			} else {
				return false;
			}
		}
		
		// only block id
		if (pReturn.blockID != -1 && pReturn.pos == null) {
//			System.out.println("Get block " + pReturn.blockID);
			int id = pReturn.blockID;
			Block targetBlock = null;
			
			// find the block
			Position blockPos = null;
			//pick up and unstack			
				for (Block b : this.env.getBlocks()) {
//					System.out.println("check block " + b.getID());
					
					Block current = b;
					while(current != null) {
						if (current.getID() == id) {
//							System.out.println("find block " + id);
							targetBlock = current;
							
							blockPos = current.getPosition();
						}
						current = current.getNextBlock();
					}
				}
				
				if (targetBlock == null) {
					System.out.println("Block " + id + " does not exist");
					return false;
				}
			
			// Check valid
			if (!checkSingleAction(pReturn.action, blockPos)) {
//				System.out.println("Cannot pick up");
				return false;
			} else {
				if(this.bfs(blockPos)) {
//					for(int i = 0; i < this.actions.size(); i++) {
//						System.out.print("\t" + this.actions.get(i));
//					}
//					System.out.println();
					this.actions.add(pReturn.action);
					return true;
				}else {
					System.out.println("Cannot get to block");
					return false;
				}
			}
		}
		
		// only position
		if (pReturn.blockID == -1 && pReturn.pos != null) {
			// Check valid
			Position blockPos = pReturn.pos;
			if (!checkSingleAction(pReturn.action, blockPos)) {
				return false;
			} else {
				if(this.bfs(blockPos)) {
					this.actions.add(pReturn.action);
					return true;
				}else {
					System.out.println("Cannot get to position");
					return false;
				}
			}
		}
		return false;
		
	}
		


	/**
	 * This method implements breadth-first search. It populates the path LinkedList
	 * and sets pathFound to true, if a path has been found. IMPORTANT: This method
	 * increases the openCount field every time your code adds a node to the open
	 * data structure, i.e. the queue or priorityQueue
	 * 
	 */
	public boolean bfs(Position target) {
		// start up
		Queue<State> open = new LinkedList<State>();

		State start = new State(this.posRow, this.posCol, 0, this.actions, target);
		open.add(start);
		ArrayList<State> closed = new ArrayList<>();

		// child build helper list
		int[][] step = { { -1, 0 }, { 1, 0 }, { 0, 1 }, { 0, -1 } };
		Action[] act = { Action.MOVE_UP, Action.MOVE_DOWN, Action.MOVE_RIGHT, Action.MOVE_LEFT };
		
		// pop
		while (!open.isEmpty()) {
			State current = open.poll();

			// check current
			if (target.getRow() == current.row && target.getCol() == current.col) {
				this.actions = current.path;
				return true;
			}	
			
			// add child
			for (int i = 0; i < 4; i++) {

				if (!this.env.getTileStatus(current.row + step[i][0], current.col + step[i][1]).equals(TileStatus.IMPASSABLE)) {// valid pos && !this.env.getTileStatus(current.row - 1, current.col).equals(TileStatus.DIRTY)
					
					LinkedList newPath = (LinkedList) current.path.clone();
//					System.out.println("add step");
					newPath.add(act[i]);
					State newState = new State(current.row + step[i][0], current.col + +step[i][1], current.cost + 1, newPath, current.target);
					// check open and closed
					if (!open.contains(newState) && !closed.contains(newState)) {
//						this.openCount++;
						open.add(newState);
					}
				}
				
			}
			closed.add(current);
		}

		return false;

	}

	public class State implements Comparable {
		public int row;
		public int col;
		public int cost;
		public LinkedList<Action> path;
		public Position target;
		public int estimate;

		public State(int row, int col, int cost, LinkedList<Action> path, Position target) {
			this.row = row;
			this.col = col;
			this.cost = cost;
			this.path = (LinkedList) path.clone();
			this.target = target;
		}

		public State(int row, int col, int cost, LinkedList path, Position target, int estimate) {
			this.row = row;
			this.col = col;
			this.cost = cost;
			this.path = (LinkedList) path.clone();
			this.target = target;
			this.estimate = estimate;
		}

		@Override
		public boolean equals(Object obj) {
			// ll eq
			return this.row == ((State) obj).row && this.col == ((State) obj).col;
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
//		System.out.println("Check action " + action.toString() + " at position " + pos.getRow() + " " + pos.getCol());
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
		} else if (!this.env.isTarget(row, col)) {// does not already have block
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
		@Override
		public String toString() {
			String pos;
			if(this.pos == null) {
				pos = "null";
			}else {
				pos = this.pos.getRow() + " " + this.pos.getCol();
			}
			return this.action.toString() + " " + this.blockID + pos;
		}

		Action action;
		int blockID;
		Position pos;
		
		public ProcessReturn(Action action, int blockID, Position pos) {
			this.action = action;
			this.blockID = blockID;
			this.pos = pos;
		}

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
		
		public void addPos(String posString) {
			if (posString.isBlank()) {
				this.pos = null;
			} else {
				this.pos = stringToPos(posString);
			}
		}
		
		public void addID(String blockIDString) {
			if (blockIDString.isBlank()) {
				this.blockID = -1;
			} else {
				this.blockID = Integer.parseInt(blockIDString);
			}
		}


	}
	public Position stringToPos(String s) {
		String[] arrOfStr = s.split(",", 2);
		Position p = new Position(Integer.parseInt(arrOfStr[0]), Integer.parseInt(arrOfStr[1]));
		return p;
	}

	public boolean isCoordinate(String s) {
		if (s.contains(",")) {
			return true;
		} else {
			return false;
		}
	}

	private Robot.ProcessReturn processPlace(IndexedWord word) {
		String blockID = "";
		String index = "";
		Action outputAct = Action.DO_NOTHING;
		List<IndexedWord> lst = graph.getChildList(word);
//		System.out.println("child list: " + lst);
		for (IndexedWord w : lst) {

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

//		System.out.println("Action: " + outputAct.toString());
//		System.out.println("Block ID: " + blockID);
//		System.out.println("Position: " + index);

		return new ProcessReturn(outputAct, blockID, index);
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

//		System.out.println("Action: " + outputAct.toString());
//		System.out.println("Block ID: " + blockID);
//		System.out.println("Position: " + index);

		return new ProcessReturn(outputAct, blockID, index);
	}

	public ProcessReturn processPutDown(IndexedWord word) {
		String blockID = "";
		String index = "";
		Action outputAct = Action.DO_NOTHING;
		List<IndexedWord> lst = graph.getChildList(word);
//		System.out.println("child list: " + lst);
		for (IndexedWord w : lst) {
			if ((w.tag().equals("RP") || w.tag().equals("RB")) && w.word().equals("down") && outputAct.equals(Action.DO_NOTHING)) {
				outputAct = Action.PUT_DOWN;
//				System.out.println("Action changed to putdown");
			}

			if (w.tag().equals("NN")) {
				if (w.word().equals("block")) {
					List<IndexedWord> blockChildLst = graph.getChildList(w);
//					System.out.println("block child list: " + blockChildLst);
					for (IndexedWord w1 : blockChildLst) {
						if (w1.tag().equals("CD") && blockID == "") {
							if (this.isCoordinate(w1.word())) {
								index = w1.word();
							} else {
								blockID = w1.word();
							}
//							System.out.println("blockID: " + blockID);
						}
					}
				} else if (w.word().equals("position")) {

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

//		System.out.println("Action: " + outputAct.toString());
//		System.out.println("Block ID: " + blockID);
//		System.out.println("Position: " + index);

		return new ProcessReturn(outputAct, blockID, index);
	}

	public ProcessReturn processStack(IndexedWord word) {
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
							if (this.isCoordinate(w1.word())) {
								index = w1.word();
							} else {
								blockID = w1.word();
							}
//							System.out.println("blockID: " + blockID);
						}
					}
				} else if (w.word().equals("position")) {

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

//		System.out.println("Action: " + outputAct.toString());
//		System.out.println("Block ID: " + blockID);
//		System.out.println("Position: " + index);
		
		return new ProcessReturn(outputAct, blockID, index);
	}

	public ProcessReturn processUnstack(IndexedWord word) {
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
							if (this.isCoordinate(w1.word())) {
								index = w1.word();
							} else {
								blockID = w1.word();
							}
//							System.out.println("blockID: " + blockID);
						}
					}
				} else if (w.word().equals("position")) {

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

//		System.out.println("Action: " + outputAct.toString());
//		System.out.println("Block ID: " + blockID);
//		System.out.println("Position: " + index);
		
		return new ProcessReturn(outputAct, blockID, index);
	}
	
	public boolean sentiment() {
		switch (this.sentiment) {
		case "Very positive":
//	  System.out.println("Vp");
			thankYou();
			return true;
		case "Positive":
// 	System.out.println("p");
			thankYou();
			return true;
		case "Very negative":
//		System.out.println("Vn");
			Sorry();
			return true;
		case "Negative":
//	  System.out.println("n");
			Sorry();
			return true;
		}
		return false;
		
	}
	
	private void thankYou() {
//System.out.println("Thank you");
		int i = (int)(Math.random() * 3);
//System.out.print(i);
		switch(i) {
		case 0: 
			System.out.println("Thanks!");
			break;
		case 1: 
			System.out.println("Thank you!");
			break;
		case 2: 
			System.out.println("You are welcome");
			break;
		default:
			System.out.println("should not say this");
		}
	}
	
	private void Sorry() {
		int i = (int)(Math.random() * 3);
		switch(i) {
		case 0: 
			System.out.println("Sorry");
			break;
		case 1: 
			System.out.println("Sad");
			break;
		case 2: 
			System.out.println("I'm so sorry");
			break;
		default:
			System.out.println("should not say this");
		}
		
	}


}
