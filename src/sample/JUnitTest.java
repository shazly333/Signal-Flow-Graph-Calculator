package sample;

import static org.junit.Assert.assertEquals;

import javafx.scene.layout.Pane;
import org.junit.Test;
import sfg.SignalFlow;

import javax.script.ScriptException;

/**
 * @author mohamed kamal. sdvsdvsdvsdv.
 */
public class JUnitTest {
    /**
     * sdvsdv.
     */
    SignalFlow signalFlow;

    @Test
    public void test1() throws ScriptException {
            signalFlow = new SignalFlow(6);
            input("1", "2", "1");
            input("2", "3", "1");
            input("3", "4", "1");
            input("4", "5", "1");
            input("5", "6", "1");
            input("2", "4", "1");
            input("2", "5", "1");
            input("3", "2", "-1");
            input("4", "3", "-1");
            input("4", "4", "-1");
            signalFlow.getForwardPaths();
            signalFlow.getCycles();
            String[] delta = signalFlow.getDelta();
        System.out.println("Transfer Function: " + signalFlow.calculateTransferFunction());
        assertEquals("Wrong",1.25 , signalFlow.calculateTransferFunction(),0.001);

    }

    @Test
    public void test1WithParallelAndRemove() throws ScriptException {
        signalFlow = new SignalFlow(6);
        input("1", "2", "1");
        input("1", "2", "-1");
        input("1", "2", "1");
        input("2", "3", "1");
        signalFlow.removeGain(2, 3, new Pane());
        signalFlow.removeGain(2, 3, new Pane());
        input("2", "3", "-4");
        input("2", "3", "5");
        input("3", "4", "1");
        input("4", "5", "1");
        input("5", "6", "1");
        input("2", "4", "-1");
        input("2", "4", "2");
        input("2", "5", "1");
        input("3", "2", "-1");
        input("4", "3", "-1");
        input("4", "4", "-1");
        signalFlow.getForwardPaths();
        signalFlow.getCycles();
        String[] delta = signalFlow.getDelta();
        System.out.println("Transfer Function: " + signalFlow.calculateTransferFunction());
        assertEquals("Wrong",1.25 , signalFlow.calculateTransferFunction(),0.001);

    }

    @Test
    public void test2() throws ScriptException {

            signalFlow = new SignalFlow(5);
            input("1", "2", "1");
            input("2", "3", "1");
            input("3", "4", "1");
            input("4", "5", "1");
            input("1", "3", "1");
            input("4", "3", "-1");
            input("4", "2", "-1");
            signalFlow.getForwardPaths();
            signalFlow.getCycles();
            String[] delta = signalFlow.getDelta();
        System.out.println("Transfer Function: " + signalFlow.calculateTransferFunction());
            assertEquals("Wrong",0.6667 , signalFlow.calculateTransferFunction(),0.001);

    }
    @Test
    public void test3() throws ScriptException {

        signalFlow = new SignalFlow(6);
        input("1", "2", "1");
        input("2", "3", "1");
        input("3", "4", "1");
        input("4", "5", "1");
        input("5", "6", "1");
        input("2", "4", "1");

        input("3", "2", "-1");
        input("5", "2", "-1");
        input("5", "4", "-1");
        signalFlow.getForwardPaths();
        signalFlow.getCycles();
        String[] delta = signalFlow.getDelta();
        System.out.println("Transfer Function: " + signalFlow.calculateTransferFunction());
        assertEquals("Wrong",0.3333 , signalFlow.calculateTransferFunction(),0.001);

    }

    @Test
    public void test4() throws ScriptException {

        signalFlow = new SignalFlow(7);
        input("1", "2", "1");
        input("2", "3", "1");
        input("3", "4", "1");
        input("4", "6", "1");
        input("5", "6", "1");
        input("6", "7", "1");
        input("2", "5", "1");

        input("4", "3", "-1");
        input("5", "5", "-1");
        input("6", "4", "-1");
        input("6", "2", "-1");

        signalFlow.getForwardPaths();
        signalFlow.getCycles();
        String[] delta = signalFlow.getDelta();
        System.out.println("Transfer Function: " + signalFlow.calculateTransferFunction());
        assertEquals("Wrong",0.4 , signalFlow.calculateTransferFunction(),0.001);

    }
    @Test
    public void test5() throws ScriptException {

        signalFlow = new SignalFlow(9);
        input("1", "2", "1");
        input("2", "3", "1");
        input("3", "4", "1");
        input("4", "5", "1");
        input("5", "6", "1");
        input("6", "7", "1");
        input("7", "8", "1");
        input("8", "9", "1");
        input("3", "8", "1");

        input("8", "2", "-1");
        input("8", "5", "-1");
        input("6", "3", "-1");
        signalFlow.getForwardPaths();
        signalFlow.getCycles();
        String[] delta = signalFlow.getDelta();
        System.out.println("Transfer Function: " + signalFlow.calculateTransferFunction());
        assertEquals("Wrong",0.5 , signalFlow.calculateTransferFunction(),0.001);

    }
    @Test
    public void test6() throws ScriptException {

        signalFlow = new SignalFlow(9);
        input("1", "2", "1");
        input("2", "3", "1");
        input("3", "4", "1");
        input("4", "5", "1");
        input("5", "6", "1");
        input("6", "7", "1");
        input("7", "8", "1");
        input("8", "9", "1");
        input("4", "7", "1");
        input("6", "8", "1");

        input("8", "2", "-1");
        input("8", "6", "-1");
        input("6", "5", "-1");
        input("7", "3", "-1");

        signalFlow.getForwardPaths();
        signalFlow.getCycles();
        String[] delta = signalFlow.getDelta();
        System.out.println("Transfer Function: " + signalFlow.calculateTransferFunction());
        assertEquals("Wrong",0.333 , signalFlow.calculateTransferFunction(),0.001);

    }

    @Test
    public void test7() throws ScriptException {
        signalFlow = new SignalFlow(8);
        input("1", "2", "1");
        input("2", "3", "1");
        input("3", "4", "1");
        input("4", "5", "1");
        input("5", "6", "1");
        input("6", "7", "1");
        input("7", "8", "1");
        input("3", "2", "-1");
        input("5", "4", "-1");
        input("7", "6", "-1");
        signalFlow.getForwardPaths();
        signalFlow.getCycles();
        String[] delta = signalFlow.getDelta();
        System.out.println("Transfer Function: " + signalFlow.calculateTransferFunction());
        assertEquals("Wrong",0.125 , signalFlow.calculateTransferFunction(),0.001);


    }

    @Test
    public void test8() throws ScriptException {

        signalFlow = new SignalFlow(10);
        input("1", "2", "1");
        input("2", "3", "1");
        input("3", "4", "1");
        input("4", "5", "1");
        input("5", "6", "1");
        input("6", "7", "1");
        input("7", "8", "1");
        input("8", "9", "1");
        input("9", "10", "1");
        input("3", "2", "-1");
        input("5", "4", "-1");
        input("7", "6", "-1");
        input("9", "8", "-1");
        signalFlow.getForwardPaths();
        signalFlow.getCycles();
        String[] delta = signalFlow.getDelta();
        System.out.println("Transfer Function: " + signalFlow.calculateTransferFunction());
        assertEquals("Wrong",0.0625 , signalFlow.calculateTransferFunction(),0.001);


    }


    private void input (String source, String destination, String gain){

        signalFlow.addGain(Integer.parseInt(source), Integer.parseInt(destination), gain, null);
    }

}
