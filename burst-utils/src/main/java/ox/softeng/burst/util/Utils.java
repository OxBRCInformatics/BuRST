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
package ox.softeng.burst.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 08/05/2017
 */
@SuppressWarnings("SameParameterValue")
public class Utils {

    public static final Logger logger = LoggerFactory.getLogger(Utils.class);

    public static Integer convertToInteger(String propertyName, String property, Integer defaultValue) {
        try {
            return Integer.parseInt(property);
        } catch (NumberFormatException ignored) {
            logger.warn("Could not convert property [{}] to integer, using default value {}", propertyName, defaultValue);
        }
        return defaultValue;
    }

    public static Long convertToLong(String propertyName, String property, Long defaultValue) {
        try {
            return Long.parseLong(property);
        } catch (NumberFormatException ignored) {
            logger.warn("Could not convert property [{}] to long, using default value {}", propertyName, defaultValue);
        }
        return defaultValue;
    }
}
