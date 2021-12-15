package Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

public class Grid {
	//add colors to console prints
	/*public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_GREEN = "\u001b[32m";
    public static final String ANSI_WHITE = "\u001b[37m";
    public static final String ANSI_RESET = "\u001B[0m";*/
	
    // bidimensional array of squares that hold the squares into a grid configuration, the representation of the map
	private Square[][] gridSquares;
	//List of Room elements, list the rooms of the dungeon
	public ArrayList<Room> dungeonRooms;
	//List of corridor elements, list of the corridors linking rooms
	private ArrayList<Corridor> dungeonCorridors;
	//limit size for the dungeon, the space in which the algorithm is working
	private int gridSize;
	public int numRooms;
	static Random random = new Random();
	
	public int fitness;
	public double fitnessFeature;
	
	
	//Grid initiated,the grid is created
	public Grid(int size){
		//arbitrary numbers
		switch(size){
		case 1:
			this.gridSize = 50;
			this.numRooms = 6;
			break;
		case 2:
			this.gridSize = 75;
			this.numRooms = 10;
			break;
		case 3:
			this.gridSize = 100;
			this.numRooms = 20;
			break;
		}
		//System.out.println("numRooms "+numRooms);
		gridSquares = new Square[gridSize][gridSize];
		dungeonRooms = new ArrayList<Room>();
		dungeonCorridors = new ArrayList<Corridor>();
        //Generate grid using dimensions
        for(int i = 0; i < gridSize; i++){
            for(int j = 0; j < gridSize; j++){
                gridSquares[i][j] = new Square(i,j);
            }
        }
        createMap();
       evaluateFitness();
       evaluateFitnessFeature();
    }
	//To copy the grid, 
	//the new grid have copies of the rooms and corridors, and needs to relate these rooms like they are related is the original
	public Grid(Grid grid) {
		Collections.sort(grid.dungeonRooms, Room.RoomIDComparator);
		Collections.sort(grid.dungeonCorridors, Corridor.CorridorIDComparator);
		//atributes, should be fine
		this.fitness = grid.fitness;
		this.fitnessFeature = grid.fitnessFeature;
		this.gridSize = grid.gridSize;
		this.numRooms = grid.numRooms;
		
		//inicializing the data structures
		this.gridSquares =new Square[gridSize][gridSize];
		this.dungeonCorridors = new ArrayList<Corridor>();
		this.dungeonRooms = new ArrayList<Room>();
		
		for(int i=0;i<grid.dungeonRooms.size();i++){
			this.dungeonRooms.add(new Room(grid.dungeonRooms.get(i)));
		}
		//System.out.println("Number of Rooms = "+this.dungeonRooms.size());
		for(int i=0;i<grid.dungeonRooms.size();i++){
			//System.out.println("Connecting rooms to room "+this.dungeonRooms.get(i).id);
			//System.out.println(grid.dungeonRooms.get(i).connectedRooms.size());
			
			int index =-1;
			
			
			for(int j=0;j<grid.dungeonRooms.get(i).connectedRooms.size();j++){
				//System.out.println(j);
				
				
				this.dungeonRooms.get(i).connectedRooms.add(this.dungeonRooms.get(grid.dungeonRooms.get(j).id-1));
			}
		}
		
		for(int i=0;i<grid.dungeonCorridors.size();i++){
			this.dungeonCorridors.add(new Corridor(grid.dungeonCorridors.get(i)));
			
		}
		
		for(int i=0;i<grid.dungeonCorridors.size();i++){
			int index =-1;
			for(int j=0;j<grid.dungeonRooms.size();j++){
				if(grid.dungeonCorridors.get(i).startCorridor.id == grid.dungeonRooms.get(j).id){
					index = j;
					break;
				}
			}
			if(index==-1){
				System.out.print("An error occured");
			}
			this.dungeonCorridors.get(i).startCorridor = this.dungeonRooms.get(index);
			
			index =-1;
			for(int j=0;j<grid.dungeonRooms.size();j++){
				if(grid.dungeonCorridors.get(i).endsCorridor.contains(grid.dungeonRooms.get(j))){
					index = j;
					this.dungeonCorridors.get(i).endsCorridor.add(this.dungeonRooms.get(index));
				}
			}
			
			/*for(int j=0;j<grid.dungeonCorridors.get(i).endsCorridor.size();j++){
				this.dungeonCorridors.get(i).endsCorridor.add(this.dungeonRooms.get(grid.dungeonCorridors.get(i).endsCorridor.get(j).id-1));
			}*/
		}
		
		for(int i=0;i<gridSize;i++){
			for(int j=0;j<gridSize;j++){
				this.gridSquares[i][j] = new Square(grid.gridSquares[i][j]);
				if(grid.gridSquares[i][j].corridor != null){
					if(grid.gridSquares[i][j].corridor.corridorId<this.dungeonCorridors.size())
						this.gridSquares[i][j].corridor = this.dungeonCorridors.get(grid.gridSquares[i][j].corridor.corridorId);
				}
				if(grid.gridSquares[i][j].room != null){
					int index =-1;
					for(int k=0;k<grid.dungeonRooms.size();k++){
						if(grid.gridSquares[i][j].room.id == grid.dungeonRooms.get(k).id){
							index = k;
							break;
						}
					}
					if(index==-1){
						System.out.println("An error occured");
					}
					
					
					this.gridSquares[i][j].room = this.dungeonRooms.get(index);
				}
			}
		}
	}
	public void evaluateFitness() {
		this.fitness = calculateFitness();
	}
	private int calculateFitness(){
		//piores casos: um só quarto ou quartos não conectados
		int fitness;
		if(this.dungeonRooms.size()<2){
			return 0;
		}
		for(int i=0;i<this.dungeonRooms.size();i++){
			if(this.dungeonRooms.get(i).isConnected==false){
				return 0;
			}
		}
		fitness = 1000; //numero arbitrariamente grande
		for(int k=0;k<this.dungeonRooms.size();k++){
			boolean doorFlag1=false;
			boolean doorFlag2=false;
			for(int i=0;i<this.dungeonRooms.get(k).roomXSize;i++){
				for(int j=0;j<this.dungeonRooms.get(k).roomYSize;j++){
					if(this.dungeonRooms.get(k).roomGrid[i][j].isDoor){
						if(!doorFlag1){
							doorFlag1 = true;
						}
						else{
							if(!doorFlag2){
								doorFlag2 = true;
							}
							else{
								fitness= fitness-100;
							}
						}
					}
					else{
						doorFlag1 = false;
						doorFlag2 = false;
					}
				}
			}
		}
		fitness = fitness+((this.dungeonRooms.size()-this.dungeonCorridors.size())*100);
		for(int i=0;i<this.dungeonCorridors.size();i++){
			fitness = fitness - this.dungeonCorridors.get(i).corridorSize;
		}
		return fitness;
	}
	public void printGrid(){
		System.out.println();
		for(int i = 0; i < this.gridSquares.length; i++){
            for(int j = 0; j < this.gridSquares[0].length; j++){
            	if(gridSquares[i][j].isWall){
            		System.out.print(/*ANSI_RED +*/"w" /*+ ANSI_RESET*/);
            	}
            	else if(gridSquares[i][j].isDoor){
            		System.out.print(/*ANSI_BLUE+*/"/"/*+ANSI_RESET*/);
            	}
            	else if(gridSquares[i][j].isCorridor){
            		System.out.print(/*ANSI_GREEN+*/"c"/*+ANSI_RESET*/);
            	}
            	else{
            		if(gridSquares[i][j].roomId==0){
            			System.out.print(" ");
            		}
            		else{
            			System.out.print(getCharForNumber(gridSquares[i][j].roomId));
            		}
            
            	}
            }
            System.out.println();
        }
	}
	
	private String getCharForNumber(int i) {
	    return i > 0 && i < 27 ? String.valueOf((char)(i + 64)) : null;
	}
	//create rooms and add them to the grid
	public void createRooms(){
		//int numberOfRooms = 3 + random.nextInt(7);
		for(int n=1;n<=this.numRooms;n++){
			Room r = new Room(gridSize,n);
			//if the room fits into the grid, insert it. otherwise create a new room
			if(fitRoomInGrid(r)){
				addRoom(r);
			}
			else{
				n--;
			}
			
		}
	}
	// set the perimeter of the room(inside the room boundaries) into walls
	private void markWallsInGrid(Room r) {
		//set the four corners
		gridSquares[r.roomXPos][r.roomYPos].setCorner();
		gridSquares[r.roomXPos+r.roomXSize-1][r.roomYPos].setCorner();
		gridSquares[r.roomXPos][r.roomYPos+r.roomYSize-1].setCorner();
		gridSquares[r.roomXPos+r.roomXSize-1][r.roomYPos+r.roomYSize-1].setCorner();
		//set the perimeter
		for(int i = r.roomXPos; i < r.roomXPos + r.roomXSize; i++){
			if(i>gridSize-1)
				break;
            for(int j = r.roomYPos; j < r.roomYPos+r.roomYSize; j++){
            	if(j>gridSize-1)
					break;
            	if(i==r.roomXPos || i==r.roomXPos+r.roomXSize-1 || j==r.roomYPos ||j==r.roomYPos+r.roomYSize-1)
            		gridSquares[i][j].isWall = true;
            }
		}
	}
	/*checks if the randomly generated room fits into the grid without collisions,
	 * if it doesn't fit, return false.
	*/
	public boolean fitRoomInGrid(Room r) {
		boolean fits = true;
		for(int i = r.roomXPos; i < r.roomXPos + r.roomXSize; i++){
			if(i>gridSize-1)
				break;
            for(int j = r.roomYPos; j < r.roomYPos+r.roomYSize; j++){
            	if(j>gridSize-1)
					break;
            	if(gridSquares[i][j].roomId !=0)//not blank space
            		fits = false;
            }
		}
		return fits;
	}
	
	//connect the grid's rooms with doors and corridors
	public void newConnectRooms(){
		for(int i = 0;i<dungeonRooms.size();i++){
			//connect rooms that are adjacent
			checkPerimeter(dungeonRooms.get(i));
		}
		for(int i = 1;i<dungeonRooms.size();i++){
			//DepthSearch(dungeonRooms.get(0));
			//if the first room in the list doesn't have room i connected directly or indirectly, will try to connect it.
			if(!dungeonRooms.get(0).connectedRooms.contains(dungeonRooms.get(i))){
				uniteRooms(dungeonRooms.get(i));
			}
		}
	}
	
	/*
	 *will connect the room to the first room in the list
	 *1. create corridor square next to it
	 *2. create a path of corridor squares in the direction of room 0
	 *3. If the corridor meets another room in the way, connect to it and end method
	 *4. If the corridor meets another corridor in the way, connect to it and end method*/
	private void uniteRooms(Room room) {
		Room alone = room;
		Room target = dungeonRooms.get(0);
		Corridor corridor = new Corridor(alone,dungeonCorridors.size());
		dungeonCorridors.add(corridor);
		//System.out.println("connecting room "+alone.id+" to room "+target.id);
		//programn needs to figure out which quadrant to advance trough, check the rooms coordinates and dimensions
		if(alone.roomXPos < target.roomXPos){
			if(alone.roomYPos<target.roomYPos){
				//cases when the room is bigger than the target and only need to advance either to the south or to the east
				if(alone.roomYPos+alone.roomYSize>target.roomYPos+target.roomYSize){
					Square door = alone.createSouthDoor();
					if(door!=null){
						if(gridSquares[door.getX()+1][door.getY()].isCorner){
							door.removeDoor();
						}
						else{
							door = advanceSouth(alone, target, door, corridor);
						}
					}
				}
				else if(alone.roomXPos+alone.roomXSize>target.roomXPos+target.roomXSize){
					Square door = alone.createEastDoor();
					if(door!=null){
						if(gridSquares[door.getX()][door.getY()+1].isCorner){
							door.removeDoor();
						}
						else{
							door = AdvanceEast(alone, target, door, corridor);
						}
					}
					
				}
				else{
					//advance south-east
					if(random.nextBoolean()){
						Square door = alone.createSouthDoor();
						if(door!=null){
							if(gridSquares[door.getX()+1][door.getY()].isCorner){
								door.removeDoor();
							}
							else{
								door = advanceSouth(alone, target, door, corridor);
								door = AdvanceEast(alone, target, door, corridor);
							}
						}
						
						
						//door = advanceSouth(alone, target, door, corridor);
					}
					else{
						Square door = alone.createEastDoor();
						if(door!=null){
							if(gridSquares[door.getX()][door.getY()+1].isCorner){
								door.removeDoor();
							}
							else{
								door = AdvanceEast(alone, target, door, corridor);
								door = advanceSouth(alone, target, door, corridor);
							}
						}
						
						
						
						//door = AdvanceEast(alone, target, door, corridor);
						
					}
				}
			}
			else{
				if(alone.roomXPos+alone.roomXSize>target.roomXPos+target.roomXSize){
					Square door = alone.createWestDoor();
					if(door!=null){
						if(gridSquares[door.getX()][door.getY()-1].isCorner){
							door.removeDoor();
						}
						else{
							door = advanceWest(alone, target, door, corridor);
						}
					}
					
				}
				else{
					//advance south-west
					if(random.nextBoolean()){
						Square door = alone.createSouthDoor();
						if(door!=null){
							if(gridSquares[door.getX()+1][door.getY()].isCorner){
								door.removeDoor();
							}
							else{
								door = advanceSouth(alone, target, door, corridor);
								door = advanceWest(alone, target, door, corridor);
							}
						}
						
						
						
						//door = advanceSouth(alone, target, door, corridor);
						//door = advanceWest(alone, target, door, corridor);
						
					}
					else{
						Square door = alone.createWestDoor();
						if(door!=null){
							if(gridSquares[door.getX()][door.getY()-1].isCorner){
								door.removeDoor();
							}
							else{
								door = advanceWest(alone, target, door, corridor);
								door = advanceSouth(alone, target, door, corridor);
							}
						}
						
						
						
						//door = advanceWest(alone, target, door, corridor);
						//door = advanceSouth(alone, target, door, corridor);
						
					}
				}
				
			}
		}
		else{
			if(alone.roomYPos<target.roomYPos){
				if(alone.roomYPos+alone.roomYSize>target.roomYPos+target.roomYSize){
					Square door = alone.createNorthDoor();
					if(door!=null){
						if(gridSquares[door.getX()-1][door.getY()].isCorner){
							door.removeDoor();
						}
						else{
							door = advanceNorth(alone, target, door, corridor);
						}
					}
					
					
				}
				else{
					//advance north-east
					if(random.nextBoolean()){
						Square door = alone.createNorthDoor();
						if(door!=null){
							if(gridSquares[door.getX()-1][door.getY()].isCorner){
								door.removeDoor();
							}
							else{
								door = advanceNorth(alone, target, door, corridor);
								door = AdvanceEast(alone, target, door, corridor);
							}
						}
						
						
						
						//door = advanceNorth(alone, target, door, corridor);
						//door = AdvanceEast(alone, target, door, corridor);
						
					}
					else{
						Square door = alone.createEastDoor();
						if(door!=null){
							if(gridSquares[door.getX()][door.getY()+1].isCorner){
								door.removeDoor();
							}
							else{
								door = AdvanceEast(alone, target, door, corridor);
								door = advanceNorth(alone, target, door, corridor);
							}
						}
						
						
						
						//door = AdvanceEast(alone, target, door, corridor);
						//door = advanceNorth(alone, target, door, corridor);
						
					}
				}
			}
			else{
				//advance north-west
				if(random.nextBoolean()){
					Square door = alone.createNorthDoor();
					if(door!=null){
						if(gridSquares[door.getX()-1][door.getY()].isCorner){
							door.removeDoor();
						}
						else{
							door = advanceNorth(alone, target, door, corridor);
							door = advanceWest(alone, target, door, corridor);
						}
					}
					
					
					
					//door = advanceNorth(alone, target, door, corridor);
					//door = advanceWest(alone, target, door, corridor);
					
				}
				else{
					Square door = alone.createWestDoor();
					if(door!=null){
						if(gridSquares[door.getX()][door.getY()-1].isCorner){
							door.removeDoor();
						}
						else{
							door = advanceWest(alone, target, door, corridor);
							door = advanceNorth(alone, target, door, corridor);
						}

					}
										
					
					//door = advanceWest(alone, target, door, corridor);
					//door = advanceNorth(alone, target, door, corridor);
					
				}
			}
		}
	}
	private void DepthSearch(Room room) {
		// TODO Auto-generated method stub 
		    for(int i=0;i<this.dungeonRooms.size();i++){
		    	dungeonRooms.get(i).visited = false;
		    }
	        // Create a queue for BFS 
	        LinkedList<Room> queue = new LinkedList<Room>(); 
	  
	        // Mark the current node as visited and enqueue it 
	        room.visited=true; 
	        queue.add(room); 
	  
	        while (queue.size() != 0) 
	        { 
	            // Dequeue a vertex from queue and print it 
	            room = queue.poll(); 
	            //System.out.print(room.id+" "); 
	  
	            // Get all adjacent vertices of the dequeued vertex s 
	            // If a adjacent has not been visited, then mark it 
	            // visited and enqueue it 
	            for(int i=0;i<room.connectedRooms.size();i++){
	            	Room n = room.connectedRooms.get(i);
	            	if (!n.visited) 
	                { 
	                    n.visited = true; 
	                    queue.add(n); 
	                }
	            }
	        }
	}
	
	
	/*
	 * The following methods were created to solve a problem of the corridor meeting
	 * a room's corner. It shouldn't be able to connect to the room if so.
	 * These methods see the squares that are to the side of the direction it is travelling.
	 *And see the square foward in the direction it needs to go.
	 *If it is a blank squre, the side square becomes the new part of the corridor.
	 *
	 *Example: With a corridor building to the north that is currently a the square (15,8)
	 *The next square to the north(15,7) is a corner. The method checks the two squares to the west (14,8) and east (15,8)
	 *to see if they are blank and look to the square to their north (14,7) and (16,7) to see if the corridor can proceed that way
	 *
	 *Possible bug: The corridor may be trapped if it meet adjacents rooms.
	 **/
	
	private Square lookWestByNorthAndSouth(Square s){
		Square squareNorth = gridSquares[s.getX()-1][s.getY()];
		Square squareSouth = gridSquares[s.getX()+1][s.getY()];
		if(!squareNorth.isWall){
			squareNorth.roomId= -1;
			squareNorth.isCorridor = true;
			squareNorth.corridor = s.corridor;
			s.corridor.corridorSize++;
			if(lookWest(squareNorth)){
				//TODO
				s.corridor.corridorSquares.add(squareNorth);
				return squareNorth;
			}
			else{
				squareNorth.roomId = 0;
				squareNorth.isCorridor = false;
				squareNorth.corridor=null;
				s.corridor.corridorSize--;
			}
		}
		if(!squareSouth.isWall){
			squareSouth.roomId= -1;
			squareSouth.isCorridor = true;
			squareSouth.corridor = s.corridor;
			s.corridor.corridorSize++;
			if(lookWest(squareSouth)){
				s.corridor.corridorSquares.add(squareSouth);
				return squareSouth;
			}
			else{
				squareSouth.roomId = 0;
				squareSouth.isCorridor = false;
				squareSouth.corridor=null;
				s.corridor.corridorSize--;
			}
		}
		return null;
	}
	private Square lookEastByNorthAndSouth(Square s){
		Square squareNorth = gridSquares[s.getX()-1][s.getY()];
		Square squareSouth = gridSquares[s.getX()+1][s.getY()];
		if(!squareNorth.isWall){
			squareNorth.roomId= -1;
			squareNorth.isCorridor = true;
			squareNorth.corridor = s.corridor;
			s.corridor.corridorSize++;
			if(lookEast(squareNorth)){
				s.corridor.corridorSquares.add(squareNorth);
				return squareNorth;
			}
			else{
				squareNorth.roomId = 0;
				squareNorth.isCorridor = false;
				squareNorth.corridor=null;
				s.corridor.corridorSize--;
			}
		}
		if(!squareSouth.isWall){
			squareSouth.roomId= -1;
			squareSouth.isCorridor = true;
			squareSouth.corridor = s.corridor;
			s.corridor.corridorSize++;
			if(lookEast(squareSouth)){
				s.corridor.corridorSquares.add(squareSouth);
				return squareSouth;
			}
			else{
				squareSouth.roomId = 0;
				squareSouth.isCorridor = false;
				squareSouth.corridor=null;
				s.corridor.corridorSize--;
			}
		}
		return null;
	}
	private Square lookNorthByEastAndWest(Square s){
		Square squareEast = gridSquares[s.getX()][s.getY()+1];
		Square squareWest = gridSquares[s.getX()][s.getY()-1];
		if(!squareEast.isWall){
			squareEast.roomId= -1;
			squareEast.isCorridor = true;
			squareEast.corridor = s.corridor;
			s.corridor.corridorSize++;
			if(lookNorth(squareEast)){
				s.corridor.corridorSquares.add(squareEast);
				return squareEast;
			}
			else{
				squareEast.roomId = 0;
				squareEast.isCorridor = false;
				squareEast.corridor=null;
				s.corridor.corridorSize--;
			}
		}
		if(!squareWest.isWall){
			squareWest.roomId= -1;
			squareWest.isCorridor = true;
			squareWest.corridor = s.corridor;
			s.corridor.corridorSize++;
			if(lookNorth(squareWest)){
				s.corridor.corridorSquares.add(squareWest);
				return squareWest;
			}
			else{
				squareWest.roomId = 0;
				squareWest.isCorridor = false;
				squareWest.corridor=null;
				s.corridor.corridorSize--;
			}
		}
		return null;
	}
	private Square lookSouthByEastAndWest(Square s){
		Square squareEast = gridSquares[s.getX()][s.getY()+1];
		Square squareWest = gridSquares[s.getX()][s.getY()-1];
		if(!squareEast.isWall){
			squareEast.roomId= -1;
			squareEast.isCorridor = true;
			squareEast.corridor = s.corridor;
			s.corridor.corridorSize++;
			if(lookSouth(squareEast)){
				s.corridor.corridorSquares.add(squareEast);
				return squareEast;
			}
			else{
				squareEast.roomId = 0;
				squareEast.isCorridor = false;
				squareEast.corridor=null;
				s.corridor.corridorSize--;
			}
		}
		if(!squareWest.isWall){
			squareWest.roomId= -1;
			squareWest.isCorridor = true;
			squareWest.corridor = s.corridor;
			s.corridor.corridorSize++;
			if(lookSouth(squareWest)){
				s.corridor.corridorSquares.add(squareWest);
				return squareWest;
			}
			else{
				squareWest.roomId = 0;
				squareWest.isCorridor = false;
				squareWest.corridor=null;
				s.corridor.corridorSize--;
			}
		}
		return null;
	}
	
	/*
	 * The advanceDirection methods create the corridors segments 
	 * one square per iteration, they see the perimeter to see if it didn't bump into
	 * anything, if they bump into a room they stop the process and 
	 * connect to that room, if they bump into another corridor they cease the process
	 * and connect to that other corridor. */
	private Square advanceWest(Room alone, Room target, Square s, Corridor corridor) {
		// TODO Auto-generated method stub
		boolean isOver = false;
		Square newS = s;
		if(s == null){
			return null;
		}
		//System.out.println("Start advacing west in coordinates ("+newS.getX()+","+newS.getY());
				while(!isOver){
					if(newS.getY()-1>0){
						newS  = gridSquares[newS.getX()][newS.getY()-1];
						//System.out.println("moved now to ("+newS.getX()+","+newS.getY()+")");
						if(newS.roomId == 0){
							newS.roomId= -1;
							newS.isCorridor = true;
							newS.corridor = corridor;
							newS.corridor.corridorSize++;
							corridor.corridorSquares.add(newS);
							if(gridSquares[newS.getX()][newS.getY()-1].isCorner){
								newS = lookWestByNorthAndSouth(newS);
								isOver=true;
								return newS;
							}
							if(lookNorth(newS) ||lookWest(newS) || lookWest(newS)){
								//System.out.println("found something in my way");
								isOver = true;
								return null;
								
							}
							else if(newS.getY()<target.roomYPos+target.roomYSize-1){
								//System.out.println("will advance by another way");
								isOver = true;
								return newS;
							}
						}
						//TODO
					}
					else{
						isOver=true;
						return null;
					}
				}
		return newS;
	}
	private Square advanceNorth(Room alone, Room target, Square s, Corridor corridor) {
		// TODO Auto-generated method stub
		boolean isOver = false;
		Square newS = s;
		if(s == null){
			return null;
		}
		//System.out.println("Start advacing north in coordinates ("+newS.getX()+","+newS.getY());
		while(!isOver){
			if(newS.getX()-1>0){
				newS  = gridSquares[newS.getX()-1][newS.getY()];
				//System.out.println("moved now to ("+newS.getX()+","+newS.getY()+")");
				if(newS.roomId == 0){
					newS.roomId= -1;
					newS.isCorridor = true;
					newS.corridor = corridor;
					newS.corridor.corridorSize++;
					corridor.corridorSquares.add(newS);
					if(gridSquares[newS.getX()-1][newS.getY()].isCorner){
						newS = lookNorthByEastAndWest(newS);
						isOver=true;
						return newS;
					}
					if(lookNorth(newS) ||lookEast(newS) || lookWest(newS)){
						isOver = true;
						return null;
					}
					else if(newS.getX()<target.roomXPos+target.roomXSize-1){
						isOver = true;
						return newS;
					}
				}
			}
			else{
				isOver=true;
				return null;
			}
		}
		return newS;
	}
	private Square AdvanceEast(Room alone, Room target, Square s, Corridor corridor) {
		// TODO Auto-generated method stub
		boolean isOver = false;
		Square newS = s;
		if(s == null){
			return null;
		}
		//System.out.println("Start advacing east in coordinates ("+newS.getX()+","+newS.getY());
		while(!isOver){
			if(newS.getY()+1<gridSize-1){
				newS  = gridSquares[newS.getX()][newS.getY()+1];
				//System.out.println("moved now to ("+newS.getX()+","+newS.getY()+")");
				if(newS.roomId == 0){
					newS.roomId= -1;
					newS.isCorridor = true;
					newS.corridor = corridor;
					newS.corridor.corridorSize++;
					corridor.corridorSquares.add(newS);
					if(gridSquares[newS.getX()][newS.getY()+1].isCorner){
						newS = lookEastByNorthAndSouth(newS);
						isOver=true;
						return newS;
					}
					if(lookNorth(newS) ||lookEast(newS) || lookSouth(newS)){
						isOver = true;
						return null;
					}
					else if(newS.getY()>target.roomYPos){
						isOver = true;
						return newS;
					}
				}
			}
			else{
				isOver=true;
				return null;
			}
		}
		return newS;
	}
	private Square advanceSouth(Room alone, Room target, Square s, Corridor corridor) {
		// TODO Auto-generated method stub
		boolean isOver = false;
		Square newS = s;
		if(s == null){
			return null;
		}
		//System.out.println("Start advacing south in coordinates ("+newS.getX()+","+newS.getY());
		while(!isOver){
			if(newS.getX()+1<gridSize-1){
				newS  = gridSquares[newS.getX()+1][newS.getY()];
				//System.out.println("moved now to ("+newS.getX()+","+newS.getY()+")");
				if(newS.roomId == 0){
					newS.roomId= -1;
					newS.isCorridor = true;
					newS.corridor = corridor;
					newS.corridor.corridorSize++;
					corridor.corridorSquares.add(newS);
					if(gridSquares[newS.getX()+1][newS.getY()].isCorner){
						newS = lookSouthByEastAndWest(newS);
						isOver=true;
						return newS;
					}
					if(lookEast(newS) ||lookWest(newS) || lookSouth(newS)){
						isOver = true;
						return null;
					}
					else if(newS.getX()>target.roomXPos){
						isOver = true;
						return newS;
					}
				}
			}
			else{
				isOver=true;
				return null;
			}
		}
		return newS;
	}
	/*
	 * The look methods check if the adjacent square is eligible to connect the corridor
	 * to a room or another corridor
	 */
	private boolean lookEast(Square s) {
		// TODO Auto-generated method stub
		if(gridSquares[s.getX()][s.getY()+1].roomId!=0){
			if(gridSquares[s.getX()][s.getY()+1].roomId==-1){
				s.corridor.encounterCorridor(gridSquares[s.getX()][s.getY()+1].corridor, dungeonCorridors);
			}
			else{
				if(s.corridor.startCorridor!=gridSquares[s.getX()][s.getY()+1].room && !gridSquares[s.getX()][s.getY()+1].isCorner){
					s.corridor.encounterRoom(gridSquares[s.getX()][s.getY()+1].room);
					gridSquares[s.getX()][s.getY()+1].setDoor();
				}
				else{
					return false;
				}
			}
			return true;
		}
		return false;
		
	}
	private boolean lookWest(Square s) {
		// TODO Auto-generated method stub
		if(gridSquares[s.getX()][s.getY()-1].roomId!=0){
			if(gridSquares[s.getX()][s.getY()-1].roomId==-1){
				s.corridor.encounterCorridor(gridSquares[s.getX()][s.getY()-1].corridor,  dungeonCorridors);
			}
			else{
				if(s.corridor.startCorridor!=gridSquares[s.getX()][s.getY()-1].room && !gridSquares[s.getX()][s.getY()-1].isCorner){
					s.corridor.encounterRoom(gridSquares[s.getX()][s.getY()-1].room);
					gridSquares[s.getX()][s.getY()-1].setDoor();
				}
				else{
					return false;
				}
			}
			return true;
		}
		return false;
	}
	private boolean lookSouth(Square s) {
		// TODO Auto-generated method stub
		if(gridSquares[s.getX()+1][s.getY()].roomId!=0){
			if(gridSquares[s.getX()+1][s.getY()].roomId==-1){
				s.corridor.encounterCorridor(gridSquares[s.getX()+1][s.getY()].corridor, dungeonCorridors);
			}
			else{
				if(s.corridor.startCorridor!=gridSquares[s.getX()+1][s.getY()].room && !gridSquares[s.getX()+1][s.getY()].isCorner){
					s.corridor.encounterRoom(gridSquares[s.getX()+1][s.getY()].room);
					gridSquares[s.getX()+1][s.getY()].setDoor();
				}
				else{
					return false;
				}
			}
			return true;
		}
		return false;
	}
	private boolean lookNorth(Square s) {
		// TODO Auto-generated method stub
		if(gridSquares[s.getX()-1][s.getY()].roomId!=0){
			if(gridSquares[s.getX()-1][s.getY()].roomId==-1){
				s.corridor.encounterCorridor(gridSquares[s.getX()-1][s.getY()].corridor, dungeonCorridors);
			}
			else{
				if(s.corridor.startCorridor!=gridSquares[s.getX()-1][s.getY()].room && !gridSquares[s.getX()-1][s.getY()].isCorner){
					s.corridor.encounterRoom(gridSquares[s.getX()-1][s.getY()].room);
					gridSquares[s.getX()-1][s.getY()].setDoor();
				}
				else{
					return false;
				}
			}
			return true;
		}
		return false;
	}
	private void checkPerimeter(Room room) {
		//System.out.println("the south of room number "+room.id+" is beign checked");
		checkSouthPerimeter(room);
		//System.out.println("the north of room number "+room.id+" is beign checked");
		checkNorthPerimeter(room);
		//System.out.println("the west of room number "+room.id+" is beign checked");
		checkWestPerimeter(room);
		//System.out.println("the east of room number "+room.id+" is beign checked");
		checkEastPerimeter(room);
		
	}
	private void checkSouthPerimeter(Room r) {
		// TODO Auto-generated method stub
		if(r.roomXPos+r.roomXSize<gridSize){
			for(int i = 0; i < r.roomYSize; i++){
				if(gridSquares[r.roomXPos+r.roomXSize][r.roomYPos+i].roomId!=0){
					
					if(!gridSquares[r.roomXPos+r.roomXSize][r.roomYPos+i].isCorner && !gridSquares[r.roomXPos+r.roomXSize-1][r.roomYPos+i].isCorner){
						Room connectedRoom = findRoombyId(gridSquares[r.roomXPos+r.roomXSize][r.roomYPos+i].roomId);
						gridSquares[r.roomXPos+r.roomXSize][r.roomYPos+i].setDoor();
						gridSquares[r.roomXPos+r.roomXSize-1][r.roomYPos+i].setDoor();
						boolean alreadyConnected = false;
						for(int n=0;n<r.connectedRooms.size();n++){
							if(r.connectedRooms.get(n).id==connectedRoom.id){
								alreadyConnected = true;
								break;
							}
						}
						if(!alreadyConnected){
							r.connectedRooms.add(connectedRoom);
							r.isConnected = true;
						}
					}
				}
			}
		}
	}
	private void checkNorthPerimeter(Room r) {
		// TODO Auto-generated method stub
		if(r.roomXPos>0){
			for(int i = 0; i < r.roomYSize; i++){
				if(gridSquares[r.roomXPos-1][r.roomYPos+i].roomId!=0){
					if(!gridSquares[r.roomXPos-1][r.roomYPos+i].isCorner && !gridSquares[r.roomXPos][r.roomYPos+i].isCorner){
						Room connectedRoom = findRoombyId(gridSquares[r.roomXPos-1][r.roomYPos+i].roomId);
						gridSquares[r.roomXPos-1][r.roomYPos+i].setDoor();
						gridSquares[r.roomXPos][r.roomYPos+i].setDoor();
						boolean alreadyConnected = false;
						for(int n=0;n<r.connectedRooms.size();n++){
							if(r.connectedRooms.get(n).id==connectedRoom.id){
								alreadyConnected = true;
								break;
							}
						}
						if(!alreadyConnected){
							r.connectedRooms.add(connectedRoom);
							r.isConnected = true;
						}
					}
				}
			}
		}
	}
	private void checkWestPerimeter(Room r) {
		// TODO Auto-generated method stub
		if(r.roomYPos>0){
			for(int i = 0; i < r.roomXSize; i++){
				if(gridSquares[r.roomXPos+i][r.roomYPos-1].roomId!=0){
					if(!gridSquares[r.roomXPos+i][r.roomYPos-1].isCorner && !gridSquares[r.roomXPos+i][r.roomYPos].isCorner){
						Room connectedRoom = findRoombyId(gridSquares[r.roomXPos+i][r.roomYPos-1].roomId);
						gridSquares[r.roomXPos+i][r.roomYPos-1].setDoor();
						gridSquares[r.roomXPos+i][r.roomYPos].setDoor();
						boolean alreadyConnected = false;
						for(int n=0;n<r.connectedRooms.size();n++){
							if(r.connectedRooms.get(n).id==connectedRoom.id){
								alreadyConnected = true;
								break;
							}
						}
						if(!alreadyConnected){
							r.connectedRooms.add(connectedRoom);
							r.isConnected = true;
						}
					}
				}
			}
		}
	}
	private void checkEastPerimeter(Room r) {
		// TODO Auto-generated method stub
		if(r.roomYPos+r.roomYSize<gridSize){
			for(int i = 0; i < r.roomXSize; i++){
				if(gridSquares[r.roomXPos+i][r.roomYPos+r.roomYSize].roomId!=0){
					if(!gridSquares[r.roomXPos+i][r.roomYPos+r.roomYSize].isCorner && !gridSquares[r.roomXPos+i][r.roomYPos+r.roomYSize-1].isCorner){
						Room connectedRoom = findRoombyId(gridSquares[r.roomXPos+i][r.roomYPos+r.roomYSize].roomId);
						gridSquares[r.roomXPos+i][r.roomYPos+r.roomYSize].setDoor();
						gridSquares[r.roomXPos+i][r.roomYPos+r.roomYSize-1].setDoor();
						boolean alreadyConnected = false;
						for(int n=0;n<r.connectedRooms.size();n++){
							if(r.connectedRooms.get(n).id==connectedRoom.id){
								alreadyConnected = true;
								break;
							}
						}
						if(!alreadyConnected){
							r.connectedRooms.add(connectedRoom);
							r.isConnected = true;
						}
					}
				}
			}
		}
	}
	private Room findRoombyId(int roomId) {
		for(int i=0;i<dungeonRooms.size();i++){
			if(dungeonRooms.get(i).id==roomId){
				return dungeonRooms.get(i);
			}
		}
		return null;
	}
	
	public void printRooms(){
		for(int i =0;i<dungeonRooms.size();i++){
			dungeonRooms.get(i).print();
		}
	}
	
	public void printCorridors(){
		for(int i=0;i<dungeonCorridors.size();i++){
			dungeonCorridors.get(i).print();
		}
	}
	public void createMap(){
		//Grid map = new Grid();
		//map.numRooms=9;
		this.createRooms();
		//map.exampleGrid();
		//map.printGrid();
		//map.connectRooms();
		this.newConnectRooms();
		//map.printRooms();
		//map.printCorridors();
		//map.printGrid();
	}
	private void exampleGrid() {
		// TODO Auto-generated method stub
		for(int n=1;n<6;n++){
			Room r = new Room(n);
			if(fitRoomInGrid(r)){
				dungeonRooms.add(r);
				markWallsInGrid(r);
				
				Square[][] roomGrid = new Square[r.roomXSize][r.roomYSize];
				for(int i = r.roomXPos; i < r.roomXPos + r.roomXSize; i++){
					if(i>gridSize-1){
						break;
					}
		            for(int j = r.roomYPos; j < r.roomYPos+r.roomYSize; j++){
		            	if(j>gridSize-1){
							break;
						}
		            	gridSquares[i][j].roomId = r.id;
		            	gridSquares[i][j].room = r;
		            	roomGrid[i-r.roomXPos][j-r.roomYPos] = gridSquares[i][j];
		            }
				}
				r.roomGrid = roomGrid;
			}
	}
	}
	//remove the corridors from the map, leaving only the rooms
	public void purgeCorridors() {
		// TODO Auto-generated method stub
		for(int i = 0; i < this.gridSquares.length; i++){
            for(int j = 0; j < this.gridSquares[0].length; j++){
            	if(gridSquares[i][j].isDoor){
            		gridSquares[i][j].removeDoor();
            	}
            	else if(gridSquares[i][j].isCorridor){
            		gridSquares[i][j].setBlank();
            	}
            }
         }
		this.dungeonCorridors = new ArrayList<Corridor>();
        for(int i =0;i<this.dungeonRooms.size();i++){
        	this.dungeonRooms.get(i).connectedRooms = new ArrayList<Room>();
       		this.dungeonRooms.get(i).isConnected = false;
         }
	}
	//change a feature from a room
	public void mutateFeature(){
		Room room = dungeonRooms.get(random.nextInt(dungeonRooms.size()));
		if(random.nextBoolean()){
			if(room.enemies){
				room.enemies = false;
			}
			else{
				room.enemies = true;
			}
		}
		else{
			if(room.treasure){
				room.treasure = false;
			}
			else{
				room.treasure=true;
			}
		}
		evaluateFitnessFeature();
	}
	public void mutate() {
		// TODO Auto-generated method stub
		this.purgeCorridors();
		//if true, add a new room, if false,remove a room
		int mutationCase = random.nextInt(4);
		switch(mutationCase){
		case 0: //add room
			//System.out.println("A room was added");
			this.numRooms++;
			//System.out.println("numRooms "+numRooms);
			boolean itDoesntFit = true;
			while(itDoesntFit){
			Room r = new Room(gridSize,this.numRooms);
			//if the room fits in the grid, it insert it. otherwise it creates a new room
			if(fitRoomInGrid(r)){
				addRoom(r);
				itDoesntFit = false;
			}
			else{
				itDoesntFit = true;
			}
			}
			break;
		case 1://remove room
			int i = random.nextInt(this.dungeonRooms.size());
			//System.out.println("This room was removed:"+(i+1));
			Room r = this.dungeonRooms.get(i);
			removeRoom(r);
			break;
		case 2: //remove a room and add another
			int e = random.nextInt(this.dungeonRooms.size());
			//System.out.println("This room was removed:"+(e+1));
			Room room = this.dungeonRooms.get(e);
			removeRoom(room);
			//System.out.println("A room was added");
			this.numRooms++;
			//System.out.println("numRooms "+numRooms);
			boolean itDoesnotFit = true;
			while(itDoesnotFit){
			room = new Room(gridSize,this.numRooms);
			//if the room fits in the grid, it insert it. otherwise it creates a new room
			if(fitRoomInGrid(room)){
				addRoom(room);
				itDoesnotFit = false;
			}
			else{
				itDoesnotFit = true;
			}
			}
			break;
		case 3: //just redo corridors;
			//System.out.println("corridors rearanged");
			break;
		}
			this.newConnectRooms();
			evaluateFitness();
	}
	public void removeRoom(Room r) {
		// TODO Auto-generated method stub
		for(int i = r.roomXPos; i < r.roomXPos + r.roomXSize; i++){
            for(int j = r.roomYPos; j < r.roomYPos+r.roomYSize; j++){
            	gridSquares[i][j].setBlank();
            }
		}
		
		for(int e= r.id;e<dungeonRooms.size();e++){
			Room c = dungeonRooms.get(e);
			c.id--;
			for(int i = c.roomXPos; i < c.roomXPos + c.roomXSize; i++){
				if(i>gridSize-1)
					break;
	            for(int j = c.roomYPos; j < c.roomYPos+c.roomYSize; j++){
	            	if(j>gridSize-1)
						break;
	            	gridSquares[i][j].roomId = c.id;
	            }
			}
		}
		this.dungeonRooms.remove(r);
		this.numRooms--;
		//System.out.println("numRooms "+numRooms);
	}
	
	public void addRoom(Room r){
		dungeonRooms.add(r);
		markWallsInGrid(r);
		Square[][] roomGrid = new Square[r.roomXSize][r.roomYSize];
		for(int i = r.roomXPos; i < r.roomXPos + r.roomXSize; i++){
			if(i>gridSize-1)
				break;
            for(int j = r.roomYPos; j < r.roomYPos+r.roomYSize; j++){
            	if(j>gridSize-1)
					break;
            	gridSquares[i][j].roomId = r.id;
            	gridSquares[i][j].room = r;
            	roomGrid[i-r.roomXPos][j-r.roomYPos] = gridSquares[i][j];
            }
		}
		r.roomGrid = roomGrid;
	}
	
	public void addRoomCrossover(Room r){
		addRoom(r);
		this.numRooms++;
		//System.out.println("numRooms "+numRooms);
	}
	

	public boolean fitRoomInGridCrossover(Room r) {
		boolean fits = true;
		for(int i = r.roomXPos; i < r.roomXPos + r.roomXSize; i++){
			if(i>gridSize-1)
				break;
            for(int j = r.roomYPos; j < r.roomYPos+r.roomYSize; j++){
            	if(j>gridSize-1)
					break;
            	if(gridSquares[i][j].roomId !=0)
            		fits = false;
            }
		}
		return fits;
		}
	public void removeRoomCrossover(Room r) {
		// TODO Auto-generated method stub
		for(int i = r.roomXPos; i < r.roomXPos + r.roomXSize; i++){
            for(int j = r.roomYPos; j < r.roomYPos+r.roomYSize; j++){
            	gridSquares[i][j].setBlank();
            }
		}
		this.dungeonRooms.remove(r);
		this.numRooms--;
		//System.out.println("numRooms "+numRooms);
	}
	
	public static Comparator<Grid> GridFitnessComparator = new Comparator<Grid>() {

		public int compare(Grid g1, Grid g2) {
		   Integer GridFit1 = new Integer(g1.fitness);
		   Integer GridFit2 = new Integer(g2.fitness);

		   //ascending order
		   //return GridFit1.compareTo(GridFit2);

		   //descending order
		   return GridFit2.compareTo(GridFit1);
	    }};
	    
	    public static Comparator<Grid> GridFitnessFeatureComparator = new Comparator<Grid>() {

			public int compare(Grid g1, Grid g2) {
			   Double GridFit1 = new Double(g1.fitnessFeature);
			   Double GridFit2 = new Double(g2.fitnessFeature);

			   //ascending order
			   //return GridFit1.compareTo(GridFit2);

			   //descending order
			   return GridFit2.compareTo(GridFit1);
		    }};


	public void generateFeatures(int enemiesOcurrence, int treasureOcurrence) {
		// TODO Auto-generated method stub
		Random ran = new Random();
		double enemiesChance = (double) (0.25*enemiesOcurrence);
		double treasureChance = (double) (0.25*treasureOcurrence);
		for(int i=0;i<this.dungeonRooms.size();i++){
			boolean ranBool = getBooleanWithChance(enemiesChance);
			dungeonRooms.get(i).enemies = ranBool;
			ranBool = getBooleanWithChance(treasureChance);
			dungeonRooms.get(i).treasure = ranBool;
		}
		evaluateFitnessFeature();
	}
	
	boolean getBooleanWithChance(double probability){
		return random.nextFloat()<probability;
	}

	public void evaluateFitnessFeature(){
		int emptyRooms =0;
		float numTreasure=0;
		float numEnemies=0;
		for(int i=0;i<this.dungeonRooms.size();i++){
			if(dungeonRooms.get(i).enemies){
				numEnemies++;
			}
			if(dungeonRooms.get(i).treasure){
				numTreasure++;
			}
			if(dungeonRooms.get(i).enemies == false && dungeonRooms.get(i).treasure == false){
				emptyRooms++;
			}
		}
		this.fitnessFeature = 100;
		if(numEnemies!=0 && numTreasure!=0){
			if(emptyRooms>(0.2*this.dungeonRooms.size())){
				fitnessFeature = fitnessFeature - 30;
			}

		}
		else{
			this.fitnessFeature=0;
		}
		
	}
	public void printFeatures() {
		// TODO Auto-generated method stub
		for(int i=0;i<dungeonRooms.size();i++){
			Room r = dungeonRooms.get(i);
			System.out.print("ROOM "+getCharForNumber(r.id) +" - ");
			if(r.enemies){
				System.out.print("Has enemies");
				if(r.treasure){
					System.out.print(" and Treasure");
				}
			}
			else if(r.treasure){
				System.out.print("Has treasure");
			}
			else{
				System.out.print("Is empty.");
			}
			System.out.println();
		}
	}
}