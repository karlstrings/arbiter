package net.mcclendo.arbiter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ConfigValueTest {

    @Before
    public void before() {
        System.setProperty("ARBITER_CONFIG_PREFIX", "/arbiter");
    }

    @Component
    public static final class StandardComponentImpl {

        private final String s;
        private final int ip;
        private final int i;
        private final short shp;
        private final Short sh;

        public StandardComponentImpl(
                @ConfigValue("test-string") final String s,
                @ConfigValue("test-int") final int ip,
                @ConfigValue("test-int") final Integer i,
                @ConfigValue("test-short") final short shp,
                @ConfigValue("test-short") final Short sh) {
            this.s = s;
            this.ip = ip;
            this.i = i;
            this.shp = shp;
            this.sh = sh;
        }
    }

    @Test
    public void control() {
        try (Arbiter arbiter = new Arbiter()) {
            final StandardComponentImpl impl = arbiter.getComponent(StandardComponentImpl.class);
            Assert.assertEquals("test this", impl.s);
            Assert.assertEquals(5, impl.ip);
            Assert.assertEquals(5, impl.i);
            Assert.assertEquals((short) 6, impl.shp);
            Assert.assertEquals((short) 6, (short) impl.sh);
        }
    }
}

