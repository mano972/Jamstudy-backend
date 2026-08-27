package com.app.studentromania.util;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * Helpers for the handful of places where user input has to be spliced into a
 * raw N1QL string instead of being passed as a bound parameter (the dynamic
 * search/sort clause builders in {@code ReviewFilter} / {@code QuestionFilter}).
 *
 * <ul>
 *   <li>{@link #escapeStringLiteral(String)} makes a value safe to embed inside a
 *       single-quoted N1QL string literal, so a stray quote or backslash can't
 *       break out of the literal and inject query syntax. N1QL string literals
 *       use backslash escaping, so {@code \} and {@code '} are the characters
 *       that matter. LIKE wildcards ({@code %}, {@code _}) are intentionally left
 *       alone - matching semantics are unchanged, this only closes the injection.</li>
 *   <li>{@link #sanitizeOrderByClause} whitelists the column/direction of an
 *       {@code ORDER BY} against the set the UI actually offers - an identifier
 *       position can't be quoted/escaped, so anything unrecognised falls back to
 *       the default sort.</li>
 * </ul>
 */
public class N1qlUtils {

	private static final Set<String> SORT_DIRECTIONS = new LinkedHashSet<>(Arrays.asList("asc", "desc"));

	public static String escapeStringLiteral(String input) {
		if (input == null) {
			return StringUtils.EMPTY;
		}
		return input.replace("\\", "\\\\").replace("'", "\\'");
	}

	/**
	 * @param orderBy         raw "field,direction" value from the request (may be null/blank/garbage)
	 * @param allowedFields   the only column names that may appear in ORDER BY
	 * @param fieldPrefix     optional alias prefix to prepend to the column (e.g. "r." for a join), or ""
	 * @param defaultClause   the full clause to return when orderBy is missing or not whitelisted
	 */
	public static String sanitizeOrderByClause(String orderBy, Set<String> allowedFields, String fieldPrefix,
			String defaultClause) {
		if (StringUtils.isBlank(orderBy)) {
			return defaultClause;
		}
		String[] parts = orderBy.split(",");
		if (parts.length != 2) {
			return defaultClause;
		}
		String field = parts[0].trim();
		String direction = parts[1].trim().toLowerCase();
		if (!allowedFields.contains(field) || !SORT_DIRECTIONS.contains(direction)) {
			return defaultClause;
		}
		return " order by " + StringUtils.defaultString(fieldPrefix) + field + " " + direction;
	}

}
