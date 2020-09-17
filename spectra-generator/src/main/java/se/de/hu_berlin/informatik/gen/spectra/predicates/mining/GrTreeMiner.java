package se.de.hu_berlin.informatik.gen.spectra.predicates.mining;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Math.log;

public class GrTreeMiner {

    Database startDB;
    HashSet<HashSet<GrTree.Item>> generators =  new HashSet<>();





    public HashMap<Pair<Integer, Integer>, HashSet<GrTree.Item>> MineSignatures(Database D, int k, int neg_sup, int size_limit)
    {
        HashMap<Pair<Integer, Integer>, HashSet<GrTree.Item>> GS = new HashMap<>();
        this.startDB = D;
        GrTree Tree = new GrTree(D);
        MineRec(Tree,k,neg_sup,size_limit,GS);

        return GS;
    }

    public void MineRec(GrTree tree, int k, int neg_sup, int size_limit, HashMap<Pair<Integer, Integer>, HashSet<GrTree.Item>> GS)
    {
        if (tree.isEmpty())
            return;

        for (GrTree.Item item : tree.headTable.getSorted()) {
            item.addToId(tree.prefix);
            int pos = item.positiveSupport;
            int neg = item.negativeSupport;
            UpdateResult(GS,k,pos,neg, item);
        }

        if (tree.prefix.size() + 1 == size_limit)
            return;
        if (tree.isSinglePath())
            return;

        for (GrTree.Item item : tree.headTable.getSorted()) {
            //List<GrTree.Item> prefixedItem = new ArrayList<>();
            //prefixedItem.add(item);
            //prefixedItem.addAll(tree.prefix);
            Database newDB = tree.getConditionalDatabase(item);
            newDB.PurgeFullSupport();
            newDB.RemoveItemsBelowSupport(neg_sup);
            //newDB.RemoveGenerators(generators);
            GrTree newTree = new GrTree(newDB);
            //TODO branch and bound
            MineRec(newTree,k,neg_sup,size_limit,GS);
            //item.removeFromId(tree.prefix);
        }

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
                    double DS = DiscriminativeSignificance(pair.getLeft(), pair.getRight(), startDB.getPositiveCount(), startDB.getNegativeCount());
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

    public double DiscriminativeSignificance(int p, int n, int db_pos, int db_neg){
        if (n / (double) db_neg > p / (double) db_pos){
            return InformationGain(p,n,db_pos,db_neg);
        }
        return 0;
    }



    private double InformationGain(int p, int n, int db_pos, int db_neg){
        double d = db_pos + db_neg;
        double first = Entropy(db_pos,db_neg);
        double second = ((p+n)/d) * Entropy(p,n);
        double third = ((d-p+n)/d) * Entropy(db_pos-p,db_neg-n);
        return first - second - third;
    }

    private double Entropy(int a, int b){
        double total = a + b;
        double first = a/total == 0 ? 0 : -(a/total) * log(a/total);
        double second = b/total == 0 ? 0 : -(b/total) * log(b/total);
        return (first+second)/log(2);
    }

}

