package org.dataconservancy.ui.dao;


import org.dataconservancy.storage.dropbox.model.DropboxToken;
import org.dataconservancy.ui.model.Person;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class DropboxDAOJdbcImplTest extends BaseDaoTest {
    
    @Autowired
    private DropboxDAOJdbcImpl dropboxDao;
    
    @Autowired
    @Qualifier("unapprovedRegisteredUser")
    protected Person pendingUser;

    private DropboxToken dropboxToken;

    private String appKey;
    
    private String appSecret;
    
    @Before
    public void setUp() {
        appKey = "23jhaskdjh23";
        appSecret = "223jakaslkjhas923l";
        dropboxToken = new DropboxToken(appKey, appSecret);
    }

    /**
     * Tests the insertion of a unique pair of App Key/Secret into the database for a given userId.
     */
    @Test
    public void testInsertAppKeyAppSecret() {
        Assert.assertTrue("Could not insert the row.",
 dropboxDao.insertToken(dropboxToken, pendingUser.getId()));
    }
    
    /**
     * Tests the retrieval of a pair of App key/secret for a specific userId.
     */
    @Test
    public void testRetrieveAppKeyAppSecret() {
        DropboxToken token = dropboxDao.getToken(pendingUser.getId());
        Assert.assertNotNull("Could not retrieve the tokens.", token);
        Assert.assertEquals(appKey, token.getAppKey());
        Assert.assertEquals(appSecret, token.getAppSecret());
    }
    
}
