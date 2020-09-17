package se.de.hu_berlin.informatik.gen.spectra.predicates.extras;

import java.util.ArrayList;
import java.util.List;

public class Profile {
    public ArrayList<Integer> predicates;
    public boolean positiveSupport;

    public Profile(List<Integer> ids, boolean Support) {
        this.positiveSupport = Support;
        //ids.forEach(integer -> this.predicates.add(new Predicate(integer)));
        this.predicates = new ArrayList<>(ids);

    }
}
