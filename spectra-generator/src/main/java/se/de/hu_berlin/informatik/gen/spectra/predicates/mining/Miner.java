package se.de.hu_berlin.informatik.gen.spectra.predicates.mining;

import com.google.common.primitives.Ints;
import org.apache.commons.lang3.tuple.Pair;
import se.de.hu_berlin.informatik.gen.spectra.predicates.extras.Profile;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;

public class Miner {

    public LinkedHashMap<Signature.Identifier, Signature> mine(String folder) {

        String row;
        Database db = new Database();

        Log.out(Miner.class, "Mining %s.", folder);

        //reset file
        try {
            new FileOutputStream(Paths.get(folder, "Signatures.csv").toFile()).close();
        }
        catch (IOException ex) {
            Log.abort(Miner.class,"could not reset Signatures.csv");
        }

        Log.out(this, "Setting up first DB.");
        try {
            BufferedReader csvReader = new BufferedReader(new FileReader(Paths.get(folder, "Profiles.csv").toString()));
            while ((row = csvReader.readLine()) != null) {
                String[] profileString = row.split(";");
                List<Integer> predicates = new ArrayList<>();
                if (!profileString[0].isEmpty()) {
                    predicates = Ints.asList(Arrays.stream(profileString[0].split(",")).mapToInt(Integer::parseInt).toArray());
                }
                Profile profile = new Profile(predicates,Boolean.parseBoolean(profileString[1]));
                db.addProfile(profile);
            }
            csvReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        int neg_support = (int) Math.ceil(db.getNegativeCount() / 2.0);

        db.PurgeFullSupport();

        //Log.out(this, MessageFormat.format("Starting Mining with {0} Items in DB", db.transactions.size()));
        long startTime = new Date().getTime();
        GrTreeMiner miner = new GrTreeMiner(db);
        HashMap<Pair<Integer, Integer>, HashSet<GrTree.Item>> generators = miner.MineSignatures(3,neg_support,3);

        Log.out(this, "Mining time: %s",  Misc.getFormattedTimerString(new Date().getTime() - startTime));
        Log.out(this, "Finish Mining.");
        List<Map.Entry<Pair<Integer, Integer>, HashSet<GrTree.Item>>> sorted = new ArrayList<>(generators.entrySet());
        sorted.sort(Comparator.comparingDouble(entry -> miner.DiscriminativeSignificance(entry.getKey().getLeft(), entry.getKey().getRight())));
        Collections.reverse(sorted);
        Log.out(this, "Creating Signatures.");
        LinkedHashMap<Signature.Identifier, Signature> signatures = new LinkedHashMap<>();
        try {
            Database finalDb = db;
            sorted.forEach(entry -> {
                entry.getValue().forEach(item -> {
                    Signature.Identifier newIdent = new Signature.Identifier(entry.getKey().getLeft(), entry.getKey().getRight(), finalDb, miner, item);
                    Signature sig = signatures.get(newIdent);
                    if (sig == null) {
                        sig = new Signature(newIdent);
                    }

                    sig.allItems.add(item);

                    signatures.put(newIdent, sig);
                });
                if (signatures.size() >= 10) throw new SizeLimitException();
            });
        }
        catch (SizeLimitException ignored) {
            //limit to 10 result entries
        }



        return signatures;
    }


    private static class SizeLimitException extends RuntimeException{
    }
}
