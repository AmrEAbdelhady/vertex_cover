import static java.lang.Math.*;
import static java.util.Arrays.*;

import java.io.*;
import java.util.*;

import tc.wata.debug.*;
import tc.wata.io.*;
import tc.wata.util.*;
import tc.wata.util.SetOpt.*;

public class Main {
	
	@Option(abbr = 'r', usage = "0: deg1+dominance+fold2, 1:LP, 2:unconfined+twin+funnel+desk, 3:packing")
	public static int reduction = 3;
	
	@Option(abbr = 'l', usage = "0: nothing, 1:clique, 2:LP, 3:cycle, 4:all")
	public static int lb = 4;
	
	@Option(abbr = 'b', usage = "0:random, 1:mindeg, 2:maxdeg")
	public static int branching = 2;
	
	@Option(abbr = 'o')
	public static boolean outputLP = false;
	
	@Option(abbr = 'd')
	public static int debug = 0;
	
	int[][] read(String file) {
		if (file.endsWith(".dat")) {
			GraphIO io = new GraphIO();
			io.read(new File(file));
			return io.adj;
		} else {
			GraphConverter conv = new GraphConverter();
			conv.file = file;
			conv.type = "snap";
			conv.undirected = true;
			conv.sorting = true;
			try {
				conv.read();
			} catch (Exception e) {
				conv = new GraphConverter();
				conv.file = file;
				conv.type = "dimacs";
				conv.undirected = true;
				conv.sorting = true;
				try {
					conv.read();
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			}
			return conv.adj;
		}
	}
	
	void run(String file, String timeFrame, String output_file) {
		System.err.println("reading the input graph...");
		int[][] adj = read(file);
		if (debug > 0) Stat.setShutdownHook();
		int m = 0;
		for (int i = 0; i < adj.length; i++) m += adj[i].length;
		m /= 2;
		System.err.printf("n = %d, m = %d%n", adj.length, m);
		VCSolver vc = new VCSolver(adj, adj.length);
		VCSolver.nBranchings = 0;
		VCSolver.REDUCTION = reduction;
		VCSolver.LOWER_BOUND = lb;
		VCSolver.BRANCHING = branching;
		VCSolver.outputLP = outputLP;
		VCSolver.debug = debug;
		long start, end;
		Stat.init();
		try (Stat stat = new Stat("solve")) {
			start = System.currentTimeMillis();
			vc.start = System.currentTimeMillis();//to set start time for the to VCSolver
			vc.debug = 1;//To print out each reduction rule's performance
			vc.timeFrame = Long.parseLong(timeFrame);//setting time frame
			System.err.println(Long.parseLong(timeFrame));
			vc.solve(output_file);
			end = System.currentTimeMillis();
		}
		// String reductionResult = Stat.reductionsResult(file, vc.opt, end, start);//build reductions details
		// System.out.println(reductionResult);//print reductions details
		String totalTimeTemp = String.format("%.3f", 1e-3 * (end - start));
		System.out.print(vc.opt+","+totalTimeTemp+",");
		// String unconfinedResult = testUnconfinedReduction(end, start, file, VCSolver.totalUnconfined, VCSolver.confinedNeighbors, VCSolver.repeatUnconfined);
		//System.err.println(unconfinedResult);//print out percentage
		// String resultFromCmpUnconfiedSpeedAndSize = Stat.comprareSpeedAndReducedSizeUnconfined();
		// resultFromCmpUnconfiedSpeedAndSize += (" "+"VCsize: " + vc.opt);
		// System.err.println(resultFromCmpUnconfiedSpeedAndSize);
		adj = read(file);
		// System.out.println("unconfinedHelper reduced " + Stat.getCount("reduceN_deg1"));
        // Stat.stat();
        // double counter = vc.counter/1000.0;
        // System.out.println("time spent on restore() && return successfully is " + counter);
		int sum = 0;
		for (int i = 0; i < adj.length; i++) {
			sum += vc.y[i];
			Debug.check(vc.y[i] == 0 || vc.y[i] == 1);
			for (int j : adj[i]) Debug.check(vc.y[i] + vc.y[j] >= 1);
		}
		Debug.check(sum == vc.opt);
		if (debug > 0) {
			System.out.printf("%d\t%d\t%d\t%.3f\t%d%n", adj.length, m, vc.opt, 1e-3 * (end - start), VCSolver.nBranchings);
		}
		//reRun(file, timeFrame,vc.y,vc.opt);

	}
	
	void debug(Object...os) {
		System.err.println(deepToString(os));
	}

// 	void reRun(String file, String timeFrame,int[] y, int opt, String output_file) {
//         System.err.println("reading the input graph...");
// 		int[][] adj = read(file);
// 		if (debug > 0) Stat.setShutdownHook();
// 		int m = 0;
// 		for (int i = 0; i < adj.length; i++) m += adj[i].length;
// 		m /= 2;
// 		System.err.printf("n = %d, m = %d%n", adj.length, m);
// 		VCSolver vc = new VCSolver(adj, adj.length);
// 		VCSolver.nBranchings = 0;
// 		VCSolver.REDUCTION = reduction;
// 		VCSolver.LOWER_BOUND = lb;
// 		VCSolver.BRANCHING = branching;
// 		VCSolver.outputLP = outputLP;
// 		VCSolver.debug = debug;
// 		long start, end;
// 		Stat.init();
// 		try (Stat stat = new Stat("solve")) {
// 			start = System.currentTimeMillis();
//             vc.y = y;
//             vc.opt = opt;
// 			vc.start = System.currentTimeMillis();//to set start time for the to VCSolver
// 			vc.debug = 1;//To print out each reduction rule's performance
// 			vc.timeFrame = Long.parseLong(timeFrame);//setting time frame
// 			System.err.println(Long.parseLong(timeFrame));
// 			vc.solve(output_file);
// 			end = System.currentTimeMillis();
// 		}
// 		String reductionResult = Stat.reductionsResult(file, vc.opt, end, start);//build reductions details
// 		// System.out.println(reductionResult);//print reductions details
// 		String totalTimeTemp = String.format("%.3f", 1e-3 * (end - start));
// 		System.out.print(vc.opt+","+totalTimeTemp+",");
// 		// String unconfinedResult = testUnconfinedReduction(end, start, file, VCSolver.totalUnconfined, VCSolver.confinedNeighbors, VCSolver.repeatUnconfined);
// 		//System.err.println(unconfinedResult);//print out percentage
// 		String resultFromCmpUnconfiedSpeedAndSize = Stat.comprareSpeedAndReducedSizeUnconfined();
// 		resultFromCmpUnconfiedSpeedAndSize += (" "+"VCsize: " + vc.opt);
// 		// System.err.println(resultFromCmpUnconfiedSpeedAndSize);
// 		adj = read(file);
// //		System.out.println("unconfinedHelper reduced " + Stat.getCount("reduceN_deg1"));
// //                Stat.stat();
// //                double counterRestore = vc.counterRestore/1000.0;
// //                double counterLB = vc.counterLowerBound/1000.0;
// //                double counterDec = vc.counterDecomp/1000.0;
// //                System.out.println("time spent on restore() " + counterRestore);
// //                System.out.println("time spent on lowerbound() " + counterLB);
// //                System.out.println("time spent on decompose() " + counterDec);
// 		int sum = 0;
// 		for (int i = 0; i < adj.length; i++) {
// 			sum += vc.y[i];
// 			Debug.check(vc.y[i] == 0 || vc.y[i] == 1);
// 			for (int j : adj[i]) Debug.check(vc.y[i] + vc.y[j] >= 1);
// 		}
// 		Debug.check(sum == vc.opt);
// 		if (debug > 0) {
// 			System.out.printf("%d\t%d\t%d\t%.3f\t%d%n", adj.length, m, vc.opt, 1e-3 * (end - start), VCSolver.nBranchings);
// 		}
//         }
	// public static String testUnconfinedReduction(long end, long start, String file, long totalUnconfined, long confinedNeighbors, long repeatUnconfined) {
		// StringBuilder sb = new StringBuilder();
		// String totalTimeTemp = String.format("%.3f", 1e-3 * (end - start));
		// sb.append(file + ",");
		// sb.append(totalTimeTemp + ",");
		// sb.append((long)totalUnconfined + ",");
		// sb.append((long)confinedNeighbors + ",");
		// sb.append((long)repeatUnconfined + ",");
		// sb.append(100*(double)VCSolver.confinedNeighbors/(double)VCSolver.totalUnconfined + ",");
		// sb.append(100*(double)VCSolver.repeatUnconfined/(double)VCSolver.totalUnconfined);
		// return sb.toString();
	// }
	
	public static void main(String[] args) {
		Main main = new Main();
		args = SetOpt.setOpt(main, args);
		main.run(args[0],args[1], args[2]);
	}
}
