package student;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Liveness {
    final boolean DEBUG = false;
    
    class Pair {
        int start;
        int end;

        Pair(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public boolean intersects(Pair other) {
            return (this.start >= other.start && this.start <= other.end) ||
                   (this.end >= other.start && this.end <= other.end);
        }

        @Override
        public String toString() {
            return String.format("(%d, %d)", start, end);
        }
    }

    class Variable {
        String name;
        ArrayList<Pair> pairs = new ArrayList<>();

        Variable(String name, int start) {
            this.name = name;
            this.pairs.add(new Pair(start, start));
        }

        @Override
        public String toString() {
            return String.format("%s=%s", name, pairs.stream().map(Pair::toString).collect(Collectors.joining("," )));
        }
    }

    String variableRegex = "[a-zA-Z][a-zA-Z0-9]";
    String rawStatementRegex = String.format("(?:live-\\w+)|(?:mem)|(%s) ?:=|(%s)", variableRegex, variableRegex);
    Pattern statementRegex = Pattern.compile(rawStatementRegex);

    public TreeMap<String, Integer> generateSolution(String fInName) {
        // PRE: fInName is a valid input file
        // POST: returns a TreeMap mapping variables (String) to registers (Integer)

        //Read every line in the file and assign each variable a line range
        HashMap<String, Variable> variableMap = new HashMap<>();
        try (Stream<String> line = Files.lines(Paths.get(fInName))) {
            AtomicInteger lineNumber = new AtomicInteger();
            line.forEachOrdered((s) -> {
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
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        //Find an optimal arrangement for variable layout
        //1st idea using EDF scheduling and repeating it for as long as there are valid positions
        ArrayList<Variable> variables = new ArrayList<>(variableMap.values());
        variables.sort(Comparator.comparingInt((Variable a) -> -a.pairs.get(0).end));
        println(variables);

        int registerCount = 1;
        TreeMap<String, Integer> registers = new TreeMap<>();
        while (registers.size() < variableMap.size()) {
            //Find the node with the earliest deadline
            Variable firstDeadline = variables.remove(0);
            println("First deadline: " + firstDeadline);
            println("Assigning register: " + firstDeadline.name + " value " + registerCount);
            registers.put(firstDeadline.name, registerCount);

            Optional<Variable> first;
            do {
                //Filter out every variable node, that is not the firstDeadline,
                //is less than the start of the firstDeadline
                //and intersects with the firstDeadline.
                Variable finalFirstDeadline = firstDeadline;
                Stream<Variable> stream = variables.stream()
                        .filter((v) -> v != finalFirstDeadline)
                        .filter((v) -> v.pairs.get(0).end < finalFirstDeadline.pairs.get(0).start)
                        .filter((v) -> v.pairs.stream().noneMatch((p1) -> finalFirstDeadline.pairs.stream().anyMatch(p1::intersects)));

                //Get the first node that fulfills the requirements
                //and remove it from the variables list and set it to the same register as the firstDeadline
                first = stream.findFirst();
                if (first.isPresent()) {
                    firstDeadline = first.get();
                    println("Next deadline that doesn't intersect: " + firstDeadline);

                    variables.remove(firstDeadline);
                    println("Assigning register: " + firstDeadline.name + " value " + registerCount);
                    registers.put(firstDeadline.name, registerCount);
                }
                //Repeat until there are no nodes that furfill the requirements.
            } while (first.isPresent());

            registerCount++;
        }

        println(registers);

        return registers;
    }

    public void writeSolutionToFile(TreeMap<String, Integer> tree, String solnName) {
        // PRE: t represents a valid register allocation
        // POST: the register allocation in t is written to file solnName
        println(solnName);

        long registerCount = tree.values().stream().distinct().count();

        Path path = Paths.get(solnName);
        //Use try-with-resource to get auto-closeable writer instance
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(String.valueOf(registerCount)+"\n");
            for (Map.Entry<String, Integer> entry : tree.entrySet()) {
                writer.write(String.format("%s %d\n", entry.getKey(), entry.getValue()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void println(Object o) {
        if (DEBUG) System.out.println(o);
    }

    public static void main(String[] args) {
        String dataFileName = "ex1";
        String dataDir = new File("data", dataFileName).getAbsolutePath();
        String fInName = dataDir + ".dat";
        String solnInName = dataDir + ".out.pro";
        
        Liveness liveness = new Liveness();
        TreeMap<String, Integer> soln = liveness.generateSolution(fInName);
        liveness.writeSolutionToFile(soln, solnInName);
    }
}
