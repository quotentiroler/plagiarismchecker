Plagiarism check based on fingerprinting

Check multiple C++ source-dirs for plagiarism

How to run:

1. run "mvn clean compile assembly:single" to get executable .jar file, execute it
2. open localhost:8080 in your browser

How to use:

1. Have every source dir in a folder which name starts with "task"
2. Do not have sub-dirs in that folder
3. Zip all the source-dirs
4. Upload .zip file
5. (Optional) Exclude fingerprints by uploading a .json file from upload-dir/results - e.g. assignment code
6. Download results (.txt-files)

What it does:

1. Walk through src-dirs (they will be compared in the end, need to start with "task")
2. tokenize every codeFile (split by empty space)
3. create JSON, consisting of JSONObjects for each file with fingerprints, save to ../results/
4. compare JSON files using gson and "com.google.common.collect Interface MapDifference<K,V>"
5. create output .txt files for every src-dir 

