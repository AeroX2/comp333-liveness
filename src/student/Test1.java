package student;

import static org.junit.Assert.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.Test;

public class Test1 {
	private String dataFileName = "ex2";
	private String dataDir = new File("data", dataFileName).getAbsolutePath();
	private String fInName = dataDir + ".dat";
	private String solnInName = dataDir + ".out.pro";

	private Liveness liveness = new Liveness();

    private int randomRange(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max+1);
    }

    private ArrayList<String> firstSet;
    private ArrayList<String> secondSet;
    private String newId() {
        return firstSet.remove(0) + secondSet.remove(0);
    }

    private void reset() {
        firstSet = new ArrayList<>(Arrays.asList(
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".split("")));
        secondSet = new ArrayList<>(Arrays.asList(
                "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".split("")));
        Collections.shuffle(firstSet);
        Collections.shuffle(secondSet);
    }

    public Test1() {
        reset();
    }

	@Test
	public void testReadWrite() {
		Liveness a = new Liveness();
		TreeMap<String, Integer> soln = a.generateSolution(fInName);
		a.writeSolutionToFile(soln, solnInName);
	}

	@Test
    public void testParsing() {
        String lines = "live-in a1\n" +
                "hJ := t0 + K7\n" + //Normal test
                "Yy := mem[jH + fG + 7Y]\n" + //Test mem with padding
                "op := mem[zz+oo+u9]\n" + //Test mem without padding
                "ww := 1+1+1+1+1+1\n" + //Test variables without other
                "H8 := 1+1+1+1+1+jI+iJ\n" + //Test variables inside operations
                "H8 := 1 + jk + ui\n" + //Test variables inside operations with padding
                "a3 := 12\n" + //Test repeats
                "a3 := 13\n" +
                "a3 := 28\n" +
                "a3 := 36\n" +
                "X7 := me*yu-gz\n" + //Test more repeats
                "X7 := me*yu-gz\n" +
                "X7 := me*yu-gz\n" +
                "X7 := me*yu-gz\n" +
                "X7 := me*yu-gz\n" +
                "live-out a1";
        String[] expected = new String[]{"H8", "K7", "X7", "Yy", "a1", "a3",
                                         "fG", "gz", "hJ", "iJ", "jH", "jI",
                                         "jk", "me", "oo", "op", "t0", "u9",
                                         "ui", "ww", "yu", "zz"};
        TreeMap<String, Integer> results = liveness.generateSolutionFromStream(Arrays.stream(lines.split("\n")));
        assertArrayEquals(expected, results.keySet().stream().sorted().toArray());
    }

    @Test
    public void testEmptyProgram() {
        String lines = "live-in a1\n" +
                       "live-out a1";
        TreeMap<String, Integer> results = liveness.generateSolutionFromStream(Arrays.stream(lines.split("\n")));
        assertTrue(results.size() > 0);
        assertEquals(Integer.valueOf(1), results.get("a1"));
    }

    @Test
    public void testOneLineProgram() {
        String lines = "live-in\n" +
                       "a1 := 24\n" +
                       "live-out";
        TreeMap<String, Integer> results = liveness.generateSolutionFromStream(Arrays.stream(lines.split("\n")));
        assertTrue(results.size() > 0);
        assertEquals(Integer.valueOf(1), results.get("a1"));
    }

	@Test
	public void testRandomProgram() throws IOException {
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

		ArrayList<String> lines = new ArrayList<>();
		lines.add(String.valueOf(registerAmount));
        lines.add(registerLineAssignment.toString());

		for (Set<Integer> registerSet : registerLineAssignment.values()) {
		    ArrayList<Integer> registers = new ArrayList<>(registerSet);

		    StringBuilder line = new StringBuilder();
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

            lines.add(line.toString());
		}
		lines.add("live-out");

        TreeMap<String, Integer> result = liveness.generateSolutionFromStream(lines.stream());
        assertTrue(result.values().stream().distinct().count() <= registerAmount);
	}

	@Test
    public void testRandomProgramMultiple() {
        for (int i = 0; i < 100; i++) {
            try {
                reset();
                testRandomProgram();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
