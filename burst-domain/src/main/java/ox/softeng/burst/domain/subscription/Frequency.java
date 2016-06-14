package ox.softeng.burst.domain.subscription;

import ox.softeng.burst.domain.FrequencyEnum;

import javax.persistence.*;

@Entity
@Table(name = "frequency", schema = "subscription")
@NamedQuery(name = "frequency.getFrequency", query = "select f from Frequency f where f.frequency = :frequencyEnum")
public class Frequency {

    @Id
    @Enumerated(EnumType.STRING)
    private FrequencyEnum frequency;

    public Frequency() {
        frequency = null;
    }

    public Frequency(FrequencyEnum freq) {
        frequency = freq;
    }


    public FrequencyEnum getFrequency() {
        return frequency;
    }

    public void setFrequency(FrequencyEnum frequency) {
        this.frequency = frequency;
    }

    static Frequency from(String frequency) {
        return from(FrequencyEnum.valueOf(frequency));
    }

    static Frequency from(FrequencyEnum frequency) {
        return new Frequency(frequency);
    }
}
