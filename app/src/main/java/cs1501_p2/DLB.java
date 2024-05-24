package cs1501_p2;

import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class DLB implements Dict {
    private DLBNode root;
    private int wcount;
    private DLBNode searchNode; // Used for by-character search
    private ArrayList<Character> searchPrefix; // Tracks the current prefix for by-character search

    public DLB() {
        root = new DLBNode('\0'); // Root node with a character
        wcount = 0;
        searchPrefix = new ArrayList<>();
        resetByChar(); // searchNode to root for by-character search
    }

    public DLB(String filePath) {
    this(); // Call the default constructor to initialize the DLB structure
    try {
        loadFromFile(filePath);
    } catch (Exception e) {
        System.err.println("Error loading from file: " + filePath + ". Initializing an empty DLB object.");
       
    }
}

private void loadFromFile(String filePath) throws FileNotFoundException {
    File file = new File(filePath);
    Scanner scanner = new Scanner(file);
    while (scanner.hasNextLine()) {
        String word = scanner.nextLine().trim();
        if (!word.isEmpty()) {
            add(word);
        }
    }
    scanner.close();
}

    @Override
public void add(String key) {
    if (key == null || key.isEmpty()) return;

    DLBNode node = root;
    for (char c : key.toCharArray()) {
        if (node.child == null) {
            node.child = new DLBNode(c);
            node = node.child;
        } else {
            node = node.child;
            while (node.character != c && node.sibling != null) {
                node = node.sibling;
            }
            if (node.character != c) {
                node.sibling = new DLBNode(c);
                node = node.sibling;
            }
        }
    }
    if (!node.isWordEnd) {
        node.isWordEnd = true;
        wcount++;
    }
}

    @Override
    public boolean contains(String key) {
        DLBNode node = findNode(key);
        return node != null && node.isWordEnd;
    }

    @Override
    public boolean containsPrefix(String pre) {
        return findNode(pre) != null;
    }

    private DLBNode findNode(String str) {
        DLBNode node = root;
        for (char c : str.toCharArray()) {
            node = node.child;
            while (node != null && node.character != c) {
                node = node.sibling;
            }
            if (node == null) {
                return null;
            }
        }
        return node;
    }

   @Override
    public int searchByChar(char next) {
        searchPrefix.add(next);
        if (searchNode.character == '\0') searchNode = searchNode.child;
        
        while (searchNode != null && searchNode.character != next) {
            searchNode = searchNode.sibling;
        }

        if (searchNode == null) {
            resetByChar();
            return -1;
        } else {
            if (searchNode.isWordEnd && searchNode.child != null) {
                searchNode = searchNode.child; // to child for next search
                return 2; // Is word end and has next char
            } else if (searchNode.isWordEnd) {
                return 1; // no next char
            } else if (searchNode.child != null) {
                searchNode = searchNode.child; // for next character search
                return 0; // but has next char
            }
        }
        return -1; // Shouldn't reach here ideally
    }

    @Override
    public void resetByChar() {
        searchNode = root;
        searchPrefix.clear();
    }

   @Override
public ArrayList<String> suggest() {
    ArrayList<String> suggestions = new ArrayList<>();
    
    String currentPrefix = arrayListToString(searchPrefix);

    
    generateSuggestions(root, "", suggestions, currentPrefix, 0);
    return suggestions;
}

private String arrayListToString(ArrayList<Character> list) {
    char[] chars = new char[list.size()];
    for (int i = 0; i < list.size(); i++) {
        chars[i] = list.get(i);
    }
    return new String(chars);
}

private void generateSuggestions(DLBNode node, String prefix, ArrayList<String> suggestions, String currentPrefix, int depth) {
    if (node == null || suggestions.size() >= 5) {
        return;
    }

    // Check if the current node's character is part of the currentPrefix
    if (depth < currentPrefix.length()) {
        char nextChar = currentPrefix.charAt(depth);
        for (DLBNode child = node.child; child != null; child = child.sibling) {
            if (child.character == nextChar) {
                generateSuggestions(child, prefix + nextChar, suggestions, currentPrefix, depth + 1);
                break;
            }
        }
    } else {
        // Collect suggestions if the currentPrefix fully matches
        if (node.isWordEnd) {
            suggestions.add(prefix);
        }
        for (DLBNode child = node.child; child != null; child = child.sibling) {
            generateSuggestions(child, prefix + child.character, suggestions, currentPrefix, depth + 1);
        }
    }
}
    @Override
    public ArrayList<String> traverse() {
        ArrayList<String> words = new ArrayList<>();
        char[] prefixArray = new char[100]; // Assuming a reasonable max length for a word
        traverseHelper(root, prefixArray, 0, words);
        return words;
    }

    @Override
    public int count() {
        return wcount;
    }

    private void collectSuggestions(DLBNode node, char[] prefixArray, int length, ArrayList<String> list, int limit) {
        if (list.size() >= limit || node == null) return;

        char[] newPrefixArray = new char[prefixArray.length];
        System.arraycopy(prefixArray, 0, newPrefixArray, 0, length);

        if (node.character != '\0') {
            newPrefixArray[length++] = node.character;
        }

        if (node.isWordEnd) {
            list.add(new String(newPrefixArray, 0, length));
        }

        if (node.child != null) {
            collectSuggestions(node.child, newPrefixArray, length, list, limit);
        }
        if (node.sibling != null) {
            collectSuggestions(node.sibling, prefixArray, length - 1, list, limit); // Use original length for siblings
        }
    }

    private void traverseHelper(DLBNode node, char[] prefixArray, int length, ArrayList<String> words) {
        if (node == null) return;

        if (node.character != '\0') {
            prefixArray[length++] = node.character;
        }

        if (node.isWordEnd) {
            words.add(new String(prefixArray, 0, length));
        }

        if (node.child != null) {
            traverseHelper(node.child, prefixArray, length, words);
        }
        if (node.sibling != null) {
            traverseHelper(node.sibling, prefixArray, length - 1, words); // Use original length for siblings
        }
    }

    private class DLBNode {
        char character;
        DLBNode sibling, child;
        boolean isWordEnd;

        public DLBNode(char character) {
            this.character = character;
            this.sibling = null;
            this.child = null;
            this.isWordEnd = false;
        }
    }
}