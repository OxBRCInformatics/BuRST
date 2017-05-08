/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 James Welch
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package ox.softeng.burst.domain.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import java.util.Collection;
import java.util.List;

/**
 * @since 04/05/2017
 */
abstract public class DomainClass {

    public void save(EntityManagerFactory entityManagerFactory) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.merge(this);
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    public static <K extends DomainClass> Integer count(EntityManagerFactory entityManagerFactory, Class<K> clazz) {
        return list(entityManagerFactory, clazz).size();
    }

    @SuppressWarnings("unchecked")
    public static <K extends DomainClass> List<K> list(EntityManagerFactory entityManagerFactory, Class<K> clazz) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        String queryStr = "SELECT res FROM " + clazz.getName() + " res";
        Query query = entityManager.createQuery(queryStr);
        List<K> results = query.getResultList();
        entityManager.close();
        return results;
    }

    public static <K extends DomainClass> void saveAll(EntityManagerFactory entityManagerFactory, Collection<K> entities) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        entities.forEach(entityManager::merge);
        entityManager.getTransaction().commit();
        entityManager.close();
    }
}
