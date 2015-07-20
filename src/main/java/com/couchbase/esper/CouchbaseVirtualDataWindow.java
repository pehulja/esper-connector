package com.couchbase.esper;

import java.util.Collections;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.couchbase.client.deps.com.fasterxml.jackson.core.JsonProcessingException;
import com.couchbase.client.deps.com.fasterxml.jackson.databind.ObjectMapper;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.RawJsonDocument;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.hook.VirtualDataWindow;
import com.espertech.esper.client.hook.VirtualDataWindowContext;
import com.espertech.esper.client.hook.VirtualDataWindowEvent;
import com.espertech.esper.client.hook.VirtualDataWindowLookup;
import com.espertech.esper.client.hook.VirtualDataWindowLookupContext;

/**
 * Created by David on 03/02/2015.
 */
public class CouchbaseVirtualDataWindow implements VirtualDataWindow {
	private static final Log log = LogFactory.getLog(CouchbaseVirtualDataWindowFactory.class);
	private final VirtualDataWindowContext context;
	private final Class type;
	private final Bucket bucket;
	private final ObjectMapper mapper;

	public CouchbaseVirtualDataWindow(Bucket bucket, VirtualDataWindowContext context) {
		this.context = context;
		this.bucket = bucket;
		this.type = context.getEventType().getUnderlyingType();
		this.mapper = new ObjectMapper();

		log.debug("CouchbaseVirtualDataWindow(): " + type);
	}

	public VirtualDataWindowLookup getLookup(VirtualDataWindowLookupContext desc) {
		// Place any code that interrogates the hash-index and btree-index
		// fields here.
		// Return the index representation.
		log.debug("getLookup()" + desc.getHashFields() + " : " + desc.getBtreeFields());

		return new CouchbaseVirtualDataWindowKeyValueLookup(bucket, type, context);
	}

	@Override
	public void handleEvent(VirtualDataWindowEvent virtualDataWindowEvent) {
		log.debug("handleEvent()");
	}

	public void update(EventBean[] newData, EventBean[] oldData) {
		// Insert events into window
		if (newData != null && newData.length > 0) {
			log.debug("Starting insert...");
			for (EventBean insertBean : newData) {
				try {
					String json = mapper.writeValueAsString(insertBean.get("value"));
					bucket.upsert(RawJsonDocument.create(insertBean.get("key").toString(), json));
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
			}
			log.debug("Finished insert...");
		}
		// Delete events from window
		if (oldData != null && oldData.length > 0) {
			log.debug("Starting delete...");
			for (EventBean deleteBean : oldData) {
				bucket.remove(deleteBean.get("key").toString());
			}

			log.debug("Finished delete...");
		}
		context.getOutputStream().update(newData, oldData);
	}

	public void destroy() {
		bucket.close();
	}

	@Override
	public Iterator<EventBean> iterator() {
		return Collections.<EventBean> emptyList().iterator();
	}
}