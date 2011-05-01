package fr.bodysplash.mongolink.mapper;


import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.util.Map;

public class SubclassMapper<T> extends Mapper<T> {

    public SubclassMapper(Class<T> type) {
        super(type);
    }

    @Override
    protected void doPopulate(T instance, DBObject from) {
        parentMapper.populate(from, instance);
    }

    @Override
    protected void doSave(Object element, BasicDBObject object) {
        parentMapper.save(element, object);
        object.put("__discriminator", discriminator());
    }

    String discriminator() {
        return getPersistentType().getSimpleName();
    }

    void setParentMapper(Mapper<?> parentMapper) {
        this.parentMapper = parentMapper;
    }

    private Mapper<?> parentMapper;

    public static String discriminatorValue(DBObject from) {
        Object discriminator = from.get("__discriminator");
        return discriminator == null ? "" : discriminator.toString();
    }
}
