package jco.ql.db.ds.server.util;

public final class DataSourceUtils {

	private static final String VALID_ELEMENT_NAME_REGEX = "([A-Za-z0-9\\-\\_]+)";

	public static boolean validDatabaseName(String name) {
		return checkValidElementName(name);
	}

	public static boolean validCollectionName(String name) {
		return checkValidElementName(name);
	}
	
	private static boolean checkValidElementName(String name) {
		return name != null && !name.trim().isEmpty()
				&& name.matches(VALID_ELEMENT_NAME_REGEX);
	}
}
