/*
 * MongoLink, Object Document Mapper for Java and MongoDB
 *
 * Copyright (c) 2012, Arpinum or third-party contributors as
 * indicated by the @author tags
 *
 * MongoLink is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MongoLink is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the Lesser GNU General Public License
 * along with MongoLink.  If not, see <http://www.gnu.org/licenses/>. 
 *
 */

package org.mongolink.domain.updateStrategy;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.mongodb.BasicDBList;

import java.util.ArrayList;
import java.util.Iterator;


public class ListVisitor extends Visitor {

	public ListVisitor(final DbObjectDiff dbObjectDiff, final BasicDBList origin) {
		super(dbObjectDiff, origin);
	}

	@Override
	public void visit(final Object target) {
		final BasicDBList targetList = (BasicDBList) target;
        Iterator<Object> targetIterator = targetList.iterator();
        Iterator<Object> originIterator = getOrigin().iterator();
        int index = 0;
        while(targetIterator.hasNext() && originIterator.hasNext()) {
            compare(index, originIterator.next(), targetIterator.next());
            index++;
        }
        addNewElements(targetIterator);
        removeDeletedElements(originIterator, index);
	}

    private void compare(int index, Object originElement, Object targetElement) {
        if(!Objects.equal(originElement, targetElement)) {
            getDbObjectDiff().pushKey(String.valueOf(index));
            getDbObjectDiff().addSet(targetElement);
            getDbObjectDiff().popKey();
        }
    }

    private void addNewElements(Iterator<Object> targetIterator) {
        ArrayList<Object> newElements = Lists.newArrayList(targetIterator);
        if(newElements.size() == 1) {
            getDbObjectDiff().addPush(newElements.get(0));
        }
        if(newElements.size() > 1) {
            getDbObjectDiff().addPushAll(newElements);
        }
    }

    private void removeDeletedElements(Iterator<Object> originIterator, int index) {
        if(originIterator.hasNext()) {
            getDbObjectDiff().addPull(null);
        }
        while (originIterator.hasNext()) {
            getDbObjectDiff().pushKey(String.valueOf(index));
            getDbObjectDiff().addUnset();
            getDbObjectDiff().popKey();
            originIterator.next();
            index++;
        }
    }

    @Override
	protected BasicDBList getOrigin() {
		return (BasicDBList) super.getOrigin();
	}

}
