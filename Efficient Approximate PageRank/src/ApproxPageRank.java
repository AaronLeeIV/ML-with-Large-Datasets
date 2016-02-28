import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * class ApproxPageRank represents a sampler of a Graph by ApproxPageRank
 */
public class ApproxPageRank {
	private String inputPath;
	private String seed;
	private double alpha;
	private double epsilon;
	private Map<String, Double> p;
	private Map<String, Double> r;
	private Set<String> nodes;
	private Map<String, String[]> neighbors;

	/**
	 * constructor
	 */
	public ApproxPageRank(String inputPath, String seed, double alpha, double epsilon) {
		this.inputPath = inputPath;
		this.seed = seed;
		this.alpha = alpha;
		this.epsilon = epsilon;
		this.p = new HashMap<>();
		this.r = new HashMap<>();
		this.neighbors = new HashMap<>();
		this.nodes = new HashSet<>();
		initialize();
	}

	private void initialize() {
		p.put(seed, 0.0);
		r.put(seed, 1.0);
		nodes.add(seed);
		getAllNeighbors();
	}

	private void getAllNeighbors() {
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(inputPath)));
			String line;
			while (!nodes.isEmpty() && (line = br.readLine()) != null) {
				String node = line.substring(0, line.indexOf("\t"));
				if (node != "" && nodes.contains(node)) {
					String[] parts = line.split("\t");
					// note the node itself is in its adjacency list for performance reason
					neighbors.put(node, parts);
					nodes.remove(node);
				}
			}
			br.close();
			nodes.clear();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private double getPageRank(String node) {
		try {
			return p.get(node);
		} catch (NullPointerException e) {
			return 0.0;
		}
	}

	private double getResidual(String node) {
		try {
			return r.get(node);
		} catch (NullPointerException e) {
			return 0.0;
		}
	}

	private double computeBoundary(Set<String> S, String newNode) {
		int boundaryChange = 0;
		String[] nodeNeighbors = neighbors.get(newNode);
		for (String neighbor : nodeNeighbors) {
			if (!S.contains(neighbor)) {
				boundaryChange++;
			} else {
				boundaryChange--;
			}
		}
		return (double) boundaryChange;
	}

	private void pagerank() {
		boolean needUpdate = true;
		while (needUpdate) {
			needUpdate = !needUpdate;

			boolean needPush = true;
			while (needPush) {
				needPush = !needPush;

				for (Map.Entry<String, String[]> entry : neighbors.entrySet()) {
					String node = entry.getKey();
					String[] nodeNeighbors = entry.getValue();
					int d = nodeNeighbors.length - 1;
					if ((r.get(node) / d) < epsilon) {
						continue;
					} else {
						needPush = true;
					}
					double curPageRank = getPageRank(node);
					double curResidual = getResidual(node);

					// p'(u) = p(u) + alpha * r(u)
					p.put(node, curPageRank + alpha * curResidual);
					// r'(u) = (1 - alpha) * r(u) / 2
					double newResidual = (1.0 - alpha) * curResidual / 2.0;
					r.put(node, newResidual);

					for (int i = 1; i < nodeNeighbors.length; i++) {
						String neighbor = nodeNeighbors[i];
						// r'(v) = r(v) + (1 - alpha) * r(u) / (2 * d(u))
						//       = r(v) + r'(u) / d(u)
						double nbrResidual = getResidual(neighbor);
						r.put(neighbor, nbrResidual + newResidual / d);
					}
				}
			}
			
			// shouldn't put this update inside of push operation
			for (Map.Entry<String, Double> entry : r.entrySet()) {
				boolean exits = neighbors.containsKey(entry.getKey());
				if (!exits && entry.getValue() > epsilon) {
					nodes.add(entry.getKey());
				}
			}
			getAllNeighbors();

			for (Entry<String, String[]> entry : neighbors.entrySet()) {
				if ((r.get(entry.getKey()) / (entry.getValue().length - 1)) > epsilon) {
					needUpdate = true;
					break;
				}
			}
		}
		r.clear();
	}

	private void subSampling() {
		Set<String> S = new HashSet<>();
		List<String> S_ = new ArrayList<>();
		List<String> temp = new ArrayList<>();

		S.add(seed);
		S_.add(seed);

		double totalVolume = neighbors.get(seed).length - 1;
		double totalBoundary = computeBoundary(S, seed);
		double conductanceS = totalBoundary / totalVolume;
		double conductanceS_ = conductanceS;

		List<Map.Entry<String, Double>> orderedPR = new ArrayList<>(p.entrySet());
		Collections.sort(orderedPR, new Comparator<Map.Entry<String, Double>>() {
			@Override
			public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
				return Double.compare(o2.getValue(), o1.getValue());
			}
		});

		int size = orderedPR.size();
		for (int i = 0; i < size; i++) {
			String node = orderedPR.get(i).getKey();
			if (node.equals(seed)) {
				continue;
			}

			S.add(node);
			temp.add(node);
			totalVolume += neighbors.get(node).length - 1;
			totalBoundary += computeBoundary(S, node);
			conductanceS = totalBoundary / totalVolume;

			if (conductanceS < conductanceS_) {
				S_.addAll(temp);
				temp.clear();
				conductanceS_ = conductanceS;
			}
		}

		for (String node : S_) {
			System.out.println(node + "\t" + p.get(node).toString());
		}

		// graph2GDF(S_);
	}

//	private void graph2GDF(final Set<String> S_) {
//		Set<String> edgeSet = new HashSet<String>();
//		for (String node : S_) {
//			String[] nodeNeighbors = neighbors.get(node);
//			for (int i = 1; i < nodeNeighbors.length; i++) {
//				if (node != nodeNeighbors[i] && S_.contains(nodeNeighbors[i])) {
//					edgeSet.add(node + "," + nodeNeighbors[i]);
//				}
//			}
//		}
//		System.out.println("nodedef>name VARCHAR,width DOUBLE");
//		double temp;
//		for (String node : S_) {
//			temp = Math.log(p.get(node) / epsilon);
//			temp = temp < 1.0 ? 1.0 : temp;
//			System.out.println(node + "," + temp);
//		}
//
//		System.out.println("edgedef>node1 VARCHAR,node2 VARCHAR");
//		for (String edge : edgeSet) {
//			System.out.println(edge);
//		}
//	}

	public static void main(String[] args) throws IOException {
		ApproxPageRank myApr = new ApproxPageRank(args[0], args[1], Double.parseDouble(args[2]), Double.parseDouble(args[3]));
		myApr.pagerank();
		myApr.subSampling();
	}
}