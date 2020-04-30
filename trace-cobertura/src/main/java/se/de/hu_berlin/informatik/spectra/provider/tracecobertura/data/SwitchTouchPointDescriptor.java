package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data;


import org.objectweb.asm.Label;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class representing a touch-point connected to a a SWITCH instruction in a source-code
 *
 * <p>A SWITCH touch-point uses one more counter then distinct number destination labels ({@link #getCountersForLabelsCnt()}).
 * <p>
 * One 'internal' counterId ({@link #counterId}) is a special identifier of SWITCH statement (used in runtime), but in fact we don't expect any
 * incrementation of the counter. We implemented this to use a counterId because we are storing the value inside 'internal variable' and we need to be sure
 * that the value is connected to the last seen SWITCH statement.
 * <p>
 * Or other counterIds represents different branches (different destination labels of the switch).
 * </p>
 *
 * <p>We also storing a {@code methodName} and a {@code methodSignature} (consider to move this fields into {@link TouchPointDescriptor}).
 * Those fields are needed to properly create instance of {@code LineData}. </p>
 *
 * @author piotr.tabor@gmail.com
 */
public class SwitchTouchPointDescriptor extends TouchPointDescriptor {
    private final Label defaultDestinationLabel;
    private final Label[] labels;
    /**
     * Encoded as: org.objectweb.asm.commons#AnalyzerAdapter#stack
     */
    private final String enum_type;

    private Integer counterId;
    private Map<Label, Integer> label2counterId;

    /**
     * Creates o new switch-touch point.
     *
     * @param eventId     - eventId connected to the SWITCH instruction
     * @param currentLine - line number of the switch
     * @param def         - internal identifier of a default destination label
     * @param labels      - table of other destination labels for different values (duplicates allowed)
     * @param enum_type   - enum type string
     */
    public SwitchTouchPointDescriptor(int eventId, int currentLine, Label def,
                                      Label[] labels, String enum_type) {
        super(eventId, currentLine);
        this.labels = labels;
        this.defaultDestinationLabel = def;
        this.enum_type = enum_type;
    }

    public Integer getCounterId() {
        return counterId;
    }

    public void setCounterId(Integer counterId) {
        this.counterId = counterId;
    }

    @Override
    public int assignCounters(AtomicInteger idGenerator) {
        counterId = idGenerator.incrementAndGet();
        label2counterId = new HashMap<>();
        int idp = idGenerator.incrementAndGet();
        label2counterId.put(defaultDestinationLabel, idp);
        int i = 0;
        for (Label l : labels) {
            i++;
            idp = idGenerator.incrementAndGet();
            label2counterId.put(l, idp);
        }
        return i + 2;
    }

    public Integer getCounterIdForLabel(Label label) {
        return label2counterId.get(label);
    }

    public Collection<Integer> getCountersForLabels() {
        return label2counterId.values();
    }

    /**
     * <p>Works before calling 'assignCounters'</p>
     *
     * @return Number of distinct destination labels of the SWITCH (It's the same as number of branches supported by the switch).
     */
    public int getCountersForLabelsCnt() {
        Set<Label> l = new HashSet<>(Arrays.asList(labels));
        l.add(defaultDestinationLabel);
        return l.size();
    }

    public String getEnumType() {
        return enum_type;
    }

}
