package slalg;

import spn.GraphSPN;
import data.Dataset;

import matlabcontrol.*;
import matlabcontrol.extensions.*;
import matlabcontrol.internal.*;
import java.io.*;

public interface SLAlg {
	public GraphSPN learnStructure(Dataset d) throws FileNotFoundException, MatlabConnectionException, MatlabInvocationException;
}
