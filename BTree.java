class BTreeNode {
    int[] keys;
    int T;
    BTreeNode[] children;
    int n;
    boolean leaf;

    public BTreeNode(int T, boolean leaf) {
        this.T = T;
        this.leaf = leaf;
        this.keys = new int[2 * T - 1];
        this.children = new BTreeNode[2 * T];
        this.n = 0;
    }

    public BTreeNode search(int k) {
        int i = 0;
        while (i < n && k > keys[i]) {
            i++;
        }

        if (i < n && keys[i] == k) {
            return this;
        }

        if (leaf) {
            return null;
        }

        return children[i].search(k);
    }
}

public class BTree {
    private BTreeNode root;
    private int T;

    public BTree(int t) {
        this.root = null;
        this.T = t;
    }

    public void insert(int k) {
        if (root == null) {
            root = new BTreeNode(T, true);
            root.keys[0] = k;
            root.n = 1;
        } else {
            if (root.n == 2 * T - 1) {
                BTreeNode s = new BTreeNode(T, false);
                s.children[0] = root;
                splitChild(s, 0, root);
                int i = 0;
                if (s.keys[0] < k) {
                    i++;
                }
                insertNonFull(s.children[i], k);
                root = s;
            } else {
                insertNonFull(root, k);
            }
        }
    }

    private void insertNonFull(BTreeNode x, int k) {
        int i = x.n - 1;
        if (x.leaf) {
            while (i >= 0 && x.keys[i] > k) {
                x.keys[i + 1] = x.keys[i];
                i--;
            }
            x.keys[i + 1] = k;
            x.n = x.n + 1;
        } else {
            while (i >= 0 && x.keys[i] > k) {
                i--;
            }
            if (x.children[i + 1].n == 2 * T - 1) {
                splitChild(x, i + 1, x.children[i + 1]);
                if (x.keys[i + 1] < k) {
                    i++;
                }
            }
            insertNonFull(x.children[i + 1], k);
        }
    }

    private void splitChild(BTreeNode x, int i, BTreeNode y) {
        BTreeNode z = new BTreeNode(y.T, y.leaf);
        z.n = T - 1;
        for (int j = 0; j < T - 1; j++) {
            z.keys[j] = y.keys[j + T];
        }
        if (!y.leaf) {
            for (int j = 0; j < T; j++) {
                z.children[j] = y.children[j + T];
            }
        }
        y.n = T - 1;
        for (int j = x.n; j >= i + 1; j--) {
            x.children[j + 1] = x.children[j];
        }
        x.children[i + 1] = z;
        for (int j = x.n - 1; j >= i; j--) {
            x.keys[j + 1] = x.keys[j];
        }
        x.keys[i] = y.keys[T - 1];
        x.n = x.n + 1;
    }

    public BTreeNode search(int k) {
        if (root == null) return null;
        return root.search(k);
    }

    public void remove(int k) {
        if (root == null) return;
        removeFromNode(root, k);
        if (root.n == 0) {
            if (root.leaf) root = null;
            else root = root.children[0];
        }
    }

    private void removeFromNode(BTreeNode node, int k) {
        int idx = findKey(node, k);
        if (idx < node.n && node.keys[idx] == k) {
            if (node.leaf) removeFromLeaf(node, idx);
            else removeFromNonLeaf(node, idx);
        } else {
            if (node.leaf) return;
            boolean flag = (idx == node.n);
            if (node.children[idx].n < T) fill(node, idx);
            if (flag && idx > node.n) removeFromNode(node.children[idx - 1], k);
            else removeFromNode(node.children[idx], k);
        }
    }

    private int findKey(BTreeNode node, int k) {
        int idx = 0;
        while (idx < node.n && node.keys[idx] < k) idx++;
        return idx;
    }

    private void removeFromLeaf(BTreeNode node, int idx) {
        for (int i = idx + 1; i < node.n; ++i) node.keys[i - 1] = node.keys[i];
        node.n--;
    }

    private void removeFromNonLeaf(BTreeNode node, int idx) {
        int k = node.keys[idx];
        if (node.children[idx].n >= T) {
            int pred = getPredecessor(node, idx);
            node.keys[idx] = pred;
            removeFromNode(node.children[idx], pred);
        } else if (node.children[idx + 1].n >= T) {
            int succ = getSuccessor(node, idx);
            node.keys[idx] = succ;
            removeFromNode(node.children[idx + 1], succ);
        } else {
            merge(node, idx);
            removeFromNode(node.children[idx], k);
        }
    }

    private int getPredecessor(BTreeNode node, int idx) {
        BTreeNode cur = node.children[idx];
        while (!cur.leaf) cur = cur.children[cur.n];
        return cur.keys[cur.n - 1];
    }

    private int getSuccessor(BTreeNode node, int idx) {
        BTreeNode cur = node.children[idx + 1];
        while (!cur.leaf) cur = cur.children[0];
        return cur.keys[0];
    }

    private void fill(BTreeNode node, int idx) {
        if (idx != 0 && node.children[idx - 1].n >= T) borrowFromPrev(node, idx);
        else if (idx != node.n && node.children[idx + 1].n >= T) borrowFromNext(node, idx);
        else {
            if (idx != node.n) merge(node, idx);
            else merge(node, idx - 1);
        }
    }

    private void borrowFromPrev(BTreeNode node, int idx) {
        BTreeNode child = node.children[idx];
        BTreeNode sibling = node.children[idx - 1];
        for (int i = child.n - 1; i >= 0; --i) child.keys[i + 1] = child.keys[i];
        if (!child.leaf) {
            for (int i = child.n; i >= 0; --i) child.children[i + 1] = child.children[i];
        }
        child.keys[0] = node.keys[idx - 1];
        if (!child.leaf) child.children[0] = sibling.children[sibling.n];
        node.keys[idx - 1] = sibling.keys[sibling.n - 1];
        child.n++;
        sibling.n--;
    }

    private void borrowFromNext(BTreeNode node, int idx) {
        BTreeNode child = node.children[idx];
        BTreeNode sibling = node.children[idx + 1];
        child.keys[child.n] = node.keys[idx];
        if (!child.leaf) child.children[child.n + 1] = sibling.children[0];
        node.keys[idx] = sibling.keys[0];
        for (int i = 1; i < sibling.n; ++i) sibling.keys[i - 1] = sibling.keys[i];
        if (!sibling.leaf) {
            for (int i = 1; i <= sibling.n; ++i) sibling.children[i - 1] = sibling.children[i];
        }
        child.n++;
        sibling.n--;
    }

    private void merge(BTreeNode node, int idx) {
        BTreeNode child = node.children[idx];
        BTreeNode sibling = node.children[idx + 1];
        child.keys[T - 1] = node.keys[idx];
        for (int i = 0; i < sibling.n; ++i) child.keys[i + T] = sibling.keys[i];
        if (!child.leaf) {
            for (int i = 0; i <= sibling.n; ++i) child.children[i + T] = sibling.children[i];
        }
        for (int i = idx + 1; i < node.n; ++i) node.keys[i - 1] = node.keys[i];
        for (int i = idx + 2; i <= node.n; ++i) node.children[i - 1] = node.children[i];
        child.n += sibling.n + 1;
        node.n--;
    }

    public static void main(String[] args) {
        java.util.Scanner sc = new java.util.Scanner(System.in);
        BTree arbol = new BTree(3);
        int opcion, valor;

        do {
            System.out.println("\n--- MENU ARBOL B ---");
            System.out.println("1. Insertar clave");
            System.out.println("2. Eliminar clave");
            System.out.println("3. Buscar clave");
            System.out.println("4. Salir");
            System.out.print("Seleccione: ");
            opcion = sc.nextInt();

            switch (opcion) {
                case 1:
                    System.out.print("Ingrese valor a insertar: ");
                    valor = sc.nextInt();
                    arbol.insert(valor);
                    break;
                case 2:
                    System.out.print("Ingrese valor a eliminar: ");
                    valor = sc.nextInt();
                    arbol.remove(valor);
                    break;
                case 3:
                    System.out.print("Ingrese valor a buscar: ");
                    valor = sc.nextInt();
                    if (arbol.search(valor) != null) {
                        System.out.println("Clave encontrada.");
                    } else {
                        System.out.println("Clave no encontrada.");
                    }
                    break;
            }
        } while (opcion != 4);
    }
}