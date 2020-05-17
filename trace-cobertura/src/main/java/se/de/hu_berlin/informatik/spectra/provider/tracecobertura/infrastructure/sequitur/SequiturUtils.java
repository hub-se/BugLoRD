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
        ObjectInputStream objIn = new ObjectInputStream(byteIn);
        if (inGrammar == null) {
            InputSequence inputSequence = InputSequence.readFrom(objIn);
            return inputSequence;
        } else {
            InputSequence inputSequence = InputSequence.readFrom(objIn, inGrammar);
            return inputSequence;
        }
    }

    public static InputSequence getInputSequenceFromByteArray(byte[] bytes)
            throws IOException {
        return getInputSequenceFromByteArray(bytes, null);
    }

    public static byte[] convertToByteArray(SharedOutputGrammar outputGrammar)
            throws IOException {
        if (outputGrammar == null) {
            return null;
        }
        // store/load the current shared grammar (convert from output grammar to byte array...)
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
        outputGrammar.writeOut(objOut);
        objOut.close();
        byte[] bytesg = byteOut.toByteArray();

        return bytesg;
    }

    public static byte[] convertToByteArray(OutputSequence outSeq, final boolean includeGrammar)
            throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
        outSeq.writeOut(objOut, includeGrammar);
        objOut.close();
        byte[] bytes = byteOut.toByteArray();

        return bytes;
    }

    public static SharedInputGrammar convertToInputGrammar(byte[] storedGrammar)
            throws IOException {
        if (storedGrammar == null) {
            return null;
        }
        // load the current shared grammar (convert from byte array to input grammar...)
        ByteArrayInputStream byteIng = new ByteArrayInputStream(storedGrammar);
        ObjectInputStream objIng = new ObjectInputStream(byteIng);
        SharedInputGrammar inGrammar = SharedInputGrammar.readFrom(objIng);
        return inGrammar;
    }

    public static SharedInputGrammar convertToInputGrammar(SharedOutputGrammar outputGrammar)
            throws IOException {
        return convertToInputGrammar(convertToByteArray(outputGrammar));
    }

}
