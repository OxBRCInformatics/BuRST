/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2017
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
package ox.softeng.burst.service.report;

import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 18/07/2017
 */
public class HttpService implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(HttpService.class);
    private final String content;
    private final String endpoint;
    private boolean requestSent;

    HttpService(String endpoint, String content) {
        this.endpoint = endpoint;
        this.content = content;
        this.requestSent = false;
    }

    @Override
    public void run() {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(this.endpoint);
            StringEntity body = new StringEntity(this.content);
            httpPost.setEntity(body);
            ResponseHandler<Boolean> responseHandler = response -> {
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode >= 200 && statusCode < 300) {
                    this.requestSent = true;
                    return true;
                } else {
                    logger.error("Error HTTP code '{}' received calling endpoint: '{}'", statusLine, this.endpoint);
                    this.requestSent = false;
                    return false;
                }
            };
            httpClient.execute(httpPost, responseHandler);
        } catch (Exception e) {
            logger.error("Error whilst attempting to send http request: " + e.getMessage());
            this.requestSent = false;
        }

    }

    boolean isRequestSent() {
        return requestSent;
    }
}