package ox.softeng.burst.domain;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="Users", schema="Subscription")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;
	@Column(unique = true)
	protected String emailAddress;
	protected String firstName;
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id = null;
    protected String lastName;
    protected String organisation;
    
    public User(String firstName, String lastName, String emailAddress, String organisation)
    {
    	this.firstName = firstName;
    	this.lastName = lastName;
    	this.emailAddress = emailAddress;
    	this.organisation = organisation;
    }
    
    public User()
    {
    	
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
