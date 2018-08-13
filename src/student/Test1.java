package student;

import static org.junit.Assert.*;
import java.io.File;
import java.util.TreeMap;
import org.junit.Test;

public class Test1 {
	private String dataFileName = "ex2";
	private String dataDir = new File("data", dataFileName).getAbsolutePath();
	private String fInName = dataDir + ".dat";
	private String solnInName = dataDir + ".out";

	@Test
	public void testReadWrite() {
		TreeMap<String, Integer> soln;
		Liveness a = new Liveness();
		soln = a.generateSolution(fInName);
		a.writeSolutionToFile(soln, solnInName);

		//TODO Write tests
	}
}
