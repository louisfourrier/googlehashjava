package practice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Algo {
    public ArrayList<Slice> slices;
    public Pizza pizza;

    private static final Logger LOGGER = Logger.getLogger(Algo.class.getName());

    // Algo is a class. With a Pizza attribute and an array of slices.
    public Algo(Pizza pizza) {
    	this.pizza = pizza;
    	this.slices = new ArrayList<Slice>();
    }

    // check if the result of the Algo solves the problem correctly
    public boolean checkCorrect() {

    	boolean[][] usedCell = new boolean[pizza.R][pizza.C];
    	// start all cells at false: they are in no slice
    	
    	// Special Array List configuration
    	for(Slice slice : slices) {

    		// check that cells are not already used
    		for(int r = slice.r1; r<= slice.r2; r++) {
    			for(int c = slice.c1; c<= slice.c2; c++) {
        			if(usedCell[r][c]) {
        				System.out.println(String.format("Cell already used at (%d,%d)", r, c));
        				return false;
        			} else {
        				// mark it as used
        				usedCell[r][c] = true;
        			}
        		}
    		}

    		int nMushrooms = pizza.countMushrooms(slice);
    		if(nMushrooms < pizza.L) {
    			System.out.println(String.format("Not enough mushrooms: %d/%d", nMushrooms, pizza.L));
				return false;
    		}

    		int nTomatoes = pizza.countTomatoes(slice);
    		if(nTomatoes < pizza.L) {
    			System.out.println(String.format("Not enough tomatoes: %d/%d", nTomatoes, pizza.L));
				return false;
    		}

    		int sliceSize = slice.sliceSize();
    		if(sliceSize > pizza.H) {
    			System.out.println(String.format("Slice too big: %d/%d", sliceSize, pizza.H));
				return false;
    		}
    	}

    	return true;
    }

    public int computeScore() {
    	int n = 0;
    	for(Slice slice : slices) {
    		n += slice.sliceSize();
    	}
    	return n;
    }

    public void printToFile(File file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
	 	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

		bw.write(Integer.toString(slices.size()));
		bw.newLine();
		for(Slice slice : slices) {
		    bw.write(slice.toString());
			bw.newLine();
		}
	 	bw.close();

	 	 System.out.println(String.format("Saved at: %s", file.getPath()));
	}

    public void DoAlgo0() throws IOException {
    	// corresponds to the example
    	slices = new ArrayList<Slice>(); // just making sure
	    slices.add(new Slice(0, 0, 2, 1));
	    slices.add(new Slice(0, 2, 2, 2));
	    slices.add(new Slice(0, 3, 2, 4));
    }

    public void DoAlgo1(File outFile) {

    	Random rn = new Random(23); // repeatable
    	int iterations = 5*1000*1000;
    	int currentScore = computeScore(); // could be good if we just read an existing solution

    	for(int n=0; n<iterations; n++) {

    		if(currentScore == pizza.maxScore()) {
    			System.out.println(n + " iterations, reached max score: " + currentScore);
    			break;
    		}

    		if(n%(100*1000)==0 && n > 0) {
    			try {
    				this.printToFile(outFile);
    			} catch(Exception e) {
    				LOGGER.log(Level.SEVERE, "can't write", e);
    			}
    			System.out.println(n + " iterations, current score: " + currentScore + ", saved.");
    		}

    		// consider a random slice, of size < H
    		Slice slice = Slice.randomSlice(rn, this.pizza);

    		if(slice == null) {
    			continue;
    		}

    		// count the score if we removed slices overlaping new slice
    		// and added the new random slice instead.

    		// first find overlapping slices
    		ArrayList<Slice> currentSlices = this.slices;
    		ArrayList<Slice> newSlices = new ArrayList<Slice>();
    		newSlices.add(slice);
    		for(Slice s : currentSlices) {
    			if(!s.overlap(slice)) {
    				newSlices.add(s);
    			}
    		}

    		this.slices = newSlices;
    		int newScore = this.computeScore();

    		if(newScore > currentScore || (newScore==currentScore && rn.nextInt(2)==0)) {
    			// we keep the new one
    			currentScore = newScore;
    		} else {
    			// we keep what we had before
    			this.slices = currentSlices;
    		}

    	}
    }

    public static ArrayList<Slice> readSolutionFile(File file) throws IOException {
    	FileInputStream fstream = new FileInputStream(file);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

		int numLine = -1;
		String strLine;

		ArrayList<Slice> result = new ArrayList<Slice>();
		int expectedLength = 0;

		while ((strLine = br.readLine()) != null)   {
		  if(numLine==-1) {
			  expectedLength = Integer.parseInt(strLine);
		  } else {
			  String[] ss = strLine.split(" ");
			  int r1 = Integer.parseInt(ss[0]);
			  int c1 = Integer.parseInt(ss[1]);
			  int r2 = Integer.parseInt(ss[2]);
			  int c2 = Integer.parseInt(ss[3]);
			  result.add(new Slice(r1, c1, r2, c2));
		  }
		  numLine++;
		}

		//Close the input stream
		br.close();

		if(result.size() != expectedLength) {
			System.out.println(String.format("Expected %d slices but found /%d", expectedLength, result.size()));
		}

		return result;
    }

    public static void doIt(String nameSize) throws IOException {

    	Pizza pizza = new Pizza(new File("practice_data/inputs/"+nameSize+".in"));

	    Algo algo = new Algo(pizza);

	    File outFile = new File("practice_data/outputs/manu_"+nameSize+"_2.out");

	    algo.slices = readSolutionFile(outFile);
	    
	    System.out.println(String.format("Mushroom %d", pizza.countTotalMushrooms()));
	    System.out.println(String.format("Tomatoes %d", pizza.countTotalTomatoes()));

//	    algo.DoAlgo1(outFile);
//
//	    boolean correct = algo.checkCorrect();
//	    System.out.println(String.format("Correct: %b", correct));
//	    int score = algo.computeScore();
//	    System.out.println(String.format("Score: %d/%d", score, pizza.maxScore()));
//
//	    algo.printToFile(outFile);
    }

    public static void main(String[] args) throws IOException {
    	doIt("big"); // is either "example", "small", "medium" or "big"
    }
}
