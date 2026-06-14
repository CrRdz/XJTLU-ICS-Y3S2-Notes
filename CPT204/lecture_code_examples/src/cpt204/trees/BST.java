package cpt204.trees;

import java.util.ArrayList;
import java.util.List;

public class BST<E extends Comparable<E>> {
    protected TreeNode<E> root;
    protected int size;

    protected static class TreeNode<E> {
        E element;
        TreeNode<E> left;
        TreeNode<E> right;

        TreeNode(E element) {
            this.element = element;
        }
    }

    public boolean insert(E element) {
        if (root == null) {
            root = new TreeNode<>(element);
        } else {
            TreeNode<E> parent = null;
            TreeNode<E> current = root;
            while (current != null) {
                if (element.compareTo(current.element) < 0) {
                    parent = current;
                    current = current.left;
                } else if (element.compareTo(current.element) > 0) {
                    parent = current;
                    current = current.right;
                } else {
                    return false;
                }
            }
            if (element.compareTo(parent.element) < 0) {
                parent.left = new TreeNode<>(element);
            } else {
                parent.right = new TreeNode<>(element);
            }
        }
        size++;
        return true;
    }

    public boolean search(E element) {
        TreeNode<E> current = root;
        while (current != null) {
            if (element.compareTo(current.element) < 0) {
                current = current.left;
            } else if (element.compareTo(current.element) > 0) {
                current = current.right;
            } else {
                return true;
            }
        }
        return false;
    }

    public List<E> inorder() {
        ArrayList<E> result = new ArrayList<>();
        inorder(root, result);
        return result;
    }

    private void inorder(TreeNode<E> root, List<E> result) {
        if (root == null) {
            return;
        }
        inorder(root.left, result);
        result.add(root.element);
        inorder(root.right, result);
    }
}
