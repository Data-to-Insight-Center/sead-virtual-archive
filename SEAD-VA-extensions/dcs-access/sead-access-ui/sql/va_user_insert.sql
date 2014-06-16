insert into roles (ROLE) values('Administrator');
insert into roles (ROLE) values('Curator');
insert into roles (ROLE) values('Researcher');
insert into roles (ROLE) values('Repository_representative');

insert into users
 (FIRSTNAME, LASTNAME, EMAILADDRESS, PASSWORD, REGSTATUS, ROLEID)
 values('SEAD', 'Administrator', 'seadva@gmail.com','5f4dcc3b5aa765d61d8327deb882cf99', 'approved', 1);