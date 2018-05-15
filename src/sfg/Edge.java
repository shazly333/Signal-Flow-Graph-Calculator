package sfg;

/**
 * Created by M.Sharaf on 14/04/2018.
 */
public class Edge {
    private int destination;
    private String gain;
    private int source;
    private SfgLine line;


    public Edge(int destinationNode, int source, String gain, SfgLine line) {
        this.source = source;
        this.destination = destinationNode;
        this.gain = gain;
        this.line = line;
    }
    public Edge(int destinationNode,int source, String gain) {
        this.destination = destinationNode;
        this.gain = gain;
        this.source = source;
    }

    public int getSource() {
        return source;
    }

    public int getDestination() {

        return destination;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }

    public String getGain() {
        return gain;
    }

    public void setGain(String gain) {
        this.gain = gain;
    }

    public SfgLine getLine() {
        return line;
    }
}
