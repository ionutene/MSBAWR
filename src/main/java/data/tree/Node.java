package data.tree;

import java.util.LinkedList;
import java.util.List;

public class Node<T> {
    private T data;
    private Node<T> parent;
    private List<Node<T>> children;

    public Node() {
    }

    public Node(T data) {
        this.data = data;
        this.children = new LinkedList<>();
    }

    public Node(T data, Node<T> parent) {
        this.data = data;
        this.parent = parent;
        this.children = new LinkedList<>();
    }

    public Node(T data, Node<T> parent, List<Node<T>> children) {
        this.data = data;
        this.parent = parent;
        this.children = children;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Node<T> getParent() {
        return parent;
    }

    public void setParent(Node<T> parent) {
        this.parent = parent;
    }

    public List<Node<T>> getChildren() {
        return children;
    }

    public void setChildren(List<Node<T>> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return "Node{" +
                "data=" + data +
                '}';
    }
}