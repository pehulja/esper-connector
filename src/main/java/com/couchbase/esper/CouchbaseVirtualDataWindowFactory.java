package com.couchbase.esper;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.espertech.esper.client.hook.VirtualDataWindow;
import com.espertech.esper.client.hook.VirtualDataWindowContext;
import com.espertech.esper.client.hook.VirtualDataWindowFactory;
import com.espertech.esper.client.hook.VirtualDataWindowFactoryContext;

/**
 * Created by David on 03/02/2015.
 */
public class CouchbaseVirtualDataWindowFactory implements VirtualDataWindowFactory {

	private static final Log log = LogFactory.getLog(CouchbaseVirtualDataWindowFactory.class);
	private Cluster cluster;
	private Bucket bucket;

	@Override
	public void initialize(VirtualDataWindowFactoryContext virtualDataWindowFactoryContext) {
		log.debug(virtualDataWindowFactoryContext);

		cluster = CouchbaseCluster.create("localhost");
		bucket = cluster.openBucket("esper", "Xig2eogh");
	}

	@Override
	public VirtualDataWindow create(VirtualDataWindowContext context) {
		return new CouchbaseVirtualDataWindow(bucket, context);
	}

	@Override
	public void destroyAllContextPartitions() {
		log.debug("destroyAllContextPartitions()");
	}

	@Override
	public Set<String> getUniqueKeyPropertyNames() {
		log.debug("getUniqueKeyPropertyNames()");
		return Collections.singleton("couchbaseId");
	}
}