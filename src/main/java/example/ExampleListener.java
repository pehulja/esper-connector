package example;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class ExampleListener implements UpdateListener {
    public void update(EventBean[] newEvents, EventBean[] oldEvents) {
        System.out.println("CATCHED");
    }
}
