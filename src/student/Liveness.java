package student;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Liveness {
    class Pair {

    }

    class Variable {
        String name;
        int start;
        int end;

        Variable(String name, int start, int end) {
            this.name = name;
            this.start = start;
            this.end = end;
        }

        @Override
        public String toString() {
            return String.format("%s=(%d, %d)", name, start, end);
        }
    }

    Pattern variableRegex = Pattern.compile("(live-\\w+)|(mem)|([a-zA-Z][a-zA-Z0-9])");

    public TreeMap<String, Integer> generateSolution(String fInName) {
        // PRE: fInName is a valid input file
        // POST: returns a TreeMap mapping variables (String) to registers (Integer)

        //Read every line in the file and assign each variable a line range
        HashMap<String, Variable> variableMap = new HashMap<>();
        try (Stream<String> line = Files.lines(Paths.get(fInName))) {
            AtomicInteger lineNumber = new AtomicInteger();
            line.forEachOrdered((s) -> {
                int count = lineNumber.incrementAndGet();

                Matcher match = variableRegex.matcher(s);
                while (match.find()) {
                    String variableName = match.group(3);
                    if (variableName == null) continue;

                    if (variableMap.containsKey(variableName)) {
                        variableMap.get(variableName).end = count;
                    } else {
                        variableMap.put(variableName, new Variable(variableName, count, count));
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
        variables.sort(Comparator.comparingInt((Variable a) -> a.end));
        System.out.println(variables);

        int registerCount = 1;
        TreeMap<String, Integer> registers = new TreeMap<>();
        while (registers.size() < variableMap.size()) {
            Variable firstDeadline = variables.remove(variables.size()-1);
            System.out.println("First deadline: " + firstDeadline);
            System.out.println("Assigning register: " + firstDeadline.name + " value " + registerCount);
            registers.put(firstDeadline.name, registerCount);

            while (true) {
                Variable finalFirstDeadline = firstDeadline;
                List<Variable> stream = variables.stream()
                        .filter((v) -> v != finalFirstDeadline)
                        .filter((v) -> v.end < finalFirstDeadline.start)
                        .collect(Collectors.toList());
                System.out.println("Non intersecting deadlines: "+ stream);

                if (stream.size() <= 0) break;

                firstDeadline = stream.get(stream.size()-1);
                System.out.println("Next deadline that doesn't intersect: " + firstDeadline);

                variables.remove(firstDeadline);
                System.out.println("Assigning register: " + firstDeadline.name + " value " + registerCount);
                registers.put(firstDeadline.name, registerCount);

//                Optional<Variable> first = new Optional<>(stream.get(0)); //stream.findFirst();
//
//                if (first.isPresent()) {
//                } else break;
            }

            registerCount++;
        }

        System.out.println(registers);

        return registers;
    }

    public void writeSolutionToFile(TreeMap<String, Integer> tree, String solnName) {
        // PRE: t represents a valid register allocation
        // POST: the register allocation in t is written to file solnName

        System.out.println(solnName);
    }

    public static void main(String[] args) {
    }
}
