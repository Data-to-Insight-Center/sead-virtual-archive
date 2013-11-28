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
package org.dataconservancy.ui.services;

import org.dataconservancy.ui.model.Role;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.PersonWrapper;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Adapts the {@link UserService} interface to the Spring Security {@link UserDetails} interface.
 */
public class UserDetailsServiceAdapter implements UserDetailsService {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private UserService userService;

    public UserDetailsServiceAdapter(UserService userService) {
        this.userService = userService;
        log.trace("Instantiating {} with {}", this, userService);
    }

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException, DataAccessException {
        Person p = userService.get(id);
        if (p == null) {
            throw new UsernameNotFoundException("User ID " + id + " not found.");
        }
        return new UserDetailsWrapper(p);
    }

    /**
     * Wraps a {@link Person} object as a {@link UserDetails} object.
     */
    public class UserDetailsWrapper extends PersonWrapper implements UserDetails {

        UserDetailsWrapper(Person person) {
            super(person);
            log.trace("Wrapping person {}", person);
        }

        @Override
        public Collection<GrantedAuthority> getAuthorities() {
            List<GrantedAuthority> auths = new ArrayList<GrantedAuthority>(getRoles().size());
            for (final Role r : getRoles()) {
                auths.add(new GrantedAuthority() {
                    @Override
                    public String getAuthority() {
                        return r.name();
                    }
                });
            }

            return auths;
        }

        @Override
        public String getUsername() {
            return getEmailAddress();
        }

        @Override
        public boolean isAccountNonExpired() {
            return isEnabled();
        }

        @Override
        public boolean isAccountNonLocked() {
            return isEnabled();
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return isEnabled();
        }

        @Override
        public boolean isEnabled() {
            return getRoles().contains(Role.ROLE_USER);
        }
    }
}
