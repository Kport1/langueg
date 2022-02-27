package com.kport.langueg.parse.typeCheck;

import java.util.ArrayList;
import java.util.List;

public class BlockTree {
    public ArrayList<BlockTree> children;
    public BlockTree parent;

    public int depth, count;

    public BlockTree(BlockTree parent_, int depth_, int count_, BlockTree... children_){
        parent = parent_;
        depth = depth_;
        count = count_;
        children = new ArrayList<>(List.of(children_));
    }

    public BlockTree findInChildren(int depth_, int count_){
        if(depth == depth_ && count == count_){
            return this;
        }
        if(children == null || children.size() == 0){
            return null;
        }

        for (BlockTree child : children) {
            BlockTree result = child.findInChildren(depth_, count_);
            if(result != null){
                return result;
            }
        }

        return null;
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
}
