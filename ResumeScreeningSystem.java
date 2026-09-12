import java.io.*;
import java.util.*;

public class ResumeScreeningSystem {

    // Candidate represents one resume in the corpus.
    static class Candidate {
        int id;
        String name;
        String email;
        String education;
        int experience;
        Set<String> skills;
        String summary;
        double score;
        int matchedSkills;

        Candidate(int id, String name, String email, String education,
                  int experience, Set<String> skills, String summary) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.education = education;
            this.experience = experience;
            this.skills = skills;
            this.summary = summary;
        }
    }

    // ArrayList stores all resumes loaded from the corpus.
    static List<Candidate> corpus = new ArrayList<>();

    // Inverted index: skill -> candidate IDs.
    // This is used to locate candidates who contain a requested skill.
    static Map<String, Set<Integer>> invertedIndex = new HashMap<>();

    static String normalize(String text) {
        return text.toLowerCase().trim().replaceAll("\\s+", " ");
    }

    static Set<String> parseSkills(String skillText) {
        Set<String> result = new HashSet<>();

        for (String skill : skillText.split(";")) {
            String cleaned = normalize(skill);
            if (!cleaned.isEmpty()) {
                result.add(cleaned);
            }
        }

        return result;
    }

    // Loads the resume corpus from resumes.csv.
    static void loadCorpus(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            br.readLine(); // Skip CSV header.

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);

                if (parts.length >= 7) {
                    int id = Integer.parseInt(parts[0].trim());
                    String name = parts[1].trim();
                    String email = parts[2].trim();
                    String education = parts[3].trim();
                    int experience = Integer.parseInt(parts[4].trim());
                    Set<String> skills = parseSkills(parts[5]);
                    String summary = parts[6].trim();

                    corpus.add(new Candidate(
                        id, name, email, education,
                        experience, skills, summary
                    ));
                }
            }

            buildInvertedIndex();

        } catch (IOException e) {
            System.out.println("Error reading corpus: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Invalid numeric value in corpus.");
        }
    }

    // Creates a reverse lookup structure for faster skill-based searching.
    static void buildInvertedIndex() {
        invertedIndex.clear();

        for (Candidate candidate : corpus) {
            for (String skill : candidate.skills) {
                invertedIndex
                    .computeIfAbsent(skill, k -> new HashSet<>())
                    .add(candidate.id);
            }
        }
    }

    static Candidate getCandidateById(int id) {
        for (Candidate candidate : corpus) {
            if (candidate.id == id) {
                return candidate;
            }
        }
        return null;
    }

    // Calculates relevance using required skill matches.
    // Experience adds a small bonus when requested.
    static void calculateScores(List<Candidate> candidates,
                                Set<String> requiredSkills,
                                int minimumExperience) {

        for (Candidate candidate : candidates) {
            int matched = 0;

            for (String required : requiredSkills) {
                if (candidate.skills.contains(required)) {
                    matched++;
                }
            }

            candidate.matchedSkills = matched;

            double skillScore = requiredSkills.isEmpty()
                    ? 0
                    : (matched * 100.0) / requiredSkills.size();

            double experienceBonus = 0;

            if (minimumExperience > 0 && candidate.experience >= minimumExperience) {
                experienceBonus = 10;
            }

            candidate.score = Math.min(100, skillScore * 0.9 + experienceBonus);
        }
    }

    // Sorts candidates by score, then by experience.
    static void rankCandidates(List<Candidate> candidates) {
        candidates.sort((a, b) -> {
            int scoreCompare = Double.compare(b.score, a.score);

            if (scoreCompare != 0) {
                return scoreCompare;
            }

            return Integer.compare(b.experience, a.experience);
        });
    }

    static Set<Integer> searchCandidates(Set<String> requiredSkills) {
        Set<Integer> candidateIds = new HashSet<>();

        // Union of candidates containing at least one required skill.
        for (String skill : requiredSkills) {
            Set<Integer> ids = invertedIndex.get(skill);

            if (ids != null) {
                candidateIds.addAll(ids);
            }
        }

        return candidateIds;
    }

    static void printCandidate(Candidate candidate, int rank) {
        System.out.printf(
            "%d. %-20s Score: %6.2f%% | Skills: %d | Experience: %d years%n",
            rank,
            candidate.name,
            candidate.score,
            candidate.matchedSkills,
            candidate.experience
        );
        System.out.println("   Education : " + candidate.education);
        System.out.println("   Email     : " + candidate.email);
        System.out.println("   Skills    : " + String.join(", ", candidate.skills));
        System.out.println("   Summary   : " + candidate.summary);
        System.out.println();
    }

    static void performSearch(Scanner scanner) {
        System.out.println("\n----------------------------------------");
        System.out.println("         CANDIDATE SEARCH");
        System.out.println("----------------------------------------");

        System.out.print("Enter required skills separated by commas: ");
        String input = scanner.nextLine();

        Set<String> requiredSkills = new LinkedHashSet<>();

        for (String skill : input.split(",")) {
            String cleaned = normalize(skill);

            if (!cleaned.isEmpty()) {
                requiredSkills.add(cleaned);
            }
        }

        if (requiredSkills.isEmpty()) {
            System.out.println("Please enter at least one skill.");
            return;
        }

        System.out.print("Minimum experience in years (enter 0 to ignore): ");
        int minimumExperience;

        try {
            minimumExperience = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid experience value.");
            return;
        }

        Set<Integer> matchingIds = searchCandidates(requiredSkills);
        List<Candidate> results = new ArrayList<>();

        for (Integer id : matchingIds) {
            Candidate candidate = getCandidateById(id);

            if (candidate != null) {
                results.add(candidate);
            }
        }

        calculateScores(results, requiredSkills, minimumExperience);
        rankCandidates(results);

        System.out.println("\n========================================");
        System.out.println("             SEARCH RESULTS");
        System.out.println("========================================");
        System.out.println("Required skills: " + String.join(", ", requiredSkills));
        System.out.println("Minimum experience: " + minimumExperience + " years");
        System.out.println("Candidates considered: " + results.size());
        System.out.println();

        int rank = 1;

        for (Candidate candidate : results) {
            if (candidate.score > 0) {
                printCandidate(candidate, rank);
                rank++;
            }
        }

        if (rank == 1) {
            System.out.println("No matching candidates found.");
        }
    }

    static void showAllCandidates() {
        System.out.println("\n========================================");
        System.out.println("           RESUME CORPUS");
        System.out.println("========================================");

        for (Candidate candidate : corpus) {
            System.out.println(
                candidate.id + " | " + candidate.name +
                " | " + candidate.experience + " years | " +
                String.join(", ", candidate.skills)
            );
        }
    }

    static void showSkillIndex() {
        System.out.println("\n========================================");
        System.out.println("          SKILL INDEX");
        System.out.println("========================================");

        List<String> skills = new ArrayList<>(invertedIndex.keySet());
        Collections.sort(skills);

        for (String skill : skills) {
            System.out.println(
                skill + " -> " +
                invertedIndex.get(skill).size() +
                " candidate(s)"
            );
        }
    }

    static void printMenu() {
        System.out.println("\n========================================");
        System.out.println("  RESUME SEARCH & CANDIDATE SCREENING");
        System.out.println("========================================");
        System.out.println("1. Search and rank candidates");
        System.out.println("2. Display resume corpus");
        System.out.println("3. Display skill index");
        System.out.println("4. Exit");
        System.out.println("----------------------------------------");
        System.out.print("Enter your choice: ");
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Loading resume corpus...");
        loadCorpus("resumes.csv");

        if (corpus.isEmpty()) {
            System.out.println("No resumes loaded.");
            System.out.println("Place resumes.csv in the same folder as the Java file.");
            scanner.close();
            return;
        }

        System.out.println("Corpus loaded successfully.");
        System.out.println("Total resumes: " + corpus.size());
        System.out.println("Unique indexed skills: " + invertedIndex.size());

        while (true) {
            printMenu();
            String choice = scanner.nextLine();

            if (choice.equals("1")) {
                performSearch(scanner);
            } else if (choice.equals("2")) {
                showAllCandidates();
            } else if (choice.equals("3")) {
                showSkillIndex();
            } else if (choice.equals("4")) {
                System.out.println("\nThank you for using the system.");
                break;
            } else {
                System.out.println("Invalid choice. Please select 1-4.");
            }
        }

        scanner.close();
    }
}
