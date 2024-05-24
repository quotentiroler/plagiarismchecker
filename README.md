# Plagiarism Detection Tool

This tool allows you to detect plagiarism in code by comparing document fingerprints. It supports two methods of input:

1. Entering URLs of GitHub repositories into a textarea.
2. Manually uploading a zip file.

This tool can be very helpful if you are a lecturer or a tutor and you want to find out if your students were working together on an assignment.

## How to Use


Download the executable file from [here](https://www.file-upload.net/download-15334347/PlagCheckApp.exe.html)

or clone and run this app. This maven application can be packed into an executable .jar-file using the following command: 

```mvn clean compile assembly:single```

### Method 1: GitHub Repositories

Enter the URLs of the GitHub repositories you want to check in the provided textarea.

### Method 2: Zip File Upload

Follow these steps:

1. Ensure every source directory is in a folder whose name starts with "task".
2. Do not include sub-directories in the source directories.
3. Zip all the source directories.
4. Upload the .zip file.
5. (Optional) Exclude fingerprints by uploading a .json file from `upload-dir/results` (e.g., assignment code).
6. Download the results (.txt-files). You can also find them in the `/upload-dir` folder.

## Explanation

1. The tool walks through source directories.
2. It tokenizes every code file (split by empty space) and makes every token unique by hashing them.
3. It creates a JSON file in the format: `{"codeFile.cpp":[{"line":[["value"]]}]}`. Here, "line" indicates the first occurrence of "value" in the code file.
4. It compares JSON files and logs the position of equal values.
5. It creates a `Summary.txt` that gives an overview, and also a `.json.txt` file for every compared source directory. This includes a JSON in the format: `{"value":["/codeFile.cpp/0/line/0/position"]}`. The "value" becomes the key because the same value can be in multiple files, while at this point every value is unique. Example token "token" has multiple positions, "token" is the key and its positions are the values in an array.

It will be furtherly developed (slowly). In the future, the positions of the tokens will be analyzed to give more accurate results.

## Results

The results show a fingerprint similarity of 2 source-dirs between 0 and 1, representing the ratio of matching values to all values. The code files need to be formatted in a IDE-typical way to have accurate results. The results look like this:

``` 
task1-aaaa.json compared to task1-bbbbbb.json: Total matches = 2 Unique entries: 995 Similarity: 0.002006018
/server.cpp/0/12/0/0: <google/protobuf/io/coded_stream.h>
/server.cpp/0/1/0/0: #include
task1-aaaa.json compared to task1-ccccc.json: Total matches = 1 Unique entries: 1251 Similarity: 7.9872203E-4
/server.cpp/0/1/0/0: #include
task1-aaaa.json compared to task1-dddddd.json: Total matches = 0 Unique entries: 876 Similarity: 0.0
task1-aaaa.json compared to task1-eeeeee.json: Total matches = 8 Unique entries: 1614 Similarity: 0.0049321824
/server.cpp/0/154/0/1: buf.get(),
/shared.cpp/0/81/0/0: bytes
/shared.cpp/0/81/0/1: 0LL;
/shared.cpp/0/82/0/0: remaining_bytes
/shared.cpp/0/82/0/1: len;
/shared.cpp/0/84/0/0: *tmp
/shared.cpp/0/84/0/1: data;
/shared.cpp/0/86/0/0: (remaining_bytes
task1-aaaaa.json compared to task1-ffffff.json: Total matches = 1 Unique entries: 2111 Similarity: 4.7348486E-4
/server_thread.h/0/4/0/0: "shared.h"
```

The similarity is usually very low for different code files. For any similarity that is clearly above average, further investigation is recommended.

The use of the tool alone is not sufficient to conscientiously detect plagiarism!

## Testing:

Test folder is not up to date with code. You can test the fingerprint exclusion functionality at
localhost:8080/excludefp

In fact, fingerprint exclusion is an important function of this lib. If you are a tutor or a lecturer of computer science related subjects, you most likely gave your students some kind of assignment code which also has its own document fingerprint. You can first generate the fingerprint and then upload only that fingerprint file in order to remove the fingerprint from the rest of the analysis. This makes the results significantly more accurate.




