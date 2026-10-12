import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class ResumeRepository {
    private final List<Candidate> candidates = new ArrayList<>();
    private final Map<String, Set<Integer>> skillIndex = new HashMap<>();

    public void load(Path path) throws IOException {
        candidates.clear(); skillIndex.clear();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] p = line.split("\\|", -1);
                if (p.length < 8) continue;
                List<String> skills = Arrays.asList(p[6].split(";"));
                Candidate c = new Candidate(Integer.parseInt(p[0]), p[1], p[2], p[3],
                    Integer.parseInt(p[4]), p[5], skills, p[7]);
                candidates.add(c);
                for (String skill : c.getSkills())
                    skillIndex.computeIfAbsent(skill, ignored -> new LinkedHashSet<>()).add(c.getId());
            }
        }
    }
    public List<Candidate> all() { return Collections.unmodifiableList(candidates); }
    public Map<String, Set<Integer>> skillIndex() { return Collections.unmodifiableMap(skillIndex); }
    public List<Candidate> byIds(Collection<Integer> ids) {
        List<Candidate> out = new ArrayList<>();
        for (Candidate c : candidates) if (ids.contains(c.getId())) out.add(c);
        return out;
    }
}
