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
package org.dataconservancy.dcs.lineage.http.support;

import org.dataconservancy.dcs.lineage.api.Lineage;
import org.dataconservancy.dcs.lineage.http.LineageModelAttribute;
import org.dataconservancy.model.dcs.DcsEntity;
import org.springframework.ui.Model;

import java.util.Date;
import java.util.List;

import static org.dataconservancy.dcs.lineage.http.LineageModelAttribute.ACCEPT;
import static org.dataconservancy.dcs.lineage.http.LineageModelAttribute.ENTITIES;
import static org.dataconservancy.dcs.lineage.http.LineageModelAttribute.ETAG;
import static org.dataconservancy.dcs.lineage.http.LineageModelAttribute.IFMODIFIEDSINCE;
import static org.dataconservancy.dcs.lineage.http.LineageModelAttribute.LASTMODIFIED;
import static org.dataconservancy.dcs.lineage.http.LineageModelAttribute.LINEAGE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Utility class supporting assertions of the Model.
 */
public class ModelAssert {

    /**
     * Asserts that the supplied Model object is not null.
     *
     * @param m the model
     */
    public static void assertNotNullModel(Model m) {
        assertNotNull("Expected the Model to be not-null!", m);
    }

    /**
     * Asserts that a HTTP request resulted in no Lineage or entities being found.  This is expressed in the Model
     * by a {@code null Lineage} and an empty {@code List} of DCS entities.
     *
     * @param m the model
     */
    public static void assertEmptyResponse(Model m) {
        assertNullLineage(m);
        assertEntitiesEmpty(m);
    }

    /**
     * Asserts that a HTTP request resulted in a Lineage and entities being found.  This is expressed in the Model
     * by a not-{@code null Lineage} and an non-empty {@code List} of DCS entities.
     *
     * @param m the model
     */
    public static void assertNotEmptyResponse(Model m) {
        assertNotNullLineage(m);
        assertEntitiesNotEmpty(m);
    }

    /**
     * Obtains the LINEAGE from the Model, and asserts that it is {@code null}.
     *
     * @param m the Model, must not be {@code null}
     */
    public static void assertNullLineage(Model m) {
        assertModelAttributeValueNull(m, LINEAGE);
    }

    /**
     * Obtains the LINEAGE from the Model, and asserts that it is not {@code null}.
     *
     * @param m the model
     */
    public static void assertNotNullLineage(Model m) {
        assertModelAttributeValueNotNull(m, LINEAGE);
    }

    /**
     * Asserts that the Model <em>does not</em> contain the LINEAGE attribute.
     *
     * @param m the Model, must not be {@code null}
     */
    public static void assertNoLineage(Model m) {
        assertModelDoesNotContainAttribute(m, LINEAGE);
    }

    /**
     * Obtains the LINEAGE from the Model and asserts that it is equal to the expected Lineage.  The expected Lineage
     * <em>must not</em> be {@code null}.
     *
     * @param m the Model, must not be {@code null}
     * @param expected the expected Lineage, must not be {@code null}
     */
    public static void assertLineageEquals(Model m, Lineage expected) {
        assertModelLineageValueEquals(m, LINEAGE, expected);
    }

    /**
     * Obtains the ENTITIES {@code List} from the Model, and asserts that it is not null and empty.
     *
     * @param m the Model, must not be {@code null}
     */
    public static void assertEntitiesEmpty(Model m) {
        assertModelEmptyListValue(m, ENTITIES);
    }

    /**
     * Obtains the ENTITIES {@code List} from the Model, and asserts that it is not null and not empty.
     *
     * @param m the model
     */
    public static void assertEntitiesNotEmpty(Model m) {
        assertModelNotEmptyListValue(m, ENTITIES);
    }

    /**
     * Obtains the ENTITIES {@code List} from the Model, and asserts it is equal to the supplied {@code List}.  Empty
     * Lists are allowed.
     *
     * @param m the model
     * @param expected the entities, may be empty but not null
     */
    public static void assertEntitiesEquals(Model m, List<DcsEntity> expected) {
        assertModelListValueEquals(m, ENTITIES, expected);
    }

    /**
     * Obtains the ETAG from the Model and asserts that it is null.
     *
     * @param m the model
     */
    public static void assertEtagNull(Model m) {
        assertModelAttributeValueNull(m, ETAG);
    }

    /**
     * Obtains the ETAG from the Model and asserts that it is not null.
     *
     * @param m the model
     */
    public static void assertEtagNotNull(Model m) {
        assertModelAttributeValueNotNull(m, ETAG);
    }

    /**
     * Asserts that the ETAG model attribute value is equal to the expected value.  The expected value <em>must not</em>
     * be {@code null}.
     *
     * @param m the Model, must not be {@code null}
     * @param expectedEtag the expected value of the ETAG model attribute, must not be {@code null}
     */
    public static void assertEtagEquals(Model m, String expectedEtag) {
        assertModelStringValueEquals(m, ETAG, expectedEtag);
    }

    /**
     * Asserts that the Model does not contain the ETAG attribute.
     *
     * @param m the Model, must not be {@code null}
     */
    public static void assertNoEtag(Model m) {
        assertModelDoesNotContainAttribute(m, ETAG);
    }

    /**
     * Asserts that the Model contains the LASTMODIFIED model attribute, and that its value is {@code null}.
     *
     * @param m the Model, must not be {@code null}
     */
    public static void assertLastModifiedNull(Model m) {
        assertModelAttributeValueNull(m, LASTMODIFIED);
    }

    /**
     * Asserts that the LASTMODIFIED model attribute value is equal to the expected {@code Date}.  The expected
     * value <em>must not</em> be {@code null}.
     *
     * @param m the Model, must not be {@code null}
     * @param expected the expected value of the Model LASTMODIFIED attribute, must not be {@code null}
     */
    public static void assertLastModifiedEquals(Model m, Date expected) {
        assertModelDateValueEquals(m, LASTMODIFIED, expected);
    }

    /**
     * Asserts that the IFMODIFIEDSINCE model attribute value is equal to the expected {@code Date}.  The expected
     * value <em>must not</em> be {@code null}.
     *
     * @param m the Model, must not be {@code null}
     * @param expected the expected value of the Model IFMODIFIEDSINCE attribute, must not be {@code null}
     */
    public static void assertIfModifiedSinceEquals(Model m, Date expected) {
        assertModelDateValueEquals(m, IFMODIFIEDSINCE, expected);
    }

    /**
     * Asserts that the ACCEPT model attribute value is equal to the expected value.  The expected value <em>must
     * not</em> be {@code null}
     *
     * @param m the Model, must not be {@code null}
     * @param expected the expected value of the Model ACCEPT attribute, must not be {@code null}
     */
    public static void assertAcceptEquals(Model m, String expected) {
        assertModelStringValueEquals(m, ACCEPT, expected);
    }

    /**
     * Asserts that the Model contains the {@code attribute}, and that the value is {@code null}.
     *
     * @param m         the Model, must not be {@code null}
     * @param attribute the Model attribute, must not be {@code null}
     */
    public static void assertModelAttributeValueNull(Model m, LineageModelAttribute attribute) {
        assertModelContainsAttribute(m, attribute);
        assertNull("Expected a null value for attribute " + attribute.name() +
                " (value was '" + m.asMap().get(attribute.name()) + ")", m.asMap().get(attribute.name()));
    }

    /**
     * Asserts that the Model contains the {@code attribute}, and that the value is not null.
     *
     * @param m the Model, must not be {@code null}
     * @param attribute the Model attribute, must not be {@code null}
     */
    public static void assertModelAttributeValueNotNull(Model m, LineageModelAttribute attribute) {
        assertModelContainsAttribute(m, attribute);
        assertNotNull("Expected a not-null value for attribute " + attribute.name(), m.asMap().get(attribute.name()));
    }

    /**
     * Asserts that the Model contains the {@code attribute} and has the expected value ({@code null} values are
     * <em>not</em> allowed).
     *
     * @param m the Model, must not be {@code null}
     * @param attribute the Model attribute, must not be {@code null}
     * @param expectedValue the expected value of the attribute, must not be {@code null}
     */
    private static void assertModelStringValueEquals(Model m, LineageModelAttribute attribute, String expectedValue) {
        assertModelContainsAttribute(m, attribute);
        assertModelAttributeValueNotNull(m, attribute);
        assertEquals("Value for model attribute " + attribute.name() + " does not equal expected value.",
                expectedValue, (String)m.asMap().get(attribute.name()));
    }

    /**
     * Asserts that the Model contains the {@code attribute} and has the expected value ({@code null} values are
     * not allowed).
     *
     * @param m the Model, must not be {@code null}
     * @param attribute the Model attribute, must not be {@code null}
     * @param expectedValue the expected value of the attribute, must not be {@code null}
     */
    private static void assertModelDateValueEquals(Model m, LineageModelAttribute attribute, Date expectedValue) {
        assertModelContainsAttribute(m, attribute);
        assertModelAttributeValueNotNull(m, attribute);
        assertEquals("Value for model attribute " + attribute.name() + " does not equal expected value.",
                expectedValue, m.asMap().get(attribute.name()));
    }

    /**
     * Asserts that the Model contains the {@code attribute} and has the expected value ({@code null} values are
     * allowed).
     *
     * @param m the Model, must not be {@code null}
     * @param attribute the Model attribute, must not be {@code null}
     * @param expectedValue the expected value of the attribute, may be {@code null}
     */
    private static void assertModelListValueEquals(Model m, LineageModelAttribute attribute, List expectedValue) {
        assertModelContainsAttribute(m, attribute);
        assertEquals("Value for model attribute " + attribute.name() + " does not equal expected value.",
                expectedValue, m.asMap().get(attribute.name()));
    }

    /**
     * Asserts that the Model contains the {@code attribute} and has the expected value ({@code null} values are
     * <em>not</em> allowed).
     *
     * @param m the Model, must not be {@code null}
     * @param attribute the Model attribute, must not be {@code null}
     * @param expectedValue the expected value of the attribute, must not be {@code null}
     */
    private static void assertModelLineageValueEquals(Model m, LineageModelAttribute attribute, Lineage expectedValue) {
        assertModelContainsAttribute(m, attribute);
        assertModelAttributeValueNotNull(m, attribute);
        assertEquals("Value for model attribute " + attribute.name() + " does not equal expected value.",
                expectedValue, m.asMap().get(attribute.name()));
    }

    /**
     * Asserts that the Model contains the {@code attribute} and that the value is empty.
     *
     * @param m the Model, must not be {@code null}
     * @param attribute the Model attribute, must not be {@code null}
     */
    private static void assertModelEmptyListValue(Model m, LineageModelAttribute attribute) {
        assertModelContainsAttribute(m, attribute);
        assertTrue("Value for model attribute " + attribute.name() + " should be empty.",
                ((List) m.asMap().get(attribute.name())).isEmpty());
    }

    /**
     * Asserts that the Model contains the {@code attribute} and that the value is not empty.
     *
     * @param m the Model, must not be {@code null}
     * @param attribute the Model attribute, must not be {@code null}
     */
    private static void assertModelNotEmptyListValue(Model m, LineageModelAttribute attribute) {
        assertModelContainsAttribute(m, attribute);
        assertTrue("Value for model attribute " + attribute.name() + " should not be empty.",
                ((List) m.asMap().get(attribute.name())).size() > 0);
    }

    /**
     * Asserts that the Model is not {@code null}, and contains the {@code attribute}.  The value is not checked.
     *
     * @param m the Model, must not be {@code null}
     * @param attribute the attribute, must not be {@code null}
     */
    public static void assertModelContainsAttribute(Model m, LineageModelAttribute attribute) {
        assertNotNullModel(m);
        assertNotNull("Model attribute must not be null!", attribute);
        assertTrue("Expected Model to contain attribute " + attribute.name(), m.containsAttribute(attribute.name()));
    }

    /**
     * Asserts that the Model is not {@code null}, and <em>does not</em> contain the {@code attribute}.  The value is
     * not checked.
     *
     * @param m the Model, must not be {@code null}
     * @param attribute the attribute, must not be {@code null}
     */
    public static void assertModelDoesNotContainAttribute(Model m, LineageModelAttribute attribute) {
        assertNotNullModel(m);
        assertNotNull("Model attribute must not be null!", attribute);
        assertFalse("Expected Model to not contain attribute " + attribute.name(),
                m.containsAttribute(attribute.name()));
    }
}
