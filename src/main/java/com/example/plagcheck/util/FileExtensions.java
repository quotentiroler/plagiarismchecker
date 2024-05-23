package com.example.plagcheck.util;
public enum FileExtensions implements IFileType {

    CPP(".cpp"),
    H(".h"),
    HPP(".hpp"),
    JAVA(".java");

 private String extension ="";

    FileExtensions(String s) {
        extension = s;
    }

    @Override
    public String getExtension() {
        return extension;
    }

}
