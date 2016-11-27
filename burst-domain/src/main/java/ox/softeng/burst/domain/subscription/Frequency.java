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

import ox.softeng.burst.util.FrequencyEnum;

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
