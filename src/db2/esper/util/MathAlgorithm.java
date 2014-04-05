package db2.esper.util;

/**
 * A collection of algorithm to do the math calculation behind the anomaly detection:
 * - intersection in space...
 * - collision?
 * NB: all this method has to be STATIC method!
 */
public class MathAlgorithm {

	// è giusto una bozza....qualcosa di più pedante (spero però non errata) non potevo farla...non ridere troppo! sistemi tu?
	
	
	public static boolean crossSegments (double[] A, double[] B, double[] C, double[] D){
	double[] cross_scale = new double[2];
	double[] vettCA= new double[2];
	double[] vettDC = new double[2];
	double[] vettBA = new double[2];
	double[] vettAC = new double[2];
	boolean result=true;
	
	for (int i=0 ; i<vettCA.length ; i++)
		vettCA[i]= C[i]-A[i];
	for (int i=0 ; i<vettDC.length ; i++)
		vettDC[i]= D[i]-C[i];
	for (int i=0 ; i<vettBA.length ; i++)
		vettBA[i]= B[i]-A[i];
	for (int i=0 ; i<vettAC.length ; i++)
		vettAC[i]= A[i]-C[i];
	
	//cross_scale(1)=cprod((c(1:2)-a(1:2)),d(1:2)-c(1:2)) / cprod(b(1:2)-a(1:2),d(1:2)-c(1:2)):
	cross_scale[1] = cprod(vettCA , vettDC) / cprod (vettBA , vettDC);
	
	//cross_scale(2)=cprod((a(1:2)-c(1:2)),b(1:2)-a(1:2)) /  cprod(d(1:2)-c(1:2),b(1:2)-a(1:2));
	cross_scale[2] = cprod(vettAC , vettBA) / cprod (vettDC , vettBA);
	
	//bol= all(cross_scale>=0 & cross_scale<=1):
	for (int i=0 ; i<cross_scale.length ; i++)
		if (cross_scale[i]<0 || cross_scale[1]>1){
			result=false;
		}
	return result;
	}
	
	//function c=cprod(a,b): c=a(1)*b(2)-a(2)*b(1)
	public static double cprod (double[] pt1, double[] pt2){
		double c=0;
		c=pt1[0]*pt2[1]-pt1[1]*pt2[0];
		return c;
	}
}
