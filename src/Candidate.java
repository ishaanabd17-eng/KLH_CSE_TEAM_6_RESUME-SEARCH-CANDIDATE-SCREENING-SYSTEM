import java.util.*;

public class Candidate {
    private final int id;
    private final String name, email, education, location, summary;
    private final int experience;
    private final Set<String> skills;

    public Candidate(int id, String name, String email, String education, int experience,
                     String location, Collection<String> skills, String summary) {
        this.id = id; this.name = name; this.email = email; this.education = education;
        this.experience = experience; this.location = location; this.summary = summary;
        this.skills = new LinkedHashSet<>();
        for (String skill : skills) this.skills.add(TextUtils.normalize(skill));
    }
    public int getId(){ return id; }
    public String getName(){ return name; }
    public String getEmail(){ return email; }
    public String getEducation(){ return education; }
    public int getExperience(){ return experience; }
    public String getLocation(){ return location; }
    public String getSummary(){ return summary; }
    public Set<String> getSkills(){ return Collections.unmodifiableSet(skills); }
}
