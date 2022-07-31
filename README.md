## Plagiarism detection based on document fingerprint creation + comparison
### Check multiple C++ source-dirs for plagiarism

This maven application can be packed into an executable .jar-file using the following command: 

```mvn clean compile assembly:single```

Run it, then open localhost:8080 in your browser

How to use:

1. Have every source dir in a folder which name starts with "task"
2. Do not have sub-dirs in the source dirs
3. Zip all the source-dirs
4. Upload .zip file
5. (Optional) Exclude fingerprints by uploading a .json file from upload-dir/results - e.g. assignment code
6. Download results (.txt-files) (you can also find it in folder /upload-dir)

What it does:

1. Walk through src-dirs (they will be compared in the end, need to start with "task")
2. tokenize every codeFile (split by empty space)
3. create JSON having format: {"codeFile.cpp":[{"line":[["value"]]}]} - line indicates first occurence of value in codeFile
4. compare JSON files, log position of equal values
5. create Summary.txt that gives an overview, and also .json.txt file for every compared source-dir. This includes a JSON having format: {"value":["/codeFile.cpp/0/line/0/position"]}. The value becomes the key because the same value can be in multiple files, while at this point very value is unique. 

What do the results say?

The results show a fingerprint similariy of 2 source-dirs between 0 and 1, representing the ratio of matching values to all values. The code files need to be formatted in a IDE-typical way to have accurate results.

Testing:

Test folder is not up to date with code. You can test the fingerprint exclusion functionality at
localhost:8080/excludefp


