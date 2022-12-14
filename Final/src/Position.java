/** 
 * DO NOT MODIFY.
 * @author Michael Wollowski
 */
public class Position {
	private int row;
	private int col;
		
	public Position(int row, int col){
		this.row = row;
		this.col = col;
	}
	
	public boolean equals(Position p) {
		return row == p.row && col == p.col;
	}
		
	int getRow() {
		return row;
	}
		
	int getCol() {
		return col;
	}
		
	void setRow(int row) {
		this.row = row;
	}
		
	void setCol(int col) {
		this.col = col;
	}
}