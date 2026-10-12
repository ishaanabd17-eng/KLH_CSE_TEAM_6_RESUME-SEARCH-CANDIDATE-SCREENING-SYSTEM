public class RankedCandidate {
    private final Candidate candidate;
    private final double score;
    private final int matchedSkills, totalRequired;
    public RankedCandidate(Candidate candidate, double score, int matchedSkills, int totalRequired) {
        this.candidate=candidate; this.score=score; this.matchedSkills=matchedSkills; this.totalRequired=totalRequired;
    }
    public Candidate getCandidate(){ return candidate; }
    public double getScore(){ return score; }
    public int getMatchedSkills(){ return matchedSkills; }
    public int getTotalRequired(){ return totalRequired; }
}
