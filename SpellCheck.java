import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;

public class SpellCheck {

    public class Trie{
        // define a root for the tree so it is not null
        TrieNode root = new TrieNode();

        class TrieNode{
            // create a base list for all possible children (all of the letters in the alphabet)
            TrieNode[] base = new TrieNode[26];
            // add a flag to denote leaf nodes
            boolean leafNode;
            // add an int storage for how many hits this node has
            int score;

            // constructor
            TrieNode(){
                // not a leaf by default so that instead of changing each node as letters get inserted, I just have to change the last one once
                leafNode = false;

                // set score to 0 by default becasue null never appears
                score = 0;

                for (int i = 0; i < 26; i++) {
                    // set all of them to null by default, since there are no initial values
                    base[i] = null;
                }
            }
        }


        void insert(String word, int weight){
            int wordLen = word.length();
            TrieNode current = root;
            for (int i = 0; i < wordLen; i++) {
                // find the actual index that the letter exists at in the Node's base list and put the next letter there
                // this basically reduces the ASCII value down to the regular value of 0-25 and stores that
                int index = word.charAt(i) - 'a';
                // check if it has been acessed before
                if (current.base[index] == null){
                    // if not then add a new node there, so a branch can be made
                    current.base[index] = new TrieNode();
                    current.base[index].score = weight;
                }
                // update the weight ot the highest given so the validity of links is maintained
                else if (current.base[index].score < weight){
                    current.base[index].score = weight;
                }
                // move current to the next
                current = current.base[index];

            }
            // once the word is inserted, then mark the last node as a leaf
            current.leafNode = true;
        }


        // find a given word in the Trie
        boolean find(String word){
            int wordLen = word.length();
            TrieNode current = root;

            for (int i = 0; i < wordLen; i++) {
                // use the same trick as insert to find correct index
                int index = word.charAt(i) - 'a';

                // if there is no letter there, then return false
                if (current.base[index] == null){
                    return false;
                }
                if ((current.leafNode) && (i + 1 == wordLen)){
                    return true;
                }
                // move current to the next
                current = current.base[index];
            }

            // if it is the end of the word and no null has been hit then return true though this may mean the word exists inside of another word, which is convient fot the pruposes of this assignment
            return true;
        }
    }


    public class Data{
        // this is where I am gonna read from the csv and put all the types (words) into a new Trie
        public Trie populateTree(String fileName){
            Trie returnTrie = new Trie();
            String line = "";
            String splitAt = ",";
            ArrayList<String> wordList = new ArrayList<String>();
            ArrayList<Integer> weightList = new ArrayList<Integer>();
            // read the file and insert all words (with their weights) into the returnTrie
            try{
                BufferedReader br = new BufferedReader(new FileReader("CSVDemo.csv"));
                while ((line = br.readLine()) != null){
                    String[] infoList = line.split(splitAt);
                    int hold = Integer.parseInt(infoList[1]);
                    wordList.add(infoList[0]);
                    weightList.add(hold);
                }
            }
            catch (IOException e){
                System.out.println("Invalid file name/path. File not found!");
                return null;
            }

            // insert each word into the trie
            for (int i = 0; i < wordList.size(); i++) {
                returnTrie.insert(wordList.get(i), weightList.get(i));
            }

            return returnTrie;
        }
    }


    // this is essentially gonna be the driver class because i can't figure out how to give it the Trie otherwise
    public class Spelling{
        public Trie setupTrie;
        public String trieFilling;

        // default constructor
        Spelling(){
            trieFilling = "unigram_freq.csv";
            Data formatData = new Data();
            setupTrie = formatData.populateTree(trieFilling);
        }

        // extra constructor
        Spelling(String fileName){
            trieFilling = fileName;
            Data formatData = new Data();
            setupTrie = formatData.populateTree(trieFilling);
        }

        public List<List<String>> suggest(String token, int count){
            List<List<String>> returnList = new ArrayList<>();
            int wordLen = token.length();
            StringBuilder buildingWord = new StringBuilder();

            for (int i = 0; i < wordLen; i++) {
                // reset current each time to the top so navigation is repeatable
                Trie.TrieNode current = setupTrie.root;
                // grab a new char to add to sb each time
                char newChar = token.charAt(i);
                buildingWord.append(newChar);
                String finWord = buildingWord.toString();

                // with each piece of word, go one further down the Trie and grab the next (whatever count equals) letters with the highest score
                int partWordLen = finWord.length();
                // loop until at one layer under given letters
                for (int k = 0; k < partWordLen; k++) {
                    // use the same trick as insert to find correct index
                    int index = token.charAt(k) - 'a';
                    // move current to the next
                    if (current.base[index] != null){
                        current = current.base[index];
                    }
                }

                // now at one layer under, look at each possible letter and find the (count number of) highest
                // a list to hold the letters that are chosen
                String[] pieceArr = new String[count];
                // an int array to make sure none of the letters are repeated
                int[] indexArr = new int[count];
                for (int j = 0; j < count; j++) {
                    int maxScore = 0;
                    int indVal = -1;
                    // in case I hit a null layer, this will let me know to handle it
                    boolean nullFlag = false;
                    // look at each possible letter
                    for (int k = 0; k < 26; k++) {
                        // set a flag to determine whether or not it is a valid index
                        boolean usedFlag = false;
                        for (int l = 0; l < indexArr.length; l++) {
                            try{
                                if (indexArr[l] == k){
                                    usedFlag = true;
                                }
                            }
                            catch(NullPointerException e){
                                    break;
                            }
                        }
                        if (current == null){
                            nullFlag = true;
                            break;
                        }
                        try{
                            if (current.base[k].score > maxScore && !usedFlag){
                                maxScore = current.base[k].score;
                                indVal = k;
                            }
                        }
                        catch (NullPointerException e){
                            nullFlag = true;
                            break;
                        }
                    }
                    if (!nullFlag) {
                        // convert the index to a letter
                        int index = indVal + 96;
                        // store the letter
                        pieceArr[j] = String.valueOf(index);
                        // store the index
                        indexArr[j] = indVal;
                    }

                }
                returnList.add(Arrays.asList(pieceArr));
            }

            return returnList;
        }
    }


    public static void main(String[] args) {
        String fileName = args[0];
        int count = Integer.parseInt(args[1]);

        List<List<String>> finList;
        SpellCheck outer = new SpellCheck();

        // use the extra constructor to take in the filename
        SpellCheck.Spelling testSpell = outer.new Spelling(fileName);

        // MODIFY THE TOKEN VARIABLE IN THIS LINE TO CHANGE WHAT WORD GETS USED AS THE TOKEN
        finList = testSpell.suggest("test", count);

        System.out.println(finList);

        // Part 4 Response
        // Probably the most effective change that could be made would be if there was a way to take the distance between two keys on a keyboard and have that weigh as a factor on wich letters are prioritized.
    }
}
