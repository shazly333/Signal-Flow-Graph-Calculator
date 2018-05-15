package sfg;

import java.util.ArrayList;

public class SfgPath {
    ArrayList<Edge> path = new ArrayList<>();

    public SfgPath(ArrayList<Edge> path) {
        this.path = path;
    }

    public ArrayList<Edge> getPath() {
        return path;
    }
    public void addEdgeToPath(Edge edge) {
        path.add(edge);
    }
    public void removeLastNode() {
        path.remove(path.size() - 1);
    }

}
