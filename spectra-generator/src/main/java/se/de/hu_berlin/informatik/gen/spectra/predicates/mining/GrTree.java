package se.de.hu_berlin.informatik.gen.spectra.predicates.mining;

import se.de.hu_berlin.informatik.gen.spectra.predicates.extras.Profile;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;


public class GrTree {
    public final HeadTable headTable;
    private final TreeNode root;
    public ArrayList<Integer> prefix;

    private GrTree(){
        headTable = new HeadTable();
        root = new TreeNode(-1);
        root.isRoot = true;
    }

    public GrTree(Database db) {
        this();
        this.prefix = db.prefix;
        db.transactions.forEach(this::addPath);
    }

    void addPath(Profile profile){
        root.addNodes(this,profile.predicates,profile.positiveSupport);
    }

    TreeNode createNode(int id, boolean positiveSupport) {
        TreeNode Node = new TreeNode(id);
        Item headTableItem = headTable.get(id);
        if (headTableItem == null) {
            headTableItem = new Item();
            headTableItem.id.add(id);
            this.headTable.put(id, headTableItem);
        }
        headTableItem.nodes.add(Node);
        headTableItem.setSupport(positiveSupport);
        return Node;
    }

    void updateNode(int id, boolean positiveSupport){
        this.headTable.get(id).setSupport(positiveSupport);
    }

    boolean isEmpty() {
        return this.headTable.isEmpty();
    }

    boolean isSinglePath() {
        int count = root.children.size();
        TreeNode currentNode = root;
        while (count > 0) {
            if (count > 1)
                return false;
            currentNode = currentNode.children.get(0);
            count = currentNode.children.size();
        }
        return true;
    }

    public Database getConditionalDatabase(Item item) {
        ArrayList<Profile> al = new ArrayList<>();

        item.nodes.forEach(treeNode -> {
            List<TreeNode> path = treeNode.getRootPath(new ArrayList<>());
            path.remove(0);//root
            if (path.size() <= 1)
                return;
//            path.remove(path.size()-1);//item
            int posSupport = treeNode.positiveSupport;
            int negSupport = treeNode.negativeSupport;
            while (posSupport > 0) {
                List<Integer> list = path.stream().map(tN -> tN.id).collect(Collectors.toList());
                al.add(new Profile(list,true));
                posSupport--;
            }
            while (negSupport > 0) {
                al.add(new Profile(path.stream().map(tN -> tN.id).collect(Collectors.toList()),false));
                negSupport--;
            }
        });
        ArrayList<Integer> newPrefix = new ArrayList<>(this.prefix);
        newPrefix.addAll(item.id);
        return new Database(al,newPrefix);
    }

    public int GetUnavoidableTransactionsPositiveLeafSupport() {
        TreeNode currentNode = this.root;
        List<Integer> currentPath = new ArrayList<>();

        List<Integer> list = RecFindLeafPathWithUnavoidableTransactions(this.root,currentPath);

        if (list != null) {
            return this.headTable.get(list.get(list.size() - 1)).positiveSupport;
        }
        return -1;

    }

    private List<Integer> RecFindLeafPathWithUnavoidableTransactions(TreeNode node, List<Integer> currentPath) {
        currentPath.add(node.id);

        if (currentPath.containsAll(this.headTable.keySet())) {
            return currentPath;
        }

        if (node.children.isEmpty()) {
            return null;
        }

        for (TreeNode child : node.children) {

            List<Integer> list = RecFindLeafPathWithUnavoidableTransactions(child,currentPath);
            if (list != null) {
                return list;
            }
        }
        return null;
    }


    private class TreeNode implements Serializable {
        int id;
        int positiveSupport;
        int negativeSupport;
        List<TreeNode> children = new ArrayList<>();
        TreeNode parent;
        boolean isRoot;

        public TreeNode(int id) {
            this.id = id;
        }

        void addNodes(GrTree Tree, List<Integer> predicates, boolean positiveSupport){
            if (predicates.isEmpty())
                return;
            TreeNode nextNode;
            Optional<TreeNode> childNodeOptional = this.children.stream().filter(treeNode -> treeNode.id == predicates.get(0)).findFirst();
            if (childNodeOptional.isPresent()){
                nextNode = childNodeOptional.get();
                Tree.updateNode(nextNode.id,positiveSupport);
            }
            else {
                nextNode = Tree.createNode(predicates.get(0),positiveSupport);
                this.children.add(nextNode);
            }
            nextNode.setSupport(positiveSupport);
            nextNode.parent = this;
            nextNode.addNodes(Tree,predicates.stream().skip(1).collect(Collectors.toList()), positiveSupport);
        }

        private void setSupport(boolean positiveSupport) {
            if (positiveSupport)
                this.positiveSupport++;
            else
                this.negativeSupport++;
        }

        List<TreeNode> getRootPath(List<TreeNode> list){
            if (this.isRoot)
                return list;
            list.add(0,parent);
            return parent.getRootPath(list);
        }
    }

    public class Item implements Serializable {
        TreeSet<Integer> id = new TreeSet<>();
        public TreeSet<Integer> prefixedId = new TreeSet<>();
        int positiveSupport;
        int negativeSupport;
        List<TreeNode> nodes = new ArrayList<>();

        public int getSupport() {
            return this.positiveSupport + this.negativeSupport;
        }

        public void addToId(ArrayList<Integer> prefixes){
            //this.prefixedId = (TreeSet<Integer>) this.id.clone();
            //this.prefixedId = SerializationUtils.clone(this.id);
            this.prefixedId = new TreeSet<>(this.id);
            this.prefixedId.addAll(prefixes);
        }

        public void removeFromId(ArrayList<Integer> prefixes){
            for (Integer i : prefixes){
                this.id.remove((i));
            }
        }

        private void setSupport(boolean positiveSupport) {
            if (positiveSupport)
                this.positiveSupport++;
            else
                this.negativeSupport++;
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
            Item item = (Item) o;
            // field comparison
            return this.id.containsAll(item.id) && item.id.containsAll(this.id);
        }

        @Override
        public int hashCode() {
            return this.id.stream().mapToInt(Integer::intValue).sum();
        }
    }

    public class HeadTable extends TreeMap<Integer,Item> {
        public List<Item> getSorted() {
            return this.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.comparingInt(Item::getSupport).reversed())).map(Map.Entry::getValue).collect(Collectors.toList());
        }

        @Override
        public Item get(Object key) {
            return super.get(key);
        }
    }

}

