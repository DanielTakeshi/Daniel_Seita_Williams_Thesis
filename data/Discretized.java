package data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Discretized extends SparseDataset{
	// Comma-separated
	private String delim = ",";
	String name;

	public static class Abalone extends Discretized{
		public Abalone() {super("abalone", 31, 3134, 417, 626);}
	}
	public static class S20NG extends Discretized{
		public S20NG() {super("small20ng", 930, 11293, 3764, 3764);}
	}
	public static class Adult extends Discretized{
		public Adult() {super("adult", 125, 36631, 4884, 7327);}
	}
	public static class Audio extends Discretized{
		public Audio() {super("baudio", 100, 15000, 2000, 3000);}
	}
	public static class Book extends Discretized{
		public Book() {super("book", 500, 8700, 1159, 1739);}
	}
	public static class Covertype extends Discretized{
		public Covertype() {super("cov", 84, 30000, 4000, 6000);}
	}
	public static class EachMovie extends Discretized{
		public EachMovie() {super("tmovie", 500, 4524, 1002, 591);}
	}
	public static class Jester extends Discretized{
		public Jester() {super("jester", 100, 9000, 1000, 4116);}
	}
	public static class KDD extends Discretized{
		public KDD() {super("kdd", 64, 180092, 19907, 34955);}
	}
	public static class MSNBC extends Discretized{
		public MSNBC() {super("msnbc", 17, 291326, 38843, 58265);}
	}
	public static class MSWeb extends Discretized{
		public MSWeb() {super("msweb", 294, 29441, 3270, 5000);}
	}
	public static class Netflix extends Discretized{
		public Netflix() {super("bnetflix", 100, 15000, 2000, 3000);}
	}
	public static class NLTCS extends Discretized{
		public NLTCS() {super("nltcs", 16, 16181, 2157, 3236);}
	}
	public static class Plants extends Discretized{
		public Plants() {super("plants", 69, 17412, 2321, 3482);}
	}
	public static class R52 extends Discretized{
		public R52() {super("r52", 941, 6532, 1028, 1540);}
	}
	public static class School extends Discretized{
		public School() {super("school", 66, 44443, 5925, 8888);}
	}
	public static class Traffic extends Discretized{
		public Traffic() {super("traffic", 128, 3311, 441, 662);}
	}
	public static class WebKB extends Discretized{
		public WebKB() {super("webkb", 843, 2803, 558, 838);}
	}
	public static class Wine extends Discretized{
		public Wine() {super("wine", 48, 4874, 650, 975);}
	}

	public static class Accidents extends Discretized{
		public Accidents() {super("accidents"	,	111	,	12758	,	1700	,	2551);}
	}
	public static class Ad extends Discretized{
		public Ad() {super("ad"	,	1556	,	2461	,	327	,	491);}
	}
	public static class BBC extends Discretized{
		public BBC() {super("bbc"	,	1058	,	1670	,	225	,	330);}
	}
	public static class C20NG extends Discretized{
		public C20NG() {super("c20ng"	,	910	,	11293	,	3764	,	3764);}
	}
	public static class CWebKB extends Discretized{
		public CWebKB() {super("cwebkb"	,	839	,	2803	,	558	,	838);}
	}
	public static class DNA extends Discretized{
		public DNA() {super("dna"	,	180	,	1600	,	400	,	1186);}
	}
	public static class Kosarek extends Discretized{
		public Kosarek() {super("kosarek"	,	190	,	33375	,	4450	,	6675);}
	}
	public static class Retail extends Discretized{
		public Retail() {super("tretail"	,	135	,	22041	,	2938	,	4408);}
	}
	public static class Pumsb_Star extends Discretized{
		public Pumsb_Star() {super("pumsb_star"	,	163	,	12262	,	1635	,	2452);}
	}
	public static class CR52 extends Discretized{
		public CR52() {super("cr52"	,	889	,	6532	,	1028	,	1540);}
	}

    // MY CUSTOM DATASETS!
    // First, let's start with the RANDOM datasets...
    public static class r25_500 extends Discretized {
        public r25_500() {super("r25_500", 25, 500, 50, 100);}
    }
    public static class r25_1000 extends Discretized {
        public r25_1000() {super("r25_1000", 25, 1000, 100, 200);}
    }
    public static class r25_2000 extends Discretized {
        public r25_2000() {super("r25_2000", 25, 2000, 200, 400);}
    }
    public static class r25_3000 extends Discretized {
        public r25_3000() {super("r25_3000", 25, 3000, 300, 600);}
    }
    public static class r25_4000 extends Discretized {
        public r25_4000() {super("r25_4000", 25, 4000, 400, 800);}
    }

    public static class r50_500 extends Discretized {
        public r50_500() {super("r50_500", 50, 500, 50, 100);}
    }
    public static class r50_1000 extends Discretized {
        public r50_1000() {super("r50_1000", 50, 1000, 100, 200);}
    }
    public static class r50_2000 extends Discretized {
        public r50_2000() {super("r50_2000", 50, 2000, 200, 400);}
    }
    public static class r50_3000 extends Discretized {
        public r50_3000() {super("r50_3000", 50, 3000, 300, 600);}
    }
    public static class r50_4000 extends Discretized {
        public r50_4000() {super("r50_4000", 50, 4000, 400, 800);}
    }

    public static class r75_500 extends Discretized {
        public r75_500() {super("r75_500", 75, 500, 50, 100);}
    }
    public static class r75_1000 extends Discretized {
        public r75_1000() {super("r75_1000", 75, 1000, 100, 200);}
    }
    public static class r75_2000 extends Discretized {
        public r75_2000() {super("r75_2000", 75, 2000, 200, 400);}
    }
    public static class r75_3000 extends Discretized {
        public r75_3000() {super("r75_3000", 75, 3000, 300, 600);}
    }
    public static class r75_4000 extends Discretized {
        public r75_4000() {super("r75_4000", 75, 4000, 400, 800);}
    }
    
    // Now the ANDOR datasets...
    public static class ao6_5000 extends Discretized {
        public ao6_5000() {super("ao6_5000", 6, 5000, 500, 1000);}
    }
    public static class ao6_10000 extends Discretized {
        public ao6_10000() {super("ao6_10000", 6, 10000, 1000, 2000);}
    }
    public static class ao6_20000 extends Discretized {
        public ao6_20000() {super("ao6_20000", 6, 20000, 2000, 4000);}
    }
    public static class ao6_30000 extends Discretized {
        public ao6_30000() {super("ao6_30000", 6, 30000, 3000, 6000);}
    }

    public static class aor6_5000 extends Discretized {
        public aor6_5000() {super("aor6_5000", 6, 5000, 500, 1000);}
    }
    public static class aor6_10000 extends Discretized {
        public aor6_10000() {super("aor6_10000", 6, 10000, 1000, 2000);}
    }
    public static class aor6_20000 extends Discretized {
        public aor6_20000() {super("aor6_20000", 6, 20000, 2000, 4000);}
    }
    public static class aor6_30000 extends Discretized {
        public aor6_30000() {super("aor6_30000", 6, 30000, 3000, 6000);}
    }

    // Now the DICE datasets...
    public static class d30_5000 extends Discretized {
        public d30_5000() {super("d30_5000", 30, 5000, 500, 1000);}
    }
    public static class d30_10000 extends Discretized {
        public d30_10000() {super("d30_10000", 30, 10000, 1000, 2000);}
    }
    public static class d30_20000 extends Discretized {
        public d30_20000() {super("d30_20000", 30, 20000, 2000, 4000);}
    }
    public static class d30_30000 extends Discretized {
        public d30_30000() {super("d30_30000", 30, 30000, 3000, 6000);}
    }

    public static class d60_5000 extends Discretized {
        public d60_5000() {super("d60_5000", 60, 5000, 500, 1000);}
    }
    public static class d60_10000 extends Discretized {
        public d60_10000() {super("d60_10000", 60, 10000, 1000, 2000);}
    }
    public static class d60_20000 extends Discretized {
        public d60_20000() {super("d60_20000", 60, 20000, 2000, 4000);}
    }
    public static class d60_30000 extends Discretized {
        public d60_30000() {super("d60_30000", 60, 30000, 3000, 6000);}
    }

    // Pretty sure Intel has 215 variables
    public static class Intel extends Discretized {
        public Intel() {super("intel", 215, 13541, 1805, 2708);}
    }

    // MORE custom datasets!
    public static class dice_0 extends Discretized {
        public dice_0() {super("dice_60_10000_0", 60, 10000, 1000, 2000);}
    }
    public static class dice_5 extends Discretized {
        public dice_5() {super("dice_60_10000_5", 60, 10000, 1000, 2000);}
    }
    public static class dice_10 extends Discretized {
        public dice_10() {super("dice_60_10000_10", 60, 10000, 1000, 2000);}
    }
    public static class dice_15 extends Discretized {
        public dice_15() {super("dice_60_10000_15", 60, 10000, 1000, 2000);}
    }
    public static class dice_20 extends Discretized {
        public dice_20() {super("dice_60_10000_20", 60, 10000, 1000, 2000);}
    }
    public static class dice_25 extends Discretized {
        public dice_25() {super("dice_60_10000_25", 60, 10000, 1000, 2000);}
    }
    public static class dice_30 extends Discretized {
        public dice_30() {super("dice_60_10000_30", 60, 10000, 1000, 2000);}
    }
    public static class dice_35 extends Discretized {
        public dice_35() {super("dice_60_10000_35", 60, 10000, 1000, 2000);}
    }
    public static class dice_40 extends Discretized {
        public dice_40() {super("dice_60_10000_40", 60, 10000, 1000, 2000);}
    }
    public static class dice_45 extends Discretized {
        public dice_45() {super("dice_60_10000_45", 60, 10000, 1000, 2000);}
    }
    public static class dice_50 extends Discretized {
        public dice_50() {super("dice_60_10000_50", 60, 10000, 1000, 2000);}
    }



    // Now let's get back to Robert's code...
	private Discretized(String name, int numVar, int numTrain, int numValid, int numTest) {
		this.name = name;
		this.numVar = numVar;
		this.numTraining = numTrain;
		this.numValidation = numValid;
		this.numTesting = numTest;
		attrSizes = new int[numVar];
		data = new int[numTraining + numValidation + numTesting][numVar];

		for(int i=0;i<numVar;i++){ attrSizes[i]=2; }

		int c = 0;

		System.out.println("Dataset "+name+", "+numVar+" vars, "+numTrain+"tr, "+numValid+"va, "+numTest+"te");
		String prefix = "data/";
		String trainfilename = prefix + name + ".ts.data";
		String validfilename = prefix + name + ".valid.data";
		String testfilename  = prefix + name + ".test.data";
		try {
			// Read training
			System.out.println("Loading training...");
			BufferedReader br = new BufferedReader(new FileReader(trainfilename));
			String line = null;
			while ((line = br.readLine()) != null) {
				String toks[] = line.split(delim);
				for(int f=0; f<numVar; f++){
					// Where the permutation is used
					data[c][f] = Integer.parseInt(toks[f]);
				}
				c++;
			}
			br.close();

			// Read validation
			System.out.println("Loading validation...");
			br = new BufferedReader(new FileReader(validfilename));
			while ((line = br.readLine()) != null) {
				String toks[] = line.split(delim);
				for(int f=0; f<numVar; f++){
					data[c][f] = Integer.parseInt(toks[f]);
				}
				c++;
			}
			br.close();

			// Read testing
			System.out.println("Loading testing...");
			br = new BufferedReader(new FileReader(testfilename));
			br.readLine(); // skip first line
			while ((line = br.readLine()) != null) {
				String toks[] = line.split(delim);
				for(int f=0; f<numVar; f++){
					data[c][f] = Integer.parseInt(toks[f]);
				}
				c++;
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		makesparse();
	}

	@Override
	public String toString() {
		return name;
	}



}
