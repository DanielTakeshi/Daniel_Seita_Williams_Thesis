package exp.inference;

import spn.GraphSPN;
import util.Parameter;
import data.Dataset;
import data.Partition;
import exp.RunSLSPN;

public class SPNInfTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RunSLSPN.parseParameters(args);
		String prefix = "data/";
		
		// Get the data name and load the data
		Dataset d = null;
		try {
			d = (Dataset) RunSLSPN.ds[RunSLSPN.data_id].newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Loads the SPN for that data (well, depends on user input but it better be right)
		GraphSPN spn = GraphSPN.load(Parameter.filename, d);
		
		// For each instance in the testing set ...
		// ill = the upward pass of the spn, and the log likelihood for that one element
		// LL is the cumulative total, which we then divide to print at the end
		double LL = 0, LLsq = 0;
		long tic = System.currentTimeMillis();
		for(int inst=0; inst<d.getNumTesting(); inst++){
			double ill = 0;
			
			d.show(inst, Partition.Testing); // Not sure what this does ... just a numbering for the instance ???
			ill = spn.upwardPass(); // This is the key method!
			
			System.out.println(ill);
			LL += ill;
			LLsq += ill*ill;
		}
		long toc = System.currentTimeMillis();
		LL /= d.getNumTesting();
		LLsq /= d.getNumTesting();
		
		System.out.println("avg = "+LL+" +/- "+Math.sqrt(LLsq - LL*LL));
		System.out.println("Total time: "+(1.0*(toc-tic)/1000)+"s");
		// avg = -21.734815 +/- 0.363440
		// Total time: 424.504467s

	}

}
