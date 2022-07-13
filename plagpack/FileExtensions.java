package plagpack;

public enum FileExtensions implements IFileType {

    CPP(".cpp"),
    H(".h"),
    HPP(".hpp");

 private String extension ="";

    FileExtensions(String s) {
        extension = s;
    }

    @Override
    public String getExtension() {
        return extension;
    }

}
