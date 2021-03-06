/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.task.repository.database.support;

import java.util.Map;

import org.springframework.batch.item.database.Order;

/**
 * Utility class that generates the actual SQL statements used by query
 * providers.
 *
 * @author Glenn Renfro
 */
public class SqlPagingQueryUtils {

	/**
	 * Generate SQL query string using a LIMIT clause
	 *
	 * @param provider {@link AbstractSqlPagingQueryProvider} providing the
	 * implementation specifics
	 * @param limitClause the implementation specific top clause to be used
	 * @return the generated query
	 */
	public static String generateLimitJumpToQuery(AbstractSqlPagingQueryProvider provider, String limitClause) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ").append(provider.getSelectClause());
		sql.append(" FROM ").append(provider.getFromClause());
		sql.append(provider.getWhereClause() == null ? "" : " WHERE " + provider.getWhereClause());
		sql.append(" ORDER BY ").append(buildSortClause(provider));
		sql.append(" " + limitClause);

		return sql.toString();
	}

	/**
	 * Generate SQL query string using a TOP clause
	 *
	 * @param provider {@link AbstractSqlPagingQueryProvider} providing the
	 * implementation specifics
	 * @param topClause the implementation specific top clause to be used
	 * @return the generated query
	 */
	public static String generateTopJumpToQuery(AbstractSqlPagingQueryProvider provider, String topClause) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ").append(topClause).append(" ").append(provider.getSelectClause());
		sql.append(" FROM ").append(provider.getFromClause());
		sql.append(provider.getWhereClause() == null ? "" : " WHERE " + provider.getWhereClause());
		sql.append(" ORDER BY ").append(buildSortClause(provider));

		return sql.toString();
	}

	public static String generateRowNumSqlQueryWithNesting(AbstractSqlPagingQueryProvider provider,
														   String selectClause, boolean remainingPageQuery, String rowNumClause) {
		return generateRowNumSqlQueryWithNesting(provider, selectClause, selectClause, remainingPageQuery, rowNumClause);
	}

	public static String generateRowNumSqlQueryWithNesting(AbstractSqlPagingQueryProvider provider,
														   String innerSelectClause, String outerSelectClause, boolean remainingPageQuery, String rowNumClause) {

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ").append(outerSelectClause).append(" FROM (SELECT ").append(outerSelectClause)
				.append(", ").append("ROWNUM as TMP_ROW_NUM");
		sql.append(" FROM (SELECT ").append(innerSelectClause).append(" FROM ").append(provider.getFromClause());
		buildWhereClause(provider, remainingPageQuery, sql);
		sql.append(" ORDER BY ").append(buildSortClause(provider));
		sql.append(")) WHERE ").append(rowNumClause);

		return sql.toString();

	}

	private static void buildWhereClause(AbstractSqlPagingQueryProvider provider, boolean remainingPageQuery,
										 StringBuilder sql) {
		if (remainingPageQuery) {
			sql.append(" WHERE ");
			if (provider.getWhereClause() != null) {
				sql.append("(");
				sql.append(provider.getWhereClause());
				sql.append(") AND ");
			}

		}
		else {
			sql.append(provider.getWhereClause() == null ? "" : " WHERE " + provider.getWhereClause());
		}
	}

	/**
	 * Generates ORDER BY attributes based on the sort keys.
	 *
	 * @param provider {@link AbstractSqlPagingQueryProvider} providing the
	 * implementation specifics
	 * @return a String that can be appended to an ORDER BY clause.
	 */
	public static String buildSortClause(AbstractSqlPagingQueryProvider provider) {
		return buildSortClause(provider.getSortKeys());
	}

	/**
	 * Generates ORDER BY attributes based on the sort keys.
	 *
	 * @param sortKeys generates order by clause from map
	 * @return a String that can be appended to an ORDER BY clause.
	 */
	public static String buildSortClause(Map<String, Order> sortKeys) {
		StringBuilder builder = new StringBuilder();
		String prefix = "";

		for (Map.Entry<String, Order> sortKey : sortKeys.entrySet()) {
			builder.append(prefix);

			prefix = ", ";

			builder.append(sortKey.getKey());

			if(sortKey.getValue() != null && sortKey.getValue() == Order.DESCENDING) {
				builder.append(" DESC");
			}
			else {
				builder.append(" ASC");
			}
		}

		return builder.toString();
	}

}
