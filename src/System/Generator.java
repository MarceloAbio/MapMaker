package System;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

import Map.Grid;
import Map.Room;

public class Generator {
	//print text in different colours
	/*public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_GREEN = "\u001b[32m";
    public static final String ANSI_RESET = "\u001B[0m";*/
    
	private static final double mutationRate = 0.4;
	private static final int numOfGenerations=10;
	private static final int numOfPopulation=10;
	//private static int numOfRooms = 6;
	private static Random ran;
	
	
	private static void generatePopulation(ArrayList<Grid> population, int mapSize) {
		for(int i =0;i<numOfPopulation;i++){
			Grid map = new Grid(mapSize);
			population.add(map);
			map.evaluateFitness();
		}
	}
	
	public static double sumAvaliations(ArrayList<Grid> population)
	{
		double soma = 0;
		for(int i =0;i<population.size();i++)
		{
			soma = soma+population.get(i).fitness;
		}
		return soma;
		
	}
	public static double sumAvaliationsFeature(ArrayList<Grid> population)
	{
		double soma = 0;
		for(int i =0;i<population.size();i++)
		{
			
			soma = soma+population.get(i).fitnessFeature;
		}
		return soma;
		
	}
	
	private static int roulette(ArrayList<Grid> population) {
		int i;
		double aux =0;
		Random random = new Random();
		double sum = sumAvaliations(population);
		double limite = random.nextDouble()*sum;
		for(i=0;((i<population.size()) && (aux<limite));i++)
		{
			aux = aux + population.get(i).fitness;
		}
		i--;
		return i;
	}
	private static int rouletteFeature(ArrayList<Grid> population) {
		int i;
		double aux =0;
		Random random = new Random();
		double sum = sumAvaliationsFeature(population);
		double limite = random.nextDouble()*sum;
		for(i=0;((i<population.size()) && (aux<limite));i++)
		{
			aux = aux + population.get(i).fitnessFeature;
		}
		i--;
		return i;
	}
	
	
	public static void main(String[] args){
		
		ArrayList<Grid> population = new ArrayList<Grid>();
		//menu attributes
		int mapSize =1;
		int enemiesOcurrence =1;
		int treasureOcurrence = 1;
		Scanner read = new Scanner(System.in);
		System.out.println("Wellcome to Mapmaker. Type the number of the desired option.");
		System.out.println("What the size of the dungeon?");
		System.out.println("1 - Small: 5 to 10 rooms.");
		System.out.println("2 - Medium: 10 to 15 rooms.");
		System.out.println("3 - Large: 15 to 20 rooms.");
		do{
			if(mapSize<1 || mapSize>3){
				System.out.println("Please choose an available option.");
			}
			while (!read.hasNextInt()){
				System.out.println("Please choose an available option.");
				read.next();
			}
			mapSize = read.nextInt();
		}
		while(mapSize<1 || mapSize>3);
		
		System.out.println();
		System.out.println("What is the percentage of rooms that should have enemies?");
		System.out.println("1 - 25%");
		System.out.println("2 - 50%");
		System.out.println("3 - 75%");
		do{
			if(enemiesOcurrence<1 || enemiesOcurrence>3){
				System.out.println("Please choose an available option.");
			}
			while (!read.hasNextInt()){
				System.out.println("Please choose an available option.");
				read.next();
			}
			enemiesOcurrence = read.nextInt();
		}
		while(enemiesOcurrence<1 || enemiesOcurrence>3);
		System.out.println();
		System.out.println("What is the percentage of rooms that should have treasures?");
		System.out.println("1 - 25%");
		System.out.println("2 - 50%");
		System.out.println("3 - 75%");
		do{
			if(treasureOcurrence<1 || treasureOcurrence>3){
				System.out.println("Please choose an available option.");
			}
			while (!read.hasNextInt()){
				System.out.println("Please choose an available option.");
				read.next();
			}
			treasureOcurrence = read.nextInt();
		}
		while(treasureOcurrence<1 || treasureOcurrence>3);
		System.out.println("Please wait while the maps are created.");
		
		//first stage: layout generation
		population = GALayout(population, mapSize);
		//sort maps by fitness
		Collections.sort(population,Grid.GridFitnessComparator);
		ArrayList<Grid> resultGrids = new ArrayList<Grid>();
		//takes the grids with highest fitness
		resultGrids.add(population.get(0));
		for(int i=1;i<population.size();i++){
			if((population.get(i).fitness!=population.get(i-1).fitness) && 
				(population.get(i).fitness!=0)){
				resultGrids.add(population.get(i));
			}
		}
		//second stage: features generation
		ArrayList<Grid> finalPopulation = new ArrayList<Grid>();
		for(int i=0;i<resultGrids.size();i++){
			Grid featureGrid = resultGrids.get(i);
			finalPopulation.add(GAFeatures(featureGrid,enemiesOcurrence, treasureOcurrence));
		}
		
		System.out.println(/*ANSI_RED +*/"w" /*+ ANSI_RESET*/ + " = walls");
		System.out.println(/*ANSI_BLUE+*/"/"/*+ANSI_RESET*/ + " = doors");
		System.out.println(/*ANSI_GREEN+*/"c"/*+ANSI_RESET*/ + "= corridors");
		printResult(finalPopulation);
	}
	


	private static Grid GAFeatures(Grid initialGrid, int enemiesOcurrence, int treasureOcurrence){
		
		ArrayList<Grid> descendants = new ArrayList<Grid>();
		ArrayList<Grid> population = new ArrayList<Grid>();
		ran = new Random();
		for(int i=0;i<10;i++){
			population.add(clone(initialGrid));
			
		}
		
		for(int i=0;i<population.size();i++){
			population.get(i).generateFeatures(enemiesOcurrence,treasureOcurrence);
			
		}
		
		for(int i=0;i<numOfGenerations;i++){
			
			descendants =  new ArrayList<Grid>();
			for(int j=0;j<population.size()/2;j++){
				Grid chosenGrid1;
				Grid chosenGrid2;
				int indexFather = rouletteFeature(population);
				int indexMother = rouletteFeature(population);
				
				while(indexFather == indexMother){
					indexMother = rouletteFeature(population);
				}
			
				Grid clone1 = clone(population.get(indexFather));
				Grid clone2 = clone(population.get(indexMother));
				crossoverFeature(clone1,clone2);
				
				if(clone1.fitnessFeature>population.get(indexFather).fitnessFeature){
					chosenGrid1 = clone1;
				}
				else{
					chosenGrid1 = population.get(indexFather);
				}
				descendants.add(chosenGrid1);
				if(clone2.fitnessFeature>population.get(indexMother).fitnessFeature){
					chosenGrid2 = clone2;
				}
				else{
					chosenGrid2 = population.get(indexMother);
				}
				descendants.add(chosenGrid2);
				double mutateMaybe = ran.nextDouble();
				boolean fatherOrMother = ran.nextBoolean();
				if(mutateMaybe<mutationRate){
					if(fatherOrMother){
						chosenGrid1.mutateFeature();
					}
					else{
						chosenGrid2.mutateFeature();
					}
				}
			}
			population = descendants;
		}
		Collections.sort(population, Grid.GridFitnessFeatureComparator);
		return population.get(0);
	}

	private static ArrayList<Grid> GALayout(ArrayList<Grid> population, int mapSize) {
		ArrayList<Grid> descendants = new ArrayList<Grid>();
		ran = new Random();
		generatePopulation(population,mapSize);
		//printPopulation(population);
		

		for(int i=0;i<numOfGenerations;i++){
			descendants =  new ArrayList<Grid>();
			for(int j=0;j<population.size()/2;j++){
				int indexFather = roulette(population);
				int indexMother = roulette(population);
				while(indexFather == indexMother){
					indexMother = roulette(population);
				}
				Grid chosenGrid1;
				Grid chosenGrid2;
				Grid clone1 = clone(population.get(indexFather));
				Grid clone2 = clone(population.get(indexMother));
				
				crossover(clone1,clone2);
				//select either new or previous cromossomes, whichever has higher fitness
				if(clone1.fitness>population.get(indexFather).fitness){
					chosenGrid1 = clone1;
				}
				else{
					chosenGrid1 = population.get(indexFather);
				}
				descendants.add(chosenGrid1);
				if(clone2.fitness>population.get(indexMother).fitness){
					chosenGrid2 = clone2;
				}
				else{
					chosenGrid2 = population.get(indexMother);
				}
				descendants.add(chosenGrid2);
				double mutateMaybe = ran.nextDouble();
				boolean fatherOrMother = ran.nextBoolean();
				if(mutateMaybe<mutationRate){
					if(fatherOrMother){
						chosenGrid1.mutate();
					}
					else{
						chosenGrid2.mutate();
					}
				}
			}
			population = descendants;
		}

		return population;
	}

	//create a new grid with the same content as the original
	private static Grid clone(Grid grid) {
		Grid newGrid = new Grid(grid);
		return newGrid;
	}

	private static void printPopulation(ArrayList<Grid> population) {
		for(int i=0;i<population.size();i++){
			population.get(i).printGrid();
			System.out.println(population.get(i).fitness);
		}
	}
	
	private static void printResult(ArrayList<Grid> resultGrids){
		for(int i=0;i<resultGrids.size();i++){
			resultGrids.get(i).printGrid();
			resultGrids.get(i).printFeatures();
			//System.out.println(resultGrids.get(i).fitness);
		}
	}

//exchange rooms between two grids
	private static void crossover(Grid grid1, Grid grid2) {
		int numOfTries = 3;
		//remove corridors
		grid1.purgeCorridors();
		grid2.purgeCorridors();
		int chosenRoom = 0;
		for(int i=0;i<numOfTries;i++){
			chosenRoom = ran.nextInt(Math.min(grid1.dungeonRooms.size(), grid2.dungeonRooms.size()));
			Room roomGrid1 = grid1.dungeonRooms.get(chosenRoom);
			Room roomGrid2 = grid2.dungeonRooms.get(chosenRoom);
			//System.out.println("room is "+(chosenRoom+1));
			grid1.removeRoomCrossover(roomGrid1);
			grid2.removeRoomCrossover(roomGrid2);
			if(grid1.fitRoomInGridCrossover(roomGrid2) && grid2.fitRoomInGridCrossover(roomGrid1)){ //both rooms fit on the other grid
				grid1.addRoomCrossover(roomGrid2);
				grid2.addRoomCrossover(roomGrid1);
				break;
			}
			else{
				grid1.addRoomCrossover(roomGrid1);
				grid2.addRoomCrossover(roomGrid2);
			}
		}
		Collections.sort(grid1.dungeonRooms, Room.RoomIDComparator);
		Collections.sort(grid2.dungeonRooms,Room.RoomIDComparator);
		grid1.newConnectRooms();
		grid1.evaluateFitness();
		grid2.newConnectRooms();
		grid2.evaluateFitness();
	}
	
	//exchange properties between rooms from two grids.
	private static void crossoverFeature(Grid grid1, Grid grid2) {
		int chosenRoom = 0;
		
		chosenRoom = ran.nextInt(Math.min(grid1.dungeonRooms.size(), grid2.dungeonRooms.size()));
		Room roomGrid1 = grid1.dungeonRooms.get(chosenRoom);
			Room roomGrid2 = grid2.dungeonRooms.get(chosenRoom);
			//System.out.println("room is "+(chosenRoom+1));
			boolean auxEnemies = roomGrid1.enemies;
			boolean auxTreasure = roomGrid1.treasure;
			roomGrid1.enemies= roomGrid2.enemies;
			roomGrid1.treasure = roomGrid2.treasure;
			roomGrid2.enemies = auxEnemies;
			roomGrid2.treasure = auxTreasure;
		grid1.evaluateFitnessFeature();
		grid2.evaluateFitnessFeature();
	}

}
