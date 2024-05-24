package cs1501_p2;

import java.util.ArrayList;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

public class AutoCompleter implements AutoComplete_Inter {
    private DLB dict;
    private UserHistory userHistory;

    public AutoCompleter(String dictFilePath) {
        dict = new DLB(dictFilePath);
        userHistory = new UserHistory(); // Initialize with an empty UserHistory 
    }

    public AutoCompleter(String dictFilePath, String userHistoryFilePath) {
        dict = new DLB(dictFilePath);
        userHistory = new UserHistory(userHistoryFilePath);
    }

    @Override
    public ArrayList<String> nextChar(char next) {
        // Update the search states for both DLB and UserHistory
        dict.searchByChar(next);
        userHistory.searchByChar(next);

        // Get suggestions from both UserHistory and DLB
        ArrayList<String> historySuggestions = userHistory.suggest();
        ArrayList<String> dictSuggestions = dict.suggest();

        
        ArrayList<String> combinedSuggestions = new ArrayList<>();
        for (String suggestion : historySuggestions) {
            if (combinedSuggestions.size() < 5) {
                combinedSuggestions.add(suggestion);
            }
        }
        for (String suggestion : dictSuggestions) {
            if (!combinedSuggestions.contains(suggestion) && combinedSuggestions.size() < 5) {
                combinedSuggestions.add(suggestion);
            }
        }

        return combinedSuggestions;
    }

    @Override
    public void finishWord(String cur) {
        // Add the word to UserHistory and reset search states for both DLB and UserHistory
        userHistory.add(cur);
        dict.resetByChar();
        userHistory.resetByChar();
    }

    @Override
    public void saveUserHistory(String fname) {
        try {
            FileOutputStream fileOut = new FileOutputStream(fname);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(userHistory); 
            out.close();
            fileOut.close();
        } catch (Exception e) {
            System.err.println("Error saving user history: " + e.getMessage());
        }
    }
}