## Plagiarism detection based on document fingerprint creation + comparison
### Check multiple C++ source-dirs for plagiarism

This application can be packed into an executable .jar-file using the following command: 

```mvn clean compile assembly:single```

Then open localhost:8080 in your browser

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
3. create JSON, consisting of JSONObjects for each file with fingerprints, save to ../results/
4. compare JSON files, log position of equal values
5. create output .txt files for every src-dir and also Summary.txt that gives an overview

What do the results say?

The results show a fingerprint similariy between 0 and 1.
1 means that it is exactly the same fingerprint. Renaming variables and methods decreases the similarity, but this does not lead to a big change unless the code is very short and only consists of a few expressions in total. The similarity interval for possible plagiarism depends on the compared directories. It makes sense to investigate the highest similarities manually. 

Testing:

Test folder is not up to date with code. You can test the fingerprint exclusion functionality at
localhost:8080/excludefp


