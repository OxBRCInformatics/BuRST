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
package ox.softeng.burst.domain.subscription;

import ox.softeng.burst.domain.util.DomainClass;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@NamedQueries({
                      @NamedQuery(name = "user.find_by_email",
                                  query = "select u from User u" +
                                          " where u.emailAddress = :email")
              })
@Entity
@Table(name = "users", schema = "subscription")
public class User extends DomainClass implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "email_address")
    protected String emailAddress;
    @Column(name = "first_name")
    protected String firstName;
    @Column(name = "last_name")
    protected String lastName;
    protected String organisation;

    public User(String firstName, String lastName, String emailAddress, String organisation) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.organisation = organisation;
    }

    public User() {

    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    //    public Long getId() {
    //        return id;
    //   }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getOrganisation() {
        return organisation;
    }

    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }

    public static User findOrCreate(EntityManagerFactory entityManagerFactory, User user) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        TypedQuery<User> query = entityManager.createNamedQuery("user.find_by_email", User.class);
        query.setParameter("email", user.emailAddress);
        List<User> users = query.getResultList();
        entityManager.close();
        if (users.isEmpty()) return user;
        return users.get(0);
    }
}
