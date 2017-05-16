package se.de.hu_berlin.informatik.experiments.lm;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD;
import se.de.hu_berlin.informatik.experiments.lm.BuildLanguageModel.CmdOptions;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractConsumingProcessor;
import se.de.hu_berlin.informatik.utils.processors.sockets.ProcessorSocket;

public class LMBuilder extends AbstractConsumingProcessor<Integer> {

	
	private Path inputDir;
	private Path output;

	public LMBuilder(Path inputDir, Path output) {
		this.inputDir = inputDir;
		this.output = output;
	}
	
	@Override
	public void consumeItem(Integer order, ProcessorSocket<Integer, Object> socket) {
		socket.requireOptions();
		
		Path temporaryFilesDir = inputDir.resolve("_tempLMDir" + order + "_");
		FileUtils.delete(temporaryFilesDir);
		
		//file that contains a list of all token files (needed by SRILM)
		Path listFile = inputDir.resolve("file.list");
		if (!listFile.toFile().exists()) {
			Log.abort(this, "Token list file '%s' does not exist.", listFile);
		}
		
		//make batch counts with SRILM
		String countsDir = temporaryFilesDir + Defects4J.SEP + "counts";
		Paths.get(countsDir).toFile().mkdirs();
		Defects4J.executeCommand(temporaryFilesDir.toFile(), BugLoRD.getSRILMMakeBatchCountsExecutable(), 
				listFile.toString(), "10", "/bin/cat", countsDir, "-order", String.valueOf(order), "-unk");
		
		//merge batch counts with SRILM
		Defects4J.executeCommand(temporaryFilesDir.toFile(), BugLoRD.getSRILMMergeBatchCountsExecutable(), countsDir);
		
		//estimate language model of order n with SRILM
		String arpalLM = output + "_order" + order + ".arpa";
		Defects4J.executeCommand(temporaryFilesDir.toFile(), BugLoRD.getSRILMMakeBigLMExecutable(), "-read", 
				countsDir + Defects4J.SEP + "*.gz", "-lm", arpalLM, "-order", String.valueOf(order), "-unk");
		
		if (getOptions().hasOption(CmdOptions.GEN_BINARY)) {
			//build binary with kenLM
			String binaryLM = output + "_order" + order + ".binary";
			Defects4J.executeCommand(temporaryFilesDir.toFile(), BugLoRD.getKenLMBinaryExecutable(),
					arpalLM, binaryLM);
			if (new File(binaryLM).exists() && !getOptions().hasOption(CmdOptions.KEEP_ARPA)) {
				FileUtils.delete(new File(arpalLM));
			}
		}
		
		//delete the temporary LM files
		FileUtils.delete(temporaryFilesDir);
	}

}
