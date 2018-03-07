/*
 * CONFIDENTIAL -- Copyright 2017 Intuit Inc. This material contains certain trade secrets and confidential and
 * proprietary information of Intuit Inc. Use, reproduction, disclosure and distribution by any means are prohibited,
 * except pursuant to a written license from Intuit Inc. Use of copyright notice is precautionary and does not imply
 * publication or disclosure.
 *
 */

package com.intuit.sbg.appconnect.repository;

import java.util.List;

/**
 * Created by vkumar21 on 3/28/17.
 */
public interface Repository<T> {
    /**
     * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the
     * entity instance completely.
     *
     * @param entity
     */
    void save(T entity, boolean overwrite);

    /**
     * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the
     * entity instance completely.
     *
     * @param entity
     */
    void save(T entity);

    /**
     * Saves all given entities.
     *
     * @param entities
     * @throws IllegalArgumentException in case the given entity is {@literal null}.
     */
    void save(List<T> entities);

    /**
     * Retrieves an entity by its id.
     *
     * @param entity must set the primary key value.
     * @return the entity
     */
    T findOne(T entity);

    /**
     * Returns all instances of the type.
     *
     * @return all entities
     */
    List<T> findAll();

    List<T> query(T partitionKey);

    /**
     * Deletes the entity with the given id.
     *
     * @param entity must not be null.
     */
    void delete(T entity);

    /**
     * Deletes all entities managed by the repository.
     */

    void deleteAll();

    /**
     * Returns the number of entities available.
     *
     * @return the number of entities
     */
    long count();

    Class<T> getDomainClass();
}
