import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class StudentRecord {
    // Fields to hold the student record details
    private long id;
    private String lastName;
    private String firstName;
    private String letterGrade;
    private long overflowLink; // This may be used to link to additional records in case of overflow in database implementations

    // Constructor to initialize a new student record
    public StudentRecord(long id, String lastName, String firstName, String letterGrade, long overflowLink) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.letterGrade = letterGrade;
        this.overflowLink = overflowLink;
    }
    // Converts the student record into a byte array for storage or transmission
    public byte[] convertIntoBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(64); // Allocate a buffer of 64 bytes
        buffer.putLong(id); // Store the ID as a long (8 bytes)
        buffer.put(fixedSize(lastName, 20)); // Store the last name in a fixed size of 20 bytes
        buffer.put(fixedSize(firstName, 20)); // Store the first name in a fixed size of 20 bytes
        buffer.put(fixedSize(letterGrade, 2)); // Store the letter grade in a fixed size of 2 bytes
        buffer.putLong(overflowLink); // Store the overflow link as a long (8 bytes)
        return buffer.array(); // Return the underlying byte array
    }

    // Constructs a student record from a byte array
    public static StudentRecord convertFromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes); // Wrap the byte array into a ByteBuffer
        long id = buffer.getLong(); // Read the ID from the buffer
        String lastName = new String(bytes, 8, 20, StandardCharsets.UTF_8).trim(); // Extract and trim the last name
        String firstName = new String(bytes, 28, 20, StandardCharsets.UTF_8).trim(); // Extract and trim the first name
        String letterGrade = new String(bytes, 48, 2, StandardCharsets.UTF_8).trim(); // Extract and trim the letter grade
        long overflowLink = buffer.getLong(56); // Read the overflow link from the buffer
        return new StudentRecord(id, lastName, firstName, letterGrade, overflowLink); // Create a new StudentRecord object
    }

    // Helper method to ensure strings are saved in fixed byte lengths
    private byte[] fixedSize(String string, int size) {
        byte[] stringBytes = new byte[size]; // Create a byte array of the specified size
        byte[] rawStringBytes = string.getBytes(StandardCharsets.UTF_8); // Convert the string to bytes
        System.arraycopy(rawStringBytes, 0, stringBytes, 0, Math.min(rawStringBytes.length, size)); // Copy to the fixed size array, truncating if necessary
        return stringBytes;
    }

    // Getter methods to access the student record details
    public long getId() { return id; }
    public String getLastName() { return lastName; }
    public String getFirstName() { return firstName; }
    public String getLetterGrade() { return letterGrade; }
}
