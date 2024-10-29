# Student Record Management System

This project is a Java-based file-based student record management system that uses a **B+ Tree** for efficient data indexing and retrieval. 
The system stores student records in a database file and provides operations for adding, showing, loading, and merging records through a command-line interface.

## Description
### BPlusTree.java
Implements a B+ Tree structure to handle indexed file-based searches. It consists of InternalNode and LeafNode classes to manage different node types in the B+ Tree.
- `insert()`: Inserts a key-value pair (student ID and file offset) into the tree, managing node splits if necessary.
- `search()`: Searches for a student ID and returns the file offset for retrieving the student’s record.
- Supports saving and loading of nodes from a file for durability.

### Database.java
Manages the storage and retrieval of StudentRecord objects in a database file (.db). Additionally, an overflow file is used for handling records that may exceed a defined space limit.
- `addRecord()`: Adds a student record by appending it to the .db file and inserting the ID in the B+ Tree index.
- `showRecord()`: Finds and displays a student record based on the student ID.
- `loadRecords()`: Loads records from a file into the database, each converted to a byte array for storage.
- `mergeRecords()`: Merges and compacts data from the main and overflow files, updating the B+ Tree index with new offsets.

### Main.java
Provides a command-line interface to interact with the database. It allows users to execute commands such as add, show, load, and merge through a terminal.
- `add:` Prompts the user for student details and saves a new record.
- `show`: Displays a record given a student ID.
- `load`: Loads records from an external file.
- `merge`: Merges main and overflow data for optimized storage.

### StudentRecord.java
Defines the StudentRecord class to store student details (ID, last name, first name, grade, overflow link).
- `convertIntoBytes()`: Converts a student record to a 64-byte array for storage.
- `convertFromBytes()`: Reconstructs a student record from a byte array.

### Helper Functions
Fixed-size byte encoding ensures consistent data length in storage.

## Features

- **Efficient Indexing**: Utilizes a B+ Tree for fast data search and retrieval by indexing student IDs.
- **Persistent Storage**: Stores data in a file-based system, enabling records to be saved and accessed across sessions.
- **Command-line Interface**: Provides interactive commands for adding, displaying, loading, and merging records.
- **Overflow Handling**: Manages overflow data in a secondary file, supporting optimized storage during merges.

