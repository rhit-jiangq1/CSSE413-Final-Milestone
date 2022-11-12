public class Handempty implements Predicate {
	
	public String toString() {
		return "hand is empty";
	}

	@Override
	public boolean equals(Predicate p) {
		if(p instanceof Handempty) {
			return true;
		}
		return false;
	}
	
}