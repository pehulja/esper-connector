package example;

import com.couchbase.client.deps.com.fasterxml.jackson.core.JsonProcessingException;
import com.couchbase.client.deps.com.fasterxml.jackson.databind.ObjectMapper;
import com.couchbase.client.java.document.RawJsonDocument;
import com.couchbase.esper.*;
import com.espertech.esper.client.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by David on 03/02/2015.
 */
public class Main {

	private static Random random = new Random();
	private static final Log log = LogFactory.getLog(Main.class);

	public static void main(String[] args) {

		Main sample = new Main();
		try {
			sample.run();
		} catch (RuntimeException ex) {
			log.error("Unexpected exception :" + ex.getMessage(), ex);
		}
	}

	public void run() {
		log.info("Setting up engine instance.");
		Configuration config = new Configuration();
		config.addEventType(Data.class);
		config.addPlugInVirtualDataWindow("couchbase", "couchbasevdw", CouchbaseVirtualDataWindowFactory.class.getName());
		
		EPServiceProvider epService = EPServiceProviderManager.getProvider("CouchbaseExternalDataExample", config);
		epService.getEPAdministrator().createEPL("create schema CouchbaseEvent as (key string, value example.Data)");

		log.info("Creating named window with virtual.");
		EPStatement statement = epService.getEPAdministrator().createEPL("create window CouchbaseWindow.couchbase:couchbasevdw() as CouchbaseEvent");
		epService.getEPAdministrator().createEPL("insert into CouchbaseWindow select * from CouchbaseEvent");
		
		postSomeEvents(epService);
		runSampleFireAndForgetQuery(epService);
		count(epService);
	}

	private void runSampleFireAndForgetQuery(EPServiceProvider epService) {
		String fireAndForget = "select * from CouchbaseWindow where key in ('_test', 'test2')";
		EPOnDemandQueryResult result = epService.getEPRuntime().executeQuery(fireAndForget);
		log.info("Fire-and-forget query returned: ");
		for (EventBean eventBean : result.getArray()) {
			System.out.println(eventBean.get("key") + " : " + eventBean.get("value").toString());
		}
	}

	private void postSomeEvents(EPServiceProvider epService) {
		Data data = new Data("name", 10d);
		Map<String, Object> event = new HashMap<>();
		event.put("key", "test2");
		event.put("value", data);
		epService.getEPRuntime().sendEvent(event, "CouchbaseEvent");
	}
	
	private void count(EPServiceProvider epService) {
		String stmt = "select count(*) from CouchbaseWindow where key in ('_test', 'test2')";
		EPOnDemandQueryResult result = epService.getEPRuntime().executeQuery(stmt);
		for (EventBean eventBean : result.getArray()) {
			System.out.println("COUNT: " + eventBean.get("count(*)"));
		}
	}

}
