package fibLearning;

import java.util.ArrayList;

public class EquationTree {
	
	//private instance vars
	private EquationTree eq1;
	private EquationTree eq2;
	private EquationTree parent;
	private boolean isOp;
	private boolean isVar;
	private double val;
	private int op;
	
	//shared constants
	public final static int ADD = 0;
	public final static int SUB = 1;
	public final static int DIV = 2;
	public final static int MULT = 3;
	public final static int EXP = 4;
	public final static int OP_NUMS = 5;
	
	//basic constructors for the tree below
	
	public EquationTree() {
		eq1 = null;
		eq2 = null;
		parent = null;
		isOp = false;
		isVar = false;
		op = 0;
		val = 0;
	}
	
	public EquationTree(EquationTree e1, EquationTree e2, int o) {
		eq1 = new EquationTree();
		eq2 = new EquationTree();
		parent = null;
		setEq1(e1);
		setEq2(e2);
		isOp = true;
		isVar = false;
		op = o;
	}
	
	public EquationTree(double n) {
		eq1 = null;
		eq2 = null;
		parent = null;
		isOp = false;
		isVar = false;
		val = n;
	}
	
	public EquationTree(String n) {
		eq1 = null;
		eq2 = null;
		parent = null;
		isOp = false;
		isVar = true;
	}
	
	//basic functions for the tree below
	
	public void setEq1(EquationTree e) {
		eq1 = e;
		eq1.setParent(this);
	}
	
	public void setEq2(EquationTree e) {
		eq2 = e;
		eq2.setParent(this);
	}
	
	public boolean isFirstChild(EquationTree e) {
		if(eq1==e) return true;
		return false;
	}
	
	public void setVal(double v) {
		val = v;
	}
	
	public void setVar() {
		isVar = true;
	}
	
	public void setOp(int o) {
		op = o;
	}
	
	public void setParent(EquationTree e) {
		parent = e;
	}
	
	public boolean isVal() {
		return (!isOp && !isVar);
	}
	
	public boolean isVar() {
		return isVar;
	}
	
	//prints out the equation in a readable format
	public String textFormula() {
		String s = "";
		if(!isOp && isVar) s+="n";
		else if(!isOp && !isVar) s+=val;
		else {
			s+="(";
			s+=eq1.textFormula();
			if(op==ADD) s+="+";
			else if(op==SUB) s+="-";
			else if(op==DIV) s+="/";
			else if(op==MULT) s+="*";
			else if(op==EXP) s+="^";
			s+=eq2.textFormula();
			s+=")";
		}
		return s;
	}
	
	//recursively duplicates tree from starting root
	public EquationTree duplication() {
		EquationTree e;
		if(!isOp) {
			e = new EquationTree();
			e.setVal(val);
			if(isVar) e.setVar();
			return e;
		}
		e = new EquationTree(new EquationTree(), new EquationTree(), op);
		e.setEq1(eq1.duplication());
		e.setEq2(eq2.duplication());
		return e;
	}
	
	//returns recursive value of tree from starting root
	public double evaluateEquation(int n) {
		if(isOp) {
			if(op == ADD) return eq1.evaluateEquation(n)+eq2.evaluateEquation(n);
			else if(op == SUB) return eq1.evaluateEquation(n)-eq2.evaluateEquation(n);
			else if(op == DIV) return eq1.evaluateEquation(n)/eq2.evaluateEquation(n);
			else if(op == MULT) return eq1.evaluateEquation(n)*eq2.evaluateEquation(n);
			else if(op == EXP) return Math.pow(eq1.evaluateEquation(n), eq2.evaluateEquation(n));
		}
		if(isVar) {
			return 0.0+n;
		}
		return val;
	}
	
	//changes random child to an operator, maintains continuity
	public void addRandomOp() {
		EquationTree temp;
		boolean r = false;
		if(Math.random()<0.5) r = true;
		if(r) temp = eq1;
		else temp = eq2;
		if(r) {
			if(Math.random()<0.5) setEq1(new EquationTree(temp,randomNumNode(),(int)(Math.random()*OP_NUMS)));
			else setEq1(new EquationTree(randomNumNode(),temp,(int)(Math.random()*OP_NUMS)));
		}
		else {
			if(Math.random()<0.5) setEq2(new EquationTree(temp,randomNumNode(),(int)(Math.random()*OP_NUMS)));
			else setEq2(new EquationTree(randomNumNode(),temp,(int)(Math.random()*OP_NUMS)));
		}
	}
	
	//basic operations for simplifying an equation
	public void simplify() {
		if(!isOp) return;
		eq1.simplify();
		eq2.simplify();
		if(parent!=null && eq1.isVal() && eq2.isVal()) {
			val = evaluateEquation(0);
			isOp=false;
			eq1=null;
			eq2=null;
		} else if(parent!=null && eq1.isVar() && eq2.isVar()) {
			if(op==SUB) {
				val = 0;
				isOp=false;
				eq1=null;
				eq2=null;
			} else if(op==ADD) {
				op=MULT;
				setEq1(new EquationTree(2.0));
			} else if(op==DIV) {
				val = 1;
				isOp=false;
				eq1=null;
				eq2=null;
			} else if(op==MULT) {
				op=EXP;
				setEq2(new EquationTree(2.0));
			}
		}
	}
	
	//replaces itself with one of its children, randomly
	public void delete() {
		EquationTree surv;
		if(Math.random()<0.5) surv = eq1;
		else surv = eq2;
		if(parent!=null && parent.isFirstChild(this)) parent.setEq1(surv);
		else if (parent!=null) parent.setEq2(surv);
	}
	
	//generates an end node with a random number between 0 and 2, or random part of tree
	public EquationTree randomNumNode() {
		if(Math.random()<0.5) {
			EquationTree e = new EquationTree();
			if(Math.random()<0.2) isVar = true;
			else e.setVal(2*Math.random()*Math.random());
			return e;
		} else {
			return randomNearNode();
		}
		
	}
	
	//takes a random walk to a nearby node
	public EquationTree randomNearNode() {
		if(Math.random()<0.2 || (parent==null && eq1==null && eq2==null)) return this.duplication();
		else if(parent==null) {
			if(Math.random()<0.5) return eq1.randomNearNode();
			else return eq2.randomNearNode();
		}
		else if(eq1==null && eq2==null) return parent.randomNearNode();
		else {
			if(Math.random()<0.33) return parent.randomNearNode();
			else if(Math.random()<0.5)return eq1.randomNearNode();
			else return eq2.randomNearNode();
		}
	}
	
	//used for crossovers, returns actual node instead of duplication
	public EquationTree actualRandomNearNode() {
		if(Math.random()<0.2 || (parent==null && eq1==null && eq2==null)) return this;
		else if(parent==null) {
			if(Math.random()<0.5) return eq1.randomNearNode();
			else return eq2.randomNearNode();
		}
		else if(eq1==null && eq2==null) return parent.randomNearNode();
		else {
			if(Math.random()<0.33) return parent.randomNearNode();
			else if(Math.random()<0.5)return eq1.randomNearNode();
			else return eq2.randomNearNode();
		}
	}
	
	//randomly modifying tree recursively from starting root
	public void randomlyModify() {
		if(!isOp) {
			if(!isVar && Math.random()>0.5) val+=0.1*(Math.random()-0.5);
			else if(!isVar) {
				isVar = true;
			}
		}
		else if(Math.random()<0.05) op = (int) Math.random()*OP_NUMS;
		else if(Math.random()<0.05) addRandomOp();
		else if(Math.random()<0.05) delete();
		if(eq1!=null && eq2!=null) {
			eq1.randomlyModify();
			eq2.randomlyModify();
		}
	}
	
	//generates a set of randomly changed offspring from two equations
	public static ArrayList<EquationTree> mutatedCrossovers(EquationTree e1, EquationTree e2, int n) {
		ArrayList<EquationTree> eqList = new ArrayList<EquationTree>();
		for(int i=0; i<n; i++) {
			EquationTree dupe1 = e1.duplication();
			EquationTree dupe2 = e2.duplication();
			EquationTree cross1 = e1.duplication();
			EquationTree cross2 = e2.duplication();
			EquationTree targetNode1 = e1.actualRandomNearNode();
			EquationTree targetNode2 = e2.actualRandomNearNode();
			EquationTree temp = targetNode1.duplication();
			targetNode1 = targetNode2;
			targetNode2 = temp;
			int mods = 10;
			if(Math.random()<0.5) mods = 1;
			for(int q=0; q<mods; q++) {
				cross1.randomlyModify();
				cross2.randomlyModify();
				dupe1.randomlyModify();
				dupe2.randomlyModify();
			}
			cross1.simplify();
			cross2.simplify();
			dupe1.simplify();
			dupe2.simplify();
			eqList.add(cross1);
			eqList.add(cross2);
			eqList.add(dupe1);
			eqList.add(dupe2);
		}
		return eqList;
	}
	
}