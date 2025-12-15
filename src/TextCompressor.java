/******************************************************************************
 *  Compilation:  javac TextCompressor.java
 *  Execution:    java TextCompressor - < input.txt   (compress)
 *  Execution:    java TextCompressor + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *  Data files:   abra.txt
 *                jabberwocky.txt
 *                shakespeare.txt
 *                virus.txt
 *
 *  % java DumpBinary 0 < abra.txt
 *  136 bits
 *
 *  % java TextCompressor - < abra.txt | java DumpBinary 0
 *  104 bits    (when using 8-bit codes)
 *
 *  % java DumpBinary 0 < alice.txt
 *  1104064 bits
 *  % java TextCompressor - < alice.txt | java DumpBinary 0
 *  480760 bits
 *  = 43.54% compression ratio!
 ******************************************************************************/

/**
 *  The {@code TextCompressor} class provides static methods for compressing
 *  and expanding natural language through textfile input.
 *
 *  @author Zach Blick, Oliver Faris
 */
public class TextCompressor {
    private static final int EOF = 128;

    private static void compress() {
        TST tst = new TST();
        String text = BinaryStdIn.readString();
        int index = 0;
        String prefix = "";
        int code;
        int prefixNum = 0x81;
        char nextChar;

        String str;
        // Fill tst with existing values
        for (int i = 0; i < 128; i++) {
            str = String.valueOf(((char) i));
            tst.insert(str, i);
        }

        while (index < text.length()) {
            prefix = tst.getLongestPrefix(text.substring(index));

            // Write out the code
            code = tst.lookup(prefix);
            BinaryStdOut.write(code, 8);

            if (index < text.length()-prefix.length()-1 && prefixNum < 255) {
                // Look ahead to add the next char to the prefix to add to the TST
                nextChar = text.charAt(index + prefix.length());
                String prefixCopy = prefix + nextChar;
                tst.insert(prefixCopy, prefixNum);
                prefixNum++;
            }
            index += prefix.length();

        }
        BinaryStdOut.write(EOF, 8);
        BinaryStdOut.close();
    }

    private static void expand() {
        String[] prefixes = new String[256];
        int newCodeNum = 129;
        // Add existing values into map
        for (int i = 0; i < 128; i++) {
            prefixes[i] = String.valueOf(((char) i));
        }

        int code = BinaryStdIn.readInt(8);
        int lookAheadCode = BinaryStdIn.readInt(8);
        String codeStr;
        String lookAheadStr;

        while (lookAheadCode != EOF) {
            codeStr = prefixes[code];

            // Edge case where lookAheadCode is null
            if (lookAheadCode == newCodeNum)
                lookAheadStr = codeStr + codeStr.charAt(0);
            else {
                // Normal
                lookAheadStr = prefixes[lookAheadCode];
            }

            BinaryStdOut.write(codeStr);

            // Add new codes to map
            if (newCodeNum < 255) {
                prefixes[newCodeNum] = codeStr + lookAheadStr.charAt(0);
                newCodeNum++;
            }

            // Go "forward"
            code = lookAheadCode;
            lookAheadCode = BinaryStdIn.readInt(8);
        }
        // Write the last character
        BinaryStdOut.write(prefixes[code]);

        BinaryStdOut.close();
    }

    public static void main(String[] args) {
        if      (args[0].equals("-")) compress();
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }
}
