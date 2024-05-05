package ch.zhaw.prog2.io;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public class FileCopy {
    private static final String DEFAULT_SOURCE_DIR = "code/ByteCharStream/files";

	public static void main(String[] args) throws IOException {

		// get the filename from the arguments. By default, use 'files'-directory in current working directory.
        String sourceDirPath = args.length >= 1 ? args[0] : DEFAULT_SOURCE_DIR;
        File sourceDir = new File(sourceDirPath);

        // Part a – Verify the directory structure
        // Implement the method 'verifySourceDir()'
        System.out.format("Verifying Source Directory: %s%n", sourceDirPath);
        try {
            verifySourceDir(sourceDir);
        } catch (FileNotFoundException error) {
            System.err.format("Directory %s does not comply with predefined structure: %s%n", sourceDir.getPath(), error.getMessage());
            System.err.println("Terminating programm!");
            System.exit(1);
        }
        System.out.println("Source Directory verified successfully.");


        // Part b – Copy the files byte resp. char wise.
        // Implement the method 'copyFiles()'
        System.out.println("Initiating file copies.");
        try {
            copyFiles(sourceDir);
        } catch (IOException error) {
            System.err.format("Error creating file copies:  %s%n", error.getMessage());
            System.err.println("Terminating programm!");
            System.exit(2);
        }
        System.out.println("Files copied successfully.");
    }

    /**
     * Part a – directory structure.
     * <p>
     * Verify the directory structure for correctness.
     * Correct means, that the directory exists and beside the two files rmz450.jpg and rmz450-spec.txt does not contain
     * any other files or directories.
     *
     * @param sourceDir File source directory to verify it contains the correct structure
     * @throws FileNotFoundException if the source directory or required file are missing
     * @throws InvalidObjectException if the source directory contains invalid files or directories.
     */
    private static void verifySourceDir(File sourceDir) throws FileNotFoundException, InvalidObjectException, IOException {
        if (sourceDir == null) throw new InvalidObjectException("The source directory is null.");

        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new FileNotFoundException("The source directory does not exist or is not a directory.");
        }

        boolean hasSpecFile = false;
        boolean hasJpgFile = false;
        for (Path filePath : Files.newDirectoryStream(sourceDir.toPath())) {
            String fileName = filePath.getFileName().toString();
            if (fileName.equals("rmz450-spec.txt")) {
                hasSpecFile = true;
            } else if (fileName.equals("rmz450.jpg")) {
                hasJpgFile = true;
            } else {
                throw new InvalidObjectException("The source directory contains an unexpected file: " + fileName);
            }
        }
        if (!hasSpecFile) {
            throw new FileNotFoundException("The required file rmz450-spec.txt is missing from the source directory.");
        }
        if (!hasJpgFile) {
            throw new FileNotFoundException("The required file rmz450.jpg is missing from the source directory.");
        }
    }


    /**
     * Part b – Copy files.
     * <p>
     * Copies each file of the source directory twice, once character-oriented and once byte-oriented.
     * Source and target files should be opened and copied byte by byte respectively char by char.
     * The target files should be named, so the type of copy can be identified.
     *
     * @param sourceDir File representing the source directory containing the files to copy
     * @throws IOException if an error is happening while copying the files
     */
    private static void copyFiles(File sourceDir) throws IOException {
        for (Path filePath : Files.newDirectoryStream(sourceDir.toPath())) {
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile().getPath()));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile().getPath()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    writer.write(line);
                    writer.newLine();  // Adds a new line
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try (FileInputStream in = new FileInputStream(filePath.toFile().getPath());
                 FileOutputStream out = new FileOutputStream(filePath.toFile().getPath())) {
                int byteRead;
                while ((byteRead = in.read()) != -1) {
                    out.write(byteRead);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
