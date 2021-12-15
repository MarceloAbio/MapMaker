package Map;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

public class Room {
	//room coordinates
	public int roomXPos;
	public int roomYPos;
	//room dimensions
	public int roomXSize;
	public int roomYSize;
	//room number
	public int id;
	//is connected to another room or corridor, and which ones
	public boolean isConnected;
	public ArrayList<Room> connectedRooms;
	//reference to the grid
	public Square[][] roomGrid;
	public boolean visited;
	
	public boolean treasure;
	public boolean enemies;
	
	static Random random = new Random();
	
	
	public Room(int gridSize,int id){
		this.id = id;
		//4x4 is the minimum size of a room
		this.roomXSize = 4+random.nextInt(11);
		this.roomYSize = 4+random.nextInt(11);
		this.roomXPos = random.nextInt(gridSize-roomXSize);
		this.roomYPos = random.nextInt(gridSize-roomYSize);
		this.connectedRooms = new ArrayList<Room>();
	}
	
	public Room(int id){
		this.id = id;
		if(id==1){
			this.roomXSize = 5;
			this.roomYSize = 6;
			this.roomXPos = 0;
			this.roomYPos = 0;
			this.connectedRooms = new ArrayList<Room>();
		}
		
		else if(id==2){
			this.roomXSize = 5;
			this.roomYSize = 6;
			this.roomXPos = 10;
			this.roomYPos = 7;
			this.connectedRooms = new ArrayList<Room>();
		}
		else if(id==3){
			this.roomXSize = 5;
			this.roomYSize = 6;
			this.roomXPos = 6;
			this.roomYPos = 13;
			this.connectedRooms = new ArrayList<Room>();
		}
		else if(id==4){
			this.roomXSize = 4;
			this.roomYSize = 4;
			this.roomXPos = 20;
			this.roomYPos = 5;
			this.connectedRooms = new ArrayList<Room>();
		}
		else{
			this.roomXSize = 4;
			this.roomYSize = 4;
			this.roomXPos = 20;
			this.roomYPos = 12;
			this.connectedRooms = new ArrayList<Room>();
		}
		this.visited = false;
	}

	//used by corrido creation
	public Room() {
		this.id = -1;
	}


	public Room(Room room) {
		// TODO Auto-generated constructor stub
		//this.enemies;
		//this.treasure;
		this.id = room.id;
		this.isConnected = room.isConnected;;
		this.roomXPos = room.roomXPos;
		this.roomXSize = room.roomXSize;
		this.roomYPos = room.roomYPos;
		this.roomYSize = room.roomYSize;
		this.visited = room.visited;
		this.roomGrid = new Square[this.roomXSize][this.roomYSize];
		for(int i = 0; i < this.roomXSize; i++){
            for(int j = 0; j < this.roomYSize; j++){
            	
            	this.roomGrid[i][j] = new Square(room.roomGrid[i][j]);
            	this.roomGrid[i][j].room = this;
            }
		}
		
		this.connectedRooms = new ArrayList<Room>();
	}

	public void flipRoom() {
		// TODO Auto-generated method stub
		int a = this.roomXSize;
		this.roomXSize = this.roomYSize;
		this.roomYSize = a;
	}

	public void print() {
		// TODO Auto-generated method stub
		System.out.println("Room number:"+this.id+"; has "+this.connectedRooms.size()+" connections");
		if(this.connectedRooms.size()>0){
			for(int i =0;i<this.connectedRooms.size();i++){
				System.out.print(this.connectedRooms.get(i).id+" ");
			}
			System.out.println();
		}
	}

	public Square createEastDoor() {
		// TODO Auto-generated method stub
		int ran = random.nextInt(roomXSize-2);
		if(roomGrid[1+ran][this.roomYSize-1].isDoor){
			return null;
		}
		else{
			roomGrid[1+ran][this.roomYSize-1].setDoor();
			return roomGrid[1+ran][this.roomYSize-1];
		}
	}
	public Square createWestDoor() {
		// TODO Auto-generated method stub
		int ran = random.nextInt(roomXSize-2);
		if(roomGrid[ran+1][0].isDoor){
			return null;
		}
		else{
			roomGrid[ran+1][0].setDoor();
			return roomGrid[ran+1][0];
		}
	}
	public Square createNorthDoor() {
		// TODO Auto-generated method stub
		int ran = random.nextInt(roomYSize-2);
		if(roomGrid[0][ran+1].isDoor){
			return null;
		}
		else{
			roomGrid[0][ran+1].setDoor();
			return roomGrid[0][ran+1];
		}
	}
	public Square createSouthDoor() {
		// TODO Auto-generated method stub
		int ran = random.nextInt(roomYSize-2);
		if(roomGrid[this.roomXSize-1][ran+1].isDoor){
			return null;
		}
		else{
			roomGrid[this.roomXSize-1][ran+1].setDoor();
			return roomGrid[this.roomXSize-1][ran+1];
		}
	}

	public void connect(Room e) {
		// TODO Auto-generated method stub
		this.isConnected = true;
		boolean alreadyConnected = false;
		for(int i =0;i<this.connectedRooms.size();i++){
			if(this.connectedRooms.get(i).id == e.id){
				alreadyConnected  = true;
			}
		}
		if(!alreadyConnected){
			this.connectedRooms.add(e);
		}
	}
	
	 public static Comparator<Room> RoomIDComparator = new Comparator<Room>() {

			public int compare(Room r1, Room r2) {
			   Integer RoomID1 = new Integer(r1.id);
			   Integer RoomID2 = new Integer(r2.id);

			   //ascending order
			   return RoomID1.compareTo(RoomID2);

			   //descending order
			   //return StudentName2.compareTo(StudentName1);
		    }};

}
