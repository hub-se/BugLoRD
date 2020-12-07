package se.de.hu_berlin.informatik.gen.spectra.predicates.mining;

import org.apache.commons.lang3.tuple.Pair;
import org.evosuite.shaded.org.jboss.logging.Message;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Math.log;

public class GrTreeMiner {

    Database startDB;
    HashSet<HashSet<GrTree.Item>> generators =  new HashSet<>();
    Map<Pair<Integer,Integer>, Double> DSCache = new ConcurrentHashMap<>();

    public GrTreeMiner(Database db) {
        this.startDB = db;
    }



    public HashMap<Pair<Integer, Integer>, HashSet<GrTree.Item>> MineSignatures(int k, int neg_sup, int size_limit)
    {
        HashMap<Pair<Integer, Integer>, HashSet<GrTree.Item>> GS = new HashMap<>();
        GrTree Tree = new GrTree(this.startDB);
        MineRec(Tree,k,neg_sup,size_limit,GS);

        return GS;
    }

    public void MineRec(GrTree tree, int k, int neg_sup, int size_limit, HashMap<Pair<Integer, Integer>, HashSet<GrTree.Item>> GS)
    {
        if (tree.isEmpty())
            return;
        //Log.out(this, MessageFormat.format("Tree has {0} Items in DB", tree.headTable.size()));
        for (GrTree.Item item : tree.headTable.getSorted()) {
            item.addToId(tree.prefix);
            int pos = item.positiveSupport;
            int neg = item.negativeSupport;
            if (DiscriminativeSignificance(pos, neg) > 0) {
                UpdateResult(GS,k,pos,neg, item);
            }

        }

        if (tree.prefix.size() + 1 == size_limit)
            return;
        if (tree.isSinglePath())
            return;

        //List<GrTree.Item> prefixedItem = new ArrayList<>();
        //prefixedItem.add(item);
        //prefixedItem.addAll(tree.prefix);
        //newDB.RemoveGenerators(generators);
        //item.removeFromId(tree.prefix);
        List<GrTree.Item> headTableSorted = tree.headTable.getSorted();
        headTableSorted.stream().map(tree::getConditionalDatabase).forEach(newDB -> {
            newDB.PurgeFullSupport();
            newDB.RemoveItemsBelowSupport(neg_sup);
            GrTree newTree = new GrTree(newDB);
            if (newTree.isEmpty())
                return;
            if (UpperBound(newTree, newDB) < MinDS(GS))
                return;             // branch and bound
            MineRec(newTree, k, neg_sup, size_limit, GS);
        });

    }

    private double UpperBound(GrTree tree, Database dB) {
        int posSupport = tree.GetUnavoidableTransactionsPositiveLeafSupport();
        if (posSupport == -1)
            return 0;

        try {
            if (startDB.getNegativeSupport(tree.prefix) / dB.getNegativeCount() > posSupport / dB.getPositiveCount()) {
                return InformationGain(posSupport, dB.getNegativeCount(), startDB.getPositiveCount(), startDB.getNegativeCount());
            }
        } catch (ArithmeticException ex) {
            return 1;
        }
        return 0;
    }

    private void UpdateResult(HashMap<Pair<Integer, Integer>,HashSet<GrTree.Item>> GS, int k, int p , int n , GrTree.Item gen){

        if (GS.get(Pair.of(p,n)) != null) { //add to gen with same support
            GS.get(Pair.of(p,n)).add(gen);
            //generators.add(GS.get(Pair.of(p,n)));
        }
        else {
            GS.put(Pair.of(p,n), new HashSet<>(Collections.singletonList(gen))); //create new gen
            //generators.add(GS.get(Pair.of(p,n)));

            if (GS.size() > k) {
                //remove smallest
                AtomicReference<Double> smallest = new AtomicReference<>((double) 1);
                AtomicReference<Pair<Integer, Integer>> key = new AtomicReference<>();
                for (Pair<Integer, Integer> pair : GS.keySet()) {
                    double DS = DiscriminativeSignificance(pair.getLeft(), pair.getRight());
                    if (DS <= smallest.get()) {
                        key.set(Pair.of(pair.getLeft(), pair.getRight()));
                        smallest.set(DS);
                    }
                }
                if (key.get() == null)
                    key.set(GS.keySet().iterator().next());
                GS.remove(key.get());
            }
        }
    }

    private double MinDS(HashMap<Pair<Integer, Integer>,HashSet<GrTree.Item>> GS) {
        AtomicReference<Double> smallest = new AtomicReference<>((double) 1);
        GS.keySet().parallelStream().mapToDouble(pair -> DiscriminativeSignificance(pair.getLeft(), pair.getRight())).filter(DS -> DS <= smallest.get()).forEach(smallest::set);
        return smallest.get();
    }

    public double DiscriminativeSignificance(int p, int n) {
        double result = 0;

        if (DSCache.containsKey(Pair.of(p, n))) {
            return DSCache.get(Pair.of(p, n));
        } else {
            int db_pos = this.startDB.getPositiveCount();
            int db_neg = this.startDB.getNegativeCount();
            if (n / (double) db_neg > p / (double) db_pos) {
                result = InformationGain(p, n, db_pos, db_neg);
            }
        }
        DSCache.put(Pair.of(p, n),result);
        return result;
    }



    private double InformationGain(int p, int n, int db_pos, int db_neg){
        double d = db_pos + db_neg;
        double first = Entropy(db_pos, db_neg);
        double second = ((p+n)/d) * Entropy(p,n);
        double third = ((d - (p + n)) / d) * Entropy(db_pos-p,db_neg-n);
        return first - second - third;
    }

    private double Entropy(int a, int b){
        double total = a + b;
        double first = a/total == 0 ? 0 : -(a/total) * log(a/total);
        double second = b/total == 0 ? 0 : -(b/total) * log(b/total);
        return (first+second) / log(2);
    }

}

