package ox.softeng.burst.domain.subscription;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "users", schema = "subscription")
public class User implements Serializable {

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


}
