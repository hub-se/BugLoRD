package se.de.hu_berlin.informatik.gen.spectra.predicates.mining;

import se.de.hu_berlin.informatik.gen.spectra.predicates.extras.Profile;
import se.de.hu_berlin.informatik.gen.spectra.predicates.modules.Output;
import se.de.hu_berlin.informatik.gen.spectra.predicates.modules.Predicate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class Signature implements Serializable {

    public Identifier identifier;

    public List<GrTree.Item> allItems = new ArrayList<>();
    public List<Predicate> predicates =  new ArrayList<>();

    public HashSet<String> locations = new HashSet<>();

    public Signature(Identifier newIdent) {
        this.identifier = newIdent;
    }

    public void setPredicates() {
        this.allItems.forEach(item -> item.prefixedId.forEach(integer -> {
            this.predicates.add(Output.Predicates.get(integer));
        }));
    }




    public static class Identifier implements Serializable {

        public int positiveSupport;
        public int negativeSupport;
        public double DS;

        public List<Profile> tx;

        public Identifier(int positiveSupport, int negativeSupport, Database db, GrTreeMiner miner, GrTree.Item item) {
            this.positiveSupport = positiveSupport;
            this.negativeSupport = negativeSupport;
            this.DS = miner.DiscriminativeSignificance(positiveSupport, negativeSupport);
            this.tx = db.getTransactions(item.prefixedId);

        }

        @Override
        public boolean equals(Object o) {
            // self check
            if (this == o)
                return true;
            // null check
            if (o == null)
                return false;
            // type check and cast
            if (getClass() != o.getClass())
                return false;
            Identifier identifier = (Identifier) o;
            // field comparison
            return this.negativeSupport == identifier.negativeSupport && this.positiveSupport == identifier.positiveSupport
                    && this.tx.containsAll(identifier.tx) && identifier.tx.containsAll(this.tx);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 31 * hash * this.positiveSupport;
            hash = 31 * hash * this.negativeSupport;
            hash = 31 * hash * this.tx.stream().map(profile -> profile.predicates).flatMap(Collection::stream).reduce(0,Integer::sum);
            return hash;
        }
    }

}
