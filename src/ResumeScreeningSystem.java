import java.nio.file.*;
import java.util.*;

public class ResumeScreeningSystem {
    public static void main(String[] args) {
        Path dataPath = Paths.get("data", "resumes.txt");
        ResumeRepository repository = new ResumeRepository();
        try { repository.load(dataPath); }
        catch (Exception e) {
            System.out.println("Could not load " + dataPath.toAbsolutePath());
            System.out.println("Run this program from the main project folder.");
            return;
        }
        ScreeningService service = new ScreeningService(repository);
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n╭──────────────────────────────────────────────╮");
        System.out.println("│          TALENTLENS SCREENING ENGINE         │");
        System.out.println("╰──────────────────────────────────────────────╯");
        System.out.println("Loaded " + repository.all().size() + " synthetic resume profiles.");
        while (true) {
            System.out.println("\n1. Search and rank candidates");
            System.out.println("2. Browse resume database");
            System.out.println("3. View skill index");
            System.out.println("4. Show scoring model");
            System.out.println("5. Advanced DSA demonstrations (CO2-CO6)");
            System.out.println("6. Exit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    System.out.print("Required skills (comma-separated): ");
                    String skills = scanner.nextLine();
                    System.out.print("Minimum experience (0-12): ");
                    int exp = parseInt(scanner.nextLine(), 0);
                    System.out.print("Preferred location (city or Any): ");
                    String location = scanner.nextLine();
                    List<RankedCandidate> results = service.search(skills, exp, location);
                    System.out.println("\nRanked results: " + results.size());
                    int count = Math.min(20, results.size());
                    for (int i=0;i<count;i++) {
                        RankedCandidate r=results.get(i); Candidate c=r.getCandidate();
                        System.out.printf("%2d. %-24s | Fit %5.1f | %2d yrs | %-15s | Skills %d/%d%n",
                            i+1,c.getName(),r.getScore(),c.getExperience(),c.getLocation(),r.getMatchedSkills(),r.getTotalRequired());
                    }
                    if (results.isEmpty()) System.out.println("No candidates matched. Try fewer skills or location Any.");
                    break;
                case "2":
                    for (int i=0;i<Math.min(30,repository.all().size());i++) {
                        Candidate c=repository.all().get(i);
                        System.out.printf("%4d | %-24s | %2d yrs | %-15s | %s%n",
                            c.getId(),c.getName(),c.getExperience(),c.getLocation(),String.join(", ",c.getSkills()));
                    }
                    System.out.println("(Showing first 30 profiles.)");
                    break;
                case "3":
                    repository.skillIndex().entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .forEach(e -> System.out.printf("%-20s %4d candidates%n",e.getKey(),e.getValue().size()));
                    break;
                case "4":
                    System.out.println("Skills match: 50% | Experience: 30% | Location: 20%");
                    System.out.println("Experience score scales from 0 to 12 years; location is a preferred-city match.");
                    break;
                case "5":
                    System.out.println("\\n--- ADVANCED DSA DEMONSTRATIONS ---");
                    System.out.println("CO2 Z-function for 'abacaba': " + Arrays.toString(AdvancedAlgorithms.zFunction("abacaba")));
                    System.out.println("CO2 Rabin-Karp matches of 'lens' in 'talentlens lens': " + AdvancedAlgorithms.rabinKarp("talentlens lens", "lens"));
                    System.out.println("CO2 suffix array for 'banana': " + Arrays.toString(AdvancedAlgorithms.suffixArray("banana")));
                    System.out.println("CO3 LCS length ('resume','assume'): " + AdvancedAlgorithms.lcsLength("resume", "assume"));
                    System.out.println("CO3 Matrix-chain minimum cost for [10,30,5,60]: " + AdvancedAlgorithms.matrixChainCost(new int[]{10,30,5,60}));
                    System.out.println("CO3 bitmask TSP sample tour cost: " + AdvancedAlgorithms.tspBitmask(new int[][]{{0,10,15,20},{10,0,35,25},{15,35,0,30},{20,25,30,0}}));
                    System.out.println("CO4 Edmonds-Karp max flow sample: " + AdvancedAlgorithms.edmondsKarp(new int[][]{{0,3,2,0},{0,0,1,2},{0,0,0,3},{0,0,0,0}},0,3));
                    System.out.println("CO5 approximate vertex cover sample: " + AdvancedAlgorithms.approximateVertexCover(new int[][]{{0,1},{1,2},{2,3},{3,0}}));
                    int[] sample = {9,2,7,1,5,3}; AdvancedAlgorithms.randomizedQuickSort(sample, new Random(7));
                    System.out.println("CO6 randomized QuickSort sample: " + Arrays.toString(sample));
                    System.out.println("CO6 parallel prefix-sum sample: " + Arrays.toString(AdvancedAlgorithms.parallelPrefixSum(new int[]{1,2,3,4})));
                    System.out.println("CO5 theory: " + AdvancedAlgorithms.complexityNote());
                    break;
                case "6": System.out.println("Session ended. Good luck with your screening."); return;
                default: System.out.println("Please choose 1-6.");
            }
        }
    }
    private static int parseInt(String value, int fallback) {
        try { return Integer.parseInt(value.trim()); } catch (Exception e) { return fallback; }
    }
}
