import java.util.*;

public class CandidateRanker {
    public static final double SKILLS_WEIGHT = 0.50;
    public static final double EXPERIENCE_WEIGHT = 0.30;
    public static final double LOCATION_WEIGHT = 0.20;

    public List<RankedCandidate> rank(List<Candidate> pool, Set<String> requiredSkills,
                                      int minExperience, String preferredLocation) {
        List<RankedCandidate> ranked = new ArrayList<>();
        String location = TextUtils.normalize(preferredLocation);
        for (Candidate c : pool) {
            if (c.getExperience() < minExperience) continue;
            int matched = 0;
            for (String skill : requiredSkills) {
                boolean found = c.getSkills().contains(skill);
                if (!found) {
                    for (String actual : c.getSkills()) {
                        if (StringAlgorithms.editDistance(skill, actual) <= Math.max(1, skill.length()/5)) {
                            found = true; break;
                        }
                    }
                }
                if (found) matched++;
            }
            double skillScore = requiredSkills.isEmpty() ? 100 : 100.0 * matched / requiredSkills.size();
            double expScore = Math.min(100, c.getExperience() * 100.0 / 12.0);
            double locScore = location.isEmpty() || location.equals("any")
                    || TextUtils.normalize(c.getLocation()).equals(location) ? 100 : 0;
            double score = SKILLS_WEIGHT*skillScore + EXPERIENCE_WEIGHT*expScore + LOCATION_WEIGHT*locScore;
            ranked.add(new RankedCandidate(c, score, matched, requiredSkills.size()));
        }
        ranked.sort(Comparator.comparingDouble(RankedCandidate::getScore).reversed()
            .thenComparing(Comparator.comparingInt(RankedCandidate::getMatchedSkills).reversed())
            .thenComparing(Comparator.comparingInt((RankedCandidate r)->r.getCandidate().getExperience()).reversed()));
        return ranked;
    }
}
