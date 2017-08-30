package se.de.hu_berlin.informatik.experiments.lm;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD;
import se.de.hu_berlin.informatik.experiments.lm.BuildLanguageModel.CmdOptions;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.files.processors.FileToStringListReader;
import se.de.hu_berlin.informatik.utils.files.processors.ListToFileWriter;
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

		String outputPrefix = output.getFileName().toString();
		
		Path temporaryFilesDir = BugLoRD.getNewTmpDir();
//				.resolve(inputDir.getFileName())
//				.resolve("_tempLMDir" + order + "_");
//		FileUtils.delete(temporaryFilesDir);

		// file that contains a list of all token files (needed by SRILM)
		Path listFile = inputDir.resolve("file.list");
		if (!listFile.toFile().exists()) {
			Log.abort(this, "Token list file '%s' does not exist.", listFile);
		}

		// make batch counts with SRILM
		String countsDir = temporaryFilesDir + Defects4J.SEP + "counts";
		Paths.get(countsDir).toFile().mkdirs();
		Defects4J.executeCommand(
				temporaryFilesDir.toFile(), true, BugLoRD.getSRILMMakeBatchCountsExecutable(), listFile.toString(), "10",
				"/bin/cat", countsDir, "-order", String.valueOf(order), "-unk");

		// merge batch counts with SRILM
		Defects4J.executeCommand(temporaryFilesDir.toFile(), true, BugLoRD.getSRILMMergeBatchCountsExecutable(), countsDir);
		
		// estimate language model of order n with SRILM
		String tempArpalLM = temporaryFilesDir.resolve(outputPrefix + "_order" + order + ".arpa").toString();
		Defects4J.executeCommand(
				temporaryFilesDir.toFile(), true, BugLoRD.getSRILMMakeBigLMExecutable(), "-read",
				countsDir + Defects4J.SEP + "*.gz", "-lm", tempArpalLM, "-order", String.valueOf(order), "-unk");

		// log the first 100 lines of the arpa file and its file size
		logArpaFile(tempArpalLM, output);

		if (socket.getOptions().hasOption(CmdOptions.GEN_BINARY)) {
			// build binary with kenLM
			String binaryLM = output + "_order" + order + ".binary";
			Defects4J.executeCommand(
					temporaryFilesDir.toFile(), false, BugLoRD.getKenLMBinaryExecutable(), tempArpalLM, binaryLM);
			if (!socket.getOptions().hasOption(CmdOptions.KEEP_ARPA)) {
				FileUtils.delete(new File(tempArpalLM));
			} else {
				copyArpa(order, tempArpalLM);
			}
		} else {
			copyArpa(order, tempArpalLM);
		}

		// delete the temporary LM files
		FileUtils.delete(temporaryFilesDir);
	}

	private void logArpaFile(String tempArpalLM, Path output) {
		File arpa = new File(tempArpalLM);

		if (arpa.exists()) {
			Path logOutput = output.getParent().resolve("arpaLog");
			List<String> result = new FileToStringListReader(0, 10000).submit(arpa.toPath()).getResult();
			new ListToFileWriter<>(logOutput.resolve(arpa.getName() + "-" + arpa.length() + ".log"), true).submit(result);
		}
	}

	private void copyArpa(Integer order, String tempArpalLM) {
		String arpaLM = output + "_order" + order + ".arpa";
		try {
			FileUtils.copyFileOrDir(new File(tempArpalLM), new File(arpaLM));
			FileUtils.delete(new File(tempArpalLM));
		} catch (IOException e) {
			Log.abort(this, "Could not copy '%s' to '%s'.", tempArpalLM, arpaLM);
		}
	}

}
