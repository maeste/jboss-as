/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.connector.subsystems.common.pool;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.PropertiesAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.client.helpers.MeasurementUnit;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.operations.validation.EnumValidator;
import org.jboss.as.controller.operations.validation.IntRangeValidator;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.jca.common.api.metadata.Defaults;
import org.jboss.jca.common.api.metadata.common.FlushStrategy;

/**
 * @author @author <a href="mailto:stefano.maestri@redhat.com">Stefano
 *         Maestri</a>
 */
public class Constants {

    private static final String MIN_POOL_SIZE_NAME = "min-pool-size";

    private static final String INITIAL_POOL_SIZE_NAME = "initial-pool-size";

    private static final String MAX_POOL_SIZE_NAME = "max-pool-size";

    private static final String POOL_PREFILL_NAME = "pool-prefill";

    private static final String POOL_USE_STRICT_MIN_NAME = "pool-use-strict-min";

    private static final String BACKGROUNDVALIDATIONMILLIS_NAME = "background-validation-millis";

    private static final String BACKGROUNDVALIDATION_NAME = "background-validation";

    private static final String USE_FAST_FAIL_NAME = "use-fast-fail";

    private static final String VALIDATEONMATCH_NAME = "validate-on-match";

    private static final String BLOCKING_TIMEOUT_WAIT_MILLIS_NAME = "blocking-timeout-wait-millis";

    private static final String IDLETIMEOUTMINUTES_NAME = "idle-timeout-minutes";

    private static final String FLUSH_STRATEGY_NAME = "flush-strategy";

    private static final String CAPACITY_INCREMENTER_CLASS_NAME = "capacity-incrementer-class";

    private static final String CAPACITY_INCREMENTER_PROPERTIES_NAME = "capacity-incrementer-properties";

    private static final String CAPACITY_DECREMENTER_CLASS_NAME = "capacity-decrementer-class";

    private static final String CAPACITY_DECREMENTER_PROPERTIES_NAME = "capacity-decrementer-properties";


    public static final SimpleAttributeDefinition BLOCKING_TIMEOUT_WAIT_MILLIS = new SimpleAttributeDefinitionBuilder(BLOCKING_TIMEOUT_WAIT_MILLIS_NAME, ModelType.LONG, true)
            .setAttributeGroup("timeout")
            .setXmlName("blocking-timeout-millis")
            .setMeasurementUnit(MeasurementUnit.MILLISECONDS)
            .setAllowExpression(true)
            .build();


    public static final SimpleAttributeDefinition IDLETIMEOUTMINUTES = new SimpleAttributeDefinitionBuilder(IDLETIMEOUTMINUTES_NAME, ModelType.LONG, true)
            .setAttributeGroup("timeout")
            .setMeasurementUnit(MeasurementUnit.MINUTES)
            .setAllowExpression(true)
            .build();

    public static final SimpleAttributeDefinition BACKGROUNDVALIDATIONMILLIS = new SimpleAttributeDefinitionBuilder(BACKGROUNDVALIDATIONMILLIS_NAME, ModelType.LONG, true)
            .setAttributeGroup("validation")
            .setMeasurementUnit(MeasurementUnit.MILLISECONDS)
            .setValidator(new IntRangeValidator(1, true, true))
            .setAllowExpression(true)
            .build();

    public static final SimpleAttributeDefinition BACKGROUNDVALIDATION = new SimpleAttributeDefinitionBuilder(BACKGROUNDVALIDATION_NAME, ModelType.BOOLEAN, true)
            .setAttributeGroup("validation")
            .setAllowExpression(true)
            //.setDefaultValue(new ModelNode(Defaults.BACKGROUND_VALIDATION))
            .build();

    public static final SimpleAttributeDefinition USE_FAST_FAIL = new SimpleAttributeDefinitionBuilder(USE_FAST_FAIL_NAME, ModelType.BOOLEAN, true)
            .setAttributeGroup("validation")
            .setDefaultValue(new ModelNode(Defaults.USE_FAST_FAIL))
            .setAllowExpression(true)
            .build();

    public static SimpleAttributeDefinition VALIDATE_ON_MATCH = new SimpleAttributeDefinitionBuilder(VALIDATEONMATCH_NAME, ModelType.BOOLEAN, true)
            .setAttributeGroup("validation")
            .setAllowExpression(true)
            .build();

    public static final SimpleAttributeDefinition MAX_POOL_SIZE = new SimpleAttributeDefinitionBuilder(MAX_POOL_SIZE_NAME, ModelType.INT, true)
            .setAttributeGroup("pool")
            .setAllowExpression(true)
            .setDefaultValue(new ModelNode(Defaults.MAX_POOL_SIZE))
            .build();

    public static final SimpleAttributeDefinition MIN_POOL_SIZE = new SimpleAttributeDefinitionBuilder(MIN_POOL_SIZE_NAME, ModelType.INT, true)
            .setAttributeGroup("pool")
            .setAllowExpression(true)
            .setDefaultValue(new ModelNode(Defaults.MIN_POOL_SIZE))
            .build();

    public static final SimpleAttributeDefinition INITIAL_POOL_SIZE = new SimpleAttributeDefinitionBuilder(INITIAL_POOL_SIZE_NAME, ModelType.INT)
            .setAttributeGroup("pool")
            .setAllowExpression(true)
            .setAllowNull(true)
            .build();

    public static SimpleAttributeDefinition CAPACITY_INCREMENTER_CLASS = new SimpleAttributeDefinitionBuilder(CAPACITY_INCREMENTER_CLASS_NAME, ModelType.STRING, true)
            .setAttributeGroup("pool")
            .setAllowExpression(true)
            .build();

    public static PropertiesAttributeDefinition CAPACITY_INCREMENTER_PROPERTIES = new PropertiesAttributeDefinition.Builder(CAPACITY_INCREMENTER_PROPERTIES_NAME, true)
            .setAttributeGroup("pool")
            .setAllowNull(true)
            .setAllowExpression(true)
            .build();

    public static SimpleAttributeDefinition CAPACITY_DECREMENTER_CLASS = new SimpleAttributeDefinitionBuilder(CAPACITY_DECREMENTER_CLASS_NAME, ModelType.STRING, true)
            .setAttributeGroup("pool")
            .setAllowExpression(true)
            .build();

    public static PropertiesAttributeDefinition CAPACITY_DECREMENTER_PROPERTIES = new PropertiesAttributeDefinition.Builder(CAPACITY_DECREMENTER_PROPERTIES_NAME, true)
            .setAttributeGroup("pool")
            .setAllowNull(true)
            .setAllowExpression(true)
            .build();


    public static final SimpleAttributeDefinition POOL_PREFILL = new SimpleAttributeDefinitionBuilder(POOL_PREFILL_NAME, ModelType.BOOLEAN, true)
            .setAttributeGroup("pool")
            .setXmlName("prefill")
            .setDefaultValue(new ModelNode(Defaults.PREFILL))
            .setAllowExpression(true)
            .build();

    public static final SimpleAttributeDefinition POOL_USE_STRICT_MIN = new SimpleAttributeDefinitionBuilder(POOL_USE_STRICT_MIN_NAME, ModelType.BOOLEAN, true)
            .setAttributeGroup("pool")
            .setXmlName("use-strict-min")
            .setDefaultValue(new ModelNode(Defaults.USE_STRICT_MIN))
            .setAllowExpression(true)
            .build();

    public static final SimpleAttributeDefinition POOL_FLUSH_STRATEGY = new SimpleAttributeDefinitionBuilder(FLUSH_STRATEGY_NAME, ModelType.STRING)
            .setAttributeGroup("pool")
            .setDefaultValue(new ModelNode(Defaults.FLUSH_STRATEGY.getName()))
            .setAllowNull(true)
            .setAllowExpression(true)
            .setValidator(new EnumValidator<FlushStrategy>(FlushStrategy.class, true, true))
            .build();


    public static final AttributeDefinition[] POOL_ATTRIBUTES = {BLOCKING_TIMEOUT_WAIT_MILLIS, IDLETIMEOUTMINUTES, BACKGROUNDVALIDATIONMILLIS,
            BACKGROUNDVALIDATION, USE_FAST_FAIL, VALIDATE_ON_MATCH, MAX_POOL_SIZE, MIN_POOL_SIZE, INITIAL_POOL_SIZE, POOL_PREFILL, POOL_USE_STRICT_MIN, POOL_FLUSH_STRATEGY,
            CAPACITY_INCREMENTER_CLASS, CAPACITY_DECREMENTER_CLASS, CAPACITY_INCREMENTER_PROPERTIES, CAPACITY_DECREMENTER_PROPERTIES};

    public static SimpleAttributeDefinition POOL_STATISTICS_ENABLED = new SimpleAttributeDefinitionBuilder(ModelDescriptionConstants.STATISTICS_ENABLED, ModelType.BOOLEAN)
            .setStorageRuntime()
            .build();
}
