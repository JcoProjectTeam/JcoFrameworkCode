package jco.ql.db.ds.core.datatype;

import java.util.List;
import java.util.Map;

public class CollectionWrapper {
	
	private final List<Map<String, Object>> documents;
	
	private final int count;
	
	private final boolean complete;
	
	private final int remaining;
	
	private final int partialOffset;
	
	public CollectionWrapper(List<Map<String, Object>> documents, int count, boolean complete, int remaining,
			int partialOffset) {
		super();
		this.documents = documents;
		this.count = count;
		this.complete = complete;
		this.remaining = remaining;
		this.partialOffset = partialOffset;
	}

	public List<Map<String, Object>> getDocuments() {
		return documents;
	}

	public int getCount() {
		return count;
	}

	public boolean isComplete() {
		return complete;
	}

	public int getRemaining() {
		return remaining;
	}

	public int getPartialOffset() {
		return partialOffset;
	}

}
