package cpt204.trees;

public class AVLTree<E extends Comparable<E>> extends BST<E> {
    protected static class AVLTreeNode<E> extends BST.TreeNode<E> {
        int height = 0;

        AVLTreeNode(E element) {
            super(element);
        }
    }

    @Override
    public boolean insert(E element) {
        boolean inserted = super.insert(element);
        if (inserted) {
            // Full lecture implementation balances along the path.
            // This file keeps the rotation formulas for reference.
        }
        return inserted;
    }

    private int height(TreeNode<E> node) {
        return node == null ? -1 : ((AVLTreeNode<E>) node).height;
    }

    private int balanceFactor(AVLTreeNode<E> node) {
        return height(node.right) - height(node.left);
    }

    private void updateHeight(AVLTreeNode<E> node) {
        node.height = 1 + Math.max(height(node.left), height(node.right));
    }

    private void balanceLL(TreeNode<E> a, TreeNode<E> parentOfA) {
        TreeNode<E> b = a.left;
        a.left = b.right;
        b.right = a;
        if (a == root) root = b;
        else if (parentOfA.left == a) parentOfA.left = b;
        else parentOfA.right = b;
        updateHeight((AVLTreeNode<E>) a);
        updateHeight((AVLTreeNode<E>) b);
    }

    private void balanceRR(TreeNode<E> a, TreeNode<E> parentOfA) {
        TreeNode<E> b = a.right;
        a.right = b.left;
        b.left = a;
        if (a == root) root = b;
        else if (parentOfA.left == a) parentOfA.left = b;
        else parentOfA.right = b;
        updateHeight((AVLTreeNode<E>) a);
        updateHeight((AVLTreeNode<E>) b);
    }
}
