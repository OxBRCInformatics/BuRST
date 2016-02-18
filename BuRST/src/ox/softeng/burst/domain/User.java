package ox.softeng.burst.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="Users", schema="Subscription")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    protected Long id = null;

    protected String firstName;
    protected String lastName;

    @Column(unique=true)
    protected String emailAddress;
    
    public User(String firstName, String lastName, String emailAddress)
    {
    	this.firstName = firstName;
    	this.lastName = lastName;
    	this.emailAddress = emailAddress;
    }
    
    public User()
    {
    	
    }
    
	
}
