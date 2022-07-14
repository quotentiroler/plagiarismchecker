import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class DirPreparation {

    public static void rec_processFiles(File[] files) {

        File destDir = null;
        for (File file : files) {
            if (file.isDirectory() && file.getName().startsWith("task"))
                rec_processFiles(file.listFiles());
            if (file.isDirectory() && file.getName().startsWith("source")) {

                destDir = new File("/mnt/c/Users/Max/Desktop/Plagiarism Task 1/Prepared/" + file.getParent());

                try {
                    FileUtils.copyDirectory(file, destDir);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }

    }

    public static void main(String[] args) {
        String source = "/mnt/c/Users/Max/Desktop/Plagiarism Task 1/";
        File srcDir = new File(source);
        rec_processFiles(srcDir.listFiles());
    }
}
