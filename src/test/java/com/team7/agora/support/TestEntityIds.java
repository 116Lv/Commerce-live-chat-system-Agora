package com.team7.agora.support;

import org.springframework.test.util.ReflectionTestUtils;

public final class TestEntityIds {

    private TestEntityIds() {
    }

    public static void assignId(Object entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
    }
}
