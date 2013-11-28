package org.dataconservancy.ui.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(locations = {"classpath*:/test-applicationContext.xml"})
public class BusinessObjectMapTest extends BaseUnitTest {

    @Autowired
    private BusinessObjectMap mainBOM1;
    
    @Autowired
    private BusinessObjectMap childBOM1;
    
    @Autowired
    private BusinessObjectMap childBOM2;

    @Before
    public void setUp() {
        mainBOM1.setName("Main Map");
        mainBOM1.setType("Test Type");
        mainBOM1.setDepositStatus("success");
        mainBOM1.getChildren().clear();
        mainBOM1.getAlternateIds().clear();
        mainBOM1.getChildren().add(childBOM1);
        mainBOM1.getAlternateIds().add("id:alternateMainBOM");
        
        childBOM1.setName("Child Map 1");
        childBOM1.setType("Another Type");
        childBOM1.setDepositStatus("success");
        childBOM1.getChildren().clear();
        childBOM1.getAlternateIds().clear();
        childBOM1.getChildren().add(childBOM2);
        
        childBOM2.setName("Child Map 2");
        childBOM2.setType("Another Type");
        childBOM2.setDepositStatus("success");
        childBOM2.getChildren().clear();
        childBOM2.getAlternateIds().clear();
        childBOM2.getAlternateIds().add("id:subBOM2");
    }
    
    
    /**
     * Tests non-null requirement
     */
    @Test
    public void testNonNull() {
        assertFalse(mainBOM1.equals(null));
    }
    
    
    /**
     * Tests reflexive requirement
     */
    @Test
    public void testReflexive() {
        assertTrue(mainBOM1.equals(mainBOM1));
        assertFalse(mainBOM1.equals(childBOM1));
    }
    
    
    /**
     * Test symmetrical requirement
     */
    @Test
    public void testSymmetric() {
        BusinessObjectMap mainBOM2 = duplicateMap(mainBOM1);
        
        assertTrue(mainBOM1.equals(mainBOM2));
        assertTrue(mainBOM2.equals(mainBOM1));
    }
    
    
    /**
     * Test transitive requirement
     */
    @Test
    public void testTransitive() {
        BusinessObjectMap mainBOM2 = duplicateMap(mainBOM1);
        BusinessObjectMap mainBOM3 = duplicateMap(mainBOM1);
        
        assertTrue(mainBOM1.equals(mainBOM2));
        assertTrue(mainBOM2.equals(mainBOM3));
        assertTrue(mainBOM3.equals(mainBOM1));
    }
    
    
    /**
     * Equality requires children to be the same
     */
    @Test
    public void testChildrenEquality() {
        BusinessObjectMap mainBOM2 = duplicateMap(mainBOM1);
        mainBOM2.getChildren().clear();
        assertFalse(mainBOM1.equals(mainBOM2));
        
        
        mainBOM2.getChildren().addAll(mainBOM1.getChildren());
        assertTrue(mainBOM1.equals(mainBOM2));        
    }
    
    /**
     * Equality requires children to be the same in any order.
     */
    @Test
    public void testChildrenEqualityChildrenInAnyOrder() {
        BusinessObjectMap map = new BusinessObjectMap("map");

        map.getChildren().add(new BusinessObjectMap("child1"));
        map.getChildren().add(new BusinessObjectMap("child2"));

        BusinessObjectMap expected = duplicateMap(map);
        
        // Reverse children
        expected.getChildren().add(expected.getChildren().remove(0));
        
        // Make sure that the orders are indeed different, then check maps
        assertFalse(expected.getChildren().get(0).equals(map.getChildren().get(0)));
        assertEquals(expected, map);
    }
    
    /**
     * Hashcode must be the same regardless of child order.
     */
    @Test
    public void testHashCodeChildrenInAnyOrder() {
        BusinessObjectMap map = new BusinessObjectMap("map");

        map.getChildren().add(new BusinessObjectMap("child1"));
        map.getChildren().add(new BusinessObjectMap("child2"));

        BusinessObjectMap expected = duplicateMap(map);
        
        // Reverse children
        expected.getChildren().add(expected.getChildren().remove(0));
        
        assertEquals(expected.hashCode(), map.hashCode());
    }
    
    @Test
    public void testAlternateIdEquality() {
        BusinessObjectMap mainBOM2 = duplicateMap(mainBOM1);
        mainBOM2.getAlternateIds().clear();
        assertFalse(mainBOM1.equals(mainBOM2));
        
        mainBOM2.getAlternateIds().addAll(mainBOM1.getAlternateIds());
        assertTrue(mainBOM1.equals(mainBOM2));
    }
    
    
    /*
     * HELPER METHODS BELOW
     */
    
    /**
     * Duplicates a map object
     * @param original the map to copy
     * @return a new object that is a copy of the original
     */
    private BusinessObjectMap duplicateMap(BusinessObjectMap original) {
        BusinessObjectMap copy = new BusinessObjectMap("id:copy");
        
        copy.setId(original.getId());
        copy.setName(original.getName());
        copy.setType(original.getType());;
        copy.setDepositStatus(original.getDepositStatus());
        copy.getChildren().addAll(original.getChildren());
        copy.getAlternateIds().addAll(original.getAlternateIds());
        
        return copy;
    }
}
