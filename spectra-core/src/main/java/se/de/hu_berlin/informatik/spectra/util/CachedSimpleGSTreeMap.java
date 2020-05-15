package se.de.hu_berlin.informatik.spectra.util;

import java.io.*;
import java.nio.file.Path;
import se.de.hu_berlin.informatik.spectra.core.branch.SimpleIntGSArrayTree;
import se.de.hu_berlin.informatik.spectra.core.branch.SimpleIntGSArrayTree.IntGSArrayTreeNode;

public class CachedSimpleGSTreeMap extends CachedMap<SimpleIntGSArrayTree> {

    public CachedSimpleGSTreeMap(Path outputZipFile, int cacheSize, String id, boolean deleteAtShutdown) {
        super(outputZipFile, cacheSize, id, deleteAtShutdown);
    }

    @Override
    public byte[] toByteArray(SimpleIntGSArrayTree tree) {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        try (ObjectOutputStream objOut = new ObjectOutputStream(byteOut)) {
        	SimpleIntGSArrayTree.writeNode(objOut, tree.getStartNode());
            objOut.close();
            return byteOut.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public SimpleIntGSArrayTree fromByteArray(byte[] array) {
        ByteArrayInputStream byteIn = new ByteArrayInputStream(array);
        try (ObjectInputStream objIn = new ObjectInputStream(byteIn)) {
            IntGSArrayTreeNode startNode = SimpleIntGSArrayTree.readNode(objIn);
            return new SimpleIntGSArrayTree(startNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
