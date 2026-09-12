# Resume Search and Candidate Screening System

## Project Overview
This is a Java-based prototype that searches a resume corpus using recruiter-defined skills and ranks the most relevant candidates.

## Files
- `ResumeScreeningSystem.java` — complete source code
- `resumes.csv` — 40-record synthetic resume corpus
- `ABSTRACT.txt` — project abstract
- `README.md` — project documentation

## Requirements
- Java 11 or later
- VS Code or any Java-compatible IDE
- Java compiler (`javac`) available in the terminal

## How to Run in VS Code

Keep `ResumeScreeningSystem.java` and `resumes.csv` in the same folder.

Open the folder in VS Code.

Open **Terminal → New Terminal** and run:

```text
javac ResumeScreeningSystem.java
java ResumeScreeningSystem
```

The program first loads the corpus and displays the number of resumes and indexed skills.

Choose:

```text
1. Search and rank candidates
2. Display resume corpus
3. Display skill index
4. Exit
```

For a demonstration, choose `1` and enter:

```text
Java, Python, SQL
```

Then enter:

```text
2
```

The program will display the ranked candidates.

## Corpus / Dataset
The corpus is the `resumes.csv` file. It contains 40 synthetic/sample candidate records. Each record contains:
- Candidate ID
- Name
- Email
- Education
- Years of experience
- Skills
- Short resume summary

The corpus was created specifically for this academic prototype. It is synthetic data and is not claimed to represent real applicants.

## Processing Pipeline

Input recruiter requirements
        ↓
Normalize required skills
        ↓
Search inverted skill index
        ↓
Retrieve candidate records
        ↓
Compare required skills with candidate skills
        ↓
Calculate relevance score
        ↓
Sort candidates
        ↓
Display ranked results

## DSA / Algorithms Used

### 1. HashSet
Each candidate's skills are stored in a HashSet. This supports fast membership checking when the program asks whether a candidate has a required skill.

### 2. HashMap / Inverted Index
The program builds an inverted index:

`skill -> set of candidate IDs`

For example:

`java -> {1, 3, 5, 7, ...}`

This allows the system to quickly retrieve candidates associated with a requested skill.

### 3. ArrayList
All candidate records are stored in an ArrayList, allowing sequential processing and sorting.

### 4. Linear Traversal
The program traverses the required skills and candidate records to calculate how many required skills are present.

### 5. Sorting
Candidates are sorted in descending order of relevance score. Experience is used as a secondary sorting criterion when scores are equal.

## Scoring
The main relevance score is based on required skill matching.

Skill score:

`matched required skills / total required skills × 100`

The skill component contributes 90% of the final score. Candidates meeting the minimum requested experience receive an additional 10% bonus, with the final score capped at 100%.

## Input
- Required skills entered by the recruiter
- Minimum required experience

## Output
- Candidate name
- Relevance score
- Number of matched skills
- Experience
- Education
- Email
- Skills
- Resume summary

## Where is the Corpus in the Code?
Use `Ctrl + F` in VS Code and search:

`resumes.csv`

You will find:

`String corpusFile = "resumes.csv";`

The file is then passed to:

`loadCorpus(corpusFile);`

Inside `loadCorpus()`, `FileReader` and `BufferedReader` read the resume records.

## Where is the Algorithm?
Search these method names:
- `buildInvertedIndex()`
- `searchCandidates()`
- `calculateScores()`
- `rankCandidates()`

## Limitations
- The prototype uses a synthetic corpus rather than real recruitment data.
- Matching is primarily skill-based and does not deeply understand semantic meaning.
- Resume formatting and complex PDF/DOCX extraction are not included.
- Real systems would require larger datasets, more screening criteria and stronger privacy/security controls.

## Future Scope
- PDF/DOCX resume parsing
- Natural-language processing
- Semantic similarity
- More detailed experience and education matching
- Web-based recruiter interface
- Larger real-world datasets

