import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Database {

    // create private local variables
    private RandomAccessFile dbFile;
    private RandomAccessFile overflowFile;
    private BPlusTree index;
    private String baseFilename;

    public Database(String filename) throws IOException {
        this.baseFilename = filename;
        initializeFiles(); // initalize the database based on give file name by user
    }

    private void initializeFiles() throws IOException {
        // this creates the new databse file along with the overflow and index files to go with it
        File db = new File(baseFilename + ".db");
        File overflow = new File(baseFilename + ".overflow");
        File indexFile = new File(baseFilename + ".index");

        dbFile = new RandomAccessFile(db, "rw");
        overflowFile = new RandomAccessFile(overflow, "rw");

        if (!db.exists()) db.createNewFile();
        if (!overflow.exists()) overflow.createNewFile();
        if (!indexFile.exists()) indexFile.createNewFile();

        this.index = new BPlusTree(indexFile); // Initialize B+ tree 
    }

    public void addRecord(StudentRecord record) throws IOException {
        dbFile.seek(dbFile.length()); // Go to the end of the file
        dbFile.write(record.convertIntoBytes()); // Write the record in byte format
        index.insert(record.getId(), dbFile.getFilePointer() - 64); // Insert the record into the B+ tree
        System.out.println("Record added.");
    }

    public void showRecord(long id) throws IOException {
        Long offset = index.search(id);
        if (offset == null) {
            System.out.println("No record found with ID: " + id); // if there is no record found of the id in the index return error
            return;
        }
        dbFile.seek(offset);
        byte[] bytes = new byte[64]; // initialize new array with pre determined size to get and store bytes
        dbFile.readFully(bytes);
        StudentRecord record = StudentRecord.convertFromBytes(bytes);
        System.out.printf("ID: %d, Last Name: %s, First Name: %s, Grade: %s\n", // this is how the system outputs the records correctly
                          record.getId(), record.getLastName(), record.getFirstName(), record.getLetterGrade());
    }

    public void loadRecords(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length < 4) {
                    System.out.println("Skipping invalid record: " + line); // skips if there is an invalid record
                    continue;
                }
                long id = Long.parseLong(data[0].trim());
                String lastName = data[1].trim();
                String firstName = data[2].trim();
                String letterGrade = data[3].trim();
                StudentRecord record = new StudentRecord(id, lastName, firstName, letterGrade, -1);
                addRecord(record);
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + filename); // throw exceptions if file is not found or if there is error reading file
        } catch (Exception e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    public void mergeRecords() throws IOException {
        Map<Long, StudentRecord> recordsMap = new HashMap<>();

        // Read all records from the main db file and load them 
        reloadFromFileToMap(dbFile, recordsMap, false);

        reloadFromFileToMap(overflowFile, recordsMap, true);

        dbFile.setLength(0); // Reset the main database file
        overflowFile.setLength(0); 

        for (StudentRecord record : recordsMap.values()) {
            dbFile.seek(dbFile.length()); 
            dbFile.write(record.convertIntoBytes()); // Write the record
            index.insert(record.getId(), dbFile.getFilePointer() - 64); // Rebuild the index with the new file positions
        }

        System.out.println("Merge completed.");
    }

    public void close() throws IOException {
        dbFile.close();
        overflowFile.close();
        index.close();
    }
    private void reloadFromFileToMap(RandomAccessFile file, Map<Long, StudentRecord> map, boolean overwrite) throws IOException {
        file.seek(0); // Go to the beginning of the file
        while (file.getFilePointer() < file.length()) {
            byte[] bytes = new byte[64]; // here we are again assuming the size of the record is 64 bytes to store
            file.readFully(bytes);
            StudentRecord record = StudentRecord.convertFromBytes(bytes);
            if (overwrite || !map.containsKey(record.getId())) {
                map.put(record.getId(), record); // Put the record in the map, and overwrite if there is existing content
            }
        }
    }
}
