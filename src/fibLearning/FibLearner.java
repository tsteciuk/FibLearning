package fibLearning;
import java.util.ArrayList;
import java.util.Scanner;

public class FibLearner {
	
	//constant, manipulates amount of data used from sequence
	public static final int NUM_DATA=200;
	
	//vars to be generated in constructor
	public static ArrayList<Double> data;
	public static int dataMod;
	public static double sum;
	public static double rootSum;
	public static double maxWeightedDiff;
	
	//generates data for fibonacci sequence
	public FibLearner() {
		data = new ArrayList<Double>();
		data.add(0.0);
		data.add(1.0);
		sum = 1.0;
		rootSum = 1.0;
		for(int i=2; i<(NUM_DATA); i++) {
			data.add(data.get(i-1)+data.get(i-2));
			sum+=data.get(i-1)+data.get(i-2);
			rootSum+=Math.sqrt(Math.sqrt(Math.sqrt(data.get(i-1)+data.get(i-2))));
		}
		dataMod = 1;
	}
	
	/*Checks the "direct accuracy" of equation. In short, this method returns
	 * the percentage of results from an equation that are within 10% of the
	 * value found in the data*/
	public double directAcc(EquationTree e) {
		double correct = 0.0 + (NUM_DATA/dataMod);
		for(int i=0; i<(NUM_DATA/dataMod); i++) {
			if(Math.abs(e.evaluateEquation(i)-data.get(i))<data.get(i)/10) correct-=1.0;
		}
		return correct/(NUM_DATA/dataMod);
	}
	
	/* Returns the overall absolute difference between the equation and data.
	 * Sums up the difference between equation's value and each data point.*/
	public double absDiff(EquationTree e) {
		double diff = 0.0;
		for(int i=0; i<(NUM_DATA); i++) {
			diff += Math.abs(e.evaluateEquation(i)-data.get(i));
		}
		if (diff==Double.NaN) return sum;
		return diff;
	}
	
	/*Returns the "percentage difference" between the equation and the data.
	 * This value is the absolute difference between the equation and data
	 * divided by the sum of the data, meaning this is a metric which
	 * is biased toward the largest values of the data set.*/
	public double percentDiff(EquationTree e) {
		return absDiff(e)/Math.abs(sum);
	}
	
	//checks if the first value given by the equation is 0 within a margin of error
	public boolean correctStartingPoint(EquationTree e) {
		if(Math.abs(e.evaluateEquation(0))<0.01) return true;
		return false;
	}
	
	/*This method attempts to measure the "quality" of a given equation by summing
	 * its direct accuracy and percentage difference, which (for equations that are
	 * even remotely suitable) returns a value between 0 and 2. Technically, worse
	 * equations can yield values higher than 2, but as this value approaches 0, it
	 * is expected that the equation is a better approximation for the data.*/
	public double qual(double acc, double perc) {
		if(Double.isNaN(perc)) return 1000;
		double qual = 0.0;
		qual+=acc;
		qual+=perc;
		return qual;
	}
	
	//Replaces any roots worse than any of the children with the children, based on quality metric
	public ArrayList<EquationTree> qualitySelection(ArrayList<EquationTree> roots, ArrayList<EquationTree> eqs) {
		ArrayList<Double> rootQuals = new ArrayList<Double>();
		for(int i=0; i<roots.size(); i++) {
			rootQuals.add(qual(directAcc(roots.get(i)),percentDiff(roots.get(i))));
		}
		for(int i=eqs.size()-1; i>=0; i--) {
			boolean inserted = false;
			for(int j=0; j<roots.size(); j++) {
				if(qual(directAcc(eqs.get(i)),percentDiff(eqs.get(i)))<rootQuals.get(j) && !inserted) {
					rootQuals.set(j,qual(directAcc(eqs.get(i)),percentDiff(eqs.get(i))));
					roots.set(j,eqs.get(i));
					inserted = true;
				}
			}
		}
		return roots;
	}
	
	/*Filters children by comparison to roots with randomly chosen order of metrics and
	 * stops after a metric if there are less than 6 children left, or all metrics are
	 * used. Then,if more than 6 children are left, compares them to each other by a 
	 * random metric until there are just 6. If there are no children left after any 
	 * given filter, merely returns the roots.*/
	public ArrayList<EquationTree> lexicaseSelection(ArrayList<EquationTree> roots, ArrayList<EquationTree> eqs) {
		double bestPercentDiff=percentDiff(roots.get(0));
		double bestDirectAcc=directAcc(roots.get(0));
		for(int i=1; i<roots.size(); i++) {
			double thisPercentDiff = percentDiff(roots.get(i));
			double thisDirectAcc = directAcc(roots.get(i));
			if(thisPercentDiff<bestPercentDiff) bestPercentDiff = thisPercentDiff;
			if(thisDirectAcc<bestDirectAcc) bestDirectAcc = thisDirectAcc;
		}
		ArrayList<Integer> metrics = new ArrayList<Integer>();
		for(int i=0; i<3; i++) {
			metrics.add(i);
		}
		boolean fullyFiltered = false;
		while(eqs.size()>6 && !fullyFiltered) {
			int m = (int)(Math.random()*metrics.size());
			int metric = metrics.get(m);
			metrics.remove(m);
			if(metric==0) {
				for(int i=eqs.size()-1; i>=0; i--) {
					if(percentDiff(eqs.get(i))>bestPercentDiff) eqs.remove(i);
				}
			} else if(metric==1) {
				for(int i=eqs.size()-1; i>=0; i--) {
					if(directAcc(eqs.get(i))>bestDirectAcc) eqs.remove(i);
				}
			} else {
				for(int i=eqs.size()-1; i>=0; i--) {
					if(!correctStartingPoint(eqs.get(i))) eqs.remove(i);
				}
			}
			if(metrics.size()==0) fullyFiltered = true;
		}
		if(eqs.size()>6) {
			while(eqs.size()>6) {
				int i1 = (int)(Math.random()*eqs.size());
				int i2 = (int)(Math.random()*eqs.size());
				while(i2==i1) {
					i2 = (int)(Math.random()*eqs.size());
				}
				eqs.add(bestByRandomMetric(eqs.get(i1),eqs.get(i2)));
				if(i1>i2) {
					eqs.remove(i1);
					eqs.remove(i2);
				} else {
					eqs.remove(i2);
					eqs.remove(i1);
				}
			}
		}
		if(eqs.size()>0) return eqs;
		return roots;
	}
	
	//determines the best of two roots by either direct accuracy or percent difference
	public EquationTree bestByRandomMetric(EquationTree e1, EquationTree e2) {
		if(Math.random()<0.5) {
			if(percentDiff(e1)<percentDiff(e2)) return e1;
			return e2;
		}
		if(directAcc(e1)<directAcc(e2)) return e1;
		return e2;
	}
	
	//runs the program, asks user to choose whether to use lexicase selection or not
	public static void main(String[] args) {
		boolean lexicase = false;
		boolean chosen = false;
		System.out.println("Would you like to use Lexicase Selection? y/n");
		Scanner scanner = new Scanner(System.in);
		String s = scanner.nextLine();
		while(!chosen) {
			if(s.equalsIgnoreCase("y")) {
				lexicase = true;
				chosen = true;
			}
			else if(s.equalsIgnoreCase("n")) chosen = true;
			else System.out.println("Invalid Response, please type y or n");
		}
		scanner.close();
		long startTime = System.currentTimeMillis();
		FibLearner f = new FibLearner();
		ArrayList<EquationTree> rootPool = new ArrayList<EquationTree>();
		for(int i=0; i<6; i++) {
			rootPool.add(new EquationTree(new EquationTree(), new EquationTree(), EquationTree.ADD));
		}
		for(int q=0; q<1; q++) {
			boolean foundAccurateEq = false;
			int generations = 0;
			while(!foundAccurateEq) {
				ArrayList<EquationTree> crossovers = new ArrayList<EquationTree>();
				for(int p=0; p<3; p++) {
					ArrayList<EquationTree> newCrossovers = new ArrayList<EquationTree>();
					int i1 = (int)(Math.random()*rootPool.size());
					int i2 = (int)(Math.random()*rootPool.size());
					newCrossovers = EquationTree.mutatedCrossovers(rootPool.get(i1), rootPool.get(i2),250);
					while(newCrossovers.size()>0) {
						crossovers.add(newCrossovers.get(0));
						newCrossovers.remove(0);
					}
				}
				for(int i=crossovers.size()-1; i>=0; i--) {
					if(f.qual(f.directAcc(crossovers.get(i)),f.percentDiff(crossovers.get(i)))>2) crossovers.remove(i);
				}
				if(lexicase) rootPool = f.lexicaseSelection(rootPool,crossovers);
				else rootPool = f.qualitySelection(rootPool, crossovers);
				generations++;
				System.out.println();
				System.out.println("Generation " + generations + ":");
				for(int i=0; i<rootPool.size(); i++) {
					double rootAcc = f.qual(f.directAcc(rootPool.get(i)),f.percentDiff(rootPool.get(i)));
					if(rootAcc<0.01) foundAccurateEq = true;
					System.out.println("Equation "+i+": "+rootPool.get(i).textFormula());
					System.out.println("Quality "+i+": "+rootAcc);
					System.out.println("Direct Acc "+i+": "+f.directAcc(rootPool.get(i)));
					System.out.println("Percent Acc "+i+": "+f.percentDiff(rootPool.get(i)));
				}
			}
		}
		System.out.println("Total Runtime: "+(System.currentTimeMillis()-startTime)/(1000.0*60*60)+" hours");
	}
	
}
