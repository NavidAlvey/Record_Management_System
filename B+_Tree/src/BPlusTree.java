import java.io.*;
import java.util.ArrayList;
import java.util.List;

class BPlusTree {
    private static final int M = 4;  // This is a small tree order for simplicity
    private Node root; // The root node of the B+ Tree
    private RandomAccessFile indexFile; // File used to store the B+ Tree index data

    // Constructor that initializes the B+ tree using a file
    public BPlusTree(File file) throws IOException {
        this.indexFile = new RandomAccessFile(file, "rw");
        // Initialize or load the tree depending on whether the file is empty
        if (indexFile.length() == 0) {
            initialize();
        } else {
            loadTree();
        }
    }

    // Initializes a new tree with a root as a leaf node
    private void initialize() throws IOException {
        root = new LeafNode();
        saveTree();
    }

    // Loads the tree from the file by reading the root node
    private void loadTree() throws IOException {
        indexFile.seek(0); // Start reading from the beginning of the file
        long rootOffset = indexFile.readLong(); // Read the root node's offset
        root = readNode(rootOffset);  // Read the node from the offset
    }

    // Saves the tree root's offset in the file
    private void saveTree() throws IOException {
        saveNode(root);
        indexFile.seek(0); // Move to the beginning of the file
        indexFile.writeLong(root.offset); // Write the root's offset
    } 

    // Reads a node from the file at a given offset
    private Node readNode(long offset) throws IOException {
        indexFile.seek(offset);
        boolean isLeaf = indexFile.readBoolean(); // Read whether the node is a leaf
        // Return a new node based on whether it is a leaf or internal node
        return isLeaf ? new LeafNode(indexFile, offset) : new InternalNode(indexFile, offset);
    }

    // Saves a node's data to the file
    private void saveNode(Node node) throws IOException {
        indexFile.seek(node.offset); // Set the file pointer to the node's offset
        indexFile.writeBoolean(node.isLeaf()); // Write whether the node is a leaf
        node.write(indexFile); // Write the node's internal data
    }

    // Inserts a key and value in the tree and handles splitting
    public void insert(long key, long value) throws IOException {
        Split split = root.insert(key, value); // Attempt to insert the key-value pair
        if (split != null) {
            // If root splits, create a new root
            InternalNode newRoot = new InternalNode();
            newRoot.keys.add(split.key);
            newRoot.children.add(root.offset);
            newRoot.children.add(split.right.offset);
            root = newRoot;
            saveNode(split.right); // Save the new right node
            saveTree(); // Save the new tree structure
        }
        saveNode(root); // Save the root node
    }

    // Searches for a value by key
    public Long search(long key) throws IOException {
        return root.search(key);
    }

    // Closes the file used by the B+ Tree
    public void close() throws IOException {
        indexFile.close();
    }

    // Base class for tree nodes
    abstract class Node {
        List<Long> keys; // Keys stored in the node
        long offset; // Offset of the node in the file

        abstract boolean isLeaf();

        abstract Long search(long key) throws IOException;

        abstract Split insert(long key, long value) throws IOException;

        abstract void write(RandomAccessFile file) throws IOException;
    }

    // Represents a split operation result
    class Split {
        long key; // The key at which the split occurs
        Node right; // The new right node after the split


        Split(long key, Node right) {
            this.key = key;
            this.right = right;
        }
    }

    // Internal node class
    class InternalNode extends Node {
        List<Long> children; // Children pointers

        InternalNode() throws IOException {
            this.offset = indexFile.length(); // Set offset at the end of the file
            keys = new ArrayList<>();
            children = new ArrayList<>();
        }

        InternalNode(RandomAccessFile file, long offset) throws IOException {
            this.offset = offset;
            keys = new ArrayList<>();
            children = new ArrayList<>();
            int keySize = file.readInt(); // Read the number of keys
            for (int i = 0; i < keySize; i++) {
                keys.add(file.readLong());
                children.add(file.readLong()); 
            }
            children.add(file.readLong()); // Read the last child
        }

        boolean isLeaf() {
            return false;
        }

        Long search(long key) throws IOException {
            // Binary search on keys and find appropriate child node to continue searching
            int loc = 0;
            while (loc < keys.size() && key >= keys.get(loc)) {
                loc++;
            }
            return readNode(children.get(loc)).search(key);
        }

        Split insert(long key, long value) throws IOException {
            // Find correct child, insert and handle split if necessary
            int loc = 0;
            while (loc < keys.size() && key >= keys.get(loc)) {
                loc++;
            }
            Split split = readNode(children.get(loc)).insert(key, value);
            if (split == null) return null;
            
            int index = loc;
            keys.add(index, split.key);
            children.add(index + 1, split.right.offset);
            if (keys.size() < M) return null;
            
            // Split the node if it overflows
            int mid = keys.size() / 2;
            long midKey = keys.get(mid);
            InternalNode sibling = new InternalNode();
            while (keys.size() > mid) {
                sibling.keys.add(keys.remove(mid));
                sibling.children.add(children.remove(mid + 1));
            }
            return new Split(midKey, sibling);
        }

        void write(RandomAccessFile file) throws IOException {
            // Write node data to file
            file.writeInt(keys.size());
            for (int i = 0; i < keys.size(); i++) {
                file.writeLong(keys.get(i));
                file.writeLong(children.get(i));
            }
            file.writeLong(children.get(children.size() - 1)); // Write the last child
        }
    }

    // Leaf node class
    class LeafNode extends Node {
        List<Long> values; // Values corresponding to the keys

        LeafNode() throws IOException {
            this.offset = indexFile.length(); // Set offset at the end of the file
            keys = new ArrayList<>();
            values = new ArrayList<>();
        }

        LeafNode(RandomAccessFile file, long offset) throws IOException {
            this.offset = offset;
            keys = new ArrayList<>();
            values = new ArrayList<>();
            int keySize = file.readInt(); // Read the number of keys
            for (int i = 0; i < keySize; i++) {
                keys.add(file.readLong());
                values.add(file.readLong());
            }
        }

        boolean isLeaf() {
            return true;
        }

        Long search(long key) {
            // Linear search to find the key and return its corresponding value
            int loc = 0;
            while (loc < keys.size() && key > keys.get(loc)) {
                loc++;
            }
            if (loc < keys.size() && keys.get(loc) == key) return values.get(loc);
            return null;
        }

        Split insert(long key, long value) throws IOException {
            // Insert key and value in sorted order and handle split if necessary
            int loc = 0;
            while (loc < keys.size() && key > keys.get(loc)) {
                loc++;
            }
            keys.add(loc, key);
            values.add(loc, value);
            if (keys.size() < M) return null;

            // Split the node if it overflows
            int mid = keys.size() / 2;
            long midKey = keys.get(mid);
            LeafNode sibling = new LeafNode();
            while (keys.size() > mid) {
                sibling.keys.add(keys.remove(mid));
                sibling.values.add(values.remove(mid));
            }
            return new Split(midKey, sibling);
        }

        void write(RandomAccessFile file) throws IOException {
            // Write node data to file
            file.writeInt(keys.size());
            for (int i = 0; i < keys.size(); i++) {
                file.writeLong(keys.get(i));
                file.writeLong(values.get(i));
            }
        }
    }
}
