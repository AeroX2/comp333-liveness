package student;

import static org.junit.Assert.*;
import java.io.File;
import java.util.TreeMap;
import org.junit.Test;

public class Test1 {
	private String dataFileName = "ex2";
	private String dataDir = new File("data", dataFileName).getAbsolutePath();
	private String fInName = dataDir + ".dat";
	private String solnInName = dataDir + ".out.pro";

//	@Test
//	public void testReadWrite() {
//		TreeMap<String, Integer> soln;
//		Liveness a = new Liveness();
//		soln = a.generateSolution(fInName);
//		a.writeSolutionToFile(soln, solnInName);
//	}

	//TODO: Test line range

	//TODO: Check register allocations do not overlap
}
