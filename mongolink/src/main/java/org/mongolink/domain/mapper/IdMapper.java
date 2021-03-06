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

package org.mongolink.domain.mapper;


import com.mongodb.DBObject;
import org.bson.types.ObjectId;
import org.mongolink.MongoLinkError;
import org.mongolink.utils.PropertyContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

public class IdMapper implements Mapper {

    public IdMapper(PropertyContainer propertyContainer, IdGeneration generationStrategy) {
        this.propertyContainer = propertyContainer;
        this.generationStrategy = generationStrategy;
    }

    @Override
    public void save(Object instance, DBObject into) {
        try {
            final Object idValue = convertToDbValue(getIdValue(instance));
            if (idValue != null) {
                into.put(dbFieldName(), idValue);
            }
        } catch (Exception e) {
            LOGGER.error("Can't saveInto property {}", propertyContainer.shortName(), e);
        }
    }

    String dbFieldName() {
        return "_id";
    }

    @Override
    public void populate(Object instance, DBObject from) {
        try {
            Field field = mapper.getPersistentType().getDeclaredField(propertyContainer.shortName());
            field.setAccessible(true);
            field.set(instance, getIdValue(from));
            field.setAccessible(false);
        } catch (Exception e) {
            LOGGER.error("Error populating id into {}", propertyContainer, e);
        }
    }

    protected Object getIdValue(DBObject from) {
        return convertToObjectValue(from.get(dbFieldName()));
    }

    private Object convertToObjectValue(final Object id) {
        if (generationStrategy == IdGeneration.Auto) {
            return id == null ? "" : id.toString();
        }
        return id;
    }

    protected Object getIdValue(Object element) {
        try {
            return propertyContainer.invoke(element);
        } catch (Exception e) {
            throw new MongoLinkError("Can't get id value", e);
        }
    }

    Object convertToDbValue(Object id) {
        if (generationStrategy == IdGeneration.Natural) {
            return id;
        }
        if (id != null) {
            return new ObjectId(id.toString());
        }
        return null;
    }

    public void setMapper(AggregateMapper<?> mapper) {
        this.mapper = mapper;
    }

    public void natural() {
        generationStrategy = IdGeneration.Natural;
    }

    public void auto() {
        generationStrategy = IdGeneration.Auto;
    }

    private IdGeneration generationStrategy;
    private final PropertyContainer propertyContainer;
    private static final Logger LOGGER = LoggerFactory.getLogger(IdMapper.class);
    private AggregateMapper<?> mapper;
}
