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
package org.dataconservancy.ui.model;

/**
 * {@code SMTPServerSettings} encapsulatts information about the smtp server used by the system to send email notification.
 */
public class SMTPServerSettings {

	private boolean sslEnabled;
    private boolean emailServiceEnabled;
	private boolean authenticationEnabled;
	private String smtpServer;
	private String portNumber;
	private String username;
	private String password;
	
	public String getSmtpServer() {
		return smtpServer;
	}
	public void setSmtpServer(String sMTPServer) {
		smtpServer = sMTPServer;
	}
	public String getPortNumber() {
		return portNumber;
	}
	public void setPortNumber(String portNumber) {
		this.portNumber = portNumber;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	} 
	public boolean isSslEnabled() {
		return sslEnabled;
	}
	public void setSslEnabled(boolean sslEnabled) {
		this.sslEnabled = sslEnabled;
	}
	public boolean isAuthenticationEnabled() {
		return authenticationEnabled;
	}
	public void setAuthenticationEnabled(boolean authenticationEnabled) {
		this.authenticationEnabled = authenticationEnabled;
	}
    public boolean isEmailServiceEnabled() {
        return emailServiceEnabled;
    }
    public void setEmailServiceEnabled(boolean emailServiceEnabled) {
        this.emailServiceEnabled = emailServiceEnabled;
    }
}
