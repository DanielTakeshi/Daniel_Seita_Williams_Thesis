package slalg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import spn.GraphSPN;
import spn.Node;
import spn.ProdNode;
import spn.SmoothedMultinomialNode;
import spn.SumNode;
import util.SPNMath;
import data.Dataset;
import data.Partition;
import data.SparseDataset;

// Stuff from matlabcontrol
import matlabcontrol.*;
import matlabcontrol.extensions.*;
import matlabcontrol.internal.*;

import java.util.*;
import java.io.*;

public class VarInstSplit implements SLAlg {
    private static final int MAX_NB_CLUSTERS = 2000;    // Okay, this seems like a resonable upper bound
    public static boolean verbose = true;               // Oh, this just prints out extra comments... (I should do this in my own code!)
    public static double pval = 0.3;                    // Uh ... this doesn't seem to be in the code anywhere else ???
    public static double clusterPenalty = 2;            // The lambda value for the exponential prior
    public static int indepInstThresh = 1;              // Misleading to give this a variable ... used if we have 1 instance but multiple variables.
    public static boolean compPLL = true;               // Uh ... this doesn't seem to be in the code anywhere else ???
    Dataset d;                                          // Given as a parameter.
    public static boolean onlyAddMostDependent = false; // Uh ... this doesn't seem to be in the code anywhere else ???
    public static double gfactor = 1.0;                 // Used for the G-test

    @SuppressWarnings("unused")
    @Override
    // HUGE method here! Learns the structure of an SPN!
    public GraphSPN learnStructure(Dataset d) throws FileNotFoundException, MatlabConnectionException, MatlabInvocationException {

        // This section reads in a given file name based on the dataset. (Data file must be in same directory right now as this code.)
        String fileName = d.toString();
        String fullFileName = "data/" + fileName + ".ts.data";
        Scanner input = new Scanner(new File(fullFileName));

        // pre-read in the number of rows/columns
        int rows = 0;
        int columns = 0;
        while(input.hasNextLine()) {
            ++rows;
            String row = input.nextLine().replace(",", "");
            columns = row.length();
        }
        double[][] fullData = new double[rows][columns];
        // System.out.println("rows: " + rows + ", colums: " + columns);
        input.close();

        // read in the data 
        input = new Scanner(new File(fullFileName));
        int currentRow = 0;
        while (input.hasNextLine()) {
            String row = input.nextLine().replace(",", "");
            for (int j = 0; j < columns; j++) {
                String charAtJ = Character.toString(row.charAt(j));
                int resultingElement = Integer.parseInt(charAtJ);
                fullData[currentRow][j] = (double) resultingElement;
            }
            currentRow++;
        }
        /*
        System.out.println("\nNow trying to actually RUN ivag on this ...\nBy the way, here's our data:\n");
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                System.out.print(fullData[i][j] + " ");
            }
            System.out.println();
        }
        */

        // Initialize the proxy here? Should do it only once in the whole script because it starts up a brand new MATLAB session
        MatlabProxyFactory factory = new MatlabProxyFactory();
        MatlabProxy proxy = factory.getProxy();
        MatlabTypeConverter processor = new MatlabTypeConverter(proxy);

        // Now resume with the rest of the code because we don't need to use this MATLAB stuff right now...
        this.d = d;
        int numCmsg = 6;

        // Queue of <#id, {variables}x{instances}>
        // ... decide whether it's +/x and then recurse
        // List of nodes to create #0 <+, w1x#1, w2x#2>
        //                         #2 <x, #3, #4>
        // when gets to single var/inst, just create SPN node
        // when queue of slices to expand is empty, create nodes from build list

        // Queue for slices of variable-instance space to pursue
        Queue<VarInstSlice> toProcess = new LinkedList<VarInstSlice>();

        // When you build a node, you need to let its future parents know where it is
        HashMap<Integer, Node> idNodeMap = new HashMap<Integer, Node>();

        // Nodes that will be built once all leaf distributions are created
        List<BuildItem> buildList = new ArrayList<BuildItem>();

        int allInstances[] = new int[d.getNumTraining()];
        for(int i=0; i<allInstances.length; i++) allInstances[i] = i;

        int allVariables[] = new int[d.getNumFeatures()];
        for(int i=0; i<allVariables.length; i++) allVariables[i] = i;

        int nextID = 0;

        if(verbose) System.out.println("Building structure from "+allVariables.length+" vars over "+allInstances.length+" instances");

        // Start with all variables over all instances (and with ID = 1)
        // One thing to note is that allInstances and allVariables are just arrays where the ith element is just i.
        VarInstSlice wholeTraingingset = new VarInstSlice(nextID++, allInstances, allVariables);

        toProcess.add(wholeTraingingset);

        GraphSPN spn = new GraphSPN(d);

        // So each time we iterate, seems like we take out first element of queue and analyze it
        // VarInstSlice is defined at end of this code, using id, etc. we can extract information
        while(!toProcess.isEmpty()){
            VarInstSlice currentSlice = toProcess.remove();
            int id = currentSlice.id;

            // printArray(currentSlice.variables);

            // (SPECIAL CASE 1) So if there's only one variable left, return the smoothed univariate distribution and add to spn
            if(currentSlice.variables.length == 1){
                Node varnode = new SmoothedMultinomialNode(d, currentSlice.variables[0], currentSlice.instances);
                spn.order.add(varnode);
                idNodeMap.put(id, varnode);
                continue;
            }

            // (SPECIAL CASE 2) More than one variable left, and the number of instances is smaller than a given threshold
            // Okay, I get it ... indepInstThresh is defined to be 1 at the start, and this part of the code is the ony one using it.
            // Thus, if we have one (or zero, which would be weird) instance and more than one variable, automatically split it on
            // the variables. That does make sense because we CAN'T do any instance splitting.
            // Notice that each SPN node needs an ID, which corresponds to its variable/instance splice. The first node has ID 1.
            // This is why we see ch_id1 = nextID++ because each child needs a UNIQUE ID number.
            if(currentSlice.instances.length<=indepInstThresh && currentSlice.variables.length>1){
                int idx = 0;
                int ch_ids[] = new int[currentSlice.variables.length];

                // For each variable, it seems like we make a new varInstSlice with all instances but just that one variable...
                for(int var : currentSlice.variables){
                    int ch_id1 = nextID++;
                    ch_ids[idx++] = ch_id1;

                    VarInstSlice ch1 = new VarInstSlice(ch_id1, currentSlice.instances, new int[] {var});
                    toProcess.add(ch1); // Add this here so that the next iteration of this while loop, we go to SPECIAL CASE 1.
                }
                // This should build the product node with all the variables as children ... then the next step is to get multinomials.
                BuildItem newprod = new BuildItem(id, false, ch_ids, null);
                buildList.add(newprod);
                continue;
            }

            // Most of the time we won't be in the two cases above, so now we actually measure independence for variables...
            // measure appx. independence ... each element in this array is a reference to a GROUP of variables.
            int indset[] = null;

            CountCache cc = null; // This class extends HashMap<String, Count>, so maybe a count for each string?
            if(nextID > 1){
                // long tic = System.currentTimeMillis(); // Okay, this is just for timing reasons, not needed for functionality
                // long toc = System.currentTimeMillis();
                
                cc = new CountCache();

                // Okay, now we implement the MATLAB stuff ... first, let's actually obtain the data we need! Don't forget that 'fullData' is an array
                // of array of doubles which contains all the data. We need to pick out the relevant parts based on the current slice. 
                int numberOfInstances = currentSlice.instances.length;
                int numberOfVariables = currentSlice.variables.length;
                double[][] inputToMatlab = new double[numberOfInstances][numberOfVariables];
                for (int row = 0; row < numberOfInstances; row++) {
                    for (int column = 0; column < numberOfVariables; column++) {
                        // Obtain the actual numeric quantity from the current slice (not all pairs (x,y) will be used for a slice)
                        int instanceNum = currentSlice.instances[row];
                        int variableNum = currentSlice.variables[column];
                        inputToMatlab[row][column] = (double) fullData[instanceNum][variableNum];
                    }
                }

                // Now let's input the matrix (cleverly named 'inputToMatlab') into MATLAB and extract the results! 
                processor.setNumericArray("dataArray", new MatlabNumericArray(inputToMatlab, null));
                String stringOfNominals = "[2";
                for (int i = 1; i < numberOfVariables; i++) stringOfNominals += " 2";
                stringOfNominals += "]";
                String inputString = "nominalVars = " + stringOfNominals;
                proxy.eval(inputString);
                // System.out.println("\nNow trying to actually RUN ivag on this ...\nBy the way, here's our data:\n");
                // System.out.println("Variables: " + numberOfVariables + " and instances: " + numberOfInstances);
                proxy.eval("result = ivga(ivga_import_data( dataArray, 'types', nominalVars ), 'runtime', 'normal')");
                // proxy.eval("num = length(result.grouping)");
                // double numVariables = ((double[]) proxy.getVariable("num"))[0]; 
                proxy.eval("currentResult = result.grouping{1}");

                double[] variableGrouping = (double[]) proxy.getVariable("currentResult");
                
                // Daniel: MATLAB orders these variables 1, 2, ... , n, where 1 \le n \le 16. But currentSlice.variables will contain a subset of the
                // variables in {0, 1, ... , 15}, and not all of these will be used when the subsets get smaller. So we need a way of CONVERTING from
                // one to another! That will be the purpose of this code segment.
                indset = new int[variableGrouping.length];
                for (int i = 0; i < variableGrouping.length; i++) {
                    indset[i] = (int) variableGrouping[i];
                }
                // Now convert 1, 2, ... , n to the variables in our current slice! First, get the value of indset[i]. If we let it be equal to 'k',
                // then this will be the k^th variable listed in currentSlices.variables, where k=1 indicates the first variable (if all variables
                // present, then that is obviously zero, but if not, we need to change it).
                for (int i = 0; i < variableGrouping.length; i++) {
                    int whichVariable = indset[i] - 1; // Don't forget the minus one here!!
                    indset[i] = currentSlice.variables[whichVariable];
                }
            
                // This is the old code from Robert Gens (yes, it's just one line...)
                //indset = greedyIndSet(d, currentSlice,cc);
            } else {
                indset = new int[0]; // Don't try to measure independence on the first node (would be a trivial dataset)
            }

            // So this is the test of 'success' versus 'failure' of our variable clustering algorithm.
            // If found an appx. independent set ... then we create a product node with children based on the variable splits
            // Test if the number of independent sets is < than number of variables, because if x sets for x variables, all independent
            if(indset.length > 1){
                System.out.println("Indset size "+indset.length);
            } 
            if(indset.length < currentSlice.variables.length && indset.length > 0){ 
                if(currentSlice.instances.length<4 && indset.length > 1)  // This case is quite rare.
                    System.out.println("Found indset (x) ("+indset.length+"/"+currentSlice.variables.length+") over "+currentSlice.instances.length+"I");
                // printArray(indset); // My helper method
                int ch_id1 = nextID++;
                int ch_id2 = nextID++;
                // Building a PRODUCT node
                BuildItem newprod = new BuildItem(id, false, new int[] {ch_id1, ch_id2}, null);
                buildList.add(newprod);
                VarInstSlice ch1 = new VarInstSlice(ch_id1, currentSlice.instances, indset); // One variable split gets all the 'indset' variables
                VarInstSlice ch2 = new VarInstSlice(ch_id2, currentSlice.instances, setminus(currentSlice.variables, indset)); // Gets the non-'indset' variables!
                toProcess.add(ch1);
                toProcess.add(ch2);
            } else {
                // Now we CLUSTER INSTANCES! Because variables were not able to be divided up...
                // bestClustering is ... the clustering of variables! Calls the 'clusterNBInstances' for this purpose (NB = Naive Bayes)
                // IF I MAKE CHANGES TO CLUSTERING, THIS IS WHERE I WILL DO IT!
                int instsets[][] = null;
                ClustersWLL bestClustering = clusterNBInsts(d, currentSlice, cc);
                instsets = bestClustering.clusters;

                if(numCmsg-- > 0)
                    System.out.println(currentSlice.instances.length+"I "+currentSlice.variables.length+"V -> "+instsets.length+" clusters");

                double ch_weights[] = new double[instsets.length];
                int ch_ids[] = new int[instsets.length];
                for(int c=0; c<instsets.length; c++){
                    int ch_id1 = nextID++;
                    double w1 = 1.0*instsets[c].length/currentSlice.instances.length;

                    ch_ids[c] = ch_id1;
                    ch_weights[c] = w1;

                    VarInstSlice ch1 = new VarInstSlice(ch_id1, instsets[c], currentSlice.variables);
                    toProcess.add(ch1);
                }
                // Building a SUM node
                BuildItem newsum = new BuildItem(id, true, ch_ids, ch_weights);
                buildList.add(newsum);  
            }
        }

        // Disconnect the MATLAB proxy
        proxy.disconnect();

        // All right, now we're going to be BUILDING the spn (last part of code was getting the sum/products set up)
        // This part, I should not have to modify since the stuff it takes as input is the interesting part of this research
        // Main part of this code does two different types of building based on sums/products, quite straightforward
        if(verbose) System.out.println("SPN has "+spn.order.size()+" nodes");
        if(verbose) System.out.println("Building from build list ("+buildList.size()+" items)...");
        while(!buildList.isEmpty()){
            BuildItem bi = buildList.remove(buildList.size()-1);

            if(bi.sum){
                SumNode sn = new SumNode(spn);
                for(int c=0; c<bi.ids.length; c++){
                    Node ch = idNodeMap.get(bi.ids[c]);
                    if(ch instanceof SumNode){
                        SumNode snc = (SumNode) ch;
                        for(int sc=0; sc<snc.keyOrder.size(); sc++){
                            sn.addChdOnly(snc.keyOrder.get(sc), bi.weights[c] * snc.getW().get(sc));
                        }
                        spn.order.remove(snc);
                    } else {
                        sn.addChdOnly(ch, bi.weights[c]);
                    }
                }
                spn.order.add(sn);
                idNodeMap.put(bi.id, sn);
            } else {
                ProdNode pn = new ProdNode();
                for(int c=0; c<bi.ids.length; c++){
                    Node ch = idNodeMap.get(bi.ids[c]);
                    if(ch instanceof ProdNode){
                        ProdNode pnc = (ProdNode) ch;
                        for(Node pc : pnc.allChildren()){
                            pn.addChd(pc);
                        }
                        spn.order.remove(pnc);
                    } else {
                        pn.addChd(ch);
                    }
                }
                spn.order.add(pn);
                idNodeMap.put(bi.id, pn);
            }
        }
        if(verbose) System.out.println("SPN has "+spn.order.size()+" nodes");
        return spn;
    }




    /*
     * Used for clustering INSTANCES if no approximately independent sets of variabes are found
     * This is where we run the hard EM with naive bayes prior (should see where naive bayes prior is used)
     * Note here that there is stuff about log likelihood. (Need to understand how it's used for evaluation)
     * 
     * From the paper (Section 4): ran ten restarts through subset of instances four time in random order.
     * They estimated P(X_j | C_j) with Laplace smoothing, adding 0.1 to each count.
     * Great source for this is Andrew Ng: http://see.stanford.edu/materials/aimlcs229/cs229-notes2.pdf
     * Btw, I think that 'LL' in variable names stands for LOG LIKELIHOOD (takes on values between -\infty and 0)
     */
    private ClustersWLL clusterNBInsts(Dataset d, VarInstSlice currentSlice, CountCache cc) {
        int numRuns = 10;
        int numEMits = 4;

        double bestLL = Double.NEGATIVE_INFINITY;
        int bestClusters[][] = null ;

        HashSet<Integer> vars = new HashSet<Integer>();
        for(Integer var : currentSlice.variables){
            vars.add(var);
        }

        ArrayList<Integer> instanceOrder = new ArrayList<Integer>();
        for(int inst : currentSlice.instances){
            instanceOrder.add(inst);
        }

        double newClusterLL = (new Cluster(vars)).newLL();      
        double newClusterPenalizedLL = -clusterPenalty * vars.size()+newClusterLL;
        for(int r=0; r<numRuns; r++){
            System.out.println("EM Run "+r+" with "+instanceOrder.size()+" insts "+vars.size()+" vars");
            List<Cluster> nbcs = new ArrayList<Cluster>();

            Collections.shuffle(instanceOrder);

            // data structure for assignment of inst to cluster
            // {instance}->num of cluster
            Map<Integer, Cluster> inst_cluster_map = new HashMap<Integer, Cluster>();
            double LL=Double.NEGATIVE_INFINITY;

            // newClusterPenalizedLL *= 1.0 + Math.random()*0.4 - 0.2;
            for(int it=0; it<numEMits; it++){
                // LL = 0
                // System.out.print("EM Iteration "+it);
                LL = 0;

                double minBest = Double.POSITIVE_INFINITY;
                int inc=0;
                for(int inst : instanceOrder){
                    inc++;
                    // remove SS from previously assigned cluster
                    Cluster prev_cluster = inst_cluster_map.remove(inst);
                    if(prev_cluster != null){
                        prev_cluster.removeInst(inst);
                        if(prev_cluster.isEmpty()){
                            nbcs.remove(prev_cluster);
                        }
                    }

                    // set best LL to be newClusterPenalty
                    // double newClusterPenalty = -clusterPenalty * nbcs.size();
                    double bestCLL = newClusterPenalizedLL;
                    Cluster bestCluster = null;
                    // find best cluster
                    // quit when LL is less than best
                    for(Cluster c : nbcs){
                        double cll = c.ll(inst, Partition.Training, bestCLL);
                        // System.out.println("  "+inst+" vs "+c+" "+cll);
                        if(cll > bestCLL){
                            bestCLL = cll;
                            bestCluster = c;                            
                        }
                    }

                    if(bestCLL < minBest){
                        minBest = bestCLL;
                    }

                    // make new cluster if bestLL is not greater than penalty
                    if(bestCluster == null){
                        bestCluster = new Cluster(vars);
                        nbcs.add(bestCluster);

                        if(nbcs.size() > MAX_NB_CLUSTERS && currentSlice.instances.length > 10000){
                            System.out.println("Too many clusters, increase CP penalty");
                            System.exit(0);
                        }
                    }
                    // if(inc % 1000 == 0) System.out.println(inc+"inst w/ "+nbcs.size());

                    // add SS to newly assigned cluster
                    bestCluster.addInst(inst);
                    inst_cluster_map.put(inst, bestCluster);

                    LL += bestCLL;
                }
                // System.out.println("\t"+nbcs.size()+" clusters\t"+LL+" LL");

                // Always ensure there's more than one cluster
                if(nbcs.size() == 1){
                    it = 0;
                    newClusterPenalizedLL *= 0.5;
                }
            } // End EM

            // if LL > best...
            // bestLL
            // bestClusters

            LL = penalizedLL(nbcs, currentSlice.instances, currentSlice.variables.length);

            if(LL > bestLL){
                bestLL = LL;
                HashMap<Cluster, Integer> clusterToId = new HashMap<VarInstSplit.Cluster, Integer>();
                int nextID = 0;
                List<List<Integer>> instSets = new ArrayList<List<Integer>>();
                for(Cluster c : nbcs){
                    clusterToId.put(c, nextID++);
                    instSets.add(new ArrayList<Integer>());
                }
                bestClusters = new int[nbcs.size()][];
                for(Entry<Integer, Cluster> e : inst_cluster_map.entrySet()){
                    int cid = clusterToId.get(e.getValue());
                    instSets.get(cid).add(e.getKey());
                }
                for(int cid=0; cid<bestClusters.length; cid++){
                    bestClusters[cid] = new int[instSets.get(cid).size()];
                    int i=0;
                    for(Integer inst : instSets.get(cid)){
                        bestClusters[cid][i++] = inst;
                    }
                }
            } // end if this run of em is better

        } // end multiple runs of EM

        return new ClustersWLL(bestClusters, bestLL);
    }




    // Computes penalized LL, used for making clusters
    private double penalizedLL(List<Cluster> nbcs, int[] instances, int numVars) {
        double LL = 0;
        double clusterPriors[] = new double[nbcs.size()];
        for(int c=0; c<nbcs.size(); c++){
            clusterPriors[c] = 1.0 * nbcs.get(c).size / instances.length;
        }
        for(int trinst : instances){
            double logprob = Double.NEGATIVE_INFINITY;
            for(int c=0; c<nbcs.size(); c++){
                logprob = SPNMath.log_sum_exp(logprob, Math.log(clusterPriors[c]) + nbcs.get(c).ll(trinst, Partition.Training, Double.NEGATIVE_INFINITY));
            }
            LL += logprob;
        }
        LL -= clusterPenalty * numVars * nbcs.size();
        return LL;
    }




    // This gets created in the cluster naive bayes method. It holds the clusters for instances.
    class ClustersWLL {
        public int splitVar = 0;
        int clusters[][];
        double LL;
        public ClustersWLL(int[][] clusters, double lL) {
            this.clusters = clusters;
            LL = lL;
        }
        public ClustersWLL(int[][] clusters2, double pLL, Integer bestVar) {
            this.clusters = clusters2;
            LL = pLL;
            this.splitVar = bestVar;
        }
    }



    // Another cluster class ...
    private class Cluster {
        double smoo = 0.1;
        double nbsmoo = 0.1;
        // data structure for sufficient statistics
        // {num clusters}x{attrs}x{attr val}
        Map<Integer,List<Integer>> sstats = new HashMap<Integer,List<Integer>>();
        Map<Integer,Integer> sstats_nonzero = new HashMap<Integer,Integer>();
        final HashSet<Integer> vars;
        int size = 0;

        public Cluster(HashSet<Integer> vars) {
            this.vars = vars;
        }

        public void removeInst(int inst) {
            d.show(inst, Partition.Training);
            int vals[] = d.getValues();

            size--;
            // find all non-zero attrs of inst, decrement counts in sstats
            for(Integer var : ((SparseDataset) d).get_sp_inst_attr().get(inst)){
                if(!vars.contains(var)){
                    continue;
                }
                // Decrement count for the value of the non-zero attr
                sstats.get(var).set(vals[var], sstats.get(var).get(vals[var])-1);
                // Decrement count for the non-zero attr
                int newcount = sstats_nonzero.get(var)-1;
                sstats_nonzero.put(var, newcount);
                if(newcount == 0){
                    sstats.remove(var);
                    sstats_nonzero.remove(var);
                }
            }
        }

        public void addInst(int inst) {
            d.show(inst, Partition.Training);
            int vals[] = d.getValues();


            // find all non-zero attrs of inst, decrement counts in sstats
            for(Integer var : ((SparseDataset) d).get_sp_inst_attr().get(inst)){
                if(!vars.contains(var)){
                    continue;
                }

                // Create list to count values of non-zero attrs (if not already created)
                if(!sstats.containsKey(var)){
                    List<Integer> counts = new ArrayList<Integer>();
                    for(int a=0; a<d.getAttrSizes()[var]; a++){
                        counts.add(0);
                    }
                    sstats.put(var, counts);
                }

                // Increment count for the value of the non-zero attr
                sstats.get(var).set(vals[var], sstats.get(var).get(vals[var])+1);
                // Increment count for the non-zero attr
                sstats_nonzero.put(var, sstats_nonzero.containsKey(var) ? sstats_nonzero.get(var) + 1 : 1);
            }
            size++;
        }

        public double ll(int inst, Partition part, double bestCLL) {
            double l=0;

            d.show(inst, part);
            int att_sizes[] = d.getAttrSizes();
            int vals[] = d.getValues();

            for(Integer var : vars){
                // Lookup counts for this variable
                if(sstats_nonzero.containsKey(var)){ // Some vals of attr are non-zero in cluster
                    if(vals[var]==0){
                        int w = size - sstats_nonzero.get(var);
                        l += Math.log((w + smoo) / (size + att_sizes[var]*smoo));
                    } else {
                        int w = sstats.get(var).get(vals[var]);
                        l += Math.log((w + smoo) / (size + att_sizes[var]*smoo));
                    }
                } else { // All values of attr are zero in the cluster
                    if(vals[var]==0){
                        l += Math.log((size + smoo) / (size + att_sizes[var]*smoo));
                    } else {
                        l += Math.log((smoo) / (size + att_sizes[var]*smoo));
                    }
                }
                if(l < bestCLL){
                    return l;
                }
            }
            return l;
        }

        double newLL(){
            double ll=0;
            int att_sizes[] = d.getAttrSizes();

            for(Integer var : vars){
                // ll += Math.log((1 + smoo/att_sizes[var]) / (1 + smoo));
                ll += Math.log((1 + nbsmoo) / (1 + att_sizes[var]*nbsmoo));
            }
            return ll;
        }

        public boolean isEmpty() {
            return size==0;
        }
    }

    public static void printArray(int[] arrayToPrint) {
        // System.out.print("Now printing array of length " + arrayToPrint.length + ": [ ");
        for (int i : arrayToPrint) {
            // System.out.print(i + " ");
        }
        // System.out.print("]\n");
    }

    // Just a random helper method to do the set minus for variable splitting.
    private int[] setminus(int[] whole, int[] part) {
        // System.out.println("Now calling setminus with arrays of length " + whole.length + " and " + part.length);
        int toreturn[] = new int[whole.length-part.length];

        HashSet<Integer> temp = new HashSet<Integer>();
        for(int i : whole){ temp.add(i); }
        for(int i : part){ temp.remove(i); }

        // printArray(whole);
        // printArray(part);

        int idx=0;
        // System.out.println("Length of return array: " + toreturn.length + " and of temp set is " + temp.size());
        for(int i : temp){
          //   System.out.println("Current value is " + idx);
            toreturn[idx++] = i;
        }
        return toreturn;
    }


    // Called/used when we're trying to establish approximate independence w.r.t. variabes. Returns an array of ints.
    // If there's only one instance left, we split it on all variables automatically. Return a single integer? I mean,
    // if there's only one instance, we can't tell if these variables are independent or not!
    private int[] greedyIndSet(Dataset d, VarInstSlice currentSlice, CountCache cc) {
        if(currentSlice.instances.length == 1){
            int returnset[] = new int[] {currentSlice.variables[0]};
            return returnset;
        }

        HashSet<Integer> vars = new HashSet<Integer>();
        for(int i : currentSlice.variables){
            vars.add(i);
        }

        int numCalls = 0;
        HashSet<Integer> indset = new HashSet<Integer>();
        Integer seed = util.SPNUtil.RandomElement(vars);
        vars.remove(seed);
        indset.add(seed); // So this seems to identify an arbitrary variable and use it to start the first set
        Queue<Integer> toprocess = new LinkedList<Integer>();
        toprocess.add(seed);

        while(!toprocess.isEmpty()){
            Integer v = toprocess.remove();
            List<Integer> toremove = new ArrayList<Integer>();

            for(Integer ov : vars){
                numCalls++;
                // So if the variable 'ov' is NOT independent from variable 'v', then add it to the 'indset'
                // I think 'v' means 'variable' and 'ov' means 'other variable'
                if(!independent(v,ov,d,currentSlice.instances,cc)){
                    toremove.add(ov);
                    indset.add(ov);
                    toprocess.add(ov);
                }
            }
            for(Integer ov : toremove){
                vars.remove(ov);
            }
        }

        int returnset[] = new int[indset.size()];
        int i=0;
        for(Integer v : indset){
            returnset[i] = v;
            i++;
        }
        // indset may have size LESS than 'vars.size()' since I think we start with elements and if a variable is found to be
        // INDEPENDENT from a given variable, we DON'T add anything...
        return returnset; // So this is an array with # of elements equal to length of 'indset.' 
    }


    // This is Daniel's change, to make this code use more than just two children for each product node!
    private int[][] greedyIndSetDaniel(Dataset d, VarInstSlice currentSlice, CountCache cc) {
        if (currentSlice.instances.length == 1) {
            int returnset[][] = new int[][] {{ currentSlice.variables[0] }}; // Want the current variable slice to be the ONLY sub-array in the larger array
            return returnset;
        }
        HashSet<Integer> vars = new HashSet<Integer>();
        for (int i : currentSlice.variables) {
            vars.add(i);
        }
        
        // Daniel: This is where my implementation differs from his.
        // Iterate pairwise over variables ... have a list of lists kind of thing, where we start off with 1 list in the list of lists.
        // Then for each new list we add to it as necessary to create independence groups.
        // For each sublist in the list of lists, we then assign returnset to those independence groups ...
        int[][] returnset = null;
        return returnset; 
    } 


    // Used as part of 'CountCache', but I'm not sure what this is for, honestly.
    class Count {
        int counts[][];
        public Count(int c[][]) {
            this.counts = c;
        }
    }


    // Called/used when we're trying to establish approximate independence w.r.t. variabes
    class CountCache extends HashMap<String,Count>{
        private static final long serialVersionUID = 1L;
    }


    // This is where we use the G-test to determine pairwise independence
    // Called by greedyIndSet method which starts the whole pairwise independence test
    private boolean independent(Integer v, Integer ov, Dataset d, int[] instances, CountCache cc) {
        if(ov < v){
            Integer temp = v;
            v = ov;
            ov = temp;
        }

        int attrSize[] = d.getAttrSizes();
        int counts[][] = new int[attrSize[v]][attrSize[ov]];
        int vtot[] = new int[attrSize[v]];
        int ovtot[] = new int[attrSize[ov]];

        if(d instanceof SparseDataset){
            HashSet<Integer> set_instances = new HashSet<Integer>();
            for(int i: instances){
                set_instances.add(i);
            }
            counts = ((SparseDataset) d).count(v, ov, set_instances);
        }
        else {
            for(int i : instances){
                d.show(i, Partition.Training);
                int vals[] = d.getValues();
                counts[vals[v]][vals[ov]]++;
            }
        }

        if(cc != null){
            cc.put(v+","+ov,new Count(counts));
        }

        for(int j=0; j<attrSize[v]; j++){
            for(int k=0; k<attrSize[ov]; k++){
                vtot[j] += counts[j][k];
                ovtot[k] += counts[j][k];
            }
        }

        int vskip=0, ovskip=0;
        for(int j=0; j<attrSize[v]; j++){
            if(vtot[j] == 0){
                vskip++;
            }
        }

        for(int k=0; k<attrSize[ov]; k++){
            if(ovtot[k] == 0){
                ovskip++;
            }
        }

        // Double iteration through the possible values for each attribute
        double gval = 0;
        for(int j=0; j<attrSize[v]; j++){
            for(int k=0; k<attrSize[ov]; k++){
                double ecount = 1.0*vtot[j]*ovtot[k]/instances.length;
                if(counts[j][k] == 0.0) continue;
                //              chisq += Math.pow(counts[j][k] - ecount, 2.0)/ecount;
                gval += 1.0 * counts[j][k] * Math.log(1.0 * counts[j][k] / ecount);
            }
        }
        gval *= 2;

        int dof = (attrSize[v]-vskip-1)*(attrSize[ov]-ovskip-1);
        // If less than threshold, observed values could've been produced by noise on top of independent vars
        return gval < 2 * dof * gfactor + 0.001; 
    }


    // So that's where this was hiding! So a VarInstSlice is just a 3-tuple and we get id, instances, and variables.
    // One thing to note is that instances and variabes are just int arrays.
    // Not sure why Gens didn't just make methods to extract that info, but the class is private ... is that why?
    private class VarInstSlice {
        public final int id, instances[], variables[];

        public VarInstSlice(int id, int[] instances, int[] variables) {
            this.id = id;
            this.instances = instances;
            this.variables = variables;
        }
    }


    // Used to represent the construction of sum and product nodes after leaf node distributions have been formed.
    private class BuildItem {
        public final int id;
        public final boolean sum;
        public final int ids[];
        public final double weights[];

        public BuildItem(int id, boolean sum, int[] ids, double[] weights) {
            super();
            this.id = id;
            this.sum = sum;
            this.ids = ids;
            this.weights = weights;
        }
    }

}
