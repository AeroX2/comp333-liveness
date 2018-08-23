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


    private int randomRange(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max+1);
    }

    private ArrayList<String> firstSet = new ArrayList<>(Arrays.asList(
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".split("")));
    private ArrayList<String> secondSet = new ArrayList<>(Arrays.asList(
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".split("")));
    private String newId() {
        return firstSet.remove(0) + secondSet.remove(0);
    }

    public Test1() {
        Collections.shuffle(firstSet);
        Collections.shuffle(secondSet);
    }

	@Test
	public void testReadWrite() {
		Liveness a = new Liveness();
		TreeMap<String, Integer> soln = a.generateSolution(fInName);
		a.writeSolutionToFile(soln, solnInName);
	}

	@Test
	public void testEverything() {
		int programLength = randomRange(10,70);
        int registerAmount = randomRange(1,15);

        HashMap<Integer,Set<Integer>> registerLineAssignment = new HashMap<>();
		for (int i = 0; i < programLength; i++) {
			Set<Integer> randomRegisters = new HashSet<>();
			for (int j = 0; j < randomRange(1,6); j++) randomRegisters.add(randomRange(1,registerAmount));

			Set<Integer> registers = registerLineAssignment.computeIfAbsent(i, c -> new HashSet<>());
			registers.addAll(randomRegisters);
		}

		String[] operators = {"+","-","*","/"};
		String[] translation = new String[registerAmount+1];
		for (int i = 1; i <= registerAmount; i++) translation[i] = newId();

		StringBuilder line = new StringBuilder(registerAmount);
		line.append("\n")
            .append(registerAmount)
            .append("\n")
            .append(registerLineAssignment);
		for (Set<Integer> registerSet : registerLineAssignment.values()) {
		    ArrayList<Integer> registers = new ArrayList<>(registerSet);

		    line.append(translation[registers.get(0)])
                .append(" := ");
            if (registers.size() > 1) {
                line.append(registers.stream()
                        .skip(1)
                        .map(i -> translation[i])
                        .reduce((s1, s2) -> s1 + " "+operators[randomRange(0,operators.length-1)]+" " + s2)
                        .orElse("0"));
            } else {
                line.append(new Random().ints(randomRange(1,4),1,50)
                        .mapToObj(String::valueOf)
                        .reduce((s1, s2) -> s1 + " "+operators[randomRange(0,operators.length-1)]+" " + s2)
                        .orElse("0"));
            }
            line.append("\n");
		}
		line.append("live-out");

		System.out.println(line);
	}
}
