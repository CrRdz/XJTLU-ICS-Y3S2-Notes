package com.cpt202.acceptance;

import com.cpt202.integration.Cpt202IntegrationTest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.test.annotation.DirtiesContext;

/** Marks an API-level acceptance test suite with an isolated Spring context. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Cpt202IntegrationTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public @interface Cpt202AcceptanceTest {
}