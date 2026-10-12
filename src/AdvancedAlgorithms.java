import java.util.*;

/**
 * Advanced DSA demonstrations mapped to the supplied advanced DSA handout.
 * These are reusable algorithm implementations, separate from UI and I/O.
 */
public final class AdvancedAlgorithms {
    private AdvancedAlgorithms() {}

    // CO2: Z-function; returns Z[i] = length of the longest substring starting at i
    // that is also a prefix of s.
    public static int[] zFunction(String s) {
        int n = s.length();
        int[] z = new int[n];
        for (int i = 1, l = 0, r = 0; i < n; i++) {
            if (i <= r) z[i] = Math.min(r - i + 1, z[i - l]);
            while (i + z[i] < n && s.charAt(z[i]) == s.charAt(i + z[i])) z[i]++;
            if (i + z[i] - 1 > r) { l = i; r = i + z[i] - 1; }
        }
        if (n > 0) z[0] = n;
        return z;
    }

    // CO2: Rabin-Karp rolling hash for exact pattern search.
    public static List<Integer> rabinKarp(String text, String pattern) {
        List<Integer> hits = new ArrayList<>();
        int n = text.length(), m = pattern.length();
        if (m == 0) { for (int i = 0; i <= n; i++) hits.add(i); return hits; }
        if (m > n) return hits;
        long mod = 1_000_000_007L, base = 257L, power = 1, ph = 0, th = 0;
        for (int i = 0; i < m; i++) {
            ph = (ph * base + pattern.charAt(i)) % mod;
            th = (th * base + text.charAt(i)) % mod;
            if (i < m - 1) power = (power * base) % mod;
        }
        for (int i = 0; i <= n - m; i++) {
            if (ph == th && text.regionMatches(i, pattern, 0, m)) hits.add(i);
            if (i < n - m) {
                th = (th - text.charAt(i) * power % mod + mod) % mod;
                th = (th * base + text.charAt(i + m)) % mod;
            }
        }
        return hits;
    }

    // CO2: suffix array built by sorting suffix start positions (clear, compact baseline).
    public static int[] suffixArray(String s) {
        Integer[] indexes = new Integer[s.length()];
        for (int i = 0; i < indexes.length; i++) indexes[i] = i;
        Arrays.sort(indexes, Comparator.comparing(s::substring));
        int[] result = new int[indexes.length];
        for (int i = 0; i < indexes.length; i++) result[i] = indexes[i];
        return result;
    }

    // CO3: Longest Common Subsequence, tabulation DP.
    public static int lcsLength(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 1; i <= a.length(); i++)
            for (int j = 1; j <= b.length(); j++)
                dp[i][j] = a.charAt(i - 1) == b.charAt(j - 1)
                    ? dp[i - 1][j - 1] + 1 : Math.max(dp[i - 1][j], dp[i][j - 1]);
        return dp[a.length()][b.length()];
    }

    // CO3: Matrix Chain Multiplication minimum scalar multiplication count.
    public static long matrixChainCost(int[] dims) {
        int n = dims.length - 1;
        if (n <= 0) return 0;
        long[][] dp = new long[n][n];
        for (int len = 2; len <= n; len++)
            for (int i = 0; i + len <= n; i++) {
                int j = i + len - 1;
                dp[i][j] = Long.MAX_VALUE;
                for (int k = i; k < j; k++) {
                    long cost = dp[i][k] + dp[k + 1][j] + (long)dims[i] * dims[k + 1] * dims[j + 1];
                    dp[i][j] = Math.min(dp[i][j], cost);
                }
            }
        return dp[0][n - 1];
    }

    // CO3: Bitmask DP for TSP. dist must be a square matrix with n <= 20.
    public static long tspBitmask(int[][] dist) {
        int n = dist.length;
        if (n == 0) return 0;
        if (n > 20) throw new IllegalArgumentException("Bitmask TSP demo supports at most 20 cities.");
        int states = 1 << n;
        long inf = Long.MAX_VALUE / 4;
        long[][] dp = new long[states][n];
        for (long[] row : dp) Arrays.fill(row, inf);
        dp[1][0] = 0;
        for (int mask = 1; mask < states; mask++) {
            if ((mask & 1) == 0) continue;
            for (int u = 0; u < n; u++) if (dp[mask][u] < inf) {
                for (int v = 0; v < n; v++) if ((mask & (1 << v)) == 0) {
                    int next = mask | (1 << v);
                    dp[next][v] = Math.min(dp[next][v], dp[mask][u] + dist[u][v]);
                }
            }
        }
        long best = inf;
        for (int u = 1; u < n; u++) best = Math.min(best, dp[states - 1][u] + dist[u][0]);
        return n == 1 ? 0 : best;
    }

    // CO4: Edmonds-Karp maximum flow; capacity matrix is not modified by caller.
    public static int edmondsKarp(int[][] capacity, int source, int sink) {
        int n = capacity.length;
        int[][] residual = new int[n][n];
        for (int i = 0; i < n; i++) residual[i] = Arrays.copyOf(capacity[i], n);
        int maxFlow = 0;
        while (true) {
            int[] parent = new int[n]; Arrays.fill(parent, -1); parent[source] = source;
            Queue<Integer> q = new ArrayDeque<>(); q.add(source);
            while (!q.isEmpty() && parent[sink] == -1) {
                int u = q.remove();
                for (int v = 0; v < n; v++) if (parent[v] == -1 && residual[u][v] > 0) {
                    parent[v] = u; q.add(v);
                }
            }
            if (parent[sink] == -1) break;
            int flow = Integer.MAX_VALUE;
            for (int v = sink; v != source; v = parent[v]) flow = Math.min(flow, residual[parent[v]][v]);
            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v]; residual[u][v] -= flow; residual[v][u] += flow;
            }
            maxFlow += flow;
        }
        return maxFlow;
    }

    // CO5: 2-approximation for Vertex Cover on an undirected graph's edge list.
    // Each edge is {u,v}; chooses both endpoints of every uncovered edge.
    public static Set<Integer> approximateVertexCover(int[][] edges) {
        Set<Integer> cover = new LinkedHashSet<>();
        for (int[] edge : edges) {
            int u = edge[0], v = edge[1];
            if (!cover.contains(u) && !cover.contains(v)) {
                cover.add(u); cover.add(v);
            }
        }
        return cover;
    }

    // CO6: randomized QuickSort.
    public static void randomizedQuickSort(int[] values, Random random) {
        quickSort(values, 0, values.length - 1, random);
    }
    private static void quickSort(int[] a, int low, int high, Random random) {
        if (low >= high) return;
        int pivotIndex = low + random.nextInt(high - low + 1);
        int pivot = a[pivotIndex];
        int i = low, j = high;
        while (i <= j) {
            while (a[i] < pivot) i++;
            while (a[j] > pivot) j--;
            if (i <= j) { int t = a[i]; a[i] = a[j]; a[j] = t; i++; j--; }
        }
        if (low < j) quickSort(a, low, j, random);
        if (i < high) quickSort(a, i, high, random);
    }

    // CO6: parallel prefix sum using Java parallel streams for a simple demo.
    // Output is inclusive prefix sums; parallelizes independent index calculations.
    public static int[] parallelPrefixSum(int[] values) {
        int[] out = new int[values.length];
        java.util.stream.IntStream.range(0, values.length).parallel().forEach(i -> {
            int sum = 0;
            for (int j = 0; j <= i; j++) sum += values[j];
            out[i] = sum;
        });
        return out;
    }

    // CO5: concise theory helper for viva/presentation.
    public static String complexityNote() {
        return "P: solvable in polynomial time; NP: solutions verifiable in polynomial time; " +
               "NP-complete: in NP and NP-hard; NP-hard: at least as hard as every problem in NP. " +
               "Reductions establish hardness; approximation algorithms trade exact optimality for guarantees.";
    }
}
