package student;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * Name: James Ridey
 * Student ID: 44805632
 */
public class Liveness {
    private final boolean DEBUG = true;

    static class Pair {
        int start;
        int end;

        Pair(int start, int end) {
            this.start = start;
            this.end = end;
        }

        Pair(Pair pair) {
            this.start = pair.start;
            this.end = pair.end;
        }

        private boolean intersects(Pair other) {
            return this.start <= other.end && other.start <= this.end;
        }

        @Override
        public String toString() {
            return String.format("(%d, %d)", start, end);
        }
    }

    static class Variable {
        String name;
        public List<Pair> pairs = new ArrayList<>();

        Variable(String name, int start) {
            this.name = name;
            this.pairs.add(new Pair(start, start));
        }

        Variable(Variable variable) {
            this.name = variable.name;
            this.pairs = variable.pairs.stream().map(Pair::new).collect(Collectors.toList());
        }

        @Override
        public String toString() {
            return String.format("%s=%s", name, pairs.stream().map(Pair::toString).collect(Collectors.joining(",")));
        }
    }

    private String variableRegex = "[a-zA-Z][a-zA-Z0-9]";
    private String rawStatementRegex = String.format("(?:live-\\w+)|(?:mem)|(%s) *:=|(%s)", variableRegex, variableRegex);
    public Pattern statementRegex = Pattern.compile(rawStatementRegex);

    private HashMap<String, Variable> parseLines(Stream<String> lines) {
        AtomicInteger lineNumber = new AtomicInteger();
        HashMap<String, Variable> variableMap = new HashMap<>();

        lines.forEachOrdered((s) -> {
            int count = lineNumber.incrementAndGet();

            Matcher match = statementRegex.matcher(s);
            while (match.find()) {
                boolean assignment = false;
                String variableName = match.group(2);
                if (variableName == null) {
                    assignment = true;
                    variableName = match.group(1);
                }
                if (variableName == null) continue;

                if (variableMap.containsKey(variableName)) {
                    if (assignment) variableMap.get(variableName).pairs.add(0, new Pair(count, count));
                    else variableMap.get(variableName).pairs.get(0).end = count;
                } else {
                    variableMap.put(variableName, new Variable(variableName, count));
                }
            }
        });

        return variableMap;
    }

    private TreeMap<String, Integer> variableScheduling(HashMap<String, Variable> variableMap) {
        //Find an optimal arrangement for variable layout
        //1st idea using EDF scheduling and repeating it for as long as there are valid positions
        ArrayList<Variable> variablesToBeAssigned = new ArrayList<>(variableMap.values());
        variablesToBeAssigned.sort(Comparator.comparingInt((Variable a) -> -a.pairs.get(0).end));
        println(variablesToBeAssigned);

        int registerCount = 1;
        TreeMap<String, Integer> registers = new TreeMap<>();
        while (registers.size() < variableMap.size()) {
            //Find the node with the earliest deadline
            //Assign it to the registers map
            Variable firstDeadline = variablesToBeAssigned.remove(0);
            println("First deadline: " + firstDeadline);
            println("Assigning register: " + firstDeadline.name + " value " + registerCount);
            registers.put(firstDeadline.name, registerCount);

            List<Variable> variablesTemp = new ArrayList<>(variablesToBeAssigned);
            do {
                //Remove everything that intersects with the first deadline
                Variable finalFirstDeadline = firstDeadline;
                variablesTemp.removeIf((v) -> v.pairs.stream()
                        .anyMatch((p1) -> finalFirstDeadline.pairs.stream().anyMatch(p1::intersects)));

                //Get the next earliest deadline
                if (variablesTemp.size() > 0) {
                    firstDeadline = variablesTemp.get(0);
                    println("Next deadline that doesn't intersect: " + firstDeadline);

                    variablesToBeAssigned.remove(firstDeadline);

                    println("Assigning register: " + firstDeadline.name + " value " + registerCount);
                    registers.put(firstDeadline.name, registerCount);
                }
                //Repeat until there are no nodes that fulfill the requirements.
            } while (variablesTemp.size() > 0);

            registerCount++;
        }
        println(registers);

        return registers;
    }

    public TreeMap<String, Integer> generateSolutionFromStream(Stream<String> lines) {
        HashMap<String, Variable> variableMap = parseLines(lines);
        return variableScheduling(variableMap);
    }

    public TreeMap<String, Integer> generateSolution(String fInName) {
        // PRE: fInName is a valid input file
        // POST: returns a TreeMap mapping variables (String) to registers (Integer)

        //Read every line in the file and assign each variable a line range
        HashMap<String, Variable> variableMap;
        try (Stream<String> lines = Files.lines(Paths.get(fInName))) {
            return generateSolutionFromStream(lines);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void writeSolutionToFile(TreeMap<String, Integer> tree, String solnName) {
        // PRE: t represents a valid register allocation
        // POST: the register allocation in t is written to file solnName
        println(solnName);

        long registerCount = tree.values().stream().distinct().count();

        Path path = Paths.get(solnName);
        //Use try-with-resource to get auto-closeable writer instance
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(String.valueOf(registerCount) + "\n");
            for (Map.Entry<String, Integer> entry : tree.entrySet()) {
                writer.write(String.format("%s %d\n", entry.getKey(), entry.getValue()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void println(Object o) {
        if (DEBUG) System.out.println(o);
    }

    public static void main(String[] args) {
//        String dataFileName = "ex3mod";
//        String dataDir = new File("data", dataFileName).getAbsolutePath();
//        String fInName = dataDir + ".dat";
//        String solnInName = dataDir + ".out.pro";
//
        Liveness liveness = new Liveness();
//        TreeMap<String, Integer> soln = liveness.generateSolution(fInName);
//        liveness.writeSolutionToFile(soln, solnInName);

        HashMap<String, Variable> a = new HashMap<>();
        Variable v = new Variable("a", 0);
        v.pairs = Arrays.asList(new Pair(4,7), new Pair(11,12));
        a.put("a", v);

        v = new Variable("b", 0);
        v.pairs = Arrays.asList(new Pair(8,10));
        a.put("b", v);

        v = new Variable("c", 0);
        v.pairs = Arrays.asList(new Pair(1,2), new Pair(8,10));
        a.put("c", v);

        v = new Variable("d", 0);
        v.pairs = Arrays.asList(new Pair(2,6));
        a.put("d", v);

//        for (String b : a.keySet()) {
//            Collections.reverse(a.get(b).pairs);
//            a.get(b).pairs = a.get(b).pairs.stream().map((p) -> new Pair(13 - p.end, 13 - p.start)).collect(Collectors.toList());
//        }

        TreeMap<String, Integer> kdjsa = liveness.variableScheduling(a);
        int q = 0;
    }
}
