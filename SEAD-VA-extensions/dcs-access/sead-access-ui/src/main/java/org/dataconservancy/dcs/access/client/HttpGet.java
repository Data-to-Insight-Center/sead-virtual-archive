/*
 * Copyright 2012 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.dcs.access.client;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

public class HttpGet {
	public interface Callback<T> {
		void success(T result);

		void failure(String error);
	}

	/**
	 * Do multiple http requests and only return success when all complete. May
	 * be more than one failure reported.
	 * 
	 * cb.success returns a String[] parallel to urls with the results
	 * 
	 * @param urls
	 * @param cb
	 */
	public static void request(final String[] urls, final Callback<String[]> cb) {
		final String[] result = new String[urls.length];

		for (int i = 0; i < urls.length; i++) {
			RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, urls[i]);
			final int which = i;

			try {
				rb.sendRequest(null, new RequestCallback() {
					public void onError(Request req, Throwable e) {
						cb.failure(e.getMessage());
					}

					public void onResponseReceived(Request req, Response resp) {
						if (resp.getStatusCode() != 200) {
							cb.failure(resp.getText());
						} else {
							result[which] = resp.getText();

							for (int j = 0; j < result.length; j++) {
								if (result[j] == null) {
									return;
								}
							}

							cb.success(result);
						}
					}
				});
			} catch (RequestException e) {
				cb.failure(e.getMessage());
			}
		}
	}

	public static void request(final String url, final Callback<String> cb) {
		RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, url);

		try {
			rb.sendRequest(null,
					new com.google.gwt.http.client.RequestCallback() {
						public void onError(Request req, Throwable e) {
							cb.failure(e.getMessage());
						}

						public void onResponseReceived(Request req,
								Response resp) {
							if (resp.getStatusCode() != 200) {
								cb.failure(resp.getText());
							} else {
								cb.success(resp.getText());
							}
						}
					});
		} catch (RequestException e) {
			cb.failure(e.getMessage());
		}
	}
}
