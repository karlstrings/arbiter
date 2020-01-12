package net.mcclendo.arbiter;

import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("WeakerAccess")
public final class ArbiterTest {

    @Component
    public static final class StandardComponentImpl {
    }

    @Test
    public void control() {
        try (Arbiter arbiter = new Arbiter()) {
            arbiter.getComponent(StandardComponentImpl.class);
        }
    }

    @Test(expected = ArbiterException.class)
    public void mustInstantiateOnlyComponents() {
        try (Arbiter arbiter = new Arbiter()) {
            arbiter.getComponent((new Object() {
            }).getClass());
        }
    }

    @Component
    public interface ComponentInterface {
    }

    @Test(expected = ArbiterException.class)
    public void mustNotBeInterfaceWithoutImplBy() {
        try (Arbiter arbiter = new Arbiter()) {
            arbiter.getComponent(ComponentInterface.class);
        }
    }

    @Component(implementedBy = NotAssignableComponentImpl.class)
    public interface NotAssignableComponent {
    }

    public static final class NotAssignableComponentImpl {
    }

    @Test(expected = ArbiterException.class)
    public void mustNotBeInterfaceWith() {
        try (Arbiter arbiter = new Arbiter()) {
            arbiter.getComponent(NotAssignableComponent.class);
        }
    }

    @Component
    public static final class FromContainerComponentImpl {

        public int constructorCount = 0;

        public FromContainerComponentImpl() {
            this.constructorCount++;
        }
    }

    @Component
    public static final class FromContainerComponent2Impl {

        public int constructorCount = 0;

        public FromContainerComponent2Impl() {
            this.constructorCount++;
        }
    }

    @Test
    public void pullClassFromContainer() {
        try (Arbiter arbiter = new Arbiter()) {
            arbiter.getComponent(FromContainerComponentImpl.class);
            Assert.assertEquals(1, arbiter.getComponent(FromContainerComponentImpl.class).constructorCount);
            Assert.assertEquals(1, arbiter.getComponent(FromContainerComponent2Impl.class).constructorCount);

            Assert.assertEquals(1, arbiter.getComponent(FromContainerComponentImpl.class).constructorCount);
            Assert.assertEquals(1, arbiter.getComponent(FromContainerComponent2Impl.class).constructorCount);
        }
    }

    @Component
    public static final class LotsOfConstructorsImpl {

        @SuppressWarnings("unused")
        public LotsOfConstructorsImpl() {
        }

        @SuppressWarnings("unused")
        public LotsOfConstructorsImpl(int s) {
        }
    }

    @Test(expected = ArbiterException.class)
    public void tooManyConstructors() {
        try (Arbiter arbiter = new Arbiter()) {
            arbiter.getComponent(LotsOfConstructorsImpl.class);
        }
    }

    @Component
    public static final class PrivateConstructorImpl {

        private PrivateConstructorImpl() {
        }
    }

    @Test(expected = ArbiterException.class)
    public void privateConstructors() {
        try (Arbiter arbiter = new Arbiter()) {
            arbiter.getComponent(PrivateConstructorImpl.class);
        }
    }

    @Component
    public static class AmbiguousComponent {
    }

    @Component
    public static class AmbiguousComponent2 extends AmbiguousComponent {
    }

    @Test(expected = ArbiterException.class)
    public void rejectAmbiguousComponent() {
        try (Arbiter arbiter = new Arbiter()) {
            arbiter.getComponent(AmbiguousComponent.class);
            arbiter.getComponent(AmbiguousComponent2.class);
            arbiter.getComponent(AmbiguousComponent.class);
        }
    }

    @Component
    public static final class MultiArgConstructorImpl {

        public MultiArgConstructorImpl(
                @SuppressWarnings("unused") final StandardComponentImpl i) {
        }
    }

    @Test
    public void multiArgConstructor() {
        try (Arbiter arbiter = new Arbiter()) {
            arbiter.getComponent(MultiArgConstructorImpl.class);
        }
    }

    @Component
    @SuppressWarnings("InnerClassMayBeStatic")
    public class NotAStaticClass {
    }

    @Test(expected = ArbiterException.class)
    public void detectNonStaticComponentClass() {
        try (Arbiter arbiter = new Arbiter()) {
            arbiter.getComponent(NotAStaticClass.class);
        }
    }

    @Component
    public static final class ConstructorThrows {

        public ConstructorThrows() {
            throw new RuntimeException();
        }
    }

    @Test(expected = ArbiterException.class)
    public void constructorThrows() {
        try (Arbiter arbiter = new Arbiter()) {
            arbiter.getComponent(ConstructorThrows.class);
        }
    }

    @Component
    public static final class Recursive {

        public Recursive(
                @SuppressWarnings("unused") final Recursive r) {
        }
    }

    @Test(expected = ArbiterException.class)
    public void recursive() {
        try (Arbiter arbiter = new Arbiter()) {
            arbiter.getComponent(Recursive.class);
        }
    }

    @Component
    public static final class CloseMeImpl implements ComponentLifecycleDestroy {

        public int i;

        @Override
        public void onComponentDestroy() {
            i++;
        }
    }

    @Test
    public void destroy() {
        final CloseMeImpl closeMe;
        try (Arbiter arbiter = new Arbiter()) {
            closeMe = arbiter.getComponent(CloseMeImpl.class);
            Assert.assertEquals(0, closeMe.i);
        }

        Assert.assertEquals(1, closeMe.i);
    }

    @Component
    public static final class CloseMeThrowsImpl implements ComponentLifecycleDestroy {

        @Override
        public void onComponentDestroy() {
            throw new NullPointerException();
        }
    }

    @Test(expected = NullPointerException.class)
    public void destroyThrows() {
        try (Arbiter arbiter = new Arbiter()) {
            arbiter.getComponent(CloseMeThrowsImpl.class);
        }
    }
}
