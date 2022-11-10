import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
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

	/**
	 * Initializes a Robot on a specific tile in the environment.
	 */

	public Robot(Environment env, int posRow, int posCol) {
		this.env = env;
		this.posRow = posRow;
		this.posCol = posCol;
		this.toCleanOrNotToClean = false;
		this.b = null;

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
		List<IndexedWord> li = graph.getAllNodesByPartOfSpeechPattern("VB");
		for (IndexedWord word : li) {
			if (word.word().equals("pick")) {
				return processPickUp(word);
			} else if (word.word().equals("put")) {
				return processPutDown(word);
			} else if (word.word().equals("stack")) {
				return processStack(word);
			} else if (word.word().equals("unstack")) {
				return processUnstack(word);
			}

		}

		name = name.toLowerCase();
		if (name.equals("u")) {
			return Action.MOVE_UP;
		}
		if (name.equals("d")) {
			return Action.MOVE_DOWN;
		}
		if (name.equals("l")) {
			return Action.MOVE_LEFT;
		}
		if (name.equals("r")) {
			return Action.MOVE_RIGHT;
		}
		if (name.equals("us") || name.equals("unstack")) {
			return Action.UNSTACK;
		}
		if (name.equals("s") || name.equals("stack")) {
			return Action.STACK;
		}
		if (name.equals("pd") || name.equals("putdown")) {
			return Action.PUT_DOWN;
		}
		if (name.equals("pu") || name.equals("pickup")) {
			return Action.PICK_UP;
		}

		return Action.DO_NOTHING;
	}

	public Action processPickUp(IndexedWord word) {
		String blockID = "";
		String index = "";
		Action outputAct = Action.DO_NOTHING;
		List<IndexedWord> lst = graph.getChildList(word);
		System.out.println("child list: " + lst);
		for (IndexedWord w : lst) {
			if (w.tag().equals("RP") && w.word().equals("up") && outputAct.equals(Action.DO_NOTHING)) {
				outputAct = Action.PICK_UP;
				System.out.println("Action changed to pickup");
			}

			if (w.tag().equals("NN")) {
				if (w.word().equals("block")) {
					List<IndexedWord> blockChildLst = graph.getChildList(w);
					System.out.println("block child list: " + blockChildLst);
					for (IndexedWord w1 : blockChildLst) {
						if (w1.tag().equals("CD") && blockID == "") {
							blockID = w1.word();
							System.out.println("blockID: " + blockID);
						}
					}
				} else if (w.word().equals("position")) {

				}
			} else if (w.tag().equals("CD")) {
				index = w.word();
				System.out.println("index: " + index);
			}
		}
		return outputAct;
	}

	public Action processPutDown(IndexedWord word) {
		String blockID = "";
		String index = "";
		Action outputAct = Action.DO_NOTHING;
		List<IndexedWord> lst = graph.getChildList(word);
		System.out.println("child list: " + lst);
		for (IndexedWord w : lst) {
			if (w.tag().equals("RP") && w.word().equals("down") && outputAct.equals(Action.DO_NOTHING)) {
				outputAct = Action.PUT_DOWN;
				System.out.println("Action changed to putdown");
			}

			if (w.tag().equals("NN")) {
				if (w.word().equals("block")) {
					List<IndexedWord> blockChildLst = graph.getChildList(w);
					System.out.println("block child list: " + blockChildLst);
					for (IndexedWord w1 : blockChildLst) {
						if (w1.tag().equals("CD") && blockID == "") {
							blockID = w1.word();
							System.out.println("blockID: " + blockID);
						}
					}
				} else if (w.word().equals("position")) {

				}
			} else if (w.tag().equals("CD")) {
				index = w.word();
				System.out.println("index: " + index);
			}
		}
		return outputAct;
	}

	public Action processStack(IndexedWord word) {
		String blockID = "";
		String index = "";
		Action outputAct = Action.STACK;
		List<IndexedWord> lst = graph.getChildList(word);
		System.out.println("child list: " + lst);
		for (IndexedWord w : lst) {
			if (w.tag().equals("NN")) {
				if (w.word().equals("block")) {
					List<IndexedWord> blockChildLst = graph.getChildList(w);
					System.out.println("block child list: " + blockChildLst);
					for (IndexedWord w1 : blockChildLst) {
						if (w1.tag().equals("CD") && blockID == "") {
							blockID = w1.word();
							System.out.println("blockID: " + blockID);
						}
					}
				} else if (w.word().equals("position")) {

				}
			} else if (w.tag().equals("CD")) {
				index = w.word();
				System.out.println("index: " + index);
			}
		}
		return outputAct;
	}
	
	public Action processUnstack(IndexedWord word) {
		String blockID = "";
		String index = "";
		Action outputAct = Action.UNSTACK;
		List<IndexedWord> lst = graph.getChildList(word);
		System.out.println("child list: " + lst);
		for (IndexedWord w : lst) {
			if (w.tag().equals("NN")) {
				if (w.word().equals("block")) {
					List<IndexedWord> blockChildLst = graph.getChildList(w);
					System.out.println("block child list: " + blockChildLst);
					for (IndexedWord w1 : blockChildLst) {
						if (w1.tag().equals("CD") && blockID == "") {
							blockID = w1.word();
							System.out.println("blockID: " + blockID);
						}
					}
				} else if (w.word().equals("position")) {

				}
			} else if (w.tag().equals("CD")) {
				index = w.word();
				System.out.println("index: " + index);
			}
		}
		return outputAct;
	}
}
