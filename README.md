# KLH_CSE_TEAM_6_RESUME-SEARCH-CANDIDATE-SCREENING-SYSTEM
# Resume Search and Candidate Screening System

The Resume Search and Candidate Screening System is a Java-based application designed to automate basic resume searching and candidate screening. The system uses a synthetic corpus of 40 resume records stored in `resumes.csv`, with information such as candidate ID, name, email, education, experience, skills, and professional summary. A recruiter enters the required skills and minimum experience, after which the system preprocesses the input by normalizing skill names, uses a HashMap-based inverted index to retrieve candidates associated with the requested skills, and uses HashSet for efficient skill matching. Each candidate receives a relevance score based primarily on the percentage of required skills matched, with an experience component, and candidates are then sorted in descending order of their scores. The final output displays the most relevant candidates along with their score, matched skills, experience, education, email, skills, and summary. The project demonstrates practical applications of DSA concepts including HashMap, HashSet, ArrayList, searching, traversal, and sorting to solve a real-world recruitment problem. The current prototype uses synthetic data for academic demonstration and can be extended with larger datasets, PDF/DOCX resume parsing, NLP, semantic similarity, and a web-based interface.

## How to Run
Open the project folder in VS Code and make sure `ResumeScreeningSystem.java` and `resumes.csv` are in the same folder. Open the terminal and run:

javac ResumeScreeningSystem.java

java ResumeScreeningSystem

The program will load the resume corpus and display a menu for searching and ranking candidates, viewing the corpus, viewing the skill index, or exiting the application
