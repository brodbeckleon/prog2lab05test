package ch.zhaw.prog2.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class DirList {
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Print metadata of a given file, resp. all of its files if it is a directory.
     * Use {@link #printFileMetadata(File)} to print file metadata.
     *
     * @param args path to file or directory to print metadata of (optional)
     */
    public static void main(String[] args) throws IOException {
        String pathName = args.length >= 1 ? args[0] : ".";
        File file = new File(pathName);
        if (file.isDirectory()) {
            for (Path filePath : Files.newDirectoryStream(Path.of(pathName))) {
                System.out.println(printFileMetadata(filePath.toFile()));
            }
        } else System.out.println(printFileMetadata(file));
    }

    /** Write metadata of given file on a line with the following format:<br>
     * - type of file ('d'=directory, 'f'=file)<br>
     * - readable   'r', '-' otherwise<br>
     * - writable   'w', '-' otherwise<br>
     * - executable 'x', '-' otherwise<br>
     * - hidden     'h', '-' otherwise<br>
     * - modified date in format 'yyyy-MM-dd HH:mm:ss'<br>
     * - length in bytes<br>
     * - name of the file<br>
     *
     * @param file file to print metadata
     * @return String formatted as described above.
     */
    public static String printFileMetadata(File file) throws IOException {
        String metadata = "";
        metadata += file.isFile()       ? "f" : "d";
        metadata += file.canRead()      ? "r" : "-";
        metadata += file.canWrite()     ? "w" : "-";
        metadata += file.canExecute()   ? "x" : "-";
        metadata += file.isHidden()     ? "h" : "-";

        metadata += "\t";
        metadata += dateFormat.format(file.lastModified());

        metadata+= "\t";
        metadata += Files.size(Path.of(file.getPath()));

        metadata+= "\t";
        metadata += file.getName();
        return metadata;
    }
}
