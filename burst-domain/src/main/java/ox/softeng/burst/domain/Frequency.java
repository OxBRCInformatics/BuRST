package ox.softeng.burst.domain;

import javax.persistence.*;

@Entity
@Table(schema="Subscription")
public class Frequency {

	@Id
	@Enumerated(EnumType.STRING)
	private FrequencyEnum frequency;

	public Frequency() {
		frequency = null;
	}
	
	public Frequency(FrequencyEnum freq)
	{
		frequency = freq;
	}


	public FrequencyEnum getFrequency() {
		return frequency;
	}

	public void setFrequency(FrequencyEnum frequency) {
		this.frequency = frequency;
	}
	
	
}