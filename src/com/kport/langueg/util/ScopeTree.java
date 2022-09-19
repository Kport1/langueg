package com.kport.langueg.util;

import java.util.*;

public class ScopeTree {
    private final ArrayList<ArrayList<Node>> nodes = new ArrayList<>();
    private final Node rootNode;

    public class Node{
        public int depth, count;
        private final Node parent;

        private Node(Node parent_, int depth_, int count_){
            parent = parent_;
            depth = depth_;
            count = count_;

            //children = new ArrayList<>();
        }

        public Node getParent(){
            return parent;
        }

        public boolean isRoot(){
            return parent == null;
        }

        public void addChildren(int amount){
            if(amount == 0) return;

            ArrayList<Node> depthColumn;
            int startCount = 0;
            if(nodes.size() <= depth + 1){
                nodes.add(new ArrayList<>());
                depthColumn = nodes.get(depth + 1);
            }
            else {
                depthColumn = nodes.get(depth + 1);
                startCount = depthColumn.get(depthColumn.size() - 1).count + 1;
            }

            for (int i = 0; i < amount; i++) {
                depthColumn.add(new Node(this, depth + 1, startCount + i));
            }

        }

        @Override
        public int hashCode(){
            return 31 * (31 * depth + count) + parent.hashCode();
        }

        @Override
        public boolean equals(Object o){
            if(o instanceof Node n){
                return  n.depth == depth &&
                        n.count == count &&
                        Objects.equals(n.parent, parent);
            }
            return false;
        }

        @Override
        public String toString(){
            // ( d, c ){ c... }
            StringBuilder str = new StringBuilder("( " + depth + ", " + count + " )");

            if (nodes.size() <= depth + 1) return str.toString();
            ArrayList<Node> depthColumn = nodes.get(depth + 1);

            str.append("{ ");

            boolean noNode = true;
            for (Node node : depthColumn) {
                if(node.parent.equals(this)) {
                    str.append(node).append(", ");
                    noNode = false;
                }
            }
            if(noNode) return str.delete(str.length() - 2, str.length()).toString();
            return str.deleteCharAt(str.length() - 2).append("}").toString();
        }
    }

    public ScopeTree(){
        rootNode = new Node(null, 0, 0);
        nodes.add(0, new ArrayList<>(List.of(rootNode)));
    }

    public Node getNode(int depth, int count){
        ArrayList<Node> depthColumn = nodes.get(depth);
        if(depthColumn == null) return null;
        return depthColumn.get(count);
    }

    public String toString(){
        return rootNode.toString();
    }
    
    @Override
    public int hashCode(){
        return nodes.hashCode();
    }
    
    @Override
    public boolean equals(Object o){
        if(o instanceof ScopeTree t){
            return  t.nodes.equals(nodes);
        }
        return false;
    }
}
