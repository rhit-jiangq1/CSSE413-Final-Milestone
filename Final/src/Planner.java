import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Stack;

public class Planner {
	
	public static void main(String[] args) {
		LinkedList<Predicate> startState = new LinkedList<>();
		LinkedList<Predicate> goals = new LinkedList<>();
//		//Problem 1:
//		startState.add(new OnTable("A"));
//		startState.add(new Clear("A"));
//		startState.add(new Handempty());
//		goals.add(new OnTable("A"));
		
//		//Problem 2:
		// 
		// A
		// B
		// \\\\\
//		startState.add(new OnTable("B"));
//		startState.add(new On("A", "B"));
//		startState.add(new Clear("A"));
//		startState.add(new Handempty());
//		goals.add(new On("A", "B"));
		
//		//Problem 3:
		//				A
		// A B			B
		// \\\\		   \\\\
//		startState.add(new OnTable("B"));
//		startState.add(new OnTable("A"));
//		startState.add(new Clear("A"));
//		startState.add(new Clear("B"));
//		startState.add(new Handempty());
//		goals.add(new On("A", "B"));
		
//		//Problem 4:
//		startState.add(new OnTable("A"));
//		startState.add(new On("B", "A"));
//		startState.add(new Clear("B"));
//		startState.add(new Handempty());
//		goals.add(new On("A", "B"));
		
//		//Problem 5:
		//					 A
		//					 B
		//  A  B  C			 C
		//\\\\\\\\\\\       \\\\\
//		startState.add(new OnTable("A"));
//		startState.add(new OnTable("B"));
//		startState.add(new OnTable("C"));
//		startState.add(new Clear("A"));
//		startState.add(new Clear("B"));
//		startState.add(new Clear("C"));
//		startState.add(new Handempty());
//		goals.add(new On("B", "C"));
//		goals.add(new On("A", "B"));
		
//		//Problem 6:
//		startState.add(new OnTable("A"));
//		startState.add(new OnTable("B"));
//		startState.add(new OnTable("C"));
//		startState.add(new Clear("A"));
//		startState.add(new Clear("B"));
//		startState.add(new Clear("C"));
//		startState.add(new Handempty());
//		goals.add(new On("A", "B"));
//		goals.add(new On("B", "C"));
		
//		//Problem 7:
//		startState.add(new On("A", "B"));
//		startState.add(new On("B", "C"));
//		startState.add(new OnTable("C"));
//		startState.add(new Clear("A"));
//		startState.add(new Handempty());
//		goals.add(new On("A", "B"));
//		goals.add(new On("B", "C"));
		
//		//Problem 8:
//		startState.add(new OnTable("A"));
//		startState.add(new On("B", "C"));
//		startState.add(new OnTable("C"));
//		startState.add(new Clear("B"));
//		startState.add(new Clear("A"));
//		startState.add(new Handempty());
//		goals.add(new On("A", "B"));
//		goals.add(new On("B", "C"));
		
//		//Problem 9:
//		startState.add(new OnTable("A"));
//		startState.add(new On("C", "B"));
//		startState.add(new OnTable("B"));
//		startState.add(new Clear("C"));
//		startState.add(new Clear("A"));
//		startState.add(new Handempty());
//		goals.add(new On("A", "B"));
//		goals.add(new On("B", "C"));
		
//		//Problem 10:
//		startState.add(new OnTable("A"));
//		startState.add(new On("C", "B"));
//		startState.add(new OnTable("B"));
//		startState.add(new Clear("C"));
//		startState.add(new Clear("A"));
//		startState.add(new Handempty());
//		goals.add(new On("B", "C"));
//		goals.add(new On("A", "B"));
		
//		//Problem 11:
		//				  A
		//  C			  B
		//  A  B		  C
		// \\\\\\\		\\\\\\\
//		startState.add(new OnTable("A"));
//		startState.add(new On("C", "A"));
//		startState.add(new OnTable("B"));
//		startState.add(new Clear("C"));
//		startState.add(new Clear("B"));
//		startState.add(new Handempty());
//		goals.add(new On("A", "B"));
//		goals.add(new On("B", "C"));
		
//		//Problem 12:
		//				 A
		//  C			 B
		//  A  B		 C
		// \\\\\\\		\\\\\\
//		startState.add(new OnTable("A"));
//		startState.add(new On("C", "A"));
//		startState.add(new OnTable("B"));
//		startState.add(new Clear("C"));
//		startState.add(new Clear("B"));
//		startState.add(new Handempty());
//		goals.add(new On("B", "C"));
//		goals.add(new On("A", "B"));
		
//		//Problem 13:
		//  C			  A
		//  B			  B
		//  A			  C
		// \\\\\\		\\\\\\
//		startState.add(new On("C","B"));
//		startState.add(new On("B","A"));
//		startState.add(new OnTable("A"));
//		startState.add(new Clear("C"));
//		startState.add(new Handempty());
//		goals.add(new On("A","B"));
//		goals.add(new On("B","C"));
		
//		//Problem 14:
//		startState.add(new On("C","B"));
//		startState.add(new On("B","A"));
//		startState.add(new OnTable("A"));
//		startState.add(new Clear("C"));
//		startState.add(new Handempty());
//		goals.add(new On("B","C"));
//		goals.add(new On("A","B"));
		
		

		
		LinkedList<Rule> plan = new LinkedList<>();
//		ArrayList<int[]> goalPlans = generatePlans(goals);
		
		System.out.println("Start State:");
		for (Predicate ss : startState) {
			System.out.println(ss);
		}
		System.out.println("\nGoals:");
		for (Predicate g : goals) {
			System.out.println(g);
		}
		
		Set<String> objects = Objects(startState);
		STRIPS(startState, goals, plan, objects);
		System.out.println("\nResult--------------------");
		
		if (plan.isEmpty()) {
			System.out.println("\nNo plan.");
		}
		for(Rule a: plan) {
			System.out.println(a);
		}
		System.out.println("\nState:");
		for (Predicate p : startState) {
			System.out.println(p);
		}
	}
	
	public static Set<String> Objects(LinkedList<Predicate> state) {
		Set<String> objects = new HashSet<>();
		for(Predicate p : state) {
			if(p instanceof On) {
				String top = ((On) p).top;
				String bottom = ((On) p).bottom;
				objects.add(bottom);
				objects.add(top);
			}else if(p instanceof OnTable) {
				String block = ((OnTable) p).block;
				objects.add(block);
			}
		}
//		System.out.println("Objects: " + objects.toString());
		return objects;
	}
	
	private static boolean myEqual(int[] a, int[] b) {
		for(int n = 0; n < a.length; n++) {
    		if(b[n] != a[n]) {
    			return false;
    		}
    	}
		return true;
	}
	
	private static ArrayList<int[]> generatePlans(LinkedList<Predicate> goals){
		
		int[] num = new int[goals.size()]; 
		for(int i = 0; i < goals.size(); i++) {
			num[i] = i;
		}
		int[] a = num.clone();
		ArrayList<int[]> combs = new ArrayList<>();
		combs.add(num.clone());
		int temp;
	    for(int i=0; i < num.length-1 ; i++){
	        for(int j=i+1; j < num.length; j++){
	             temp = a[i];
	             a[i] = a[j];
	             a[j] = temp;
	            
	             int count = 0;
	            for(int m = 0; m < combs.size(); m++) {
	            	if(!myEqual(a,combs.get(m))) {
	            		count++;
	            	}
	            }
	            if(count == combs.size()){
	               combs.add(a.clone());
	            }
	             a = num.clone();  
	        }
	    }
//	    System.out.println("All possible order Combinations:");
//	    for(int[] obj : combs){
//	        System.out.println(Arrays.toString(obj));
//	    }
	
		return combs;
		
	}
		
	public static void STRIPS(LinkedList<Predicate> state, LinkedList<Predicate> goals, LinkedList<Rule> plan, Set<String> objects){
		LinkedList<Predicate> initState = (LinkedList<Predicate>) state.clone();
		
		Stack<Object> goalStack = new Stack<>();
//		int[] planIndex = goalPlans.get(planNum);
		for(int i = 0; i < goals.size(); i++) {
			goalStack.push(goals.get(i));
		}
		while(!goalStack.isEmpty()) {
			Object p = goalStack.pop();
			if ((p instanceof Predicate) && (myContains(state, ((Predicate) p)) != -1)) { 
//				System.out.println("goal " + p.toString() + " satisfied");
//				printGoalStack(goalStack);
//				printState(state);
				continue; }
			if (p instanceof Rule) {
//				System.out.println("perform action: " + p.toString());
//				System.out.println("Before----------------------------");
//				printGoalStack(goalStack);
//				printState(state);
				updateStateAndPlan((Rule) p, state, plan, goals, goalStack);
//				System.out.println("After--------------------------------");
//				printGoalStack(goalStack);
//				printState(state);
				continue;
			}
			if (p instanceof Predicate) {
//				System.out.println("Need to satisfy: " + p.toString());
//				System.out.println("Before--------------------------");
//				printGoalStack(goalStack);
//				printState(state);
				chooseAction((Predicate) p, state, goalStack, objects);
//				System.out.println("After---------------------------");
//				printGoalStack(goalStack);
//				printState(state);
				continue;
			}
		}
		
		//check if the final state satisfies all goals
		for(Predicate p : goals) {
			if(myContains(state, p) == -1) {
//				System.out.println("I faild for this try, restart and try alternate plan");
//				planNum++;
//				if(planNum >= goalPlans.size()) {
					System.out.println("I can't find a solution, could you help me?");
//					plan.clear();
//					state.clear();
//					for(Predicate p2 : initState) {
//						state.add(p2);
//					}
//					return;
//				}
//				plan.clear();
//				state.clear();
//				for(Predicate p2 : initState) {
//					state.add(p2);
//				}
//				STRIPS(state, goals, plan, objects, planNum, goalPlans);//TODO: comment this line out to ask help if one tril fails
			}
		}
		
	}

	private static int myContains(LinkedList<Predicate> state, Predicate predicate) {
		for(Predicate p : state) {
			if(p.equals(predicate)) {
				return 0;
			}
		}
		return -1;
	}

	private static void chooseAction(Predicate p, LinkedList<Predicate> state, Stack<Object> goalStack, Set<String> objects) {
		goalStack.push(p);
		//On(A,B) -> StackIt(A,B) -> Holding(A) && Clear(B)
		if(p instanceof On) {
			String bottom = ((On) p).bottom;
			String top = ((On) p).top;
			goalStack.push(new StackIt(top, bottom));
			goalStack.push(new Clear(bottom));
			goalStack.push(new Holding(top));
			
		// Holding(A) -> PickUp(A) -> Clear(A) && Handempty && OnTable(A) || UnStackIt(A,X) -> Clear(A) && Handempty && On(A,X)
		}else if(p instanceof Holding) {
			String block = ((Holding) p).block;
			//PickUp
			if(myContains(state, new OnTable(block)) != -1) {
				goalStack.push(new PickUp(block));
				goalStack.push(new Handempty());
				goalStack.push(new Clear(block));
				goalStack.push(new OnTable(block));
			//UnStack
			}else{
				for(String o : objects) {
					if(myContains(state, new On(block, o)) != -1) {
						goalStack.push(new UnStackIt(block, o));
						goalStack.push(new Handempty());
						goalStack.push(new Clear(block));
						goalStack.push(new On(block,o));
						return;
					}
				}
				System.out.println("Block " + block + " is not in hand nor on table or on hand");
			}
		
		// OnTable(A) -> PutDown(A) -> Holding(A)
		}else if(p instanceof OnTable) {
			String block = ((OnTable) p).block;
			goalStack.push(new PutDown(block));
			goalStack.push(new Holding(block));
			
		// Clear(A) -> UnStackIt(X, A) -> Handempty, Clear(X), On(X,A)
		}else if(p instanceof Clear) {
			String block = ((Clear) p).block;
			for(String x : objects) {
				if(myContains(state, new On(x, block)) != -1) {
					goalStack.push(new UnStackIt(x, block));
					goalStack.push(new Handempty());
					goalStack.push(new Clear(x));
					goalStack.push(new On(x, block));
					return;
				}
			}
			System.out.println("Nothing is on " + block + ". It should be clear");
			
		//Handempty -> PutDown(X) -> Holding(X) || StackIt(X) -> Holding(X) && Clear(Y)
		}else if(p instanceof Handempty) {//DONE: for now, we just put it on the table
			for(String x : objects) {
				if(myContains(state, new Holding(x)) != -1) {
					goalStack.push(new PutDown(x));
					goalStack.push(new Holding(x));
					return;
				}
			}
			System.out.println("Nothing is in hand. It should be hand empty");
		}
		//printGoalStack(goalStack);
	}

	private static void updateStateAndPlan(Rule p, LinkedList<Predicate> state, LinkedList<Rule> plan,
			LinkedList<Predicate> goals, Stack<Object> goalStack) {
		//DONE: since I added all pre to the stack, we should be able just perform the act
		plan.add(p);
		//Holding(A) && Clear(B) -> StackIt(A,B) -> +On(A,B) +Clear(A) + Handempty() -Clear(B) -Holding(A) 
		if(p instanceof StackIt) {
			String block = ((StackIt) p).block;
			String target = ((StackIt) p).target;
			if(myContains(state, new Holding(block)) == -1 || myContains(state, new Clear(target)) == -1){
				System.out.println(p.toString() + ": Something undo my pre");
				return;
			}
			state.add(new On(block, target));
			state.add(new Clear(block));
			state.add(new Handempty());
			myRemove(state, new Clear(target));
			myRemove(state, new Holding(block));
			
		//Clear(A) && OnTable(A) && Handempty -> PickUp(A) -> +Holding(A) -Clear(A) -OnTable(A) -Handempty
		}else if(p instanceof PickUp) {
			String block = ((PickUp) p).block;
			if(myContains(state, new Clear(block)) == -1 || myContains(state, new OnTable(block)) == -1){
				System.out.println(p.toString() + ": Something undo my pre");
				return;
			}
			state.add(new Holding(block));
			myRemove(state, new Clear(block));
			myRemove(state, new OnTable(block));
			myRemove(state, new Handempty());
			
		//Holding(A) -> PutDown(A) -> +Handempty +Clear(A) +OnTable(A) -Holding(A)
		}else if(p instanceof PutDown) {
			String block = ((PutDown) p).block;
			if(myContains(state, new Holding(block)) == -1){
				System.out.println(p.toString() + ": Something undo my pre");
				return;
			}
			state.add(new Handempty());
			state.add(new Clear(block));
			state.add(new OnTable(block));
			myRemove(state, new Holding(block));
			
		//Handempty && Clear(A) && On(A,B) -> UnStackIt(A,B) -> +Holding(A) +Clear(B) -Handempty - Clear(A) -On(A,B)
		}else if(p instanceof UnStackIt) {
			String block = ((UnStackIt) p).block;
			String target = ((UnStackIt) p).target;
			if(myContains(state, new Handempty()) == -1 || myContains(state, new Clear(block)) == -1 || myContains(state, new On(block, target)) == -1){
				System.out.println(p.toString() + ": Something undo my pre");
				return;
			}
			state.add(new Holding(block));
			state.add(new Clear(target));
			myRemove(state, new Handempty());
			myRemove(state, new Clear(block));
			myRemove(state, new On(block, target));
		}
		//printState(state);
		
	}
	
	private static int myRemove(LinkedList<Predicate> state, Predicate predicate) {
		for(Predicate p : state) {
			if(p.equals(predicate)) {
				state.remove(p);
				return 0;
			}
		}
		return -1;
	}

	private static void printGoalStack(Stack<Object> goalStack) {
		System.out.println("Goal Stack:");
		for(Object o : goalStack) {
			System.out.print(o);
			System.out.print(", ");
		}
		System.out.println();
	}
	
	private static void printState(LinkedList<Predicate> state) {
		System.out.println("State: ");
		for(Object p : state) {
			System.out.print(p);
			System.out.print(", ");
		}
		System.out.println();
	}
	
}
