package me.yushust.inject.test;

import me.yushust.inject.Injector;
import me.yushust.inject.InjectorFactory;
import me.yushust.inject.scope.Scopes;
import org.junit.Test;

import java.util.Objects;

import static org.junit.Assert.assertEquals;

public class SelfReferredLinkedBindingTest {

    @Test
    public void testLinkedBinding() {

        Injector injector = InjectorFactory.create(binder -> {
            binder.bind(Foo.class).scope(Scopes.SINGLETON);
        });

        Foo foo = injector.getInstance(Foo.class);
        Foo foo2 = injector.getInstance(Foo.class);

        assertEquals(foo, foo2);

    }

    public static class Foo {

        private final int id = (int) (Math.random() * 10);

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Foo foo = (Foo) o;
            return id == foo.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

}