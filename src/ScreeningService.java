import java.util.*;

public class ScreeningService {
    private final ResumeRepository repository;
    private final CandidateRanker ranker = new CandidateRanker();
    public ScreeningService(ResumeRepository repository){ this.repository=repository; }

    public List<RankedCandidate> search(String skillsInput, int minExperience, String location) {
        Set<String> skills = new LinkedHashSet<>();
        for (String skill : skillsInput.split(",")) if (!TextUtils.normalize(skill).isEmpty())
            skills.add(TextUtils.normalize(skill));
        return ranker.rank(repository.all(), skills, Math.max(0, minExperience), location);
    }
}
