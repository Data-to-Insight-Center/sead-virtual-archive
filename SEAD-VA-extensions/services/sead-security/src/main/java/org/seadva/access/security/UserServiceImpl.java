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

package org.seadva.access.security;

import com.google.api.services.oauth2.model.Userinfo;
import org.seadva.access.security.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@SuppressWarnings("serial")
public class UserServiceImpl
  implements UserService
{
    String dbUrl;

	PersonDAOJdbcImpl getPersonJdbc() throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		return new PersonDAOJdbcImpl(dbUrl);
  }

  public UserServiceImpl(String dbUrl){
      this.dbUrl = dbUrl;
  }

  public Authentication authenticate(String user, String pass) throws InstantiationException, IllegalAccessException, ClassNotFoundException
  {
	Person person = getPersonJdbc().selectPerson(user);
	Authentication authentication;
	
    if (person != null)
    {
      if (person.getRegistrationStatus() == RegistrationStatus.APPROVED)
      {
        if (person.getPassword().equals(pass))
        {
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


@Override
public Authentication authenticateOAuth(String token, OAuthType type, String[] admins) {
	Authentication authentication = null;

	if(type == OAuthType.GOOGLE){
		try {
			Userinfo userinfo = new GoogleAuthHelper().getUserInfo(token);

            //After getting the token check if user is in database
			Person person = getPersonJdbc().selectPerson(userinfo.getEmail());
			
			
		    if (person != null)
		    {
		      if (person.getRegistrationStatus() == RegistrationStatus.APPROVED)
		      {
		    	authentication = new Authentication(true);
		      }
		      else
		      {//If not, tell them they were already registered and need to wait for admin approval
		    	authentication = new Authentication(false);
		        authentication.setErrorMessage("You email address was already registered. But you are awaiting approval from an admin.");
		      }
		    }
		    else{
                authentication = new Authentication(false);
                authentication.setErrorMessage("You email address was already registered. But you are awaiting approval from an admin.");
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