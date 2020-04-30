package se.de.hu_berlin.informatik.javatokenizer.tokenize;

import se.de.hu_berlin.informatik.astlmbuilder.ASTTokenReader;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper.IBasicNodeMapper;
import se.de.hu_berlin.informatik.javatokenizer.tokenizer.SemanticMapper;
import se.de.hu_berlin.informatik.javatokenizer.tokenizer.Tokenizer;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

import java.nio.file.Path;
import java.util.ArrayList;
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
public class SemanticTokenizerParser extends AbstractProcessor<Path, List<String>> {

    private boolean eol = false;

    private final ASTTokenReader<String> reader;

    /**
     * Creates a new {@link SemanticTokenizerParser} object with the given parameters.
     *
     * @param methodsOnly         determines if only method bodies should be tokenized
     * @param eol                 determines if ends of lines (EOL) are relevant
     * @param long_tokens         whether long tokens should be produced
     * @param depth               the maximum depth of constructing the tokens, where 0 equals
     *                            total abstraction and -1 means unlimited depth
     * @param includeParent       whether to include information about the parent node in the tokens
     * @param childCountStepWidth the grouping step width to use for grouping nodes based on their number of child nodes (log-based)
     */
    public SemanticTokenizerParser(boolean methodsOnly, boolean eol, boolean long_tokens,
                                   int depth, boolean includeParent, int childCountStepWidth) {
        this.eol = eol;

        IBasicNodeMapper<String> mapper = new SemanticMapper(long_tokens, childCountStepWidth).getMapper();

        reader = new ASTTokenReader<>(
                mapper, null, null, methodsOnly, true, depth, includeParent);
    }

    @Override
    public List<String> processItem(Path inputPath) {
        List<List<String>> list = ASTTokenReader.getAllTokenSequencesAndFixCertainErrors(reader, inputPath);

        List<String> result = new ArrayList<>(list.size());
        for (List<String> element : list) {
//			Misc.out(Misc.arrayToString(element.toArray(new String[0])));
            result.add(String.join(" ", element));
        }

        if (!eol) {
            String temp = String.join(" ", result);
            result = new ArrayList<>(1);
            result.add(temp);
        }

        return result;
    }

}
