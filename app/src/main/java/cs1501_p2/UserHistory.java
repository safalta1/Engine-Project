package cs1501_p2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.io.File;
import java.io.Serializable;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class UserHistory implements Dict, Serializable {
    private TrieNode root;
    private TrieNode searchState; // For by-character search
     private StringBuilder searchPrefix;

    public UserHistory() {
        root = new TrieNode('\0');
        searchState = root; // search state to root
         searchPrefix = new StringBuilder();
    }

    public UserHistory(String filePath) {
        this(); 
        loadFromFile(filePath);
    }

    private void loadFromFile(String filePath) {
        try (Scanner scanner = new Scanner(new File(filePath))) {
            while (scanner.hasNextLine()) {
                String word = scanner.nextLine();
                add(word);
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + filePath + ". Initializing an empty UserHistory object.");
            
        }
    }

    public void add(String key) {
    TrieNode node = root;
    for (char c : key.toCharArray()) {
        node = node.children.computeIfAbsent(c, k -> new TrieNode(c));
        
    }
    node.frequency++; // increment frequency at the word's end
    node.isWordEnd = true; // Mark as end of word
    node.word = key; // Store complete word at the end node
}

    @Override
    public boolean contains(String key) {
        TrieNode node = getNode(key);
        return node != null && node.isWordEnd;
    }

    @Override
    public boolean containsPrefix(String pre) {
        return getNode(pre) != null;
    }

    @Override
    public int searchByChar(char next) {
        searchPrefix.append(next); // Append the character to the current search prefix
        if (searchState != null && searchState.children.containsKey(next)) {
            searchState = searchState.children.get(next);
            if (searchState.isWordEnd && !searchState.children.isEmpty()) {
                return 2; // Valid word and has children
            } else if (searchState.isWordEnd) {
                return 1; // Valid word, no further children
            }
            return 0; // Not a word end but has children
        }
        return -1; // Not a valid word or prefix
    }


   @Override
    public void resetByChar() {
        searchState = root; // Reset to start a new search
        searchPrefix.setLength(0); // Clear the search prefix
    }

    @Override
public ArrayList<String> suggest() {
    ArrayList<String> suggestions = new ArrayList<>();
    if (searchState == null) return suggestions; // Early return if searchState is null

    PriorityQueue<TrieNode> pq = new PriorityQueue<>((a, b) -> {
        if (b.frequency != a.frequency) return b.frequency - a.frequency; // Order by frequency
        return a.word.compareTo(b.word); // Secondary sort by alphabetical order
    });

    collectSuggestions(searchState, new StringBuilder(searchPrefix), pq, 5);

    
    while (!pq.isEmpty() && suggestions.size() < 5) {
        TrieNode node = pq.poll();
        suggestions.add(node.word); // Add directly to maintain correct priority
    }

    return suggestions;
}

    @Override
    public ArrayList<String> traverse() {
        ArrayList<String> words = new ArrayList<>();
        traverseHelper(root, new StringBuilder(), words);
        return words;
    }

    @Override
    public int count() {
        return countWords(root);
    }

   

    private TrieNode getNode(String str) {
        TrieNode node = root;
        for (char c : str.toCharArray()) {
            if (!node.children.containsKey(c)) {
                return null; // Path does not exist
            }
            node = node.children.get(c);
        }
        return node;
    }

    
    private void collectSuggestions(TrieNode node, StringBuilder prefix, PriorityQueue<TrieNode> pq, int limit) {
    if (node.isWordEnd) {
        TrieNode temp = new TrieNode(node.character);
        temp.word = prefix.toString(); // Ensure this accurately constructs the word
        temp.frequency = node.frequency;
        pq.offer(temp);
    }

    for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
        prefix.append(entry.getKey());
        collectSuggestions(entry.getValue(), prefix, pq, limit);
        prefix.setLength(prefix.length() - 1); // Backtrack to ensure correct prefix
    }
}

    private void traverseHelper(TrieNode node, StringBuilder prefix, ArrayList<String> words) {
        if (node.isWordEnd) {
            words.add(prefix.toString());
        }
        for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
            traverseHelper(entry.getValue(), new StringBuilder(prefix).append(entry.getKey()), words);
        }
    }

    private int countWords(TrieNode node) {
        int count = node.isWordEnd ? 1 : 0;
        for (TrieNode child : node.children.values()) {
            count += countWords(child);
        }
        return count;
    }

    private static class TrieNode {
        char character;
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isWordEnd = false;
        int frequency = 0;
        String word; 

        TrieNode(char character) {
            this.character = character;
        }

        
    }
}