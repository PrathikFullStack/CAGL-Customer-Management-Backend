package com.iexceed.appzillonbanking.cagl.cob.utils;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.AnnotationBasedGenerator;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.generator.GeneratorCreationContext;

import java.lang.reflect.Member;
import java.util.EnumSet;

public class StringSequenceIdGenerator
        implements BeforeExecutionGenerator, AnnotationBasedGenerator<StringSequenceGenerator> {

    private String sequenceName;

    @Override
    public void initialize(StringSequenceGenerator annotation, Member member, GeneratorCreationContext context) {
        this.sequenceName = annotation.sequenceName();
    }

    @Override
    public Object generate(SharedSessionContractImplementor session, Object owner,
                           Object currentValue, EventType eventType) {
        SequenceIdGeneratorService generator = SpringContextHolder.getBean(SequenceIdGeneratorService.class);
        return generator.nextValueAsString(sequenceName);
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EnumSet.of(EventType.INSERT);
    }
}