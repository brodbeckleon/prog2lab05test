package ch.zhaw.prog2.io.picturedb;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * Implements the PictureDatasource Interface storing the data in
 * Character Separated Values (CSV) format, where each line consists of a record
 * whose fields are separated by the DELIMITER value ";".<br>
 * See example file: db/picture-data.csv
 */
public class FilePictureDatasource implements PictureDatasource {
    // Charset to use for file encoding.
    protected static final Charset CHARSET = StandardCharsets.UTF_8;
    // Delimiter to separate record fields on a line
    protected static final String DELIMITER = ";";
    // Date format to use for date specific record fields
    protected static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private final DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

    private File datafile;
    private File tempFile;
    private static final Logger logger = Logger.getLogger(FilePictureDatasource.class.getCanonicalName());

    /**
     * Creates the FilePictureDatasource object with the given file path as datafile.
     * Creates the file if it does not exist.
     * Also creates an empty temp file for write operations.
     *
     * @param filepath of the file to use as database file.
     * @throws IOException if accessing or creating the file fails
     */
    public FilePictureDatasource(String filepath) throws IOException {
        logger.fine("Creating an instance of FilePictureDatasource...");
        if (filepath == null || filepath.isEmpty()) {
            this.datafile = new File("db/picture-data.csv");
        } else {
            this.datafile = new File(filepath);
        }

        this.tempFile = Files.createTempFile(Path.of("./db"), "temporary_file", ".csv").toFile();

        logger.fine("An Instance of FilePictureDatasource has been created.");
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void insert(Picture picture) {
        logger.fine("Inserting a picture...");
        if (picture.isNew()) picture.setId(getHighestId() + 1);

        String csvEntry = getCsvEntry(picture);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(datafile)));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile)))) {

            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                writer.write(currentLine + "\n");
            }
            writer.write(csvEntry);
        } catch (IOException e) {
            logger.severe("Failed to read or write file" + e);
            throw new RuntimeException("Failed to read or write file", e);
        }

        if (!datafile.delete()) {
            logger.warning("Failed to delete original file");
            throw new RuntimeException("Failed to delete original file");
        }

        if (!tempFile.renameTo(datafile)) {
            logger.warning("Failed to rename temporary file");
            throw new RuntimeException("Failed to rename temporary file");
        }
        logger.info("A Picture has been saved.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(Picture picture) throws RecordNotFoundException {
        logger.fine("Updating a picture...");
        boolean recordNotFound = true;
        Long id = picture.getId();

        String csvEntry = getCsvEntry(picture);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(datafile)));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile)))) {
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                String[] splitCurrentLine = currentLine.split(DELIMITER);
                if (splitCurrentLine[0].equals(String.valueOf(id))) {
                    writer.write(csvEntry);
                    recordNotFound = false;
                } else writer.write(currentLine + "\n");
            }
        } catch (IOException e) {
            logger.severe("Failed to read or write file" + e);
            throw new RuntimeException("Failed to read or write file", e);
        }

        if (recordNotFound) {
            logger.warning("This picture with the id " + picture.getId() + " doesn't exist.");
            throw new RecordNotFoundException("This picture with the id " + picture.getId() + " doesn't exist.");
        }

        if (!datafile.delete()) {
            logger.warning("Failed to delete original file");
            throw new RuntimeException("Failed to delete original file");
        }

        if (!tempFile.renameTo(datafile)) {
            logger.warning("Failed to rename temporary file");
            throw new RuntimeException("Failed to rename temporary file");
        }
        logger.info("A Picture has been updated.");

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(Picture picture) throws RecordNotFoundException {
        logger.fine("Deleting a picture...");
        Long id = picture.getId();
        boolean recordNotFound = true;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(datafile)));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile)))) {

            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                String[] splitCurrentLine = currentLine.split(DELIMITER);
                if (splitCurrentLine[0].equals(String.valueOf(id))) {
                    writer.write("");
                    recordNotFound = false;
                } else writer.write(currentLine + "\n");
            }
        } catch (IOException e) {
            logger.severe("Failed to read or write file" + e);
            throw new RuntimeException("Failed to read or write file", e);
        }

        if (recordNotFound) {
            logger.warning("This picture with the id " + picture.getId() + " doesn't exist.");
            throw new RecordNotFoundException("This picture with the id " + picture.getId() + " doesn't exist.");
        }

        if (!datafile.delete()) {
            logger.warning("Failed to delete original file");
            throw new RuntimeException("Failed to delete original file");
        }

        if (!tempFile.renameTo(datafile)) {
            logger.warning("Failed to rename temporary file");
            throw new RuntimeException("Failed to rename temporary file");
        }
        logger.info("A Picture has been deleted.");

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long count() {
        logger.fine("Counting pictures...");
        long counter = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(datafile)))) {
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                if (!currentLine.isEmpty()) counter++;
            }
        } catch (IOException e) {
            logger.severe("Failed to count pictures" + e);
            throw new RuntimeException("Failed to count pictures", e);
        }
        return counter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Picture> findById(long id) {
        logger.fine("Finding a picture by id...");
        Optional<Picture> picture = Optional.empty();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(datafile)))) {
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                String[] splitCurrentLine = currentLine.split(DELIMITER);
                if (splitCurrentLine[0].equals(String.valueOf(id))) {
                    String[] data = currentLine.split(DELIMITER);

                    picture = Optional.of(new Picture(Long.parseLong(data[0]), new URI(data[5]).toURL(), dateFormat.parse(data[1]), data[4], Float.parseFloat(data[2]), Float.parseFloat(data[3])));
                }
            }
        } catch (IOException e) {
            logger.severe("Failed to find pictures" + e);
            throw new RuntimeException("Failed to find pictures", e);
        } catch (ParseException e) {
            logger.severe("Failed to parse" + e);
            throw new RuntimeException("Failed to parse", e);
        } catch (URISyntaxException e) {
            logger.severe("Invalid URI Syntax" + e);
            throw new RuntimeException("Invalid URI Syntax", e);
        }
        logger.fine("Picture found by id.");
        return picture;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Picture> findAll() {
        logger.fine("Finding all pictures...");
        Collection<Picture> pictures = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(datafile)))) {
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                String[] data = currentLine.split(DELIMITER);

                pictures.add(new Picture(Long.parseLong(data[0]), new URI(data[5]).toURL(), dateFormat.parse(data[1]), data[4], Float.parseFloat(data[2]), Float.parseFloat(data[3])));
            }
        } catch (IOException e) {
            logger.severe("Failed to find pictures" + e);
            throw new RuntimeException("Failed to find pictures", e);
        } catch (ParseException e) {
            logger.severe("Failed to parse" + e);
            throw new RuntimeException("Failed to parse", e);
        } catch (URISyntaxException e) {
            logger.severe("Invalid URI Syntax" + e);
            throw new RuntimeException("Invalid URI Syntax", e);
        }
        logger.fine("All pictures found.");
        return pictures;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Picture> findByPosition(float longitude, float latitude, float deviation) {
        logger.fine("Searching pictures by id.");
        Collection<Picture> pictures = new ArrayList<>();
        float maxLongitude = longitude + deviation;
        float minLongitude = longitude - deviation;
        float maxLatitude = latitude + deviation;
        float minLatitude = latitude - deviation;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(datafile)))) {
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                String[] data = currentLine.split(DELIMITER);
                if (Float.parseFloat(data[2]) <= maxLongitude && Float.parseFloat(data[2]) >= minLongitude) {
                    if (Float.parseFloat(data[3]) <= maxLatitude && Float.parseFloat(data[3]) >= minLatitude) {
                        pictures.add(new Picture(Long.parseLong(data[0]), new URI(data[5]).toURL(), dateFormat.parse(data[1]), data[4], Float.parseFloat(data[2]), Float.parseFloat(data[3])));
                    }
                }
            }
        } catch (IOException | URISyntaxException | ParseException e) {
            logger.severe("Failed to read file" + e);
            throw new RuntimeException("Failed to read file", e);
        }
        logger.fine("Pictures found by position.");
        return pictures;
    }

    private long getHighestId() {
        logger.finer("Getting the highest id of the pictures...");
        long highestId = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(datafile.getPath())))) {
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                String[] data = currentLine.split(DELIMITER);
                if (Long.parseLong(data[0]) > highestId) highestId = Long.parseLong(data[0]);
            }
        } catch (IOException e) {
            logger.severe("Failed to read file" + e);
            throw new RuntimeException("Failed to read file", e);
        }
        logger.finer("Highest id of the pictures found.");
        return highestId;
    }

    private String getCsvEntry(Picture picture) {
        logger.finer("Creating a CSV entry for a picture...");
        String csvEntry = String.valueOf(picture.getId());
        csvEntry += DELIMITER;
        csvEntry += dateFormat.format(picture.getDate());
        csvEntry += DELIMITER;
        csvEntry += picture.getLongitude();
        csvEntry += DELIMITER;
        csvEntry += picture.getLatitude();
        csvEntry += DELIMITER;
        csvEntry += picture.getTitle();
        csvEntry += DELIMITER;
        csvEntry += picture.getUrl();
        csvEntry += "\n";
        logger.finer("CSV entry for a picture created.");
        return csvEntry;
    }
}
