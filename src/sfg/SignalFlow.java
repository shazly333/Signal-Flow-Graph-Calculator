package sfg;

import javafx.scene.layout.Pane;
import sample.Controller;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Created by M.Sharaf on 14/04/2018.
 */
public class SignalFlow {
    private int nodeNum;
    private ArrayList<Edge>[] graph;
    private ArrayList<SfgPath> forwardPaths, cycles;
    private boolean[] selfLoop;
    private String[] deltas;

    public SignalFlow(int nodeNum) {
        this.nodeNum = nodeNum;
        graph = new ArrayList[nodeNum];

        for (int i = 0; i < nodeNum; i++) {
            graph[i] = new ArrayList<>();
        }

        selfLoop = new boolean[nodeNum];

    }

    public int forwardPathsNum() {
        return forwardPaths.size();
    }


    public void addGain(int source, int destination, String gain, SfgLine line) {
        Edge edge = new Edge(destination - 1, source - 1, gain, line);
        graph[source - 1].add(edge);
        if (source == destination) {
            selfLoop[source] = true;
        }
    }

    public void removeGain(int source, int destination, Pane pane) {
        int index = findEdge(source, destination);
        if (index == -1)
            return;

        try {
            graph[source - 1].get(index).getLine().removeLine(pane);
        } catch (NullPointerException e) {
            //Just for JUnitTest
        }
        graph[source - 1].remove(index);
    }

    public int findEdge(int source, int destination) {
        int index = -1;
        for (int i = 0; i < graph[source - 1].size(); i++) {
            if (graph[source - 1].get(i).getDestination() == destination - 1) {
                index = i;
                break;
            }
        }
        return index;
    }

    public ArrayList<Edge>[] getGraph() {
        return graph;
    }

    public void printGraph() throws IOException {
        ArrayList<SfgPath> forwardPaths = getCycles();
        for (int i = 0; i < forwardPaths.size(); i++) {
            System.out.println("SfgPath Number " + (i + 1));
            System.out.print(forwardPaths.get(i).getPath().get(0).getSource() + "  ");
            for (int j = 0; j < forwardPaths.get(i).getPath().size(); j++)
                System.out.print(forwardPaths.get(i).getPath().get(j).getDestination() + "  ");

            System.out.println("");
        }
    }

    public ArrayList<SfgPath> getForwardPaths() {

        ArrayList<SfgPath> forwardPaths = new ArrayList<>();
        SfgPath forwardPath = new SfgPath(new ArrayList<>());
        boolean[] visited = new boolean[nodeNum];
        visited[0] = true;
        dfs(0, visited, forwardPaths, forwardPath);
        this.forwardPaths = forwardPaths;
        return forwardPaths;
    }

    private void dfs(int node, boolean[] visited, ArrayList<SfgPath> forwardPaths, SfgPath forwardPath) {

        if (node == nodeNum - 1) {
            forwardPaths.add(new SfgPath((ArrayList<Edge>) forwardPath.getPath().clone()));
            return;
        }

        for (int i = 0; i < graph[node].size(); i++) {
            Edge edge = graph[node].get(i);
            if (!visited[edge.getDestination()]) {
                visited[edge.getDestination()] = true;
                forwardPath.addEdgeToPath(edge);
                dfs(edge.getDestination(), visited, forwardPaths, forwardPath);
                forwardPath.removeLastNode();
                visited[edge.getDestination()] = false;
            }
        }

    }

    public ArrayList<SfgPath> getCycles() {

        ArrayList<SfgPath> cycles = new ArrayList<>();
        SfgPath cycle = new SfgPath(new ArrayList<>());
        for (int i = 0; i < nodeNum; i++) {
            boolean[] isVisited = new boolean[nodeNum];
            detectCycle(i, i, isVisited, cycle, cycles);
        }
        removeDuplicateCycles(cycles);
        this.cycles = cycles;
        return cycles;
    }

    public void detectCycle(int startNode, int currentNode, boolean[] isVisited, SfgPath cycle, ArrayList<SfgPath> cycles) {

        if (currentNode == startNode && cycle.getPath().size() != 0) {
            cycles.add(new SfgPath((ArrayList<Edge>) cycle.getPath().clone()));
            return;
        }

        for (int i = 0; i < graph[currentNode].size(); i++) {
            Edge edge = graph[currentNode].get(i);
            if (!isVisited[edge.getDestination()] || edge.getDestination() == startNode) {
                isVisited[edge.getDestination()] = true;
                cycle.addEdgeToPath(edge);
                detectCycle(startNode, edge.getDestination(), isVisited, cycle, cycles);
                cycle.removeLastNode();
                isVisited[edge.getDestination()] = false;

            }


        }
    }

    private void removeDuplicateCycles(ArrayList<SfgPath> cycles) {
        for (int i = 0; i < cycles.size(); i++) {
            for (int j = cycles.size() - 1; j > i; j--) {
                if (equalCycle(cycles.get(i), cycles.get(j))) {
                    cycles.remove(j);
                }
            }
        }
    }

    private boolean equalCycle(SfgPath path1, SfgPath path2) {
        HashSet<Edge> edges = new HashSet<>();
        for (int i = 0; i < path1.getPath().size(); i++)
            edges.add(path1.getPath().get(i));
        for (int i = 0; i < path2.getPath().size(); i++)
            if (!edges.contains(path2.getPath().get(i)))
                return false;
        return true;
    }

    public String[] getDelta() {
        String[] deltas = new String[forwardPaths.size() + 1];
        String[] cycleGain;
        StringBuilder gain = new StringBuilder();
        ArrayList<String> nonTouchingGain;
        ArrayList<SfgPath> cycles = this.cycles;

        for (int i = -1; i < forwardPaths.size(); i++) {
            gain.setLength(0);
            if (i != -1)//sub Delta
                cycles = removePathFromAllCycles(this.cycles, forwardPaths.get(i));

            Controller.cyclesAfterPaths.add(cycles);

            cycleGain = calculatePathGain(cycles);
            nonTouchingGain = nonTouchingLoopsGains(cycleGain, cycles);

            gain.append("1");

            if (!cycles.isEmpty()) {
                gain.append("-(");

                for (int j = 0; j < cycles.size(); j++) {
                    gain.append(cycleGain[j]);

                    if (j < cycles.size() - 1)
                        gain.append("+");
                }
                gain.append(")");

                for (int j = 0; j < nonTouchingGain.size(); j++) {
                    if (!nonTouchingGain.get(j).equals("")) {
                        if (j % 2 == 0)
                            gain.append("+(" + nonTouchingGain.get(j) + ")");
                        else
                            gain.append("-(" + nonTouchingGain.get(j) + ")");
                    }
                }
            }

            deltas[i + 1] = gain.toString();
        }
        this.deltas = deltas;
        return deltas;
    }

    private ArrayList<SfgPath> removePathFromAllCycles(ArrayList<SfgPath> cycles, SfgPath path) {
        ArrayList<SfgPath> newCycles = new ArrayList<>();
        HashSet<Integer> forwardPathNodes = new HashSet<>();
        boolean touching;

        for (int i = 0; i < path.getPath().size(); i++)
            forwardPathNodes.add(path.getPath().get(i).getSource());
        forwardPathNodes.add(nodeNum - 1);//TODO last node is not in forward paths !!!

        for (int i = 0; i < cycles.size(); i++) {
            touching = false;
            for (int j = 0; j < cycles.get(i).getPath().size() && !touching; j++) {
                if (forwardPathNodes.contains(cycles.get(i).getPath().get(j).getSource()))
                    touching = true;
            }

            if (!touching)
                newCycles.add(cycles.get(i));

        }

        return newCycles;
    }

    private ArrayList<String> nonTouchingLoopsGains(String[] cycleGain, ArrayList<SfgPath> cycles) {
        ArrayList<String> gain = new ArrayList<>();
        LinkedList<LinkedList<Integer>> nonTouched = new LinkedList<>();
        LinkedList<Integer>[] arrayOfTouchingCycles = touchingArray(cycles);//correct
        int previousNonTouched, cycleIndex;
        boolean touching = false;

        for (int i = 0; i < cycles.size(); i++) {
            nonTouched.add(new LinkedList<>());
            nonTouched.getLast().add(i);
        }

        previousNonTouched = cycles.size();
        for (int i = 0; i < previousNonTouched; i++) {
            for (int j = nonTouched.get(i).getLast() + 1; j < cycles.size(); j++) {
                touching = false;
                for (int k = 0; k < nonTouched.get(i).size() && !touching; k++) {
                    if (arrayOfTouchingCycles[nonTouched.get(i).get(k)].contains(j))
                        touching = true;
                }

                if (!touching) {
                    nonTouched.add(new LinkedList<>());
                    for (int k = 0; k < nonTouched.get(i).size(); k++) {
                        nonTouched.get(nonTouched.size() - 1).add(nonTouched.get(i).get(k));
                    }
                    nonTouched.get(nonTouched.size() - 1).add(j);
                }
            }

            if (i == previousNonTouched - 1) {
                for (int j = 0; j < previousNonTouched; j++) {
                    Controller.allNonTouched.add(nonTouched.removeFirst());
                }
                i = -1;
                previousNonTouched = nonTouched.size();
                gain.add(calculateNonTouchingGain(nonTouched, cycleGain));
            }
        }

        return gain;
    }

    private LinkedList<Integer>[] touchingArray(ArrayList<SfgPath> cycles) {
        LinkedList<Integer>[] arr = new LinkedList[cycles.size()];
        boolean touching;

        for (int i = 0; i < cycles.size(); i++)
            arr[i] = new LinkedList<>();

        for (int i = 0; i < cycles.size(); i++) {
            HashSet<Integer> nodes = new HashSet<>();
            for (int j = 0; j < cycles.get(i).getPath().size(); j++)
                nodes.add(cycles.get(i).getPath().get(j).getSource());

            for (int j = 0; j < cycles.size(); j++) {
                touching = false;
                for (int k = 0; k < cycles.get(j).getPath().size() && !touching; k++) {
                    if (nodes.contains(cycles.get(j).getPath().get(k).getSource())) {
                        touching = true;
                        break;
                    }
                }

                if (touching)
                    arr[i].add(j);
            }
        }
        return arr;
    }

    private String[] calculatePathGain(ArrayList<SfgPath> sfgPaths) {
        int i = -1;
        String[] gains = new String[sfgPaths.size()];
        StringBuilder gain = new StringBuilder();
        for (SfgPath path : sfgPaths) {
            i++;
            gain.setLength(0);
            for (Edge edge : path.getPath()) {
                gain.append(edge.getGain() + "*");
            }
            gain.setLength(gain.length() - 1);
            gains[i] = gain.toString();
        }
        return gains;
    }

    private String calculateNonTouchingGain(LinkedList<LinkedList<Integer>> cycles, String[] cycleGain) {
        StringBuilder gain = new StringBuilder();
        for (int i = 0; i < cycles.size(); i++) {
            for (int j = 0; j < cycles.get(i).size(); j++) {
                gain.append(cycleGain[cycles.get(i).get(j)] + "*");
            }
            gain.setLength(gain.length() - 1);

            if (i < cycles.size() - 1)
                gain.append("+");

        }
        return gain.toString();
    }

    public double calculateTransferFunction() throws ScriptException {

        String[] pathGain = calculatePathGain(forwardPaths);
        String[] deltaGain = this.deltas;
        double transferFunction = 0;
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        for (int i = 0; i < forwardPaths.size(); i++) {
            Double pathGainValue = new Double(String.valueOf(engine.eval(pathGain[i])));
            Double deltalGainValue = new Double(String.valueOf(engine.eval(deltaGain[i + 1])));
            transferFunction = transferFunction + (pathGainValue * deltalGainValue);
        }
        Double bigDeltalGainValue = new Double(String.valueOf(engine.eval(deltaGain[0])));
        transferFunction /= bigDeltalGainValue;
        return transferFunction;
    }

}
