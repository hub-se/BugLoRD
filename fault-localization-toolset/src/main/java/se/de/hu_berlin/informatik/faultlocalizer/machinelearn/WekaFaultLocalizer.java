/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.faultlocalizer.machinelearn;

import se.de.hu_berlin.informatik.faultlocalizer.IFaultLocalizer;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.ranking.NodeRanking;
import se.de.hu_berlin.informatik.spectra.core.ComputationStrategies;
import se.de.hu_berlin.informatik.spectra.core.ILocalizerCache;
import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;

/**
 * Machine learning based fault localization approach using Weka as ML backend.
 * <p>
 * ML algorithms can be configured.
 *
 * @param <T> type used to identify nodes in the system
 */
public class WekaFaultLocalizer<T> implements IFaultLocalizer<T> {

    /**
     * classifier name
     */
    private final String classifierName;
    /**
     * options for the classifier
     */
    private final String[] classifierOptions;

    /**
     * Construct Weka fault localizer
     *
     * @param classifierName name of the Weka classifier to use
     */
    public WekaFaultLocalizer(final String classifierName) {
        this(classifierName, new String[0]);
    }

    /**
     * Construct Weka fault localizer
     *
     * @param classifierName name of the Weka classifier to use
     * @param options        options to pass to the classifier
     */
    public WekaFaultLocalizer(final String classifierName, final String[] options) {
        super();
        this.classifierName = classifierName;
        if (options == null) {
            this.classifierOptions = new String[0];
        } else {
            this.classifierOptions = Arrays.copyOf(options, options.length);
        }
    }

    /**
     * Returns the name of the used classifier
     *
     * @return classifier name
     */
    public String getClassifierName() {
        return this.classifierName;
    }

    @Override
    public NodeRanking<T> localize(final ILocalizerCache<T> localizer, ComputationStrategies strategy) {

        // == 1. Create Weka training instance

        final List<INode<T>> nodes = new ArrayList<>(localizer.getNodes());

        // nominal true/false values
        final List<String> tf = new ArrayList<>();
        tf.add("t");
        tf.add("f");

        // create an attribute for each component
        final Map<INode<T>, Attribute> attributeMap = new HashMap<>();
        final ArrayList<Attribute> attributeList = new ArrayList<>(); // NOCS: Weka needs ArrayList..
        for (final INode<T> node : nodes) {
            final Attribute attribute = new Attribute(node.toString(), tf);
            attributeList.add(attribute);
            attributeMap.put(node, attribute);
        }

        // create class attribute (trace success)
        final Attribute successAttribute = new Attribute("success", tf);
        attributeList.add(successAttribute);

        // create weka training instance
        final Instances trainingSet = new Instances("TraceInfoInstances", attributeList, 1);
        trainingSet.setClassIndex(attributeList.size() - 1);


        // == 2. add traces to training set

        // add an instance for each trace
        for (final ITrace<T> trace : localizer.getTraces()) {
            final Instance instance = new DenseInstance(nodes.size() + 1);
            instance.setDataset(trainingSet);
            for (final INode<T> node : nodes) {
                instance.setValue(attributeMap.get(node), trace.isInvolved(node) ? "t" : "f");
            }
            instance.setValue(successAttribute, trace.isSuccessful() ? "t" : "f");
            trainingSet.add(instance);
        }

        // == 3. use prediction to localize faults

        // build classifier
        try {
            final Classifier classifier = this
                    .buildClassifier(this.classifierName, this.classifierOptions, trainingSet);
            final NodeRanking<T> ranking = new NodeRanking<>();

            Log.out(this, "begin classifying");
            int classified = 0;

            final Instance instance = new DenseInstance(nodes.size() + 1);
            instance.setDataset(trainingSet);
            for (final INode<T> node : nodes) {
                instance.setValue(attributeMap.get(node), "f");
            }
            instance.setValue(successAttribute, "f");

            for (final INode<T> node : nodes) {
                classified++;
                if (classified % 1000 == 0) {
                    Log.out(this, String.format("Classified %d nodes.", classified));
                }

                // contain only the current node in the network
                instance.setValue(attributeMap.get(node), "t");

                // predict with which probability this setup leads to a failing network
                final double[] distribution = classifier.distributionForInstance(instance);
                ranking.add(node, distribution[1]);

                // reset involvment for node
                instance.setValue(attributeMap.get(node), "f");
            }
            return ranking;
        } catch (final Exception e) { // NOCS: Weka throws only raw exceptions
            throw new RuntimeException(e);
        }
    }

    /**
     * Builds and trains a classifier.
     *
     * @param name        FQCN of the classifier
     * @param options     options to pass to the classifier
     * @param trainingSet training set to build the classifier with
     * @return trained classifier
     */
    public Classifier buildClassifier(final String name, final String[] options, final Instances trainingSet) {
        try {
            final Classifier classifier = AbstractClassifier.forName(this.classifierName, options);
            classifier.buildClassifier(trainingSet);
            return classifier;
        } catch (final Exception e1) { // NOCS: Weka throws only raw exceptions
            Log.err(this, "Unable to create classifier " + this.classifierName);
            throw new RuntimeException(e1);
        }
    }

    @Override
    public String getName() {
        assert !this.classifierName.endsWith(".");
        return "weka-" + this.classifierName.substring(this.classifierName.lastIndexOf(".") + 1);
    }

    @Override
    public double suspiciousness(INode<T> node, ComputationStrategies strategy) {
        throw new UnsupportedOperationException();
    }

}
