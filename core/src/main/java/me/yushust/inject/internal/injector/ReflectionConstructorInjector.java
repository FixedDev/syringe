package me.yushust.inject.internal.injector;

import me.yushust.inject.Injector;
import me.yushust.inject.exception.ExceptionFactory;
import me.yushust.inject.identity.type.TypeReference;
import me.yushust.inject.resolve.InjectableMember;
import me.yushust.inject.resolve.ResolvableKey;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.util.List;

import static me.yushust.inject.internal.Preconditions.checkNotNull;
import static me.yushust.inject.internal.Preconditions.checkArgument;
import static me.yushust.inject.internal.Preconditions.checkState;

public class ReflectionConstructorInjector<T> implements ConstructorInjector<T> {

    private final Injector injector;

    private final TypeReference<T> declaringClass;
    private final Constructor<T> constructor;
    private final List<ResolvableKey<?>> parameterKeys;

    @SuppressWarnings("unchecked")
    public ReflectionConstructorInjector(TypeReference<T> declaringClass, Injector injector, InjectableMember constructor) {
        checkNotNull(constructor);
        this.declaringClass = checkNotNull(declaringClass);
        this.injector = checkNotNull(injector);

        Member member = constructor.getMember();

        checkArgument(member instanceof Constructor, "Provided injectable member isn't a constructor!");
        checkState(
                declaringClass.equals(constructor.getDeclaringClass()),
                "Constructor isn't declared by the provided class"
        );

        this.constructor = (Constructor<T>) member;
        this.parameterKeys = constructor.getKeys();
    }

    @Override
    public T createInstance(Object... extraParameters) {

        int parameterCount = parameterKeys.size() + extraParameters.length;

        Object[] parameters = new Object[parameterCount];

        for (int i = 0; i < parameterKeys.size(); i++) {

            ResolvableKey<?> key = parameterKeys.get(i);
            Object param = injector.getInstance(key.getKey());

            if (param == null && !key.isOptional()) {
                throw ExceptionFactory.cannotInjectConstructor(
                        declaringClass,
                        key.getKey(),
                        "An instance could not be obtained"
                );
            }

            parameters[i] = param;

        }

        if (parameterCount - parameterKeys.size() >= 0) {
            System.arraycopy(
                    extraParameters,
                    0,
                    parameters,
                    parameterKeys.size(),
                    extraParameters.length
            );
        }

        try {
            return constructor.newInstance(parameters);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw ExceptionFactory.cannotInstantiateClass(declaringClass, e);
        }

    }

}
