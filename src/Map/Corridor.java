package Map;

import java.util.ArrayList;
import java.util.Comparator;

public class Corridor{
	public Room startCorridor;
	public ArrayList<Room> endsCorridor;
	public int corridorSize;
	public int corridorId;
	public ArrayList<Square> corridorSquares;

	public Corridor(Room s, int id) {
		this.startCorridor = s;
		this.endsCorridor = new ArrayList<Room>();
		this.corridorSize=0;
		this.corridorId= id;
		corridorSquares = new ArrayList<Square>();
	}
	
	public Corridor(Corridor corridor) {
		// TODO Auto-generated constructor stub
		this.corridorId = corridor.corridorId;
		this.corridorSize = corridor.corridorSize;
		this.corridorSquares = new ArrayList<Square>();
		this.endsCorridor = new ArrayList<Room>();
		this.startCorridor = null;
		
		for(int i=0;i<corridor.corridorSquares.size();i++){
			this.corridorSquares.add(new Square(corridor.corridorSquares.get(i)));
			this.corridorSquares.get(i).corridor = this;
		}
	}

	public void encounterRoom(Room e){
		if(!e.equals(this.startCorridor)){ //the corridor didn't loop
			this.endsCorridor.add(e);
			this.startCorridor.connect(e);
			e.connect(this.startCorridor);
		}
		
	}
	//the corridors share information
	//TODO unite them into one corridor
	public void encounterCorridor(Corridor c, ArrayList<Corridor> dungeonCorridors){
		if(!c.startCorridor.equals(this.startCorridor)){ //the corridor didn't loop
			this.endsCorridor.add(c.startCorridor);
			for(int i = 0;i<c.endsCorridor.size();i++){
				this.endsCorridor.add(c.endsCorridor.get(i));
			}
			c.endsCorridor.add(this.startCorridor);
			for(int i = 0;i<this.endsCorridor.size();i++){
				this.endsCorridor.get(i).connect(this.startCorridor);
				this.startCorridor.connect(this.endsCorridor.get(i));
			}
			c.corridorSize=c.corridorSize+this.corridorSize;
			c.corridorSquares.addAll(this.corridorSquares);
			dungeonCorridors.remove(this);
		}
	}

	public void print() {
		// TODO Auto-generated method stub
		System.out.print("Corridor "+this.corridorId+" of size "+this.corridorSize+" connects room "+this.startCorridor.id+" to: ");
		for(int i=0;i<this.endsCorridor.size();i++){
			System.out.print(endsCorridor.get(i).id+" ");
		}
		System.out.println();
	}
	
	public static Comparator<Corridor> CorridorIDComparator = new Comparator<Corridor>() {

		public int compare(Corridor c1, Corridor c2) {
		   Integer CorridorID1 = new Integer(c1.corridorId);
		   Integer CorridorID2 = new Integer(c2.corridorId);

		   //ascending order
		   return CorridorID1.compareTo(CorridorID2);

		   //descending order
		   //return StudentName2.compareTo(StudentName1);
	    }};

}
