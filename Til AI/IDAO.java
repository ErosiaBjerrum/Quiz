package bybjerrum.dao;

import java.util.Set;

/**
 * CRUD operations
 * @param <T> T is the type of object to create and receive from the database.
 */
public interface IDAO <T>{
    T create(T t);
    Set<T> get();
    T getByID(Long id);
    T update(T t);
    Long delete(T t);
}
