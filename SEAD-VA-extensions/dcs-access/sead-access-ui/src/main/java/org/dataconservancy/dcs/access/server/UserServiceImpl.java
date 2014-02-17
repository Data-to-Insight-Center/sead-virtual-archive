/*
 * Copyright 2013 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dataconservancy.dcs.access.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.dataconservancy.dcs.access.client.api.UserService;
import org.dataconservancy.dcs.access.server.model.Email;
import org.dataconservancy.dcs.access.server.model.PersonDAOJdbcImpl;
import org.dataconservancy.dcs.access.server.util.ServerConstants;
import org.dataconservancy.dcs.access.shared.Authentication;
import org.dataconservancy.dcs.access.shared.OAuthType;
import org.dataconservancy.dcs.access.shared.Person;
import org.dataconservancy.dcs.access.shared.RegistrationStatus;
import org.dataconservancy.dcs.access.shared.Role;
import org.dataconservancy.dcs.access.shared.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.oauth2.model.Userinfo;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class UserServiceImpl extends RemoteServiceServlet
  implements UserService
{
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	final static Email emailSender = new Email("iu", "seadva", ServerConstants.emailPassword);
	PersonDAOJdbcImpl getPersonJdbc() throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		String path = getServletContext().getRealPath("/sead_access/");
		return new PersonDAOJdbcImpl(path+"/Config.properties");
  }

  public UserServiceImpl(){}

  public void sendEmail(String[] toAddress, String subject, String messageStr){ 
	  emailSender.sendEmail(toAddress, subject, messageStr);
  }
  public Authentication authenticate(String url, String user, String pass) throws InstantiationException, IllegalAccessException, ClassNotFoundException
  {
	Person person = getPersonJdbc().selectPerson(user);
	Authentication authentication;
	
    if (person != null)
    {
      if (person.getRegistrationStatus() == RegistrationStatus.APPROVED)
      {
        if (person.getPassword().equals(hashPassword(pass)))
        {
          getSession().setAttribute("email", user);
          getSession().setAttribute("role", person.getRole());//Role.ROLE_ADMIN);
          getSession().setAttribute("fName", person.getFirstName());
          getSession().setAttribute("lName", person.getLastName());
          getSession().setAttribute("sessionType", "database");
          getSession().setAttribute("password", person.getPassword());
          authentication = new Authentication(true);
          return authentication;
       }
        else
        {
          authentication = new Authentication(false);
          String appendMsg = "";
          for (OAuthType b : OAuthType.values()) {
        	  if(user.endsWith(b.getSuffix())) {
        		  appendMsg = "It is possible that you previously signed in using "+b.getName()+".";
        		  break;
              }
          }
          authentication.setErrorMessage("Wrong credentials. Please check username/password."+appendMsg);
        }
      }
      else
      {
        authentication = new Authentication(false);
        authentication.setErrorMessage("User not approved.");
      }
      return authentication;
    }
    else{
    	  authentication = new Authentication(false);
          String appendMsg = "";
          for (OAuthType b : OAuthType.values()) {
        	  if(user.endsWith(b.getSuffix())) {
        		  appendMsg = "It is possible that you previously signed in using "+b.getName()+".";
        		  break;
              }
          }
          authentication.setErrorMessage("Wrong credentials. Please check username/password."+appendMsg);
          return authentication;
    }
  }

  public List<Person> getAllUsers() throws InstantiationException, IllegalAccessException, ClassNotFoundException
  {
    return getPersonJdbc().getAllUsers();
  }

  public void updateAllUsers(List<Person> userList, List<Person> sendEmailList) throws InstantiationException, IllegalAccessException, ClassNotFoundException
  {
    for (Person user : userList) {
    	getPersonJdbc().updatePerson(user);
    }

    for (Person user : sendEmailList) {
    	String[] userEmail = new String[1];
    	userEmail[0] = user.getEmailAddress();
    	emailSender.sendEmail(userEmail, "Account approved", "Hi\n " + user.getFirstName() + " " + user.getLastName() + 
        "Your account has been approved in Sead VA.\nThanks\n Sead VA Team");
    }
  }

  public String register(String firstName, String lastName, String email, String password, String confirmPwd, String[] admins) throws InstantiationException, IllegalAccessException, ClassNotFoundException
  {
	Person personCheck = getPersonJdbc().selectPerson(email);
	
    if (personCheck != null)
    {
    	if (personCheck.getRegistrationStatus() == RegistrationStatus.APPROVED)
    		return "You already have an account on this website.";
    	else if(personCheck.getRegistrationStatus() == RegistrationStatus.PENDING)
    		return "You have already requested an account on this website. You will receive an email once your account is approved.";
    }
      	
    
    if (!password.equals(confirmPwd))
      return "Passwords entered do not match.";
    if(password.length()<6)
    	return "Please ensure password contains atleast 6 characters.";
    Person person = new Person();
    person.setFirstName(firstName);
    person.setLastName(lastName);
    person.setEmailAddress(email);
    person.setPassword(hashPassword(password));
    person.setRegistrationStatus(RegistrationStatus.PENDING);
    //person.setRole(Role.ROLE_USER);
    getPersonJdbc().insertPerson(person);

    emailSender.sendEmail(admins, "New user", "New user " + firstName + " " + lastName + "(" + email + ") has requested an account in SEAD VA." + 
      "Please approve if you know the person.");
    String[] user = new String[1];
    user[0] = email;
    emailSender.sendEmail(user, "Request for account on Sead VA", "Hi " + firstName + " " + lastName + ",\nThank you for requesting an account. You will receive a notification by email once your account has been approved.\nThanks\nSead VA Team");
    return "success";
  }

  public HttpSession getSession()
  {
    return getThreadLocalRequest().getSession();
  }

  public UserSession checkSession(String token)
  {
	if(token!=null)
	  setSession(token);
    UserSession userSession = new UserSession();
    if (getSession().getAttribute("email") != null)
    {
      userSession.setEmail((String)getSession().getAttribute("email"));
      userSession.setRole((Role)getSession().getAttribute("role"));
      userSession.setfName((String)getSession().getAttribute("fName"));
      userSession.setlName((String)getSession().getAttribute("lName"));
      userSession.setSessionType((String)getSession().getAttribute("sessionType"));
      userSession.setSession(true);
      return userSession;
    }

    userSession.setSession(false);
    return userSession;
  }
  
  public void setSession(String authString)
  {
	  getSession().setAttribute("email", "googleuser");
      getSession().setAttribute("role", Role.ROLE_ADMIN);
  }

  public String hashPassword(String password)
  {
    String hashword = null;
    try {
      MessageDigest md5 = MessageDigest.getInstance("MD5");
      md5.update(password.getBytes());
      BigInteger hash = new BigInteger(1, md5.digest());
      hashword = hash.toString(16);
    }
    catch (NoSuchAlgorithmException localNoSuchAlgorithmException)
    {
    }
    return pad(hashword, 32, '0');
  }

  private String pad(String s, int length, char pad) {
    StringBuffer buffer = new StringBuffer(s);
    while (buffer.length() < length) { 
      buffer.insert(0, pad);
    }
    return buffer.toString();
  }

  public void clearSession()
  {
    getSession().invalidate();
  }

  public String getInfo(String apiUrl) {
     URL url;
     HttpURLConnection conn;
     BufferedReader rd;
     String line;
     String result = "";
     try {
        url = new URL(apiUrl);
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        while ((line = rd.readLine()) != null) {
           result += line;
        }
        rd.close();
     } catch (Exception e) {
        e.printStackTrace();
     }
     return result;
  }

@Override
public Authentication authenticateOAuth(String token, OAuthType type, String[] admins) {
	Authentication authentication = null;
	
	if(type == OAuthType.GOOGLE){
		try {
			Userinfo userinfo = new GoogleAuthHelper().getUserInfo(token);
			
			String firstName = userinfo.getGivenName();
			String lastName = userinfo.getFamilyName();
			String email = userinfo.getEmail();
			
			//After getting the token check if user is in database
			Person person = getPersonJdbc().selectPerson(userinfo.getEmail());
			
			
		    if (person != null)
		    {
		      if (person.getRegistrationStatus() == RegistrationStatus.APPROVED)
		      {
		    	//If yes, google has authenticated, create a session       
		          getSession().setAttribute("email", email);
		          getSession().setAttribute("role", person.getRole());
		          getSession().setAttribute("fName", firstName);
		          getSession().setAttribute("lName", lastName);
		          getSession().setAttribute("sessionType", "oauth");
		          authentication = new Authentication(true);
		      }
		      else
		      {//If not, tell them they were already registered and need to wait for admin approval
		    	authentication = new Authentication(false);
		        authentication.setErrorMessage("You email address was already registered. But you are awaiting approval from an admin.");
		      }
		    }
		    else{
		    	register(firstName, lastName, email, "what are the odds this would be a password", "what are the odds this would be a password",admins);  
		    	authentication = new Authentication(false);
		        authentication.setErrorMessage("You have requested an account using your "+OAuthType.GOOGLE.getName()+" id. Your request is yet to be approved by an admin.");
		    }
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	return authentication;
}

}