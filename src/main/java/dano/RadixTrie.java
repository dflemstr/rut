package dano;

import java.util.List;

public class RadixTrie<T> {

  private final Node<T> root;

  public RadixTrie(final Node<T> root) {
    this.root = root;
  }

  public T lookup(final CharSequence s) {
    return root.lookup(s, 0);
  }

  public static <T> Builder<T> builder(final Class<T> clazz) {
    return new Builder<T>();
  }

  public static class Node<T> {

    private static final char EMPTY = '\0';

    private final char first;
    private final char[] tail;
    private final Node<T> child1;
    private final Node<T> child2;
    private final Node<T>[] children;
    private final Node<T> capture;
    private final T value;

    public Node(final String prefix, final Node<T> capture,
                final List<Node<T>> children, final T value) {
      this.first = (prefix.length() == 0) ? EMPTY : prefix.charAt(0);
      this.tail = (prefix.length() > 1) ? prefix.substring(1).toCharArray() : null;
      this.capture = capture;
      this.child1 = children.size() > 0 ? children.get(0) : null;
      this.child2 = children.size() > 1 ? children.get(1) : null;
      this.children = children.size() > 2 ? toArray(children.subList(2, children.size())) : null;
      this.value = value;
    }

    @SuppressWarnings("unchecked")
    private static <T> RadixTrie.Node<T>[] toArray(final List<RadixTrie.Node<T>> children) {
      return children.toArray((RadixTrie.Node<T>[]) new RadixTrie.Node[children.size()]);
    }

    public T lookup(final CharSequence s, final int index) {
      if (!matchPrefix(s, index)) {
        return null;
      }
      final int newIndex = index + length();
      if (newIndex == s.length()) {
        return value;
      }
      final T childMatch = lookupChildren(s, newIndex);
      if (childMatch != null) {
        return childMatch;
      }
      return lookupCapture(s, newIndex);
    }

    private int length() {
      if (tail != null) {
        return 1 + tail.length;
      } else {
        return (first == EMPTY) ? 0 : 1;
      }
    }

    private boolean matchPrefix(final CharSequence s, final int index) {
      if (first == EMPTY) {
        return true;
      }
      if (index >= s.length()) {
        return false;
      }
      if (s.charAt(index) != first) {
        return false;
      }
      if (tail == null) {
        return true;
      }
      if (index + 1 + tail.length > s.length()) {
        return false;
      }
      for (int i = 0; i < tail.length; i++) {
        if (tail[i] != s.charAt(1 + index + i)) {
          return false;
        }
      }
      return true;
    }

    private T lookupCapture(final CharSequence s, final int newIndex) {
      if (capture == null) {
        return null;
      }
      final int maxCapture = indexOf(s, newIndex, '/') - 1;
      for (int i = maxCapture; i >= newIndex; i--) {
        final T value = capture.lookup(s, i);
        if (value != null) {
          return value;
        }
      }
      return null;
    }

    private T lookupChildren(final CharSequence s, final int newIndex) {
      if (child1 != null) {
        final T value = child1.lookup(s, newIndex);
        if (value != null) {
          return value;
        }
      }
      if (child2 != null) {
        final T value = child2.lookup(s, newIndex);
        if (value != null) {
          return value;
        }
      }
      if (children != null) {
        for (final Node<T> child : children) {
          final T value = child.lookup(s, newIndex);
          if (value != null) {
            return value;
          }
        }
      }
      return null;
    }

    private int indexOf(final CharSequence s, final int start, final char c) {
      int i = start;
      for (; i < s.length(); i++) {
        if (s.charAt(i) == c) {
          return i;
        }
      }
      return i;
    }

    @Override
    public String toString() {
      return "Node{'" + (first == EMPTY ? "" : String.valueOf(first)) +
                        (tail == null ? "" : new String(tail)) + "\':" +
             ", d=" + ((capture == null ? 0 : 1) +
                       (child1 == null ? 0 : 1) +
                       (child2 == null ? 0 : 1) +
                       (children == null ? 0 : children.length)) +
             ", c=" + (capture != null) +
             ", v=" + value +
             '}';
    }
  }

  @Override
  public String toString() {
    return "RadixTrie{" +
           "root=" + root +
           '}';
  }

  public static class Builder<T> {

    private final Trie<T> trie = new Trie<T>();

    public Builder<T> insert(final CharSequence path, final T value) {
      trie.insert(path, value);
      return this;
    }

    public RadixTrie<T> build() {
      return trie.compress();
    }
  }
}
