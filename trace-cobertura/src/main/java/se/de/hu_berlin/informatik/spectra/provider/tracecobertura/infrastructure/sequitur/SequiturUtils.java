package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.CoverageIgnore;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.input.InputSequence;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.input.SharedInputGrammar;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.output.OutputSequence;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.output.SharedOutputGrammar;

import java.io.*;

@CoverageIgnore
public class SequiturUtils {

    public static InputSequence getInputSequenceFromByteArray(byte[] bytes,
                                                              SharedInputGrammar inGrammar) throws IOException {
        ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
        InputStream buffer = new BufferedInputStream(byteIn);
        ObjectInputStream objIn = new ObjectInputStream(buffer);
        try {
        	if (inGrammar == null) {
        		InputSequence inputSequence = InputSequence.readFrom(objIn);
        		return inputSequence;
        	} else {
        		InputSequence inputSequence = InputSequence.readFrom(objIn, inGrammar);
        		return inputSequence;
        	}
        } finally {
        	objIn.close();
		}
    }

    public static InputSequence getInputSequenceFromByteArray(byte[] bytes)
            throws IOException {
        return getInputSequenceFromByteArray(bytes, null);
    }
    
    public static SharedInputGrammar getInputGrammarFromByteArray(byte[] storedGrammar)
            throws IOException {
        if (storedGrammar == null) {
            return null;
        }
        // load the current shared grammar (convert from byte array to input grammar...)
        ByteArrayInputStream byteIn = new ByteArrayInputStream(storedGrammar);
        InputStream buffer = new BufferedInputStream(byteIn);
        ObjectInputStream objIn = new ObjectInputStream(buffer);
        try {
        	return SharedInputGrammar.readFrom(objIn);
        } finally {
        	objIn.close();
		}
    }

    public static byte[] convertToByteArray(SharedOutputGrammar outputGrammar)
            throws IOException {
        if (outputGrammar == null) {
            return null;
        }
        // store/load the current shared grammar (convert from output grammar to byte array...)
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        OutputStream buffer = new BufferedOutputStream(byteOut);
        ObjectOutputStream objOut = new ObjectOutputStream(buffer);
        try {
        	outputGrammar.writeOut(objOut);
        	objOut.flush();
        	return byteOut.toByteArray();
        } finally {
        	objOut.close();
		}
    }

    public static byte[] convertToByteArray(OutputSequence outSeq, final boolean includeGrammar)
            throws IOException {
    	ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    	OutputStream buffer = new BufferedOutputStream(byteOut);
        ObjectOutputStream objOut = new ObjectOutputStream(buffer);
    	try {
    		outSeq.writeOut(objOut, includeGrammar);
    		objOut.flush();
    		return byteOut.toByteArray();
    	} finally {
    		objOut.close();
    	}
    }

    
    public static SharedInputGrammar convertToInputGrammar(SharedOutputGrammar outputGrammar)
            throws IOException {
        return getInputGrammarFromByteArray(convertToByteArray(outputGrammar));
    }

}
