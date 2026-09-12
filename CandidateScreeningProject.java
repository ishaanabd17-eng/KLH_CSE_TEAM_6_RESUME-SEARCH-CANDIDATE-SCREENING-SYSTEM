import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class CandidateScreeningProject {

    /*
     * DSA-3 Candidate Screening Project
     *
     * Corpus:
     *   The corpus is the collection of structured .txt resume files
     *   stored inside the "corpus" folder.
     *
     * String algorithms implemented manually:
     *   1. Naive Pattern Matching
     *   2. KMP
     *   3. Z-Function
     *   4. Rabin-Karp with rolling hash
     *
     * No Java built-in substring search is used by the four algorithms.
     */

    static class Resume {
        String fileName;
        String id;
        String name;
        String email;
        String education;
        String experience;
        String skills;
        String summary;
        String fullText;

        Resume(String fileName, String fullText) {
            this.fileName = fileName;
            this.fullText = fullText;
            this.id = getField(fullText, "RESUME ID:");
            this.name = getField(fullText, "NAME:");
            this.email = getField(fullText, "EMAIL:");
            this.education = getField(fullText, "EDUCATION:");
            this.experience = getField(fullText, "EXPERIENCE:");
            this.skills = getField(fullText, "SKILLS:");
            this.summary = getField(fullText, "SUMMARY:");
        }
    }

    static Resume[] corpus = new Resume[100];
    static int corpusSize = 0;

    static String getField(String text, String label) {
        String[] lines = text.split("\\r?\\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            if (line.startsWith(label)) {
                return line.substring(label.length()).trim();
            }
        }

        return "Not specified";
    }

    static void addResume(Resume resume) {
        if (corpusSize == corpus.length) {
            Resume[] bigger = new Resume[corpus.length * 2];

            for (int i = 0; i < corpus.length; i++) {
                bigger[i] = corpus[i];
            }

            corpus = bigger;
        }

        corpus[corpusSize] = resume;
        corpusSize++;
    }

    static String readFile(String fileName) {
        StringBuilder text = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line).append('\n');
            }
        } catch (IOException e) {
            System.out.println("Could not read " + fileName + ": " + e.getMessage());
            return "";
        }

        return text.toString();
    }

    static void loadCorpus(String folderName) {
        File folder = new File(folderName);

        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Corpus folder not found: " + folderName);
            return;
        }

        File[] files = folder.listFiles();

        if (files == null) {
            return;
        }

        for (int i = 0; i < files.length; i++) {
            File file = files[i];

            if (file.isFile() && file.getName().toLowerCase().endsWith(".txt")) {
                String text = readFile(file.getPath());

                if (!text.isEmpty()) {
                    addResume(new Resume(file.getName(), text));
                }
            }
        }
    }

    static String normalize(String text) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char ch = Character.toLowerCase(text.charAt(i));

            if (Character.isWhitespace(ch)) {
                if (result.length() == 0 || result.charAt(result.length() - 1) != ' ') {
                    result.append(' ');
                }
            } else {
                result.append(ch);
            }
        }

        return result.toString().trim();
    }

    // ------------------------------------------------------------
    // 1. NAIVE PATTERN MATCHING
    // ------------------------------------------------------------

    static int[] naiveSearch(String text, String pattern) {
        text = normalize(text);
        pattern = normalize(pattern);

        int n = text.length();
        int m = pattern.length();

        if (m == 0 || m > n) {
            return new int[0];
        }

        int[] positions = new int[n];
        int count = 0;

        for (int i = 0; i <= n - m; i++) {
            int j = 0;

            while (j < m && text.charAt(i + j) == pattern.charAt(j)) {
                j++;
            }

            if (j == m) {
                positions[count] = i;
                count++;
            }
        }

        return copyPositions(positions, count);
    }

    // ------------------------------------------------------------
    // 2. KMP
    // ------------------------------------------------------------

    static int[] buildLPS(String pattern) {
        int m = pattern.length();
        int[] lps = new int[m];

        int len = 0;
        int i = 1;

        while (i < m) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else if (len > 0) {
                len = lps[len - 1];
            } else {
                lps[i] = 0;
                i++;
            }
        }

        return lps;
    }

    static int[] kmpSearch(String text, String pattern) {
        text = normalize(text);
        pattern = normalize(pattern);

        int n = text.length();
        int m = pattern.length();

        if (m == 0 || m > n) {
            return new int[0];
        }

        int[] lps = buildLPS(pattern);
        int[] positions = new int[n];
        int count = 0;

        int i = 0;
        int j = 0;

        while (i < n) {
            if (text.charAt(i) == pattern.charAt(j)) {
                i++;
                j++;

                if (j == m) {
                    positions[count] = i - j;
                    count++;
                    j = lps[j - 1];
                }
            } else if (j > 0) {
                j = lps[j - 1];
            } else {
                i++;
            }
        }

        return copyPositions(positions, count);
    }

    // ------------------------------------------------------------
    // 3. Z-FUNCTION
    // ------------------------------------------------------------

    static int[] buildZ(String text) {
        int n = text.length();
        int[] z = new int[n];

        int left = 0;
        int right = 0;

        for (int i = 1; i < n; i++) {
            if (i <= right) {
                z[i] = Math.min(right - i + 1, z[i - left]);
            }

            while (i + z[i] < n &&
                   text.charAt(z[i]) == text.charAt(i + z[i])) {
                z[i]++;
            }

            if (i + z[i] - 1 > right) {
                left = i;
                right = i + z[i] - 1;
            }
        }

        return z;
    }

    static int[] zSearch(String text, String pattern) {
        text = normalize(text);
        pattern = normalize(pattern);

        int n = text.length();
        int m = pattern.length();

        if (m == 0 || m > n) {
            return new int[0];
        }

        /*
         * We use a separator that cannot occur in the normalized
         * resume text or pattern: ASCII character 1.
         */
        String combined = pattern + '\u0001' + text;
        int[] z = buildZ(combined);

        int[] positions = new int[n];
        int count = 0;

        for (int i = m + 1; i < combined.length(); i++) {
            if (z[i] >= m) {
                positions[count] = i - m - 1;
                count++;
            }
        }

        return copyPositions(positions, count);
    }

    // ------------------------------------------------------------
    // 4. RABIN-KARP WITH ROLLING HASH
    // ------------------------------------------------------------

    static final long MOD1 = 1000000007L;
    static final long MOD2 = 1000000009L;
    static final long BASE = 911382323L;

    static long modMultiply(long a, long b, long mod) {
        return (a * b) % mod;
    }

    static long hashOf(String text, long mod) {
        long hash = 0;

        for (int i = 0; i < text.length(); i++) {
            hash = (modMultiply(hash, BASE, mod) + text.charAt(i)) % mod;
        }

        return hash;
    }

    static long power(long base, int exponent, long mod) {
        long result = 1;

        for (int i = 0; i < exponent; i++) {
            result = modMultiply(result, base, mod);
        }

        return result;
    }

    static boolean exactMatchAt(String text, String pattern, int start) {
        for (int j = 0; j < pattern.length(); j++) {
            if (text.charAt(start + j) != pattern.charAt(j)) {
                return false;
            }
        }

        return true;
    }

    static int[] rabinKarpSearch(String text, String pattern) {
        text = normalize(text);
        pattern = normalize(pattern);

        int n = text.length();
        int m = pattern.length();

        if (m == 0 || m > n) {
            return new int[0];
        }

        long patternHash1 = hashOf(pattern, MOD1);
        long patternHash2 = hashOf(pattern, MOD2);

        long windowHash1 = hashOf(text.substring(0, m), MOD1);
        long windowHash2 = hashOf(text.substring(0, m), MOD2);

        long highPower1 = power(BASE, m - 1, MOD1);
        long highPower2 = power(BASE, m - 1, MOD2);

        int[] positions = new int[n];
        int count = 0;

        for (int i = 0; i <= n - m; i++) {
            if (windowHash1 == patternHash1 &&
                windowHash2 == patternHash2 &&
                exactMatchAt(text, pattern, i)) {

                positions[count] = i;
                count++;
            }

            if (i < n - m) {
                long outgoing = text.charAt(i);

                windowHash1 =
                    (windowHash1 - modMultiply(outgoing, highPower1, MOD1) + MOD1)
                    % MOD1;

                windowHash1 =
                    (modMultiply(windowHash1, BASE, MOD1)
                    + text.charAt(i + m)) % MOD1;

                windowHash2 =
                    (windowHash2 - modMultiply(outgoing, highPower2, MOD2) + MOD2)
                    % MOD2;

                windowHash2 =
                    (modMultiply(windowHash2, BASE, MOD2)
                    + text.charAt(i + m)) % MOD2;
            }
        }

        return copyPositions(positions, count);
    }

    // ------------------------------------------------------------
    // Utility methods
    // ------------------------------------------------------------

    static int[] copyPositions(int[] source, int count) {
        int[] result = new int[count];

        for (int i = 0; i < count; i++) {
            result[i] = source[i];
        }

        return result;
    }

    static String algorithmName(int choice) {
        if (choice == 1) return "Naive Pattern Matching";
        if (choice == 2) return "KMP";
        if (choice == 3) return "Z-Function";
        if (choice == 4) return "Rabin-Karp";
        return "Unknown";
    }

    static int[] search(int algorithm, String text, String pattern) {
        if (algorithm == 1) {
            return naiveSearch(text, pattern);
        }

        if (algorithm == 2) {
            return kmpSearch(text, pattern);
        }

        if (algorithm == 3) {
            return zSearch(text, pattern);
        }

        return rabinKarpSearch(text, pattern);
    }

    static void printPositions(int[] positions) {
        if (positions.length == 0) {
            System.out.println("No occurrence found.");
            return;
        }

        System.out.print("Match positions (0-based): ");

        for (int i = 0; i < positions.length; i++) {
            System.out.print(positions[i]);

            if (i < positions.length - 1) {
                System.out.print(", ");
            }
        }

        System.out.println();
    }

    static void displayResume(Resume resume) {
        System.out.println("----------------------------------------");
        System.out.println("Resume ID   : " + resume.id);
        System.out.println("Name        : " + resume.name);
        System.out.println("Email       : " + resume.email);
        System.out.println("Education   : " + resume.education);
        System.out.println("Experience  : " + resume.experience);
        System.out.println("Skills      : " + resume.skills);
        System.out.println("Summary     : " + resume.summary);
        System.out.println("File        : " + resume.fileName);
    }

    static void showCorpus() {
        System.out.println("\n========================================");
        System.out.println("              RESUME CORPUS");
        System.out.println("========================================");

        for (int i = 0; i < corpusSize; i++) {
            displayResume(corpus[i]);
        }

        System.out.println("----------------------------------------");
        System.out.println("Total resumes: " + corpusSize);
    }

    static void searchCorpus(int algorithm, String pattern) {
        System.out.println("\n========================================");
        System.out.println("              SEARCH RESULTS");
        System.out.println("========================================");
        System.out.println("Algorithm : " + algorithmName(algorithm));
        System.out.println("Pattern   : " + pattern);

        int totalMatches = 0;
        int matchedResumes = 0;

        for (int i = 0; i < corpusSize; i++) {
            int[] positions = search(algorithm, corpus[i].fullText, pattern);

            if (positions.length > 0) {
                matchedResumes++;
                totalMatches += positions.length;

                System.out.println("\nMATCH FOUND");
                displayResume(corpus[i]);
                System.out.println("Occurrences: " + positions.length);
                printPositions(positions);
            }
        }

        System.out.println("\n----------------------------------------");
        System.out.println("Matched resumes : " + matchedResumes);
        System.out.println("Total matches   : " + totalMatches);
    }

    static void compareAlgorithms(String pattern) {
        System.out.println("\n========================================");
        System.out.println("          ALGORITHM COMPARISON");
        System.out.println("========================================");
        System.out.println("Pattern: " + pattern);

        for (int algorithm = 1; algorithm <= 4; algorithm++) {
            int totalMatches = 0;
            int matchedResumes = 0;

            for (int i = 0; i < corpusSize; i++) {
                int[] positions =
                    search(algorithm, corpus[i].fullText, pattern);

                if (positions.length > 0) {
                    matchedResumes++;
                    totalMatches += positions.length;
                }
            }

            System.out.println();
            System.out.println(algorithmName(algorithm));
            System.out.println("Matched resumes : " + matchedResumes);
            System.out.println("Total matches   : " + totalMatches);
        }

        System.out.println("\nComplexities:");
        System.out.println("Naive      : O(nm)");
        System.out.println("KMP        : O(n + m)");
        System.out.println("Z-Function : O(n + m)");
        System.out.println("Rabin-Karp : Average O(n + m), worst-case O(nm)");
    }

    static void showMenu() {
        System.out.println("\n========================================");
        System.out.println("   RESUME SEARCH & CANDIDATE SCREENING");
        System.out.println("========================================");
        System.out.println("1. Naive Pattern Search");
        System.out.println("2. KMP Search");
        System.out.println("3. Z-Function Search");
        System.out.println("4. Rabin-Karp Search");
        System.out.println("5. Compare All String Algorithms");
        System.out.println("6. Display Resume Corpus");
        System.out.println("7. Exit");
        System.out.println("----------------------------------------");
        System.out.print("Enter choice: ");
    }

    static int readInteger(BufferedReader input) throws IOException {
        while (true) {
            String value = input.readLine();

            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                System.out.print("Enter a valid number: ");
            }
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader input =
            new BufferedReader(new java.io.InputStreamReader(System.in));

        System.out.println("Loading resume corpus from: corpus/");
        loadCorpus("corpus");

        if (corpusSize == 0) {
            System.out.println("No .txt resumes found.");
            System.out.println("Make sure the corpus folder contains resume TXT files.");
            return;
        }

        System.out.println("Corpus loaded successfully.");
        System.out.println("Total resumes: " + corpusSize);

        while (true) {
            showMenu();

            int choice = readInteger(input);

            if (choice >= 1 && choice <= 4) {
                System.out.print("Enter keyword/pattern: ");
                String pattern = input.readLine();

                if (pattern.trim().isEmpty()) {
                    System.out.println("Pattern cannot be empty.");
                } else {
                    searchCorpus(choice, pattern);
                }
            } else if (choice == 5) {
                System.out.print("Enter keyword/pattern: ");
                String pattern = input.readLine();

                if (pattern.trim().isEmpty()) {
                    System.out.println("Pattern cannot be empty.");
                } else {
                    compareAlgorithms(pattern);
                }
            } else if (choice == 6) {
                showCorpus();
            } else if (choice == 7) {
                System.out.println("Exiting Candidate Screening Project.");
                break;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }
}
