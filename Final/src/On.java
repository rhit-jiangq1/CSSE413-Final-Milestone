
public class On implements Predicate{
	String top;
	String bottom;
	
	public On(String top, String bottom) {
		this.top = top;
		this.bottom = bottom;				
	}
	
	public boolean equals(On o) {
		return top.equals(o.top) && bottom.equals(o.bottom);
	}
	
	public String toString() {
		return "on: " + top + " " + bottom;
	}

	@Override
	public boolean equals(Predicate p) {
		if(p instanceof On) {
			return this.equals((On) p);
		}
		return false;
	}

}