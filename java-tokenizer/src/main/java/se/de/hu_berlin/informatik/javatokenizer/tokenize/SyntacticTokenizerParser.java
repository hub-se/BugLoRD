package se.de.hu_berlin.informatik.javatokenizer.tokenize;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.printer.PrettyPrinterConfiguration;
import se.de.hu_berlin.informatik.javatokenizer.tokenizer.Tokenizer;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Parser module that tokenizes a given input file and outputs a
 * {@link List} of tokenized lines as {@link String}s. Uses an
 * original implementation of a Java tokenizer. For the tokenization
 * of only methods, the "com.github.javaparser" framework is used
 * to obtain the method bodies up front.
 *
 * @author Simon Heiden
 * @see Tokenizer
 */
public class SyntacticTokenizerParser extends AbstractProcessor<Path, List<String>> {

    public static final Charset[] charsets = {
            StandardCharsets.UTF_8, StandardCharsets.ISO_8859_1,
            StandardCharsets.US_ASCII, StandardCharsets.UTF_16,
            StandardCharsets.UTF_16BE, StandardCharsets.UTF_16LE};

    private boolean methodsOnly = false;
    private boolean eol = false;

    /**
     * Creates a new {@link SyntacticTokenizerParser} object with the given parameters.
     *
     * @param methodsOnly determines if only method bodies should be tokenized
     * @param eol         determines if ends of lines (EOL) are relevant
     */
    public SyntacticTokenizerParser(boolean methodsOnly, boolean eol) {
        this.methodsOnly = methodsOnly;
        this.eol = eol;
    }


    /**
     * Tokenizes the method bodys of a Java (1.8) source code input file and writes the output to another file.
     *
     * @param inputFile is the path to the input file as a {@link String}
     * @param eol       determines if ends of lines (EOL) are relevant
     * @return the tokenized methods as a {@link List} of {@link String}s or null if an error occurred
     */
    private List<String> createTokenizedMethodOutput(final Path inputFile, final boolean eol) {
        CompilationUnit cu = null;
        StringBuilder builder = new StringBuilder();
        String input = inputFile.toString();
        try (FileInputStream in = new FileInputStream(input)) {
            // parse the file
            cu = JavaParser.parse(in);

        } catch (FileNotFoundException e) {
            Log.err(this, e, "File not found on file %s.", inputFile.toString());
            return null;
        } catch (IOException e) {
            Log.err(this, e, "IO Exception on file %s.", inputFile.toString());
            return null;
        }

        PrettyPrinterConfiguration configuration = new PrettyPrinterConfiguration().setPrintComments(false);
        try {
            if (cu != null) {
                List<TypeDeclaration<?>> types = cu.getTypes();
                for (TypeDeclaration<?> type : types) {
                    List<BodyDeclaration<?>> members = type.getMembers();
                    for (BodyDeclaration<?> member : members) {
                        if (member instanceof MethodDeclaration) {
                            MethodDeclaration method = (MethodDeclaration) member;
                            if (method.getBody().isPresent()) {
                                builder.append(Misc.replaceWhitespacesInString(method.getBody().get().toString(configuration), " "));
                                builder.append("\n");
                            }
                        }
                    }
                }

                if (builder.length() > 0) {
                    //tokenize the method bodies contained in builder
                    return createTokenizedOutput(builder.toString(), eol);
                } else {
//					Misc.err(this, "No methods...");
                    return Collections.emptyList();
                }
            }
        } catch (NullPointerException e) {
            Log.err(this, e, "Null Pointer Exception...");
            return null;
        }

        return null;
    }

    /**
     * Tokenizes the given file and writes the tokenized file to the given output file.
     *
     * @param inputFile is the path to the input file as a {@link Path}
     * @param eol       determines if ends of lines (EOL) are relevant
     * @return the tokenized lines as a {@link List} of {@link String}s
     */
    private List<String> createTokenizedOutput(final Path inputFile, final boolean eol) {
        //try opening the file with different charsets
        for (Charset charset : charsets) {
            try (BufferedReader reader = Files.newBufferedReader(inputFile, charset)) {
                StreamTokenizer st = new StreamTokenizer(reader);
                return createTokenizedOutput(st, eol);
            } catch (IOException x) {
                //try next charset
            }
        }
        Log.err(this, "unknown charset!");
        return null;
    }

    /**
     * Tokenizes the given {@link String} and writes the tokenized file to the given output file.
     *
     * @param inputString contains the contents of the source code file
     * @param eol         determines if ends of lines (EOL) are relevant
     * @return the tokenized lines as a {@link List} of {@link String}s
     */
    private List<String> createTokenizedOutput(final String inputString, final boolean eol) {
        try (StringReader reader = new StringReader(inputString)) {
            StreamTokenizer st = new StreamTokenizer(reader);
            return createTokenizedOutput(st, eol);
        } catch (IOException x) {
            Log.err(this, x, "IOException!");
            return null;
        }
    }

    /**
     * Tokenizes all lines in the file that is the input for the given {@link StreamTokenizer}
     *
     * @param inputStreamTokenizer has the input source code file as its input
     * @param eol                  determines if ends of lines (EOL) are relevant
     * @return a {@link List} that contains all tokenized lines
     * @throws IOException if the {@link StreamTokenizer} has a problem
     */
    private List<String> createTokenizedOutput(final StreamTokenizer inputStreamTokenizer, final boolean eol) throws IOException {
        Tokenizer tokenizer = new Tokenizer(inputStreamTokenizer, eol);

        String token;
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        int ttype = 0;
        while (ttype != StreamTokenizer.TT_EOF) {
            if ((token = tokenizer.getNextToken()) != null) {
                line.append(token).append(" ");
            }
            ttype = tokenizer.getTtype();
            if (ttype == StreamTokenizer.TT_EOL && eol) {
                if (line.length() != 0) {
                    //delete the last space
                    line.deleteCharAt(line.length() - 1);
                }
                //add the line to the line array
                lines.add(line.toString());
                //reuse the StringBuilder
                line.setLength(0);
            }
        }

        if (line.length() != 0) {
            //delete the last space
            line.deleteCharAt(line.length() - 1);
            //add the line to the line array
            lines.add(line.toString());
        }

        return lines;
    }


    @Override
    public List<String> processItem(Path inputPath) {
        if (methodsOnly) {
            return createTokenizedMethodOutput(inputPath, eol);
        } else {
            return createTokenizedOutput(inputPath, eol);
        }
    }


}
