package Map;

public class Square {
	//Square location in space
	private int x, y;
	 public int roomId; //0 = not a room, empty space, 1,2,... rooms, -1 corridors
	 public boolean isWall;
	 public boolean isDoor;
	 public boolean isCorridor;
	 public boolean isCorner;
	 //if it belongs to a corridor or room, there is a reference for it
	 public Corridor corridor;
	 public Room room;

	 
	 public Square(int x, int y){
	        this.x = x;
	        this.y = y;
	        this.roomId = 0;
	    }

	public Square(Square square) {
		// TODO Auto-generated constructor stub
		//this.corridor;
		this.isCorner = square.isCorner;
		this.isCorridor = square.isCorridor;
		this.isDoor = square.isDoor;
		this.isWall = square.isWall;
		//this.room;
		this.roomId = square.roomId;
		this.x = square.x;
		this.y = square.y;
	}

	public void setDoor() {
		// TODO Auto-generated method stub
		this.isDoor = true;
		this.isWall = false;
	}
	public void setCorridor() {
		// TODO Auto-generated method stub
		this.isDoor = false;
		this.isWall = false;
		this.isCorridor = true;
	}
	public void setCorner(){
		this.isWall = true;
		this.isCorner = true;
	}
	
	public void setBlank(){
		this.isCorner=false;
		this.isCorridor = false;
		this.isDoor= false;
		this.isWall = false;
		this.roomId = 0;
		this.room = null;
		this.corridor = null;
	}

	public int getX() {
		// TODO Auto-generated method stub
		return this.x;
	}

	public int getY() {
		// TODO Auto-generated method stub
		return this.y;
	}

	public void removeDoor() {
		// TODO Auto-generated method stub
		this.isDoor=false;
		this.isWall=true;
	}
}
