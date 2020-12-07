package se.de.hu_berlin.informatik.gen.spectra.predicates.mining;

import se.de.hu_berlin.informatik.gen.spectra.predicates.extras.Profile;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class Database implements Serializable {

    public ArrayList<Profile> transactions;
    public ArrayList<Integer> prefix;

    private Integer m_positiveCount;
    private Integer m_negativeCount;

    Database(ArrayList<Profile> transactions, ArrayList<Integer> prefix) {
        this.transactions = transactions;
        this.prefix =  prefix;
    }
    Database() {
        this.transactions = new ArrayList<>();
        this.prefix =  new ArrayList<>();
    }

    public void PurgeFullSupport() {
        //get all ids
        Set<Integer> allIds = new HashSet<>();
        this.transactions.forEach(profile -> allIds.addAll(profile.predicates));

        allIds.forEach((value) -> {
            if (this.transactions.stream().allMatch(profile -> profile.predicates.stream().anyMatch(integer -> integer.equals(value)))) { //if ALL transactions contain a Id
                this.transactions.forEach(profile -> {
                    profile.predicates.removeIf(predicate -> predicate.equals(value)); //remove it from all
                });
            }
        });
        this.transactions.removeIf(profile -> profile.predicates.isEmpty()); //remove empty transactions
    }

    public void RemoveItemsBelowSupport(Integer neg_sup) {
        //get all ids
        Set<Integer> allIds = new HashSet<>();
        this.transactions.forEach(profile -> allIds.addAll(profile.predicates));

        for (Integer id : allIds) {
            if (getNegativeSupport(id) < neg_sup) { //if sup−(a) < neg_sup;
                this.transactions.forEach(profile -> {
                    profile.predicates.removeIf(predicate -> predicate.equals(id)); //remove it from all
                });
            }
        }
        this.transactions.removeIf(profile -> profile.predicates.isEmpty()); //remove empty transactions

    }

    public void RemoveGenerators(HashSet<HashSet<GrTree.Item>> generators) {
            for (HashSet<GrTree.Item> gen : generators) {
                gen.forEach(item -> {
                    if (item.id.isEmpty() || item.id.containsAll(this.prefix))
                        return;
                    item.addToId(this.prefix);
                    this.transactions.forEach(profile -> {
                        if (profile.predicates.containsAll(item.id)) {
                            profile.predicates.removeAll(item.id);                        }
                    });
                    item.removeFromId(this.prefix);
                });
            }
        this.transactions.removeIf(profile -> profile.predicates.isEmpty()); //remove empty transactions
    }


    public int getPositiveCount() {
        if (m_positiveCount != null)
            return m_positiveCount;
        this.m_positiveCount = (int) this.transactions.stream().filter(profile -> profile.positiveSupport).count();
        return m_positiveCount;
    }

    public int getNegativeCount() {
        if (m_negativeCount != null)
            return m_negativeCount;
        this.m_negativeCount = (int) this.transactions.stream().filter(profile -> !profile.positiveSupport).count();
        return m_negativeCount;
    }

    public void addProfile(Profile profile) {
        this.transactions.add(profile);
    }

    public int getPositiveSupport(Integer id) {
        return (int) this.transactions.stream().filter(profile -> profile.predicates.contains(id) && profile.positiveSupport).count();
    }

    public int getNegativeSupport(Integer id) {
        return (int) this.transactions.parallelStream().filter(profile -> !profile.positiveSupport && profile.predicates.contains(id)).count();
    }

    public int getNegativeSupport(Collection<Integer> ids) {
        return (int) this.transactions.parallelStream().filter(profile -> !profile.positiveSupport && profile.predicates.containsAll(ids)).count();
    }

    public List<Profile> getTransactions(Collection<Integer> ids) {
        return this.transactions.stream().filter(profile -> profile.predicates.containsAll(ids)).collect(Collectors.toList());
    }


}

