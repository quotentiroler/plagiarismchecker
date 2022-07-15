Plagiarism check based on fingerprinting

This plagiarism checker works currently with C++ files (see FileExtensions.java)

How to run:

1. have a dir with sub-dirs that start with "task" and contain the code files
2. run JSONMaker.java
3. run JSONComparison.java

What it does:

1. Walk through sub-dirs in given dir (they will be compared in the end, need to start with "task")
2. tokenize every codeFile (split by empty space)
3. create JSON, consisting of JSONObjects for each file with fingerprints, save to ../results/
4. compare JSON files using gson and lang.reflect.type
5. create output file for every sub-dir and save to ../results/out

