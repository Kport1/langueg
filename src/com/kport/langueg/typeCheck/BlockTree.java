package com.kport.langueg.typeCheck;

import java.util.*;

public class BlockTree {
    public ArrayList<BlockTree> children;
    //find direct children faster
    private static final HashMap<Map.Entry<Integer, Integer>, BlockTree> nodes = new HashMap<>();
    public BlockTree parent;

    public int depth, count;

    public BlockTree(BlockTree parent_, int depth_, int count_, BlockTree... children_){
        parent = parent_;
        depth = depth_;
        count = count_;
        children = new ArrayList<>(List.of(children_));

        nodes.put(Map.entry(depth_, count_), this);
    }

    public BlockTree getNode(int depth_, int count_){
        return nodes.get(Map.entry(depth_, count_));
    }

    public void addChildren(BlockTree... children_){
        if(children_ == null || children_.length == 0){
            return;
        }

        children.addAll(List.of(children_));

        for (BlockTree blockTree : children_) {
            blockTree.parent = this;
        }
    }

    public String toString(){
        StringBuilder r = new StringBuilder("(" + depth + " : " + count + ")");

        if(children == null || children.size() < 1){
            return r.toString();
        }

        r.append("{ ");

        for (int i = 0; i < children.size(); i++) {
            r.append(children.get(i));

            if(i == children.size() - 1){
                continue;
            }

            r.append(", ");
        }
        r.append(" }");

        return r.toString();
    }
    
    @Override
    public int hashCode(){
        return 31 * (31 + depth) + count;
        //return Objects.hash(depth, count);
    }
    
    @Override
    public boolean equals(Object o){
        if(o instanceof BlockTree t){
            return  t.depth == depth &&
                    t.count == count;
        }
        return false;
    }
}
