import java.util.ArrayList;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;


public class featureTester {
	private static ArrayList<Attribute>      atts;
    private static ArrayList<String>      attVals;
    private static Instances       data;
    private static double[]        vals;
    static int             i;
	public static void main(String args[]){
	     // 1. set up attributes
	     atts = new ArrayList<Attribute>();
	     // - numeric
	     atts.add(new Attribute("att1"));
	     // - string
	     atts.add(new Attribute("att2",(ArrayList<String>) null));
	     
	     data = new Instances("MyRelation", atts, 0);
	     vals = new double[data.numAttributes()];
	     
	     // - string
	     vals[0] = 
	     vals[1] = data.attribute(0).addStringValue("This is a string!");
	     data.add(new DenseInstance(1.0, vals));
	     vals[0] = data.attribute(0).addStringValue("This is another string!");
	     data.add(new DenseInstance(1.0, vals));
	     System.out.println(data);
	}
}
