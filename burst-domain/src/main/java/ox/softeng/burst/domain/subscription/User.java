package ox.softeng.burst.domain.subscription;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "users", schema = "subscription", uniqueConstraints = @UniqueConstraint(name = "unique_email_address", columnNames = {"email_address"}))
@SequenceGenerator(name = "usersIdSeq", sequenceName = "subscription.users_id_seq")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;
    @Column(name = "email_address")
    protected String emailAddress;
    @Column(name = "first_name")
    protected String firstName;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "usersIdSeq")
    protected Long id = null;
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

    public Long getId() {
        return id;
    }

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
