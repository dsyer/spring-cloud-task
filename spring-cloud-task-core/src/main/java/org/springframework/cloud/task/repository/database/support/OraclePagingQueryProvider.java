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

import org.springframework.cloud.task.repository.database.PagingQueryProvider;
import org.springframework.data.domain.Pageable;

/**
 * Oracle implementation of a {@link PagingQueryProvider} using database specific features.
 *
 * @author Glenn Renfro
 */
public class OraclePagingQueryProvider extends AbstractSqlPagingQueryProvider {

	@Override
	public String getPageQuery(Pageable pageable) {
		int offset = pageable.getOffset()+1;
		return SqlPagingQueryUtils.generateRowNumSqlQueryWithNesting(this, getSelectClause(), getSelectClause(), false, "TMP_ROW_NUM >= "
				+ offset + " AND TMP_ROW_NUM < " + (offset+pageable.getPageSize()));
	}
}
