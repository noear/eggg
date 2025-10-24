package org.noear.eggg;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author noear 2025/10/24 created
 *
 */
public class GenericTest {
    Eggg eggg = new Eggg();

    @Test
    public void case1() {
        Map<String, Object> map = new HashMap<>();

        eggg.getTypeEggg(map.getClass()).getClassEggg();
    }
}
