package student;

import static org.junit.Assert.*;
import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;

public class Test1 {
	private String dataFileName = "ex2";
	private String dataDir = new File("data", dataFileName).getAbsolutePath();
	private String fInName = dataDir + ".dat";
	private String solnInName = dataDir + ".out.pro";

	@Test
	public void testReadWrite() {
		Liveness a = new Liveness();
		TreeMap<String, Integer> soln = a.generateSolution(fInName);
		a.writeSolutionToFile(soln, solnInName);
	}

	private int randomRange(int min, int max) {
		return ThreadLocalRandom.current().nextInt(min, max+1);
	}

	private static int firstCharacter = 0;
    private static int secondCharacter = 0;
    private String firstSet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private String secondSet = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private String newId() {
        secondCharacter++;
        if (firstCharacter >= firstSet.length()) firstCharacter++;
        return String.valueOf(firstSet.charAt(firstCharacter))+
               String.valueOf(secondSet.charAt(secondCharacter));
    }

	//TODO: Test line range



	@Test
	public void testEverything() {
		int programLength = randomRange(10,70);
	    List<Integer> linesNotFilled = IntStream.range(1,programLength).boxed().collect(Collectors.toList());

	    //Create a bunch of ranges for each line in the program
        //And assign a register to it
	    int registerAmount = 0;
        HashMap<Integer,ArrayList<Liveness.Pair>> registerRanges = new HashMap<>();
	    while (linesNotFilled.size() > 0) {
	        int size = linesNotFilled.size()-1;
	        int indexStart = randomRange(0,size);
	        int randomPadding = randomRange(0,size);

            ArrayList<Liveness.Pair> pairs = registerRanges.computeIfAbsent(registerAmount, k -> new ArrayList<>());
	        if (indexStart+randomPadding > size) {
	            if (pairs.size() <= 0) {
	               randomPadding = 0;
                } else {
	                registerAmount++;
                    continue;
                }
            }

	        int rangeStart = linesNotFilled.get(indexStart);
            int rangeEnd   = linesNotFilled.get(indexStart+randomPadding);
            pairs.add(new Liveness.Pair(rangeStart, rangeEnd));

            if (randomPadding != 0) linesNotFilled.remove(indexStart+randomPadding);
	        linesNotFilled.remove(indexStart);
        }

        System.out.println(registerRanges);

        String[] lines = new String[programLength+1];
	    Arrays.fill(lines,"");

	    for (ArrayList<Liveness.Pair> pairs : registerRanges.values()) {
            ArrayList<String> variableNames = new ArrayList<>();
            variableNames.add(newId());

            for (Liveness.Pair pair : pairs) {
                String variable = variableNames.get(randomRange(0,variableNames.size()-1));
                if (Math.random() <= 0.5) {
                    variable = newId();
                    variableNames.add(variable);
                }

                lines[pair.start] += lines[pair.start].length() <= 0 ? (variable + " := ") : variable + " + ";
                lines[pair.end] += lines[pair.end].length() <= 0 ? (variable + " := ") : variable + " + ";
            }
        }

        System.out.println(registerAmount);

        lines[0] = "live-in";
        lines[lines.length-1] = "live-out";
        for (String line: lines) {
            System.out.println(line);
        }
	}
}
