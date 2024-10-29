import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Main <database_filename>");
            System.exit(1);  // Exit path if given input is not correct
        }

        try (Scanner scanner = new Scanner(System.in)) {
            Database db = new Database(args[0]);  // Initialize db
            while (true) {
                System.out.println("Commands: add, show, load, merge, quit");
                System.out.print("Enter command: ");
                String command = scanner.nextLine();

                switch (command) {
                    case "quit": 
                        db.close();  // Close database connections and exit loop on quit command
                        return;  
                    case "add":
                        System.out.print("Enter ID: "); // prompt student id
                        long id = Long.parseLong(scanner.nextLine());
                        System.out.print("Enter Last Name: "); // prompt last and first name credentials
                        String lastName = scanner.nextLine();
                        System.out.print("Enter First Name: ");
                        String firstName = scanner.nextLine();
                        System.out.print("Enter Letter Grade: ");// prompt the letter grade to be allocated to that id
                        String letterGrade = scanner.nextLine();
                        StudentRecord record = new StudentRecord(id, lastName, firstName, letterGrade, -1);
                        db.addRecord(record); // save this as a new record in the format mentioned in the above constructor with id, last name, first name, grade
                        break;
                    case "show": // Show the contents of the array that is pointed to by the id that the user inputs
                        System.out.print("Enter ID to show: ");
                        id = Long.parseLong(scanner.nextLine()); // read the user input for the id number
                        db.showRecord(id); // show the record in correct format based on the id
                        break;
                    case "load": //  loads records from a csv file that already exists to access
                        System.out.print("Enter filename to load from: ");
                        String filename = scanner.nextLine(); // read the file lines as input
                        db.loadRecords(filename);
                        break;
                    case "merge": //  merges records
                        db.mergeRecords(); // method to merge records together
                        break;
                    default: // base case where there is no/invalid command
                        System.out.println("Unknown command.");
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error accessing the database: " + e.getMessage()); // throw an error if there is any error accessing the db or related contents
        }
    }
}
